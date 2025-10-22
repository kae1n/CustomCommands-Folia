package com.example.customcommands.commands;

import com.example.customcommands.CustomCommandsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetCooldownCommand implements CommandExecutor {

    private final CustomCommandsPlugin plugin;

    public ResetCooldownCommand(CustomCommandsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Проверка прав
        if (!sender.hasPermission("customcommands.admin.reset_cooldown")) {
            sender.sendMessage(Component.text("У вас нет прав для использования этой команды!")
                    .color(TextColor.color(0xFF5555))
                    .decorate(TextDecoration.BOLD));
            return true;
        }

        // Проверка аргументов
        if (args.length != 1) {
            sender.sendMessage(Component.text("Использование: /unloadclcc <игрок>")
                    .color(TextColor.color(0xFF5555)));
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            sender.sendMessage(Component.text("Игрок " + targetName + " не найден или не в сети!")
                    .color(TextColor.color(0xFF5555)));
            return true;
        }

        // Сбрасываем cooldown
        plugin.getThiefManager().resetCooldown(target);

        // Отправляем сообщения
        Component successMessage = Component.text("✅ Cooldown кражи сброшен для игрока " + targetName)
                .color(TextColor.color(0x55FF55))
                .decorate(TextDecoration.BOLD);
        sender.sendMessage(successMessage);

        Component targetMessage = Component.text("Ваш cooldown кражи был сброшен администратором")
                .color(TextColor.color(0x55FF55));
        target.sendMessage(targetMessage);

        return true;
    }
}





