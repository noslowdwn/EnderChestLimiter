package noslowdwn.enderchestlimiter;

import noslowdwn.enderchestlimiter.listeners.ItemLimiter;
import noslowdwn.enderchestlimiter.utils.ColorsParser;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class EnderChestLimiter extends JavaPlugin {

    public static EnderChestLimiter instance;

    @Override
    public void onEnable() {
        instance = this;
        load();
        this.getCommand("eclimiter").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player && !sender.hasPermission("enderchestlimiter.reload"))
                sender.sendMessage(ColorsParser.of(instance.getConfig().getString("messages.no-permission")));
            else {
                reloadConfig();
                sender.sendMessage(ColorsParser.of(instance.getConfig().getString("messages.reload-message")));
            }
            return true;
        });
        this.getServer().getPluginManager().registerEvents(new ItemLimiter(), this);
    }

    private void load() {
        File file = new File(this.getDataFolder(), "config.yml");
        if (!file.exists())
            this.saveResource("config.yml", false);

        YamlConfiguration config = new YamlConfiguration();

        try {
            config.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
