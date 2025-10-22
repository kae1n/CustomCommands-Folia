package com.example.customcommands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Менеджер для интеграции с плагином Gsit
 * Блокирует функционал Gsit когда активен режим вора
 */
public class GsitManager {
    
    private final CustomCommandsPlugin plugin;
    private Plugin gsitPlugin;
    private boolean gsitAvailable = false;
    
    public GsitManager(CustomCommandsPlugin plugin) {
        this.plugin = plugin;
        initializeGsit();
    }
    
    /**
     * Инициализация Gsit плагина
     */
    private void initializeGsit() {
        try {
            gsitPlugin = Bukkit.getPluginManager().getPlugin("Gsit");
            if (gsitPlugin != null && gsitPlugin.isEnabled()) {
                gsitAvailable = true;
                plugin.getLogger().info("Gsit интеграция активирована!");
            } else {
                gsitAvailable = false;
                plugin.getLogger().info("Gsit плагин не найден или отключен - интеграция отключена");
            }
        } catch (Exception e) {
            gsitAvailable = false;
            plugin.getLogger().info("Gsit плагин недоступен - интеграция отключена");
        }
    }
    
    /**
     * Проверяет доступен ли Gsit
     */
    public boolean isGsitAvailable() {
        return gsitAvailable;
    }
    
    /**
     * Проверяет сидит ли игрок
     */
    public boolean isPlayerSitting(Player player) {
        if (!gsitAvailable || player == null) {
            return false;
        }
        
        try {
            // Проверяем через Gsit API - используем рефлексию для безопасности
            Class<?> gsitClass = Class.forName("com.github.deanveloper.gsit.Gsit");
            Object gsitInstance = gsitClass.getMethod("getInstance").invoke(null);
            Object playerSitting = gsitClass.getMethod("isPlayerSitting", Player.class).invoke(gsitInstance, player);
            return (Boolean) playerSitting;
        } catch (Exception e) {
            // Fallback: проверяем через NBT или другие методы
            return false;
        }
    }
    
    /**
     * Заставляет игрока встать (если он сидит)
     */
    public void makePlayerStand(Player player) {
        if (!gsitAvailable || player == null || !isPlayerSitting(player)) {
            return;
        }
        
        try {
            // Отправляем команду встать через Gsit
            player.performCommand("gsit unsit");
        } catch (Exception e) {
            plugin.getLogger().warning("Не удалось заставить игрока " + player.getName() + " встать: " + e.getMessage());
        }
    }
    
    /**
     * Блокирует возможность сесть для игрока в режиме вора
     */
    public boolean canPlayerSit(Player player) {
        if (!gsitAvailable) {
            return true; // Если Gsit недоступен, разрешаем обычное поведение
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
        if (!gsitAvailable) {
            return true; // Если Gsit недоступен, разрешаем
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
        if (!gsitAvailable) {
            return "";
        }
        
        if (isPlayerSitting(player)) {
            return "§cВы не можете войти в режим вора сидя! Встаньте сначала.";
        }
        
        return "";
    }
    
    /**
     * Получает сообщение о том, почему игрок не может сесть
     */
    public String getSitBlockedMessage(Player player) {
        if (!gsitAvailable) {
            return "";
        }
        
        if (plugin.getThiefManager().isInThiefMode(player)) {
            return "§cВы не можете сесть в режиме вора! Сначала выйдите из режима вора.";
        }
        
        return "";
    }
}
