package com.example.customcommands;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.GameMode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ThiefManager {
    
    private final Map<UUID, Boolean> thiefModePlayers = new HashMap<>();
    private final Map<UUID, Location> lastLocation = new HashMap<>();
    private final Map<UUID, Long> lastMoveTime = new HashMap<>();
    private final Map<UUID, Player> stealingFrom = new HashMap<>();
    private final Map<UUID, Long> stealStartTime = new HashMap<>();
    private final Map<UUID, Boolean> inCountdown = new HashMap<>();
    private final Map<UUID, Long> lastTheftTime = new HashMap<>();
    private final Map<UUID, Long> lastCooldownMessage = new HashMap<>();
    private CustomCommandsPlugin plugin;
    
    public boolean isInThiefMode(Player player) {
        return thiefModePlayers.getOrDefault(player.getUniqueId(), false);
    }
    
    public void setThiefMode(Player player, boolean enabled) {
        if (enabled) {
            thiefModePlayers.put(player.getUniqueId(), true);
            lastLocation.put(player.getUniqueId(), player.getLocation());
            lastMoveTime.put(player.getUniqueId(), System.currentTimeMillis());
        } else {
            thiefModePlayers.remove(player.getUniqueId());
            lastLocation.remove(player.getUniqueId());
            lastMoveTime.remove(player.getUniqueId());
            stealingFrom.remove(player.getUniqueId());
            stealStartTime.remove(player.getUniqueId());
            inCountdown.remove(player.getUniqueId());
            lastCooldownMessage.remove(player.getUniqueId());
        }
    }
    
    public void updatePlayerLocation(Player player) {
        if (isInThiefMode(player)) {
            Location currentLoc = player.getLocation();
            Location lastLoc = lastLocation.get(player.getUniqueId());
            
            // Если игрок в режиме отсчета, не сбрасываем попытку кражи при движении
            if (isInCountdown(player)) {
                // Разрешаем движение камерой, но обновляем позицию
                lastLocation.put(player.getUniqueId(), currentLoc);
                return;
            }
            
            // Проверяем только изменение координат (X, Y, Z), игнорируем поворот камеры
            if (lastLoc != null && 
                (currentLoc.getX() != lastLoc.getX() || 
                 currentLoc.getY() != lastLoc.getY() || 
                 currentLoc.getZ() != lastLoc.getZ())) {
                lastMoveTime.put(player.getUniqueId(), System.currentTimeMillis());
                // Сброс попытки кражи при движении только если не в отсчете
                stealingFrom.remove(player.getUniqueId());
                stealStartTime.remove(player.getUniqueId());
            }
            
            lastLocation.put(player.getUniqueId(), currentLoc);
        }
    }
    
    public boolean canStartStealing(Player thief, Player target) {
        if (!isInThiefMode(thief)) return false;
        if (stealingFrom.containsKey(thief.getUniqueId())) return false;
        
        // Если вор пытается "украсть" у самого себя, это перекладывание предметов
        if (thief.equals(target)) {
            return true; // Разрешаем перекладывание предметов
        }
        
        Long lastMove = lastMoveTime.get(thief.getUniqueId());
        if (lastMove == null) return false;
        
        return (System.currentTimeMillis() - lastMove) >= 3000; // 3 секунды
    }
    
    public void startStealing(Player thief, Player target) {
        stealingFrom.put(thief.getUniqueId(), target);
        stealStartTime.put(thief.getUniqueId(), System.currentTimeMillis());
    }
    
    public boolean isStealing(Player thief) {
        return stealingFrom.containsKey(thief.getUniqueId());
    }
    
    public Player getStealingTarget(Player thief) {
        return stealingFrom.get(thief.getUniqueId());
    }
    
    public void stopStealing(Player thief) {
        stealingFrom.remove(thief.getUniqueId());
        stealStartTime.remove(thief.getUniqueId());
    }
    
    public boolean canStealFromPlayer(Player thief, Player target) {
        // Проверка базовых прав вора
        if (!thief.hasPermission("customcommands.thief.steal")) {
            return false;
        }
        
        // Проверка, что цель не в креативном режиме (если у вора нет специального права)
        if (target.getGameMode() == GameMode.CREATIVE && 
            !thief.hasPermission("customcommands.thief.steal_creative")) {
            return false;
        }
        
        return true;
    }
    
    public String getStealBlockReason(Player thief, Player target) {
        if (!thief.hasPermission("customcommands.thief.steal")) {
            return "У вас нет прав для кражи!";
        }
        
        if (target.getGameMode() == GameMode.CREATIVE && 
            !thief.hasPermission("customcommands.thief.steal_creative")) {
            return "Нельзя красть у игроков в креативном режиме!";
        }
        
        return null; // Можно красть
    }
    
    // Новые методы для управления отсчетом
    public void startCountdown(Player player) {
        inCountdown.put(player.getUniqueId(), true);
    }
    
    public void stopCountdown(Player player) {
        inCountdown.remove(player.getUniqueId());
    }
    
    public boolean isInCountdown(Player player) {
        return inCountdown.getOrDefault(player.getUniqueId(), false);
    }
    
    // Методы для cooldown системы
    public void setPlugin(CustomCommandsPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void setLastTheftTime(Player player, boolean success) {
        long cooldownTime = success ? 30 * 60 * 1000L : 10 * 60 * 1000L; // 30 мин успех, 10 мин неудача
        lastTheftTime.put(player.getUniqueId(), System.currentTimeMillis() + cooldownTime);
    }
    
    public boolean canSteal(Player player) {
        Long lastTheft = lastTheftTime.get(player.getUniqueId());
        if (lastTheft == null) return true;
        return System.currentTimeMillis() >= lastTheft;
    }
    
    public long getRemainingCooldown(Player player) {
        Long lastTheft = lastTheftTime.get(player.getUniqueId());
        if (lastTheft == null) return 0;
        return Math.max(0, lastTheft - System.currentTimeMillis());
    }
    
    public void resetCooldown(Player player) {
        lastTheftTime.remove(player.getUniqueId());
    }
    
    public void resetCooldownByNickname(String nickname) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(nickname)) {
                resetCooldown(player);
                break;
            }
        }
    }
    
    public boolean canSendCooldownMessage(Player player) {
        Long lastMessage = lastCooldownMessage.get(player.getUniqueId());
        if (lastMessage == null) return true;
        return System.currentTimeMillis() - lastMessage >= 2000; // 2 секунды между сообщениями
    }
    
    public void setLastCooldownMessage(Player player) {
        lastCooldownMessage.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    public boolean canEnterThiefMode(Player player) {
        // Проверяем Gsit
        if (plugin.getGsitManager().isPlayerSitting(player)) {
            return false;
        }
        
        // Проверяем кастомную систему сидения
        if (plugin.getSitManager().isPlayerSitting(player)) {
            return false;
        }
        
        return true;
    }
}

