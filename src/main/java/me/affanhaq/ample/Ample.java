package me.affanhaq.ample;

import me.affanhaq.ample.data.CommandData;
import me.affanhaq.ample.data.annotation.Command;
import me.affanhaq.ample.data.annotation.Permission;
import me.affanhaq.ample.data.annotation.PlayerOnly;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class Ample {

    private static final HashMap<String, CommandData> COMMANDS = new HashMap<>();

    private final JavaPlugin plugin;
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
        Arrays.stream(objects).forEach(object ->
                Arrays.stream(object.getClass().getDeclaredMethods())
                        .filter(method -> method.isAnnotationPresent(Command.class))
                        .filter(method -> method.getParameterCount() == 2)
                        .filter(method -> CommandSender.class.isAssignableFrom(method.getParameterTypes()[0]))
                        .filter(method -> String[].class.isAssignableFrom(method.getParameterTypes()[1]))
                        .forEach(method -> {

                            method.setAccessible(true);

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
                                    return Ample.onCommand(commandSender, label, args);
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

                            // register the command in spigots command map
                            commandMap.register(plugin.getName(), bukkitCommand);

                            // register the command in our command map along with its aliases
                            COMMANDS.put(commandData.getName().toLowerCase(), commandData);
                            Arrays.stream(commandData.getAlias())
                                    .forEach(alias -> COMMANDS.put(alias.toLowerCase(), commandData));

                            // registering the command in bukkit's HelpMap
                            HelpTopic helpTopic = new GenericCommandHelpTopic(bukkitCommand);
                            plugin.getServer().getHelpMap().addTopic(new IndexHelpTopic(
                                    plugin.getName(),
                                    null,
                                    null,
                                    Collections.singletonList(helpTopic)
                            ));
                        }));
    }

    private static boolean onCommand(CommandSender commandSender, String label, String[] args) {

        CommandData commandData = COMMANDS.get(label.toLowerCase());

        // no command was found for the given input
        if (commandData == null) {
            return false;
        }

        // command is player only but non-player is calling the command
        if (commandData.isPlayerOnly() && !(commandSender instanceof Player)) {
            commandSender.sendMessage("Only players can perform this command.");
            return true;
        }

        // command has a permission which the sender might not hold
        if (commandData.getPermission() != null && !commandSender.hasPermission(commandData.getPermission())) {
            commandSender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        try {
            commandData.getMethod().invoke(
                    commandData.getMethodParent(),
                    commandSender,
                    args
            );
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return false;
    }

}