package noslowdwn.enderchestlimiter.listeners;

import net.md_5.bungee.api.ChatColor;
import noslowdwn.enderchestlimiter.utils.ColorsParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static noslowdwn.enderchestlimiter.EnderChestLimiter.instance;
import static org.bukkit.Sound.ENTITY_SHULKER_HURT_CLOSED;

public class ItemLimiter implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Inventory clickedInventory = e.getClickedInventory();
        Inventory destInv = e.getView().getTopInventory();
        Player p = (Player) e.getWhoClicked();
        if (p.hasPermission("enderchestlimiter.bypass.all")) return;

        if (clickedInventory == null || destInv.getType() != InventoryType.ENDER_CHEST) return;

        FileConfiguration cfg = instance.getConfig();

        ItemStack item;
        if ((InventoryAction.HOTBAR_SWAP.equals(e.getAction())
                || InventoryAction.HOTBAR_MOVE_AND_READD.equals(e.getAction()))
                && e.getHotbarButton() >= 0 && e.getHotbarButton() <= 8)
            item = p.getInventory().getItem(e.getHotbarButton());
        else {
            if (clickedInventory.getType() == InventoryType.ENDER_CHEST) return;
            item = e.getCurrentItem();
        }

        if (item == null) return;

        for (String group : cfg.getConfigurationSection("groups").getKeys(false)) {
            if (p.hasPermission("enderchestlimiter.bypass." + group)) return;

            List<String> includedItems = cfg.getStringList("groups." + group + ".included-items");
            for (int i = 0; i < includedItems.size(); i++)
                includedItems.set(i, includedItems.get(i).toUpperCase());

            if (!includedItems.contains(item.getType().toString())) continue;

            int limit = cfg.getInt("groups." + group + ".limit");
            
            Map<Material, Integer> itemCountMap = new HashMap<>();

            int amount = 0;
            for (ItemStack i : destInv.getStorageContents()) {
                if (i != null && includedItems.contains(i.getType().name())) {
                    itemCountMap.put(i.getType(), itemCountMap.getOrDefault(i.getType(), 0) + i.getAmount());
                    amount += i.getAmount();
                }
            }

            if (amount + item.getAmount() > limit) {
                e.setCancelled(true);
                p.sendMessage(getDenyMessage(limit, group).replace("%max%", String.valueOf(limit)));
                if (cfg.getConfigurationSection("groups." + group).getKeys(false).contains("deny-sound"))
                    playDenySound(p, cfg.getString("groups." + group + ".deny-sound"), group);
                return;
            }
        }
    }

    private String getDenyMessage(int limit, String group) {
        String messageKey;
        if (limit % 100 >= 11 && limit % 100 <= 19) {
            messageKey = "deny-message-3";
        } else {
            switch (limit % 10) {
                case 1:
                    messageKey = "deny-message-1";
                    break;
                case 2:
                case 3:
                    messageKey = "deny-message-2";
                    break;
                default:
                    messageKey = "deny-message-3";
                    break;
            }
        }
        return ColorsParser.of(instance.getConfig().getString("groups." + group + "." + messageKey));
    }

    private void playDenySound(Player player, String soundConfig, String group) {
        if (soundConfig == null || soundConfig.isEmpty()) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "There are not enough arguments to play the sound");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Path to: groups." + group + ".deny-sound");
            return;
        }

        String[] params = soundConfig.split(";", 3);
        Sound sound = ENTITY_SHULKER_HURT_CLOSED;
        try {
            sound = Sound.valueOf(params[0].toUpperCase());
        } catch (IllegalArgumentException exception) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Sound name given for sound action: " + params[0] + ", is not a valid sound!");
        }

        float volume = 1, pitch = 1;
        if (params.length >= 2) {
            try {
                volume = Float.parseFloat(params[1]);
            } catch (NumberFormatException exception) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Volume given for sound action: " + params[1] + ", is not a valid number!");
            }
        }

        if (params.length == 3) {
            try {
                pitch = Float.parseFloat(params[2]);
            } catch (NumberFormatException exception) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Pitch given for sound action: " + params[2] + ", is not a valid number!");
            }
        }

        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}