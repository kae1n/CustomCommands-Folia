package com.example.customcommands;

import com.example.customcommands.commands.CoinCommand;
import com.example.customcommands.commands.DiceCommand;
import com.example.customcommands.commands.DoCommand;
import com.example.customcommands.commands.MeCommand;
import com.example.customcommands.commands.ResetCooldownCommand;
import com.example.customcommands.commands.SitCommand;
import com.example.customcommands.commands.ThiefModeCommand;
import com.example.customcommands.commands.TryCommand;
import com.example.customcommands.commands.UnsitCommand;
import com.example.customcommands.commands.WhisperCommand;
import com.example.customcommands.listeners.GsitBlockListener;
import com.example.customcommands.listeners.SitListener;
import com.example.customcommands.listeners.ThiefListener;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomCommandsPlugin extends JavaPlugin {
    
    private ThiefManager thiefManager;
    private GsitManager gsitManager;
    private SitManager sitManager;
    
    @Override
    public void onEnable() {
        getLogger().info("=== CUSTOM COMMANDS PLUGIN ENABLED ===");
        getLogger().info("Plugin successfully loaded!");

        // Инициализация менеджеров
        thiefManager = new ThiefManager();
        thiefManager.setPlugin(this);
        gsitManager = new GsitManager(this);
        sitManager = new SitManager(this);
        
        // Регистрация команд
        if (getCommand("coin") != null) {
            getCommand("coin").setExecutor(new CoinCommand());
        }
        if (getCommand("dice") != null) {
            getCommand("dice").setExecutor(new DiceCommand());
        }
        if (getCommand("do") != null) {
            getCommand("do").setExecutor(new DoCommand());
        }
        if (getCommand("me") != null) {
            getCommand("me").setExecutor(new MeCommand());
        }
        if (getCommand("try") != null) {
            getCommand("try").setExecutor(new TryCommand());
        }
        if (getCommand("whisper") != null) {
            getCommand("whisper").setExecutor(new WhisperCommand());
        }
        
        // Новые команды для режима вора
        ThiefModeCommand thiefCommand = new ThiefModeCommand(this);
        if (getCommand("mthief") != null) {
            getCommand("mthief").setExecutor(thiefCommand);
            getLogger().info("Команда /mthief зарегистрирована успешно!");
        } else {
            getLogger().warning("Команда /mthief НЕ НАЙДЕНА в plugin.yml!");
        }
        if (getCommand("mofth") != null) {
            getCommand("mofth").setExecutor(thiefCommand);
            getLogger().info("Команда /mofth зарегистрирована успешно!");
        } else {
            getLogger().warning("Команда /mofth НЕ НАЙДЕНА в plugin.yml!");
        }
        
        // Команда для сброса cooldown
        ResetCooldownCommand resetCommand = new ResetCooldownCommand(this);
        if (getCommand("unloadclcc") != null) {
            getCommand("unloadclcc").setExecutor(resetCommand);
            getLogger().info("Команда /unloadclcc зарегистрирована успешно!");
        } else {
            getLogger().warning("Команда /unloadclcc НЕ НАЙДЕНА в plugin.yml!");
        }
        
        // Команды для сидения
        SitCommand sitCommand = new SitCommand(this);
        if (getCommand("sit") != null) {
            getCommand("sit").setExecutor(sitCommand);
            getLogger().info("Команда /sit зарегистрирована успешно!");
        } else {
            getLogger().warning("Команда /sit НЕ НАЙДЕНА в plugin.yml!");
        }
        
        UnsitCommand unsitCommand = new UnsitCommand(this);
        if (getCommand("unsit") != null) {
            getCommand("unsit").setExecutor(unsitCommand);
            getLogger().info("Команда /unsit зарегистрирована успешно!");
        } else {
            getLogger().warning("Команда /unsit НЕ НАЙДЕНА в plugin.yml!");
        }
        
        // Регистрация слушателей
        getServer().getPluginManager().registerEvents(new ThiefListener(this), this);
        getServer().getPluginManager().registerEvents(new GsitBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new SitListener(this), this);
    }
    
    @Override
    public void onDisable() {
        getLogger().info("CustomCommands plugin disabled!");
    }
    
    public ThiefManager getThiefManager() {
        return thiefManager;
    }
    
    public GsitManager getGsitManager() {
        return gsitManager;
    }
    
    public SitManager getSitManager() {
        return sitManager;
    }
}