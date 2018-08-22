package me.ihaq.ample;

import me.ihaq.ample.data.annotation.Command;
import me.ihaq.ample.data.CommandData;
import me.ihaq.ample.data.annotation.Permission;
import me.ihaq.ample.data.annotation.PlayerOnly;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Ample {

    private JavaPlugin plugin;
    private CommandMap commandMap;
    private List<CommandData> commandDataList;

    public Ample(JavaPlugin plugin) {
        this.plugin = plugin;
        commandDataList = new ArrayList<>();

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

            // checking if the method has the proper annotation
            if (!method.isAnnotationPresent(Command.class)) {
                return;
            }

            // checking if the command has only 2 parameter
            if (method.getParameterCount() != 2) {
                return;
            }

            if (!CommandSender.class.isAssignableFrom(method.getParameterTypes()[0])) {
                return;
            }

            if (!String[].class.isAssignableFrom(method.getParameterTypes()[1])) {
                return;
            }

            // getting Command annotation
            Command commandAnnotation = method.getAnnotation(Command.class);

            // making CommandData object out of the annotations
            CommandData commandData = new CommandData(
                    commandAnnotation.value(),
                    commandAnnotation.description(),
                    commandAnnotation.usage(),
                    commandAnnotation.alias(),
                    method.isAnnotationPresent(Permission.class) ? method.getAnnotation(Permission.class).value() : null,
                    method.isAnnotationPresent(PlayerOnly.class),
                    method,
                    object
            );

            // making BukkitCommand to register in CommandMap
            BukkitCommand bukkitCommand = new BukkitCommand(commandData.getName()) {
                @Override
                public boolean execute(CommandSender commandSender, String label, String[] args) {
                    return onCommand(commandSender, label, args);
                }
            };

            if (!commandData.getDescription().isEmpty()) {
                bukkitCommand.setDescription(commandData.getDescription());
            }

            if (!commandData.getUsage().isEmpty()) {
                bukkitCommand.setUsage("/" + commandData.getUsage());
            }

            if (commandData.getAlias().length != 0) {
                bukkitCommand.setAliases(Arrays.asList(commandData.getAlias()));
            }

            if (method.isAnnotationPresent(Permission.class)) {
                bukkitCommand.setPermission(method.getAnnotation(Permission.class).value());
                bukkitCommand.setPermissionMessage("You do not have permission to use this command.");
            }

            commandMap.register(plugin.getName(), bukkitCommand);
            commandDataList.add(commandData);

            // registering the command in bukkit's HelpMap
            HelpTopic helpTopic = new GenericCommandHelpTopic(bukkitCommand);
            plugin.getServer().getHelpMap().addTopic(new IndexHelpTopic(plugin.getName(), null, null, Collections.singletonList(helpTopic)));
        }));
    }

    private boolean onCommand(CommandSender commandSender, String label, String[] args) {

        CommandData commandData = getCommandData(label);

        if (commandData == null) {
            return false;
        }

        if (commandData.isPlayerOnly() && !(commandSender instanceof Player)) {
            commandSender.sendMessage("Only players can perform this command.");
            return true;
        }

        if (commandData.getPermission() != null && !commandSender.hasPermission(commandData.getPermission())) {
            commandSender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        try {
            commandData.getMethod().invoke(commandData.getMethodParent(), commandSender, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return false;
    }

    private CommandData getCommandData(String label) {
        return commandDataList.stream()
                .filter(commandData -> commandData.getName().equalsIgnoreCase(label) || Arrays.stream(commandData.getAlias()).anyMatch(s -> s.equalsIgnoreCase(label)))
                .findFirst().orElse(null);
    }

}