package com.smpmanager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class SMPManager extends JavaPlugin {

    private File balancesFile;
    private FileConfiguration balancesConfig;

    @Override
    public void onEnable() {
        createBalancesFile();
        Bukkit.getScheduler().runTaskTimer(this, this::saveBalances, 6000L, 6000L); // every 5 mins
        getLogger().info("✅ SMPManager plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        saveBalances();
        getLogger().info("🛑 SMPManager disabled. Balances saved.");
    }

    private void createBalancesFile() {
        balancesFile = new File(getDataFolder(), "balances.yml");
        if (!balancesFile.exists()) {
            balancesFile.getParentFile().mkdirs();
            try {
                balancesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        balancesConfig = YamlConfiguration.loadConfiguration(balancesFile);
    }

    private int getBalance(UUID uuid) {
        return balancesConfig.getInt(uuid.toString(), 100);
    }

    private void setBalance(UUID uuid, int amount) {
        if (amount < 0) amount = 0;
        balancesConfig.set(uuid.toString(), amount);
        saveBalances();
    }

    private void addBalance(UUID uuid, int amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    private boolean removeBalance(UUID uuid, int amount) {
        int bal = getBalance(uuid);
        if (bal < amount) return false;
        setBalance(uuid, bal - amount);
        return true;
    }

    private void saveBalances() {
        try {
            balancesConfig.save(balancesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("coins") || cmd.getName().equalsIgnoreCase("bal") ||
            cmd.getName().equalsIgnoreCase("balance") || cmd.getName().equalsIgnoreCase("money")) {

            if (!(sender instanceof Player)) {
                sender.sendMessage("Players only.");
                return true;
            }

            Player p = (Player) sender;
            if (args.length == 0) {
                int bal = getBalance(p.getUniqueId());
                p.sendMessage(ChatColor.GREEN + "Your balance: " + ChatColor.GOLD + bal + " Coins");
            } else {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    p.sendMessage(ChatColor.RED + "Player not found.");
                    return true;
                }
                int bal = getBalance(target.getUniqueId());
                p.sendMessage(ChatColor.GREEN + target.getName() + "'s balance: " + ChatColor.GOLD + bal + " Coins");
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("pay")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Players only.");
                return true;
            }

            Player p = (Player) sender;
            if (args.length != 2) {
                p.sendMessage(ChatColor.RED + "Usage: /pay <player> <amount>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                p.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                p.sendMessage(ChatColor.RED + "Enter a valid number.");
                return true;
            }

            if (amount <= 0) {
                p.sendMessage(ChatColor.RED + "Amount must be positive.");
                return true;
            }

            if (!removeBalance(p.getUniqueId(), amount)) {
                p.sendMessage(ChatColor.RED + "Not enough Coins!");
                return true;
            }

            addBalance(target.getUniqueId(), amount);
            p.sendMessage(ChatColor.GREEN + "You paid " + ChatColor.GOLD + amount + " Coins " + ChatColor.GREEN + "to " + target.getName());
            target.sendMessage(ChatColor.GOLD + p.getName() + ChatColor.GREEN + " sent you " + ChatColor.GOLD + amount + " Coins!");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("setcoins")) {
            if (!sender.hasPermission("smp.setcoins")) {
                sender.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }

            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /setcoins <player> <amount>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Enter a valid number.");
                return true;
            }

            setBalance(target.getUniqueId(), amount);
            sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s balance to " + ChatColor.GOLD + amount + " Coins");
            target.sendMessage(ChatColor.YELLOW + "Your Coins were set to " + ChatColor.GOLD + amount + " by an admin.");
            return true;
        }

        return false;
    }
}