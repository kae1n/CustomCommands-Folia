package com.example.customcommands.commands;

import com.example.customcommands.CustomCommandsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для сидения
 */
public class SitCommand implements CommandExecutor {
    
    private final CustomCommandsPlugin plugin;
    
    public SitCommand(CustomCommandsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Проверка прав
        if (!player.hasPermission("customcommands.sit.use")) {
            player.sendMessage(Component.text("У вас нет прав для использования команды сидения!")
                    .color(TextColor.color(0xFF5555)));
            return true;
        }
        
        // Проверяем аргументы
        if (args.length == 0) {
            // Сидеть на текущем месте
            if (plugin.getSitManager().isPlayerSitting(player)) {
                player.sendMessage("§eВы уже сидите! Используйте /unsit чтобы встать.");
                return true;
            }
            
            if (plugin.getSitManager().makePlayerSit(player)) {
                player.sendMessage("§aВы сели!");
            } else {
                String message = plugin.getSitManager().getSitBlockedMessage(player);
                if (!message.isEmpty()) {
                    player.sendMessage(message);
                } else {
                    player.sendMessage("§cНе удалось сесть!");
                }
            }
            return true;
        }
        
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("toggle")) {
                // Переключение сидения
                if (plugin.getSitManager().isPlayerSitting(player)) {
                    if (plugin.getSitManager().makePlayerStand(player)) {
                        player.sendMessage("§aВы встали!");
                    }
                } else {
                    if (plugin.getSitManager().makePlayerSit(player)) {
                        player.sendMessage("§aВы сели!");
                    } else {
                        String message = plugin.getSitManager().getSitBlockedMessage(player);
                        if (!message.isEmpty()) {
                            player.sendMessage(message);
                        } else {
                            player.sendMessage("§cНе удалось сесть!");
                        }
                    }
                }
                return true;
            }
            
            if (args[0].equalsIgnoreCase("info")) {
                // Информация о сидении
                if (plugin.getSitManager().isPlayerSitting(player)) {
                    player.sendMessage("§aВы сидите!");
                } else {
                    player.sendMessage("§eВы не сидите.");
                }
                return true;
            }
        }
        
        // Неправильное использование
        player.sendMessage("§cИспользование: /sit [toggle|info]");
        return true;
    }
}

