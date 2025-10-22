package com.example.customcommands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Менеджер для управления системой сидения
 * Полностью интегрированная система без внешних зависимостей
 */
public class SitManager {
    
    private final CustomCommandsPlugin plugin;
    private final Map<UUID, ArmorStand> sittingPlayers = new HashMap<>();
    private final Map<UUID, Location> lastLocation = new HashMap<>();
    private final Map<UUID, Long> lastMessageTime = new HashMap<>();
    private RotationManager rotationManager;
    
    public SitManager(CustomCommandsPlugin plugin) {
        this.plugin = plugin;
        this.rotationManager = new RotationManager(plugin);
    }
    
    /**
     * Проверяет сидит ли игрок
     */
    public boolean isPlayerSitting(Player player) {
        return sittingPlayers.containsKey(player.getUniqueId());
    }
    
    /**
     * Заставляет игрока сесть на текущем месте
     */
    public boolean makePlayerSit(Player player) {
        if (isPlayerSitting(player)) {
            return false; // Уже сидит
        }
        
        // Проверяем можно ли сесть (не в режиме вора)
        if (!canPlayerSit(player)) {
            return false;
        }
        
        Location sitLocation = player.getLocation().clone();
        // Не опускаем ArmorStand - сажаем на том же уровне
        
        // Создаем невидимый ArmorStand
        ArmorStand armorStand = player.getWorld().spawn(sitLocation, ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCanPickupItems(false);
        armorStand.setCustomName("SitSeat_" + player.getUniqueId());
        armorStand.setCustomNameVisible(false);
        armorStand.setInvulnerable(true);
        armorStand.setSilent(true);
        armorStand.setSmall(true);
        armorStand.setMarker(true);
        
        // Сажаем игрока
        armorStand.addPassenger(player);
        
        // Запускаем продвинутую систему поворота
        rotationManager.startAdvancedRotation(player, armorStand);
        
        // Сохраняем данные
        sittingPlayers.put(player.getUniqueId(), armorStand);
        lastLocation.put(player.getUniqueId(), player.getLocation());
        
        // Отправляем сообщение
        player.sendMessage("§aВы сели!");
        
        return true;
    }
    
    /**
     * Заставляет игрока сесть на блок
     */
    public boolean makePlayerSitOnBlock(Player player, Location blockLocation) {
        if (isPlayerSitting(player)) {
            return false; // Уже сидит
        }
        
        // Проверяем можно ли сесть
        if (!canPlayerSit(player)) {
            return false;
        }
        
        Location sitLocation = blockLocation.clone();
        sitLocation.setY(sitLocation.getY() + 0.5); // Поднимаем на полблока
        sitLocation.setX(sitLocation.getX() + 0.5); // Центрируем по X
        sitLocation.setZ(sitLocation.getZ() + 0.5); // Центрируем по Z
        
        // Создаем невидимый ArmorStand
        ArmorStand armorStand = player.getWorld().spawn(sitLocation, ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCanPickupItems(false);
        armorStand.setCustomName("SitSeat_" + player.getUniqueId());
        armorStand.setCustomNameVisible(false);
        armorStand.setInvulnerable(true);
        armorStand.setSilent(true);
        armorStand.setSmall(true);
        armorStand.setMarker(true);
        
        // Сажаем игрока
        armorStand.addPassenger(player);
        
        // Запускаем продвинутую систему поворота
        rotationManager.startAdvancedRotation(player, armorStand);
        
        // Сохраняем данные
        sittingPlayers.put(player.getUniqueId(), armorStand);
        lastLocation.put(player.getUniqueId(), player.getLocation());
        
        // Отправляем сообщение
        player.sendMessage("§aВы сели на блок!");
        
        return true;
    }
    
    
    /**
     * Заставляет игрока встать
     */
    public boolean makePlayerStand(Player player) {
        if (!isPlayerSitting(player)) {
            return false; // Не сидит
        }
        
        ArmorStand armorStand = sittingPlayers.get(player.getUniqueId());
        if (armorStand != null && !armorStand.isDead()) {
            // Останавливаем продвинутую систему поворота
            rotationManager.stopAdvancedRotation(player);
            
            // Убираем игрока с ArmorStand
            armorStand.removePassenger(player);
            armorStand.remove();
        }
        
        // Очищаем данные
        sittingPlayers.remove(player.getUniqueId());
        lastLocation.remove(player.getUniqueId());
        
        // Отправляем сообщение
        player.sendMessage("§aВы встали!");
        
        return true;
    }
    
    /**
     * Проверяет может ли игрок сесть
     */
    public boolean canPlayerSit(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        
        // Если игрок в режиме вора - блокируем сидение
        if (plugin.getThiefManager().isInThiefMode(player)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Проверяет может ли игрок войти в режим вора
     */
    public boolean canEnterThiefMode(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        
        // Если игрок сидит - блокируем вход в режим вора
        if (isPlayerSitting(player)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Получает сообщение о том, почему игрок не может войти в режим вора
     */
    public String getThiefModeBlockedMessage(Player player) {
        if (isPlayerSitting(player)) {
            return "§cВы не можете войти в режим вора сидя! Встаньте сначала.";
        }
        
        return "";
    }
    
    /**
     * Получает сообщение о том, почему игрок не может сесть
     */
    public String getSitBlockedMessage(Player player) {
        if (plugin.getThiefManager().isInThiefMode(player)) {
            // Проверяем задержку для сообщений (5 секунд)
            long currentTime = System.currentTimeMillis();
            Long lastMessage = lastMessageTime.get(player.getUniqueId());
            
            if (lastMessage == null || (currentTime - lastMessage) >= 5000) {
                lastMessageTime.put(player.getUniqueId(), currentTime);
                return "§cВы не можете сесть в режиме вора! Сначала выйдите из режима вора.";
            }
            
            return ""; // Не показываем сообщение из-за задержки
        }
        
        return "";
    }
    
    /**
     * Очищает все данные сидения для игрока
     */
    public void clearPlayerData(Player player) {
        if (isPlayerSitting(player)) {
            makePlayerStand(player);
        }
        rotationManager.clearPlayerData(player);
        lastMessageTime.remove(player.getUniqueId());
    }
    
    /**
     * Обновляет поворот сидящего игрока
     */
    public void updatePlayerRotation(Player player) {
        if (!isPlayerSitting(player)) {
            return;
        }
        
        ArmorStand armorStand = sittingPlayers.get(player.getUniqueId());
        if (armorStand != null && !armorStand.isDead()) {
            // Синхронизируем поворот ArmorStand с игроком
            armorStand.setRotation(player.getYaw(), player.getPitch());
        }
    }
    
    /**
     * Очищает все данные сидения
     */
    public void clearAllData() {
        for (UUID playerId : sittingPlayers.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                makePlayerStand(player);
            }
        }
        rotationManager.clearAllData();
        sittingPlayers.clear();
        lastLocation.clear();
        lastMessageTime.clear();
    }
    
}
