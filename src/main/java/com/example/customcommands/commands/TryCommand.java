package com.example.customcommands.commands;

import com.example.customcommands.utils.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

public class TryCommand implements CommandExecutor {
    
    private final Random random = new Random();
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage(Component.text("Использование: /try <действие>")
                    .color(TextColor.color(0xFF5555)));
            return true;
        }
        
        String action = String.join(" ", args);
        boolean success = random.nextBoolean();
        
        Component message = MessageFormatter.formatResult(player.getName(), action, success);
        
        player.getWorld().getNearbyPlayers(player.getLocation(), 50).forEach(p ->
            p.sendMessage(message)
        );
        
        return true;
    }
}