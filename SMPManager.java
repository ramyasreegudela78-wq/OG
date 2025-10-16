package com.smpmanager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class SMPManager extends JavaPlugin {

    private HashMap<UUID, Integer> balances = new HashMap<>();
    private int startingCoins = 100;

    @Override
    public void onEnable() {
        getLogger().info("SMP Manager plugin enabled!");
        loadBalances();
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        saveBalances();
        getLogger().info("SMP Manager plugin disabled!");
    }

    private void loadBalances() {
        File dataFolder = new File(getDataFolder(), "players");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        for (Player p : Bukkit.getOnlinePlayers()) {
            File file = new File(dataFolder, p.getUniqueId() + ".yml");
            if (!file.exists()) {
                balances.put(p.getUniqueId(), startingCoins);
            } else {
                // Simplified: no external YAML lib, so this part would read a value if you add it manually
                balances.put(p.getUniqueId(), startingCoins);
            }
        }
    }

    private void saveBalances() {
        // Simplified save (could be replaced with real YAML handling)
        getLogger().info("Balances saved (stub).");
    }

    private int getCoins(Player p) {
        return balances.getOrDefault(p.getUniqueId(), startingCoins);
    }

    private void setCoins(Player p, int amount) {
        balances.put(p.getUniqueId(), Math.max(amount, 0));
    }

    private void addCoins(Player p, int amount) {
        setCoins(p, getCoins(p) + amount);
    }

    private boolean removeCoins(Player p, int amount) {
        int current = getCoins(p);
        if (current < amount) return false;
        setCoins(p, current - amount);
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only!");
            return true;
        }

        Player player = (Player) sender;

        switch (cmd.getName().toLowerCase()) {
            case "coins":
            case "bal":
            case "balance":
                player.sendMessage(ChatColor.GREEN + "Your balance: " + ChatColor.GOLD + getCoins(player) + " Coins");
                break;

            case "pay":
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /pay <player> <amount>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                try {
                    int amt = Integer.parseInt(args[1]);
                    if (removeCoins(player, amt)) {
                        addCoins(target, amt);
                        player.sendMessage(ChatColor.GREEN + "Paid " + amt + " Coins to " + target.getName());
                        target.sendMessage(ChatColor.GREEN + "You received " + amt + " Coins from " + player.getName());
                    } else {
                        player.sendMessage(ChatColor.RED + "Not enough Coins!");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid number.");
                }
                break;

            case "setcoins":
                if (!player.hasPermission("smp.setcoins")) {
                    player.sendMessage(ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length != 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /setcoins <player> <amount>");
                    return true;
                }
                Player t = Bukkit.getPlayer(args[0]);
                if (t == null) {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                try {
                    int amt = Integer.parseInt(args[1]);
                    setCoins(t, amt);
                    player.sendMessage(ChatColor.GREEN + "Set " + t.getName() + "'s balance to " + amt + " Coins");
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid number.");
                }
                break;
        }

        return true;
    }
}
