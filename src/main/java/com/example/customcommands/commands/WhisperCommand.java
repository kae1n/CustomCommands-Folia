package com.example.customcommands.commands;

import com.example.customcommands.utils.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WhisperCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 2) {
            player.sendMessage(Component.text("Использование: /whisper <игрок> <сообщение>")
                    .color(TextColor.color(0xFF5555)));
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null || !target.isOnline()) {
            player.sendMessage(Component.text("Игрок " + targetName + " не найден или не в сети!")
                    .color(TextColor.color(0xFF5555)));
            return true;
        }
        
        if (target.equals(player)) {
            player.sendMessage(Component.text("Нельзя шептать самому себе!")
                    .color(TextColor.color(0xFF5555)));
            return true;
        }
        
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        Component whisperMessage = MessageFormatter.formatWhisper(player.getName(), target.getName(), message);
        
        player.sendMessage(whisperMessage);
        target.sendMessage(whisperMessage);
        
        return true;
    }
}