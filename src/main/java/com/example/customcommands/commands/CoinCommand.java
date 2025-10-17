package com.example.customcommands.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

public class CoinCommand implements CommandExecutor {
    
    private final Random random = new Random();
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        boolean isHeads = random.nextBoolean();
        String result = isHeads ? "ОРЁЛ" : "РЕШКА";
        TextColor color = isHeads ? TextColor.color(0x55FF55) : TextColor.color(0xFFFF55);
        
        Component message = Component.text("• ")
                .color(TextColor.color(0xAAAAAA))
                .append(Component.text(player.getName())
                        .color(TextColor.color(0x55FFFF))
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" подбрасывает монетку и выпадает: ")
                        .color(TextColor.color(0xFFFFFF)))
                .append(Component.text(result)
                        .color(color)
                        .decorate(TextDecoration.BOLD));
        
        player.getWorld().getNearbyPlayers(player.getLocation(), 50).forEach(p ->
            p.sendMessage(message)
        );
        
        return true;
    }
}