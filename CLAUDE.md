# Fabric Mod Geliştirme Kuralları (26.1.x)
# Geliştirici: Cukkoo | com.cukkoo.MODID

---

## 0. Davranış Kuralları

- **Her görev bittiğinde** şunu açıkla: Ne kadar sürdü, neden bu kadar sürdü, hangi adım gereksiz uzadı, bir dahaki seferde nasıl daha hızlı yapılabilir.
- **Uzun süren her adımdan sonra** kısa bir özet ver: "Bu adım X dakika sürdü çünkü Y. Bir dahaki seferde Z yapacağım."
- **Hata ayıklarken** önce tüm hataları oku, sonra tek seferde düzelt — her hata için ayrı build çalıştırma.
- **Bir şeyi bilmiyorsan** hemen söyle, uzun araştırma döngüsüne girme.

---

## 1. KRİTİK: Mappings

Minecraft 26.1.x'te kod **obfuscated değil**, isimler zaten okunabilir.
`build.gradle` içinde **mappings satırı OLMAMALI**.

❌ YANLIŞ:
```groovy
mappings loom.officialMojangMappings()
mappings "net.fabricmc:yarn:..."
```

✅ DOĞRU: mappings satırı yok, hiç ekleme.

---

## 2. settings.gradle (zorunlu, eksik olursa Loom bulunamaz)

```groovy
pluginManagement {
    repositories {
        maven {
            name = 'Fabric'
            url = 'https://maven.fabricmc.net/'
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
```

---

## 3. build.gradle (çalışan şablon)

```groovy
plugins {
    id 'fabric-loom' version '1.15-SNAPSHOT'
    id 'maven-publish'
}

version = project.mod_version
group = project.maven_group

base {
    archivesName = project.archives_base_name
}

repositories {
    mavenCentral()
    maven { url = "https://maven.terraformersmc.com/releases/" }
    maven { url = "https://maven.shedaniel.me/" }
}

dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    // mappings SATIRI YOK - 26.1.x obfuscated degil
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"
    // opsiyonel:
    // modCompileOnly "com.terraformersmc:modmenu:latest.release"
    // modCompileOnly "me.shedaniel.cloth:cloth-config-fabric:latest.release"
}

processResources {
    inputs.property "version", project.version
    filteringCharset = "UTF-8"
    filesMatching("fabric.mod.json") {
        expand "version": project.version
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    withSourcesJar()
}

jar {
    from("LICENSE") {
        rename { "${it}_${project.archives_base_name}" }
    }
}
```

---

## 4. gradle.properties (çalışan şablon)

```properties
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true
org.gradle.configuration-cache=false

minecraft_version=26.1.2
loader_version=0.18.5
fabric_api_version=0.145.5+26.1.2

mod_version=1.0.0
maven_group=com.cukkoo.MODID
archives_base_name=MODID
```

Gradle wrapper: `9.4.0`
Loom: `1.15-SNAPSHOT`
Java: `25`

---

## 5. fabric.mod.json (tam şablon)

```json
{
  "schemaVersion": 1,
  "id": "modid",
  "version": "${version}",
  "name": "Mod Adı",
  "description": "Açıklama.",
  "authors": ["Cukkoo"],
  "contact": {
    "sources": "https://github.com/Cukkoo12/REPO"
  },
  "license": "MIT",
  "icon": "assets/modid/icon.png",
  "environment": "client",
  "entrypoints": {
    "client": ["com.cukkoo.modid.ModAdi"]
  },
  "mixins": ["modid.mixins.json"],
  "depends": {
    "fabricloader": ">=0.18.5",
    "fabric-api": "*",
    "minecraft": ">=26.1.0"
  },
  "suggests": {
    "modmenu": "*",
    "cloth-config2": "*"
  },
  "compatibilityLevel": "JAVA_25"
}
```

**Notlar:**
- `"environment": "client"` → client-only mod
- `"environment": "*"` → hem client hem server
- `suggests` → opsiyonel bağımlılıklar buraya
- `compatibilityLevel: JAVA_25` zorunlu, eksik olursa hata

---

## 6. modid.mixins.json (şablon)

```json
{
  "required": true,
  "package": "com.cukkoo.modid.mixin",
  "compatibilityLevel": "JAVA_25",
  "mixins": [],
  "client": [
    "BirinciMixin",
    "IkinciMixin"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

**Notlar:**
- Server-side mixin → `"mixins"` listesine
- Client-side mixin → `"client"` listesine
- `defaultRequire: 1` → mixin hedef bulamazsa build hatası verir

---

## 7. Mixin şablonu

```java
package com.cukkoo.modid.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class BirinciMixin {

    @Inject(at = @At("HEAD"), method = "hedefMetod")
    private void hedefMetodBasinda(CallbackInfo ci) {
        // kod buraya
    }

    @Inject(at = @At("RETURN"), method = "hedefMetod")
    private void hedefMetodSonunda(CallbackInfo ci) {
        // kod buraya
    }
}
```

**Sık kullanılan At değerleri:**
- `"HEAD"` → metodun başında
- `"RETURN"` → metodun sonunda
- `"TAIL"` → son return'den önce

---

## 8. 26.1.x API Değişiklikleri

| Eski (1.21.x) | Yeni (26.1.x) |
|---------------|---------------|
| `new Identifier("ns", "path")` | `Identifier.of("ns", "path")` |
| `END_WORLD_TICK` | `END_SERVER_TICK` |
| `new ResourceLocation(...)` | `ResourceLocation.fromNamespaceAndPath(...)` |
| `level.getRandom()` | `protected` — doğrudan erişim yok |
| `getToastComponent()` | `getToastManager()` |
| `GuiGraphics` | Yok — `GuiGraphicsExtractor` kullan |
| `context.drawString(...)` | `extractor.text(...)` |
| `context.drawTextWithShadow(...)` | `extractor.text(...)` |
| `disconnect()V` | `disconnectWithSavingScreen()V` |
| `saveAll(ZZZ)V` | `saveAllChunks(ZZZ)Z` — dönüş tipi boolean! |
| `mouseClicked(DDI)Z` | `mouseClicked(MouseButtonEvent, boolean)Z` |
| `render(GuiGraphics, ...)` | `extractContent(GuiGraphicsExtractor, ...)` |
| `MinecraftClient` | `Minecraft` |
| `Text.translatable(...)` | `Component.translatable(...)` |
| `TextRenderer` | `Font` |
| `client.textRenderer` | `client.font` |
| `textRenderer.getWidth(...)` | `font.width(...)` |
| `IntegratedServer` (server import) | `net.minecraft.client.server.IntegratedServer` |
| `getPlayerManager()` | `getPlayerList()` |
| `getWorlds()` | `getAllLevels()` |
| `getSaveProperties().getLevelName()` | `getWorldData().getLevelName()` |
| `level.getName()` | `summary.getLevelId()` |
| `Screen` import | `net.minecraft.client.gui.screens.Screen` |

### Kritik Java kuralı — Lambda effectively final:
Lambda içinde kullanılan değişken yeniden atanmışsa hata verir:
```java
// YANLIS:
String worldName = foo();
if (worldName == null) worldName = "default"; // yeniden atama
thread.start(() -> use(worldName)); // HATA

// DOGRU:
String wn = foo();
if (wn == null) wn = "default";
final String worldName = wn; // yeni final değişken
thread.start(() -> use(worldName)); // OK
```

### Doğru Fabric API versiyonu:
`fabric_api_version=0.145.4+26.1.2` — 0.145.5 yok, en yüksek bu!
`fabric.mod.json` içinde de `"fabric-api": ">=0.145.4"` yaz, 0.145.5 yazarsa oyun başlamaz.

### disconnectWithSavingScreen() bypass:
Bu metod içinde kaydetme ekranı + bekleme döngüsü var, direkt iptal edilmeli:
```java
@Inject(method = "disconnectWithSavingScreen()V", at = @At("HEAD"), cancellable = true)
private void onDisconnectWithSavingScreen(CallbackInfo ci) {
    if (!hasSingleplayerServer()) return;
    // ... arka plan save başlat ...
    server.saveAllChunks(false, true, true); // MinecraftServerMixin yakalar
    singleplayerServer = null; // sunucuyu null'la, bekleme döngüsü atlanır
    ci.cancel(); // orijinal metodu iptal et
    disconnect(new TitleScreen(), true, true); // direkt title screen
}
```

### Thread güvenliği — saveAllChunks:
`server.saveAllChunks()` **render thread'den çağrılmamalı** — server thread'le çakışır, crash olur.
Bunun yerine sunucunun doğal kapanış save'ini yakala:

```java
// YANLIŞ - render thread'den çağırma:
server.saveAllChunks(false, true, true); // CRASH

// DOGRU - sunucunun kendi save'ini yakala:
@Inject(method = "saveAllChunks(ZZZ)Z", at = @At("HEAD"))
private void onSaveStart(...) {
    if (!InstantQuit.pendingSaveTracking) return;
    InstantQuit.WORLD_SAVE_MANAGER.startSaving(worldName);
    InstantQuit.activeSavingWorld = worldName;
}

@Inject(method = "saveAllChunks(ZZZ)Z", at = @At("RETURN"))
private void onSaveEnd(...) {
    String worldName = InstantQuit.activeSavingWorld;
    if (worldName == null) return;
    InstantQuit.activeSavingWorld = null;
    InstantQuit.onBackgroundSaveComplete(worldName);
}
```

### Test:
`./gradlew runClient` ile Minecraft başlatılabilir, ayrıca launcher açmaya gerek yok.
Yeni dünyada save süresi 0.0s göstermesi normaldir — kaydedilecek chunk yok.

---

## 9. Opsiyonel Cloth Config + Mod Menu entegrasyonu

### Çalışma anında kontrol:
```java
boolean hasClothConfig = FabricLoader.getInstance().isModLoaded("cloth-config2");
boolean hasModMenu = FabricLoader.getInstance().isModLoaded("modmenu");
```

### fabric.mod.json entrypoint:
```json
"entrypoints": {
    "client": ["com.cukkoo.modid.ModAdi"],
    "modmenu": ["com.cukkoo.modid.ModMenuIntegration"]
}
```

### ModMenuIntegration.java:
```java
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!FabricLoader.getInstance().isModLoaded("cloth-config2")) return null;
        return parent -> ModConfigScreen.create(parent);
    }
}
```

---

## 10. Config sistemi (Cloth Config olmadan, sade JSON)

```java
public class ModConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir().resolve("modid.json");

    public boolean secenek1 = true;
    public int secenek2 = 3;

    public static ModConfig load() {
        try {
            if (Files.exists(CONFIG_PATH))
                return new Gson().fromJson(Files.readString(CONFIG_PATH), ModConfig.class);
        } catch (Exception e) { /* ignore */ }
        return new ModConfig();
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH,
                new GsonBuilder().setPrettyPrinting().create().toJson(this));
        } catch (Exception e) { /* ignore */ }
    }
}
```

---

## 11. Modrinth Yükleme Checklist

- [ ] Mod ID küçük harf, alt çizgi veya tire — boşluk yok
- [ ] Versiyon formatı: `1.0.0`
- [ ] Desteklenen MC versiyonları doğru seçildi
- [ ] Loader: Fabric seçildi
- [ ] Lisans: MIT
- [ ] icon.png var (128x128 önerilen)
- [ ] Açıklama sayfası dolu
- [ ] `fabric.mod.json` depends bloğu doğru

---

## 12. Git Branch Stratejisi

```
main              → kararlı, en son sürüm
snapshot/26.2-x   → snapshot portları
port/1.21.11      → eski versiyon portları
port/1.21.4       → eski versiyon portları
port/1.20.1       → eski versiyon portları
```

Yeni port:
```bash
git checkout main
git checkout -b port/1.21.11
# gradle.properties güncelle
# API değişikliklerini düzelt
git commit -m "Port to 1.21.11"
```

---

## 13. Verimli Çalışma Kuralları

- **Minecraft jar yolu:** `.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-*/26.1.2/minecraft-merged-*.jar` — başka yerde arama
- **Sınıf/metod adı bilinmiyorsa:** `javap -public -cp <jar> <ClassName>` ile tek seferde tüm gerekli sınıfları kontrol et, her sınıf için ayrı komut çalıştırma
- **Toplu değişiklik:** Aynı değişkeni birden fazla yerde değiştiriyorsan tüm dosyayı tek seferde yeniden yaz, 6 ayrı edit yapma
- **Cache kilidi hatası:** `rm -rf .gradle/loom-cache` ile temizle, tekrar dene
- **Build hatası çıkınca:** Tüm hata çıktısını oku, her hatayı tek seferde düzelt, tek tek derleme yapma

## 14. Paket ve Dosya Yapısı

```
src/main/java/com/cukkoo/modid/
├── ModAdi.java                      # Ana mod sınıfı (ClientModInitializer)
├── config/
│   └── ModConfig.java               # Config sistemi
├── mixin/
│   └── HedefMixin.java              # Mixin sınıfları
└── integration/
    └── ModMenuIntegration.java      # Opsiyonel

src/main/resources/
├── fabric.mod.json
├── modid.mixins.json
└── assets/modid/
    ├── icon.png
    └── lang/
        └── en_us.json
```
