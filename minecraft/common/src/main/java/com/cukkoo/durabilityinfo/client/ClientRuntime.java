package com.cukkoo.durabilityinfo.client;

import com.cukkoo.durabilityinfo.core.AlertStateTracker;
import com.cukkoo.durabilityinfo.core.AlertThresholdSet;
import com.cukkoo.durabilityinfo.core.DurabilityCalculator;
import com.cukkoo.durabilityinfo.core.DurabilityChangeTracker;
import com.cukkoo.durabilityinfo.core.DurabilityInfoConfig;
import com.cukkoo.durabilityinfo.core.DurabilityInfoConfigManager;
import com.cukkoo.durabilityinfo.core.DurabilitySnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ClientRuntime {
    private static final AlertStateTracker ALERTS = new AlertStateTracker();
    private static final DurabilityChangeTracker CHANGES = new DurabilityChangeTracker();
    private static final String[] SLOT_KEYS = {"helmet", "chestplate", "leggings", "boots", "main_hand", "offhand"};
    private static final ItemStack[] STACKS = new ItemStack[6];
    private static final HudSlotView[] VIEWS = {
            new HudSlotView(), new HudSlotView(), new HudSlotView(),
            new HudSlotView(), new HudSlotView(), new HudSlotView()
    };
    private static final long[] FLASH_UNTIL = new long[6];
    private static final long[] CHANGED_AT = new long[6];
    private static final int[] LAST_DAMAGE = {-1, -1, -1, -1, -1, -1};
    private static final String[] LAST_ITEM = {"", "", "", "", "", ""};
    private static final int MAX_ICON_CACHE = 16;
    private static final Map<String, ItemStack> NOTIFICATION_ICONS = new LinkedHashMap<>(MAX_ICON_CACHE + 1, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ItemStack> eldest) {
            return size() > MAX_ICON_CACHE;
        }
    };
    private static Object lastPlayer;
    private static Object lastLevel;
    private static Component activeActionBar;
    private static long activeActionBarUntil;

    private ClientRuntime() {}

    public static void tick(Minecraft client) {
        if (client.player == null || client.level == null) {
            if (lastPlayer != null || lastLevel != null) clear();
            lastPlayer = null;
            lastLevel = null;
            return;
        }
        if (lastPlayer != client.player || lastLevel != client.level) {
            clear();
            lastPlayer = client.player;
            lastLevel = client.level;
        }
        DurabilityInfoConfig config = DurabilityInfoConfigManager.current();
        collect(client);
        long now = System.currentTimeMillis();
        for (int i = 0; i < STACKS.length; i++) {
            VIEWS[i].update(SLOT_KEYS[i], STACKS[i], config);
            DurabilitySnapshot snapshot = VIEWS[i].snapshot();
            if (!snapshot.itemKey().equals(LAST_ITEM[i]) || (LAST_DAMAGE[i] >= 0 && LAST_DAMAGE[i] != snapshot.damage())) {
                CHANGED_AT[i] = now;
            }
            LAST_ITEM[i] = snapshot.itemKey();
            LAST_DAMAGE[i] = snapshot.damage();
            boolean armor = i < 4;
            boolean alertEnabled = armor ? config.alerts.armorEnabled
                    : config.alerts.heldEnabled && (i == 4 ? config.alerts.mainHandEnabled : config.alerts.offhandEnabled);
            if (alertEnabled) {
                AlertThresholdSet thresholds = armor ? config.alerts.armor : config.alerts.held;
                final int slot = i;
                ALERTS.update(snapshot, thresholds).ifPresent(event -> emitAlert(client, event, slot, now, config));
            }
            boolean notify = config.notifications.enabled && (armor ? config.notifications.armor
                    : i == 4 ? config.notifications.mainHand : config.notifications.offhand);
            if (notify) {
                rememberIcon(snapshot, STACKS[i]);
                CHANGES.update(snapshot, now);
            }
        }
        if (config.notifications.enabled && config.notifications.entireHotbar) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = client.player.getInventory().getItem(i);
                DurabilitySnapshot snapshot = MinecraftDurabilityAdapter.snapshot("hotbar_" + i, stack);
                rememberIcon(snapshot, stack);
                CHANGES.update(snapshot, now);
            }
        }
        if (config.alerts.actionBar && activeActionBar != null && now <= activeActionBarUntil) {
            client.player.sendOverlayMessage(activeActionBar);
        } else if (now > activeActionBarUntil) {
            activeActionBar = null;
        }
    }

    public static void renderNotifications(GuiGraphicsExtractor graphics) {
        DurabilityInfoConfig config = DurabilityInfoConfigManager.current();
        if (!config.notifications.enabled) return;
        long now = System.currentTimeMillis();
        long duration = (long) (config.notifications.durationSeconds * 1000.0);
        int visibleCount = CHANGES.visibleCount(now, duration);
        if (visibleCount == 0) return;
        int width = 150;
        int height = visibleCount * 24;
        float scale = (float) config.notifications.scale;
        int scaledWidth = Math.round(width * scale);
        int scaledHeight = Math.round(height * scale);
        int x = switch (config.notifications.position) {
            case TOP_RIGHT, BOTTOM_RIGHT -> graphics.guiWidth() - scaledWidth - 6;
            default -> 6;
        };
        int y = switch (config.notifications.position) {
            case BOTTOM_LEFT, BOTTOM_RIGHT -> graphics.guiHeight() - scaledHeight - 6;
            default -> 6;
        };
        Minecraft client = Minecraft.getInstance();
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        int row = 0;
        for (var notification : CHANGES.visibleIterable(now, duration)) {
            int rowY = row * 24;
            graphics.fill(0, rowY, width, rowY + 22, 0xA0000000);
            ItemStack icon = findCurrentStack(notification.itemKey());
            if (!icon.isEmpty()) graphics.item(icon, 3, rowY + 3);
            Component detail = notificationText(notification);
            graphics.text(client.font, Component.literal(notification.displayName()), 22, rowY + 2,
                    0xFFFFFFFF, config.hud.textShadow);
            graphics.text(client.font, detail,
                    22, rowY + 12, notification.delta() < 0 ? 0xFFFF7777 : 0xFF77FF77, config.hud.textShadow);
            row++;
        }
        graphics.pose().popMatrix();
    }

    public static boolean isFlashing(String slotKey, long now) {
        int index = slotIndex(slotKey);
        return index >= 0 && now < FLASH_UNTIL[index] && (now / 250L) % 2L == 0L;
    }

    public static long changedAt(String slotKey) {
        int index = slotIndex(slotKey);
        return index < 0 ? 0L : CHANGED_AT[index];
    }

    public static HudSlotView view(int index) {
        return VIEWS[index];
    }

    public static void clear() {
        ALERTS.clear();
        CHANGES.clear();
        java.util.Arrays.fill(FLASH_UNTIL, 0L);
        java.util.Arrays.fill(CHANGED_AT, 0L);
        java.util.Arrays.fill(LAST_DAMAGE, -1);
        java.util.Arrays.fill(LAST_ITEM, "");
        activeActionBar = null;
        activeActionBarUntil = 0L;
        NOTIFICATION_ICONS.clear();
    }

    private static void emitAlert(Minecraft client, AlertStateTracker.AlertEvent event, int slot, long now,
                                  DurabilityInfoConfig config) {
        if (config.alerts.hudFlash) FLASH_UNTIL[slot] = now + (long) (config.alerts.flashSeconds * 1000.0);
        if (config.alerts.sound) client.player.playSound(SoundEvents.ITEM_BREAK.value(),
                (float) config.alerts.soundVolume, 1.0F);
        Component text = Component.translatable("durabilityinfo.alert.message",
                Component.literal(event.snapshot().displayName()),
                DurabilityCalculator.remaining(event.snapshot()),
                Component.translatable("durabilityinfo.alert.level." + event.level().key()));
        if (config.alerts.actionBar) {
            activeActionBar = text;
            activeActionBarUntil = now + (long) (config.alerts.messageSeconds * 1000.0);
        }
        if (config.alerts.chat) client.player.sendSystemMessage(text);
    }

    private static void collect(Minecraft client) {
        STACKS[0] = client.player.getItemBySlot(EquipmentSlot.HEAD);
        STACKS[1] = client.player.getItemBySlot(EquipmentSlot.CHEST);
        STACKS[2] = client.player.getItemBySlot(EquipmentSlot.LEGS);
        STACKS[3] = client.player.getItemBySlot(EquipmentSlot.FEET);
        STACKS[4] = client.player.getMainHandItem();
        STACKS[5] = client.player.getItemBySlot(EquipmentSlot.OFFHAND);
    }

    private static ItemStack findCurrentStack(String itemKey) {
        for (ItemStack stack : STACKS) if (!stack.isEmpty() && stack.getItem().toString().equals(itemKey)) return stack;
        return NOTIFICATION_ICONS.getOrDefault(itemKey, ItemStack.EMPTY);
    }

    private static void rememberIcon(DurabilitySnapshot snapshot, ItemStack stack) {
        if (DurabilityCalculator.isUsable(snapshot) && stack != null && !stack.isEmpty()) {
            NOTIFICATION_ICONS.put(snapshot.itemKey(), stack.copy());
        }
    }

    private static Component notificationText(DurabilityChangeTracker.ChangeNotification notification) {
        if (notification.delta() < 0) {
            return notification.count() > 1
                    ? Component.translatable("durabilityinfo.notification.damage_repeated", -notification.delta(),
                            notification.count(), notification.remaining(), notification.maximum())
                    : Component.translatable("durabilityinfo.notification.damage", -notification.delta(),
                            notification.remaining(), notification.maximum());
        }
        return notification.count() > 1
                ? Component.translatable("durabilityinfo.notification.repair_repeated", notification.delta(),
                        notification.count(), notification.remaining(), notification.maximum())
                : Component.translatable("durabilityinfo.notification.repair", notification.delta(),
                        notification.remaining(), notification.maximum());
    }

    private static int slotIndex(String slotKey) {
        for (int i = 0; i < SLOT_KEYS.length; i++) if (SLOT_KEYS[i].equals(slotKey)) return i;
        return -1;
    }
}
