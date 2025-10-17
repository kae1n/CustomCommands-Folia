package com.example.customcommands;

import com.example.customcommands.commands.CoinCommand;
import com.example.customcommands.commands.DiceCommand;
import com.example.customcommands.commands.DoCommand;
import com.example.customcommands.commands.MeCommand;
import com.example.customcommands.commands.TryCommand;
import com.example.customcommands.commands.WhisperCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomCommandsPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        getLogger().info("=== CUSTOM COMMANDS PLUGIN ENABLED ===");
        getLogger().info("Plugin successfully loaded!");

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
    }
    
    @Override
    public void onDisable() {
        getLogger().info("CustomCommands plugin disabled!");
    }
}