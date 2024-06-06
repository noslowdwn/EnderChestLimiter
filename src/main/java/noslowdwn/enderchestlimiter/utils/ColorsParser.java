package noslowdwn.enderchestlimiter.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorsParser {

    public static String of(String message) {
        if (message == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[EnderChestLimiter] Error message parsing!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[EnderChestLimiter] Please check your messages.yml to find error!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[EnderChestLimiter] You can also check syntax on https://yamlchecker.com/!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[EnderChestLimiter] Or just delete 'messages.yml' and reload plugin!");
            message = "&c[EnderChestLimiter] Error message parsing! Please contact administrator or check console!";
        }

        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        int subVersion = Integer.parseInt(version.replace("1_", "").replaceAll("_R\\d", "").replace("v", ""));
        if (subVersion >= 16) {
            Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
            Matcher matcher = pattern.matcher(message);

            while (matcher.find()) {
                String color = message.substring(matcher.start(), matcher.end());
                StringBuilder replacement = new StringBuilder("x");
                for (char c : color.substring(1).toCharArray())
                    replacement.append('\u00A7').append(c);
                message = message.replace(color, replacement.toString());
                matcher = pattern.matcher(message);
            }
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
