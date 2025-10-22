package com.example.customcommands.commands;

import com.example.customcommands.CustomCommandsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для вставания
 */
public class UnsitCommand implements CommandExecutor {
    
    private final CustomCommandsPlugin plugin;
    
    public UnsitCommand(CustomCommandsPlugin plugin) {
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
        
        // Проверяем сидит ли игрок
        if (!plugin.getSitManager().isPlayerSitting(player)) {
            player.sendMessage("§eВы не сидите!");
            return true;
        }
        
        // Заставляем встать
        if (plugin.getSitManager().makePlayerStand(player)) {
            player.sendMessage("§aВы встали!");
        } else {
            player.sendMessage("§cНе удалось встать!");
        }
        
        return true;
    }
}

