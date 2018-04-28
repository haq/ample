package me.ihaq.ample;

import me.ihaq.ample.data.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Arrays;

public class Ample {

    private JavaPlugin plugin;
    private CommandMap commandMap;

    public Ample(JavaPlugin plugin) {
        this.plugin = plugin;

        try {
            Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public void register(Object... objects) {
        Arrays.stream(objects).forEach(object -> Arrays.stream(object.getClass().getDeclaredMethods()).forEach(method -> {

            method.setAccessible(true);

            if (!method.isAnnotationPresent(Command.class))
                return;

            commandMap.register(plugin.getName(), new BukkitCommand("") {
                @Override
                public boolean execute(CommandSender commandSender, String s, String[] strings) {
                    return onCommand(commandSender, s, strings);
                }
            });


        }));
    }

    private boolean onCommand(CommandSender commandSender, String s, String[] strings) {
        return false;
    }
}
