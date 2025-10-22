package com.example.customcommands.listeners;

import com.example.customcommands.CustomCommandsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Слушатель для блокировки Gsit функционала в режиме вора
 */
public class GsitBlockListener implements Listener {
    
    private final CustomCommandsPlugin plugin;
    
    public GsitBlockListener(CustomCommandsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Блокирует попытки сесть на блоки в режиме вора
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Проверяем только если игрок в режиме вора
        if (!plugin.getThiefManager().isInThiefMode(player)) {
            return;
        }
        
        // Проверяем только правый клик по блоку
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        // Проверяем можно ли игроку сесть (Gsit)
        if (!plugin.getGsitManager().canPlayerSit(player)) {
            String message = plugin.getGsitManager().getSitBlockedMessage(player);
            if (!message.isEmpty()) {
                player.sendMessage(message);
            }
            event.setCancelled(true);
            return;
        }
        
    }
    
    /**
     * Блокирует попытки сесть на игроков в режиме вора
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(org.bukkit.event.player.PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        
        // Проверяем только если игрок в режиме вора
        if (!plugin.getThiefManager().isInThiefMode(player)) {
            return;
        }
        
        // Проверяем можно ли игроку сесть (Gsit)
        if (!plugin.getGsitManager().canPlayerSit(player)) {
            String message = plugin.getGsitManager().getSitBlockedMessage(player);
            if (!message.isEmpty()) {
                player.sendMessage(message);
            }
            event.setCancelled(true);
            return;
        }
        
    }
}
