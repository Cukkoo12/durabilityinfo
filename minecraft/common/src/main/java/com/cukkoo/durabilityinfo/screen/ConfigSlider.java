package com.cukkoo.durabilityinfo.screen;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

final class ConfigSlider extends AbstractSliderButton {
    private final double minimum;
    private final double maximum;
    private final Component label;
    private final boolean showLabel;
    private final DoubleFunction<String> formatter;
    private final DoubleConsumer consumer;

    ConfigSlider(int x, int y, int width, Component label, boolean showLabel,
                 double minimum, double maximum, double current,
                 DoubleFunction<String> formatter, DoubleConsumer consumer) {
        super(x, y, width, 20, Component.empty(), normalize(current, minimum, maximum));
        this.minimum = minimum;
        this.maximum = maximum;
        this.label = label;
        this.showLabel = showLabel;
        this.formatter = formatter;
        this.consumer = consumer;
        updateMessage();
    }

    @Override protected void updateMessage() {
        Component value = Component.literal(formatter.apply(actual()));
        setMessage(showLabel ? CommonComponents.optionNameValue(label, value) : value);
    }

    @Override protected MutableComponent createNarrationMessage() {
        return wrapDefaultNarrationMessage(CommonComponents.optionNameValue(
                label, Component.literal(formatter.apply(actual()))));
    }

    @Override protected void applyValue() {
        consumer.accept(actual());
        updateMessage();
    }

    private double actual() { return minimum + value * (maximum - minimum); }
    private static double normalize(double value, double min, double max) {
        return Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));
    }
}
