package com.example.customcommands.listeners;

import com.example.customcommands.CustomCommandsPlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Слушатель для обработки событий сидения
 */
public class SitListener implements Listener {
    
    private final CustomCommandsPlugin plugin;
    
    public SitListener(CustomCommandsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Обрабатывает ПКМ по блокам для сидения
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        
        if (block == null) {
            return;
        }
        
        // Проверяем можно ли сесть
        if (!plugin.getSitManager().canPlayerSit(player)) {
            String message = plugin.getSitManager().getSitBlockedMessage(player);
            if (!message.isEmpty()) {
                player.sendMessage(message);
            }
            event.setCancelled(true);
            return;
        }
        
        // Проверяем подходящие блоки для сидения
        if (isSittableBlock(block.getType())) {
            // Проверяем можно ли сесть (только для полублоков показываем предупреждения)
            if (!plugin.getSitManager().canPlayerSit(player)) {
                String message = plugin.getSitManager().getSitBlockedMessage(player);
                if (!message.isEmpty()) {
                    player.sendMessage(message);
                }
                event.setCancelled(true);
                return;
            }
            
            if (plugin.getSitManager().makePlayerSitOnBlock(player, block.getLocation())) {
                event.setCancelled(true);
            }
        } else {
            // Для обычных блоков не показываем предупреждения, просто блокируем
            if (!plugin.getSitManager().canPlayerSit(player)) {
                event.setCancelled(true);
            }
        }
    }
    
    
    /**
     * Обрабатывает движение сидящих игроков
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getSitManager().isPlayerSitting(player)) {
            return;
        }
        
        // Если игрок пытается двигаться сидя - заставляем встать (только если это не поворот головы)
        if (event.getFrom().distance(event.getTo()) > 0.1) {
            // Проверяем что это не просто поворот головы
            if (event.getFrom().getX() != event.getTo().getX() || 
                event.getFrom().getY() != event.getTo().getY() || 
                event.getFrom().getZ() != event.getTo().getZ()) {
                plugin.getSitManager().makePlayerStand(player);
            }
        }
        
        // Продвинутая система поворота работает автоматически через RotationManager
    }
    
    /**
     * Обрабатывает урон сидящих игроков
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        if (!plugin.getSitManager().isPlayerSitting(player)) {
            return;
        }
        
        // Если сидящий игрок получает урон - заставляем встать
        plugin.getSitManager().makePlayerStand(player);
        player.sendMessage("§eВы встали из-за урона!");
    }
    
    /**
     * Очищает данные при выходе игрока
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getSitManager().clearPlayerData(event.getPlayer());
    }
    
    
    /**
     * Проверяет подходит ли блок для сидения
     */
    private boolean isSittableBlock(Material material) {
        return material == Material.STONE_SLAB ||
               material == Material.OAK_SLAB ||
               material == Material.BIRCH_SLAB ||
               material == Material.SPRUCE_SLAB ||
               material == Material.JUNGLE_SLAB ||
               material == Material.ACACIA_SLAB ||
               material == Material.DARK_OAK_SLAB ||
               material == Material.CRIMSON_SLAB ||
               material == Material.WARPED_SLAB ||
               material == Material.STONE_BRICK_SLAB ||
               material == Material.SMOOTH_STONE_SLAB ||
               material == Material.SANDSTONE_SLAB ||
               material == Material.RED_SANDSTONE_SLAB ||
               material == Material.PURPUR_SLAB ||
               material == Material.QUARTZ_SLAB ||
               material == Material.PRISMARINE_SLAB ||
               material == Material.DARK_PRISMARINE_SLAB ||
               material == Material.PRISMARINE_BRICK_SLAB ||
               material == Material.POLISHED_GRANITE_SLAB ||
               material == Material.SMOOTH_RED_SANDSTONE_SLAB ||
               material == Material.MOSSY_STONE_BRICK_SLAB ||
               material == Material.POLISHED_DIORITE_SLAB ||
               material == Material.MOSSY_COBBLESTONE_SLAB ||
               material == Material.END_STONE_BRICK_SLAB ||
               material == Material.SMOOTH_SANDSTONE_SLAB ||
               material == Material.SMOOTH_QUARTZ_SLAB ||
               material == Material.GRANITE_SLAB ||
               material == Material.ANDESITE_SLAB ||
               material == Material.RED_NETHER_BRICK_SLAB ||
               material == Material.POLISHED_ANDESITE_SLAB ||
               material == Material.DIORITE_SLAB ||
               material == Material.STONE_STAIRS ||
               material == Material.OAK_STAIRS ||
               material == Material.BIRCH_STAIRS ||
               material == Material.SPRUCE_STAIRS ||
               material == Material.JUNGLE_STAIRS ||
               material == Material.ACACIA_STAIRS ||
               material == Material.DARK_OAK_STAIRS ||
               material == Material.CRIMSON_STAIRS ||
               material == Material.WARPED_STAIRS ||
               material == Material.STONE_BRICK_STAIRS ||
               material == Material.SANDSTONE_STAIRS ||
               material == Material.RED_SANDSTONE_STAIRS ||
               material == Material.PURPUR_STAIRS ||
               material == Material.QUARTZ_STAIRS ||
               material == Material.PRISMARINE_STAIRS ||
               material == Material.DARK_PRISMARINE_STAIRS ||
               material == Material.PRISMARINE_BRICK_STAIRS ||
               material == Material.POLISHED_GRANITE_STAIRS ||
               material == Material.SMOOTH_RED_SANDSTONE_STAIRS ||
               material == Material.MOSSY_STONE_BRICK_STAIRS ||
               material == Material.POLISHED_DIORITE_STAIRS ||
               material == Material.MOSSY_COBBLESTONE_STAIRS ||
               material == Material.END_STONE_BRICK_STAIRS ||
               material == Material.SMOOTH_SANDSTONE_STAIRS ||
               material == Material.SMOOTH_QUARTZ_STAIRS ||
               material == Material.GRANITE_STAIRS ||
               material == Material.ANDESITE_STAIRS ||
               material == Material.RED_NETHER_BRICK_STAIRS ||
               material == Material.POLISHED_ANDESITE_STAIRS ||
               material == Material.DIORITE_STAIRS ||
               material == Material.CHEST ||
               material == Material.TRAPPED_CHEST ||
               material == Material.ENDER_CHEST ||
               material == Material.BARREL ||
               material == Material.SHULKER_BOX ||
               material == Material.BLACK_SHULKER_BOX ||
               material == Material.BLUE_SHULKER_BOX ||
               material == Material.BROWN_SHULKER_BOX ||
               material == Material.CYAN_SHULKER_BOX ||
               material == Material.GRAY_SHULKER_BOX ||
               material == Material.GREEN_SHULKER_BOX ||
               material == Material.LIGHT_BLUE_SHULKER_BOX ||
               material == Material.LIGHT_GRAY_SHULKER_BOX ||
               material == Material.LIME_SHULKER_BOX ||
               material == Material.MAGENTA_SHULKER_BOX ||
               material == Material.ORANGE_SHULKER_BOX ||
               material == Material.PINK_SHULKER_BOX ||
               material == Material.PURPLE_SHULKER_BOX ||
               material == Material.RED_SHULKER_BOX ||
               material == Material.WHITE_SHULKER_BOX ||
               material == Material.YELLOW_SHULKER_BOX;
    }
}
