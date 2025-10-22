package com.example.customcommands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Продвинутый менеджер поворота для сидящих игроков
 * Использует техники из оригинального Gsit
 */
public class RotationManager {
    
    private final CustomCommandsPlugin plugin;
    private final Map<UUID, BukkitTask> rotationTasks = new HashMap<>();
    private final Map<UUID, Float> lastYaw = new HashMap<>();
    private final Map<UUID, Float> lastPitch = new HashMap<>();
    private final Map<UUID, Long> lastUpdate = new HashMap<>();
    
    public RotationManager(CustomCommandsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Запускает продвинутую систему поворота для игрока
     */
    public void startAdvancedRotation(Player player, ArmorStand armorStand) {
        if (rotationTasks.containsKey(player.getUniqueId())) {
            stopAdvancedRotation(player);
        }
        
        // Инициализируем данные
        lastYaw.put(player.getUniqueId(), player.getYaw());
        lastPitch.put(player.getUniqueId(), player.getPitch());
        lastUpdate.put(player.getUniqueId(), System.currentTimeMillis());
        
        // Запускаем задачу синхронизации поворота
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || armorStand.isDead()) {
                    stopAdvancedRotation(player);
                    return;
                }
                
                updatePlayerRotation(player, armorStand);
            }
        }.runTaskTimer(plugin, 0L, 1L); // Каждый тик (20 раз в секунду)
        
        rotationTasks.put(player.getUniqueId(), task);
    }
    
    /**
     * Останавливает продвинутую систему поворота
     */
    public void stopAdvancedRotation(Player player) {
        BukkitTask task = rotationTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        
        lastYaw.remove(player.getUniqueId());
        lastPitch.remove(player.getUniqueId());
        lastUpdate.remove(player.getUniqueId());
    }
    
    /**
     * Продвинутое обновление поворота игрока
     */
    private void updatePlayerRotation(Player player, ArmorStand armorStand) {
        if (!player.isOnline() || armorStand.isDead()) {
            return;
        }
        
        float currentYaw = player.getYaw();
        float currentPitch = player.getPitch();
        long currentTime = System.currentTimeMillis();
        
        // Получаем предыдущие значения
        Float lastYawValue = lastYaw.get(player.getUniqueId());
        Float lastPitchValue = lastPitch.get(player.getUniqueId());
        Long lastUpdateTime = lastUpdate.get(player.getUniqueId());
        
        if (lastYawValue == null || lastPitchValue == null || lastUpdateTime == null) {
            return;
        }
        
        // Проверяем изменился ли поворот
        boolean yawChanged = Math.abs(currentYaw - lastYawValue) > 0.1f;
        boolean pitchChanged = Math.abs(currentPitch - lastPitchValue) > 0.1f;
        
        if (yawChanged || pitchChanged) {
            // Применяем продвинутую синхронизацию
            applyAdvancedRotation(player, armorStand, currentYaw, currentPitch);
            
            // Обновляем сохраненные значения
            lastYaw.put(player.getUniqueId(), currentYaw);
            lastPitch.put(player.getUniqueId(), currentPitch);
            lastUpdate.put(player.getUniqueId(), currentTime);
        }
    }
    
    /**
     * Применяет продвинутую синхронизацию поворота
     */
    private void applyAdvancedRotation(Player player, ArmorStand armorStand, float yaw, float pitch) {
        // Метод 1: NMS-based rotation (без teleportation)
        setEntityRotationNMS(armorStand, yaw, pitch);
        
        // Метод 2: Packet-based rotation для игрока
        sendRotationPacket(player, yaw, pitch);
        
        // Метод 3: Плавная интерполяция для естественности
        smoothRotation(player, armorStand, yaw, pitch);
    }
    
    
    /**
     * NMS-based поворот Entity
     */
    private void setEntityRotationNMS(ArmorStand armorStand, float yaw, float pitch) {
        try {
            // Получаем NMS entity
            Object nmsEntity = armorStand.getClass().getMethod("getHandle").invoke(armorStand);
            
            // Устанавливаем поворот через NMS
            Method setYawPitch = nmsEntity.getClass().getMethod("setYawPitch", float.class, float.class);
            setYawPitch.invoke(nmsEntity, yaw, pitch);
            
            // Обновляем поворот в мире
            Method setHeadRotation = nmsEntity.getClass().getMethod("setHeadRotation", float.class);
            setHeadRotation.invoke(nmsEntity, yaw);
            
        } catch (Exception e) {
            // Fallback к стандартному API
            armorStand.setRotation(yaw, pitch);
        }
    }
    
    /**
     * Packet-based поворот игрока
     */
    private void sendRotationPacket(Player player, float yaw, float pitch) {
        try {
            // Получаем NMS player
            Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            
            // Устанавливаем поворот через NMS
            Method setYawPitch = nmsPlayer.getClass().getMethod("setYawPitch", float.class, float.class);
            setYawPitch.invoke(nmsPlayer, yaw, pitch);
            
            // Отправляем пакет поворота всем игрокам
            sendRotationPacketToNearbyPlayers(player, yaw, pitch);
            
        } catch (Exception e) {
            // Fallback - ничего не делаем, чтобы не сломать riding
        }
    }
    
    /**
     * Отправляет пакет поворота ближайшим игрокам
     */
    private void sendRotationPacketToNearbyPlayers(Player player, float yaw, float pitch) {
        try {
            // Получаем NMS player
            Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
            
            // Получаем connection
            Field connectionField = nmsPlayer.getClass().getField("connection");
            Object connection = connectionField.get(nmsPlayer);
            
            // Создаем пакет поворота
            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntity$PacketPlayOutEntityLook");
            Object packet = packetClass.getConstructor(int.class, byte.class, byte.class, boolean.class)
                    .newInstance(player.getEntityId(), (byte) (yaw * 256.0F / 360.0F), (byte) (pitch * 256.0F / 360.0F), true);
            
            // Отправляем пакет всем игрокам в радиусе
            for (Player nearbyPlayer : player.getWorld().getPlayers()) {
                if (nearbyPlayer.equals(player)) continue;
                
                double distance = player.getLocation().distance(nearbyPlayer.getLocation());
                if (distance <= 64) {
                    Object nearbyNmsPlayer = nearbyPlayer.getClass().getMethod("getHandle").invoke(nearbyPlayer);
                    Object nearbyConnection = nearbyNmsPlayer.getClass().getField("connection").get(nearbyNmsPlayer);
                    
                    Method sendPacket = nearbyConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.network.protocol.Packet"));
                    sendPacket.invoke(nearbyConnection, packet);
                }
            }
            
        } catch (Exception e) {
            // Fallback - ничего не делаем
        }
    }
    
    /**
     * Плавная интерполяция поворота (без teleportation)
     */
    public void smoothRotation(Player player, ArmorStand armorStand, float targetYaw, float targetPitch) {
        float currentYaw = armorStand.getYaw();
        float currentPitch = armorStand.getPitch();
        
        // Вычисляем разность углов
        float yawDiff = normalizeAngle(targetYaw - currentYaw);
        float pitchDiff = normalizeAngle(targetPitch - currentPitch);
        
        // Применяем плавную интерполяцию
        float smoothYaw = currentYaw + (yawDiff * 0.3f);
        float smoothPitch = currentPitch + (pitchDiff * 0.3f);
        
        // Нормализуем углы
        smoothYaw = normalizeAngle(smoothYaw);
        smoothPitch = normalizeAngle(smoothPitch);
        
        // Применяем поворот только к ArmorStand (без teleportation игрока)
        armorStand.setRotation(smoothYaw, smoothPitch);
    }
    
    /**
     * Нормализует угол в диапазоне -180 до 180
     */
    private float normalizeAngle(float angle) {
        while (angle > 180.0f) {
            angle -= 360.0f;
        }
        while (angle < -180.0f) {
            angle += 360.0f;
        }
        return angle;
    }
    
    /**
     * Очищает все данные поворота для игрока
     */
    public void clearPlayerData(Player player) {
        stopAdvancedRotation(player);
    }
    
    /**
     * Очищает все данные поворота
     */
    public void clearAllData() {
        for (UUID playerId : rotationTasks.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                stopAdvancedRotation(player);
            }
        }
        rotationTasks.clear();
        lastYaw.clear();
        lastPitch.clear();
        lastUpdate.clear();
    }
    
    /**
     * Проверяет активна ли продвинутая система поворота
     */
    public boolean isAdvancedRotationActive(Player player) {
        return rotationTasks.containsKey(player.getUniqueId());
    }
}
