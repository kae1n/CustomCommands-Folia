package com.example.customcommands.commands;

import com.example.customcommands.CustomCommandsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ThiefModeCommand implements CommandExecutor {
    
    private final CustomCommandsPlugin plugin;
    
    public ThiefModeCommand(CustomCommandsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (label.equalsIgnoreCase("mthief")) {
            // Проверка прав на использование режима вора
            if (!player.hasPermission("customcommands.thief.mode")) {
                player.sendMessage(Component.text("У вас нет прав для использования режима вора!")
                        .color(TextColor.color(0xFF5555)));
                return true;
            }
            
            // Проверка Gsit: если игрок сидит - блокируем
            if (!plugin.getThiefManager().canEnterThiefMode(player)) {
                String message = plugin.getGsitManager().getThiefModeBlockedMessage(player);
                if (!message.isEmpty()) {
                    player.sendMessage(message);
                }
                return true;
            }
            
            // Включить режим вора
            if (plugin.getThiefManager().isInThiefMode(player)) {
                player.sendMessage(Component.text("Вы уже в режиме вора!")
                        .color(TextColor.color(0xFF5555)));
                return true;
            }
            
            plugin.getThiefManager().setThiefMode(player, true);
            player.sendMessage(Component.text("Режим вора включен! ПКМ на игрока для кражи.")
                    .color(TextColor.color(0x55FF55))
                    .decorate(TextDecoration.BOLD));
            
        } else if (label.equalsIgnoreCase("mofth")) {
            // Выключить режим вора
            if (!plugin.getThiefManager().isInThiefMode(player)) {
                player.sendMessage(Component.text("Вы не в режиме вора!")
                        .color(TextColor.color(0xFF5555)));
                return true;
            }
            
            plugin.getThiefManager().setThiefMode(player, false);
            player.sendMessage(Component.text("Режим вора выключен.")
                    .color(TextColor.color(0x55FF55)));
        }
        
        return true;
    }
}



