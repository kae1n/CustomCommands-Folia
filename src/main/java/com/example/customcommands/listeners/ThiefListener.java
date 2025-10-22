package com.example.customcommands.listeners;

import com.example.customcommands.CustomCommandsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

public class ThiefListener implements Listener {
    
    private final CustomCommandsPlugin plugin;
    private final Random random = new Random();
    
    public ThiefListener(CustomCommandsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) return;
        
        Player thief = event.getPlayer();
        Player target = (Player) event.getRightClicked();
        
        // Проверка базовых условий (только режим вора)
        if (!plugin.getThiefManager().isInThiefMode(thief)) return;
        if (plugin.getThiefManager().isStealing(thief)) return;

        // Проверка Gsit: если вор сидит - блокируем
        if (plugin.getGsitManager().isPlayerSitting(thief)) {
            String message = plugin.getGsitManager().getThiefModeBlockedMessage(thief);
            if (!message.isEmpty()) {
                thief.sendMessage(message);
            }
            event.setCancelled(true);
            return;
        }
        
        // Проверка нашей системы сидения: если вор сидит - блокируем
        if (plugin.getSitManager().isPlayerSitting(thief)) {
            String message = plugin.getSitManager().getThiefModeBlockedMessage(thief);
            if (!message.isEmpty()) {
                thief.sendMessage(message);
            }
            event.setCancelled(true);
            return;
        }

        // Запрещаем воровать у самого себя
        if (thief.equals(target)) {
            thief.sendMessage(Component.text("Нельзя воровать у самого себя!")
                    .color(TextColor.color(0xFF5555))
                    .decorate(TextDecoration.BOLD));
            event.setCancelled(true);
            return;
        }

        // Проверка cooldown кражи
        if (!plugin.getThiefManager().canSteal(thief)) {
            // Проверяем cooldown для сообщений (2 секунды между сообщениями)
            if (plugin.getThiefManager().canSendCooldownMessage(thief)) {
                long remainingTime = plugin.getThiefManager().getRemainingCooldown(thief);
                long minutes = remainingTime / (60 * 1000);
                long seconds = (remainingTime % (60 * 1000)) / 1000;
                
                Component cooldownMessage = Component.text("Кража доступна через " + minutes + " минут " + seconds + " секунд..")
                        .color(TextColor.color(0xFFAA00));
                thief.sendMessage(cooldownMessage);
                plugin.getThiefManager().setLastCooldownMessage(thief);
            }
            event.setCancelled(true); // Отменяем событие
            return;
        }

        // Проверка прав на кражу у конкретного игрока
        if (!plugin.getThiefManager().canStealFromPlayer(thief, target)) {
            String reason = plugin.getThiefManager().getStealBlockReason(thief, target);
            thief.sendMessage(Component.text(reason)
                    .color(TextColor.color(0xFF5555))
                    .decorate(TextDecoration.BOLD));
            event.setCancelled(true);
            return;
        }
        
        // Всегда начинаем процесс кражи при ПКМ на потерпевшего
        plugin.getThiefManager().startStealing(thief, target);

        // Сразу запускаем анимацию кражи
        startStealCountdown(thief, target);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        plugin.getThiefManager().updatePlayerLocation(event.getPlayer());
    }
    
    // УДАЛЕНО: Обработчик кликов по инвентарю больше не нужен
    // Теперь используется автоматическое воровство случайного предмета
    
    // УДАЛЕНО: Обработчик закрытия инвентаря больше не нужен
    // Теперь используется автоматическое воровство случайного предмета
    
    private void startStealCountdown(Player thief, Player target) {
        // Начинаем отсчет
        plugin.getThiefManager().startCountdown(thief);
        
        // Отладочное сообщение
        plugin.getLogger().info("Начинаем отсчет для " + thief.getName() + " -> " + target.getName());
        
        // Отправляем анимированный countdown
        sendCountdownAnimation(thief, 1);
        
        // Запускаем таймер на 1 секунду
        runDelayedTask(plugin, thief, () -> {
            if (!thief.isOnline() || !target.isOnline()) {
                plugin.getThiefManager().stopStealing(thief);
                return;
            }
            
            // Проверяем, что игрок не двигался во время отсчета
            if (!plugin.getThiefManager().isStealing(thief) || 
                !plugin.getThiefManager().getStealingTarget(thief).equals(target)) {
                return;
            }
            
            plugin.getLogger().info("Отправляем сообщение 2 для " + thief.getName());
            sendCountdownAnimation(thief, 2);
            
            // Запускаем таймер на 1 секунду
            runDelayedTask(plugin, thief, () -> {
                if (!thief.isOnline() || !target.isOnline()) {
                    plugin.getThiefManager().stopStealing(thief);
                    return;
                }
                
                // Проверяем, что игрок не двигался во время отсчета
                if (!plugin.getThiefManager().isStealing(thief) || 
                    !plugin.getThiefManager().getStealingTarget(thief).equals(target)) {
                    return;
                }
                
                plugin.getLogger().info("Отправляем сообщение 3 для " + thief.getName());
                sendCountdownAnimation(thief, 3);
                
                // Запускаем таймер на 1 секунду для открытия GUI
                runDelayedTask(plugin, thief, () -> {
                    if (!thief.isOnline() || !target.isOnline()) {
                        plugin.getThiefManager().stopStealing(thief);
                        return;
                    }
                    
                    // Проверяем, что игрок не двигался во время отсчета
                    if (!plugin.getThiefManager().isStealing(thief) || 
                        !plugin.getThiefManager().getStealingTarget(thief).equals(target)) {
                        return;
                    }
                    
                    plugin.getLogger().info("Открываем инвентарь для " + thief.getName());
                    
                    // Завершаем отсчет
                    plugin.getThiefManager().stopCountdown(thief);
                    
                    // Отправляем финальное сообщение
                    sendFinalMessage(thief);
                    
                    // Автоматически выполняем воровство случайного предмета
                    performRandomTheft(thief, target);
                }, 20L); // 1 секунда = 20 тиков
            }, 20L); // 1 секунда = 20 тиков
        }, 20L); // 1 секунда = 20 тиков
    }
    
    // УДАЛЕНО: Метод openTargetInventory больше не нужен
    // Теперь используется автоматическое воровство случайного предмета
    
    // Новый метод для автоматического воровства случайного предмета
    private void performRandomTheft(Player thief, Player target) {
        // КРИТИЧЕСКАЯ ЗАЩИТА: Запрещаем само-кражу
        if (thief.equals(target)) {
            thief.sendMessage(Component.text("§c§lОШИБКА! §fВы не можете воровать у самого себя!")
                    .color(TextColor.color(0xFF0000))
                    .decorate(TextDecoration.BOLD));
            plugin.getThiefManager().stopStealing(thief);
            return;
        }
        
        // Получаем все предметы из инвентаря жертвы
        java.util.List<ItemStack> availableItems = new java.util.ArrayList<>();
        
        // Проверяем хотбар (слоты 0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack item = target.getInventory().getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                availableItems.add(item);
            }
        }
        
        // Проверяем основной инвентарь (слоты 9-35)
        for (int i = 9; i < 36; i++) {
            ItemStack item = target.getInventory().getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                availableItems.add(item);
            }
        }
        
        // Если нет предметов для кражи
        if (availableItems.isEmpty()) {
            thief.sendMessage(Component.text("§e§lПУСТО! §fУ жертвы нет предметов для кражи!")
                    .color(TextColor.color(0xFFAA00))
                    .decorate(TextDecoration.BOLD));
            plugin.getThiefManager().stopStealing(thief);
            return;
        }
        
        // Выбираем случайный предмет
        ItemStack randomItem = availableItems.get(random.nextInt(availableItems.size()));
        
        // Определяем шанс кражи
        double stealChance = getStealChance(randomItem);
        boolean success = random.nextDouble() < stealChance;
        
        if (success) {
            // Успешная кража
            ItemStack stolenItem = randomItem.clone();
            
            // Удаляем предмет из инвентаря жертвы
            for (int i = 0; i < 36; i++) {
                ItemStack item = target.getInventory().getItem(i);
                if (item != null && item.equals(randomItem)) {
                    target.getInventory().setItem(i, new ItemStack(Material.AIR));
                    break;
                }
            }
            
            // Добавляем предмет в инвентарь вора
            int freeSlot = findFreeSlot(thief.getInventory());
            if (freeSlot != -1) {
                thief.getInventory().setItem(freeSlot, stolenItem);
            } else {
                thief.getInventory().addItem(stolenItem);
            }
            
            // Сообщение об успешной краже
            String rarityText = stealChance == 0.1 ? " (редкий предмет!)" : "";
            Component successMessage = Component.text("✅ Удачная кража! Вы украли: " + stolenItem.getType().name() + rarityText)
                    .color(TextColor.color(0x55FF55))
                    .decorate(TextDecoration.BOLD);
            thief.sendMessage(successMessage);
            
            // ActionBar с успехом
            Component actionBarMessage = Component.text("🎯 Успешно украдено: " + stolenItem.getType().name())
                    .color(TextColor.color(0x55FF55));
            thief.sendActionBar(actionBarMessage);
            
            // Title для успешной кражи
            thief.sendTitle("§a§lКРАЖА УСПЕШНА!", "§fПолучен: " + stolenItem.getType().name(), 0, 30, 10);
            
            // Устанавливаем cooldown на 30 минут после успешной кражи
            plugin.getThiefManager().setLastTheftTime(thief, true);
            
            // Уведомление жертвы через 1 минуту 30 секунд (90 секунд = 1800 тиков)
            scheduleVictimNotification(target, true);
            
        } else {
            // Неудачная кража - только вор получает уведомления
            
            // Специальное сообщение для вора
            Component thiefFailMessage = Component.text("💥 Попытка кражи провалилась! Вас заметили!")
                    .color(TextColor.color(0xFF5555))
                    .decorate(TextDecoration.BOLD);
            thief.sendMessage(thiefFailMessage);
            
            // Title для вора
            thief.sendTitle("§c§lКРАЖА ПРОВАЛИЛАСЬ!", "§fВас заметили!", 0, 30, 10);
            
            // Устанавливаем cooldown на 10 минут после неудачной кражи
            plugin.getThiefManager().setLastTheftTime(thief, false);
            
            // Уведомление жертвы через 15 секунд (15 секунд = 300 тиков)
            scheduleVictimNotification(target, false);
        }
        
        // Останавливаем процесс кражи
        plugin.getThiefManager().stopStealing(thief);
    }
    
    
    
    private double getStealChance(ItemStack item) {
        // Проверяем редкость предмета через NBT или метаданные
        if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
            // Если предмет зачарован, считаем его фиолетовой редкости
            return 0.1; // 10% шанс
        }
        
        // Проверяем по типу предмета (можно расширить список)
        Material material = item.getType();
        if (isPurpleRarityItem(material)) {
            return 0.1; // 10% шанс
        }
        
        // Обычные предметы
        return 0.3; // 30% шанс
    }
    
    private boolean isPurpleRarityItem(Material material) {
        // Список предметов фиолетовой редкости
        return material == Material.NETHERITE_SWORD ||
               material == Material.NETHERITE_AXE ||
               material == Material.NETHERITE_PICKAXE ||
               material == Material.NETHERITE_SHOVEL ||
               material == Material.NETHERITE_HOE ||
               material == Material.NETHERITE_HELMET ||
               material == Material.NETHERITE_CHESTPLATE ||
               material == Material.NETHERITE_LEGGINGS ||
               material == Material.NETHERITE_BOOTS ||
               material == Material.ELYTRA ||
               material == Material.TOTEM_OF_UNDYING ||
               material == Material.ENCHANTED_GOLDEN_APPLE;
    }
    
    // Анимированный countdown через ActionBar
    private void sendCountdownAnimation(Player thief, int seconds) {
        // Анимированный ActionBar с пульсацией
        String[] animationFrames = {
            "⏰ ВСКРЫВАЕМ КАРМАНЫ...",
            "⏰ ВСКРЫВАЕМ КАРМАНЫ..",
            "⏰ ВСКРЫВАЕМ КАРМАНЫ.",
            "⏰ ВСКРЫВАЕМ КАРМАНЫ..."
        };
        
        // Показываем анимацию в ActionBar
        for (int i = 0; i < animationFrames.length; i++) {
            final int frameIndex = i;
            runDelayedTask(plugin, thief, () -> {
                Component actionBarMessage = Component.text(animationFrames[frameIndex])
                        .color(TextColor.color(0xFF00FF))
                        .decorate(TextDecoration.BOLD);
                thief.sendActionBar(actionBarMessage);
            }, i * 5L); // Каждый кадр через 5 тиков (0.25 секунды)
        }
        
        // Title для драматического эффекта
        thief.sendTitle("§d§lВСКРЫВАЕМ КАРМАНЫ", "§f§l" + seconds + "...", 0, 20, 5);
    }
    
    // Финальное сообщение перед открытием инвентаря
    private void sendFinalMessage(Player thief) {
        // Очищаем ActionBar
        thief.sendActionBar(Component.empty());
        
        // Отправляем финальное сообщение
        Component finalMessage = Component.text("✅ Карманы вскрыты! Попытка кражи...")
                .color(TextColor.color(0x55FF55))
                .decorate(TextDecoration.BOLD);
        thief.sendMessage(finalMessage);
        
        // Финальный Title
        thief.sendTitle("§a§lКАРМАНЫ ВСКРЫТЫ!", "§fПопытка кражи...", 0, 40, 10);
    }
    
    
    // УДАЛЕНО: Метод handleTheft больше не нужен
    // Теперь используется автоматическое воровство случайного предмета
    
    // Простой метод для запуска отложенных задач (только для Paper)
    private void runDelayedTask(CustomCommandsPlugin plugin, Player player, Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                if (player != null && player.isOnline()) {
                    task.run();
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка в отложенной задаче: " + e.getMessage());
            }
        }, delayTicks);
    }
    
    // Метод для поиска свободного слота в инвентаре вора
    private int findFreeSlot(PlayerInventory inventory) {
        // Сначала ищем в хотбаре (слоты 0-8)
        for (int i = 0; i < 9; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                return i;
            }
        }
        // Затем в основной части инвентаря (слоты 9-35)
        for (int i = 9; i < 36; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                return i;
            }
        }
        return -1; // Нет свободных слотов
    }
    
    // Метод для планирования уведомления жертвы
    private void scheduleVictimNotification(Player victim, boolean wasSuccessful) {
        long delayTicks;
        String[] messages;
        
        if (wasSuccessful) {
            // Успешная кража - уведомление через 1 минуту 30 секунд
            delayTicks = 1800L; // 90 секунд = 1800 тиков
            messages = new String[]{
                "Вы испытываете странные ощущения...",
                "В воздухе напряжение...",
                "В карманах стало пусто..."
            };
        } else {
            // Неудачная кража - уведомление через 15 секунд
            delayTicks = 300L; // 15 секунд = 300 тиков
            messages = new String[]{
                "Вы испытываете странные ощущения...",
                "В воздухе напряжение...",
                "В карманах стало пусто..."
            };
        }
        
        // Выбираем случайное сообщение
        String randomMessage = messages[random.nextInt(messages.length)];
        
        // Планируем отправку сообщения
        runDelayedTask(plugin, victim, () -> {
            if (victim != null && victim.isOnline()) {
                // Отправляем сообщение с напрягающим звуком
                Component notificationMessage = Component.text(randomMessage)
                        .color(TextColor.color(0xFFAA00))
                        .decorate(TextDecoration.ITALIC);
                victim.sendMessage(notificationMessage);
                
                if (wasSuccessful) {
                    // Для успешной кражи - случайный страшный звук
                    playRandomScarySound(victim);
                } else {
                    // Для неудачной кражи - обычные звуки
                    victim.playSound(victim.getLocation(), org.bukkit.Sound.ENTITY_SKELETON_AMBIENT, 0.5f, 0.8f);
                    victim.playSound(victim.getLocation(), org.bukkit.Sound.BLOCK_PORTAL_AMBIENT, 0.3f, 1.2f);
                }
            }
        }, delayTicks);
    }
    
    // Метод для воспроизведения случайного страшного звука
    private void playRandomScarySound(Player victim) {
        // Массив страшных звуков для успешной кражи
        org.bukkit.Sound[] scarySounds = {
            org.bukkit.Sound.ENTITY_SKELETON_AMBIENT,      // Звук скелета
            org.bukkit.Sound.ENTITY_ENDERMAN_AMBIENT,     // Звук эндермена
            org.bukkit.Sound.ENTITY_WITHER_SKELETON_AMBIENT, // Звук визер-скелета
            org.bukkit.Sound.ENTITY_ZOMBIE_AMBIENT,       // Звук зомби
            org.bukkit.Sound.ENTITY_PHANTOM_AMBIENT        // Звук фантома
        };
        
        // Выбираем случайный звук
        org.bukkit.Sound randomSound = scarySounds[random.nextInt(scarySounds.length)];
        
        // Воспроизводим основной страшный звук
        victim.playSound(victim.getLocation(), randomSound, 0.6f, 0.7f);
        
        // Дополнительный атмосферный звук (всегда портал)
        victim.playSound(victim.getLocation(), org.bukkit.Sound.BLOCK_PORTAL_AMBIENT, 0.4f, 1.0f);
        
        // Иногда добавляем третий звук для большей атмосферы
        if (random.nextBoolean()) {
            victim.playSound(victim.getLocation(), org.bukkit.Sound.ENTITY_BAT_AMBIENT, 0.3f, 0.5f);
        }
    }
}
