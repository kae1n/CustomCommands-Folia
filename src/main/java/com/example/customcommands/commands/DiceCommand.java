package com.example.customcommands.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

public class DiceCommand implements CommandExecutor {
    
    private final Random random = new Random();
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        int sides = 6;
        
        if (args.length > 0) {
            try {
                sides = Integer.parseInt(args[0]);
                if (sides < 2) sides = 2;
                if (sides > 100) sides = 100;
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Использование: /dice [количество-граней]")
                        .color(TextColor.color(0xFF5555)));
                return true;
            }
        }
        
        int result = random.nextInt(sides) + 1;
        
        Component message = Component.text("• ")
                .color(TextColor.color(0xAAAAAA))
                .append(Component.text(player.getName())
                        .color(TextColor.color(0x55FFFF))
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" бросает " + sides + "-гранный кубик и выпадает: ")
                        .color(TextColor.color(0xFFFFFF)))
                .append(Component.text(result)
                        .color(TextColor.color(0xFFFF55))
                        .decorate(TextDecoration.BOLD));
        
        player.getWorld().getNearbyPlayers(player.getLocation(), 50).forEach(p ->
            p.sendMessage(message)
        );
        
        return true;
    }
}