package com.example.customcommands.commands;

import com.example.customcommands.utils.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MeCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage(Component.text("Использование: /me <действие>")
                    .color(TextColor.color(0xFF5555)));
            return true;
        }
        
        String action = String.join(" ", args);
        Component message = MessageFormatter.formatAction(player.getName(), action, TextColor.color(0x55FF55));
        
        player.getWorld().getNearbyPlayers(player.getLocation(), 50).forEach(p ->
            p.sendMessage(message)
        );
        
        return true;
    }
}