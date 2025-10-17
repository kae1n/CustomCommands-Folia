package com.example.customcommands.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class MessageFormatter {
    
    public static Component formatAction(String playerName, String action, TextColor color) {
        return Component.text("• ")
                .color(TextColor.color(0xAAAAAA))
                .append(Component.text(playerName)
                        .color(TextColor.color(0x55FFFF))
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" " + action)
                        .color(color));
    }
    
    public static Component formatResult(String playerName, String action, boolean success) {
        TextColor color = success ? TextColor.color(0x55FF55) : TextColor.color(0xFF5555);
        String result = success ? "✓ успешно" : "✗ неудачно";
        
        return Component.text("• ")
                .color(TextColor.color(0xAAAAAA))
                .append(Component.text(playerName)
                        .color(TextColor.color(0x55FFFF))
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" " + action + " - " + result)
                        .color(color));
    }
    
    public static Component formatEnvironment(String action) {
        return Component.text("• ")
                .color(TextColor.color(0xAAAAAA))
                .append(Component.text(action)
                        .color(TextColor.color(0xFFAA00)));
    }
    
    public static Component formatWhisper(String from, String to, String message) {
        return Component.text("[Шепот] ")
                .color(TextColor.color(0xFF5555))
                .append(Component.text(from + " → " + to + ": ")
                        .color(TextColor.color(0x55FFFF)))
                .append(Component.text(message)
                        .color(TextColor.color(0xFFFFFF)));
    }
}