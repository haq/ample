package me.affanhaq.ample;

import com.google.common.collect.Lists;
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
import java.util.List;

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
                        .filter(method -> method.getParameterCount() >= 1)
                        .filter(method -> CommandSender.class.isAssignableFrom(method.getParameterTypes()[0]))
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
                                    Arrays.copyOfRange(method.getParameterTypes(), 1, method.getParameterTypes().length),
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

    private static boolean onCommand(CommandSender sender, String label, String[] args) {

        CommandData commandData = COMMANDS.get(label.toLowerCase());

        // no command was found for the given input
        if (commandData == null) {
            return false;
        }

        // command is player only but non-player is calling the command
        if (commandData.isPlayerOnly() && !(sender instanceof Player)) {
            sender.sendMessage("Only players can perform this command.");
            return true;
        }

        // command has a permission which the sender might not hold
        if (commandData.getPermission() != null && !sender.hasPermission(commandData.getPermission())) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        // checking if the user typed in all args
        if (args.length != commandData.getArgs().length) {
            sender.sendMessage(commandData.getUsage());
            return true;
        }

        // TODO: check if all arguments match the ones asked in the command
        List<Object> parsedArgs = Lists.newArrayList(sender);
        for (int i = 0; i < args.length; i++) {
            Class<?> cArgs = commandData.getArgs()[i];
            if (Integer.TYPE.isAssignableFrom(cArgs)) {
                try {
                    parsedArgs.add(
                            Integer.parseInt(args[i])
                    );
                } catch (NumberFormatException e) {
                    sender.sendMessage(commandData.getUsage());
                    return false;
                }
            } else if (Double.TYPE.isAssignableFrom(cArgs)) {
                try {
                    parsedArgs.add(
                            Double.parseDouble(args[i])
                    );
                } catch (NumberFormatException e) {
                    sender.sendMessage(commandData.getUsage());
                    return false;
                }
            } else if (String.class.isAssignableFrom(cArgs)) {
                parsedArgs.add(args[i]);
            } else {
                sender.sendMessage(commandData.getUsage());
                return false;
            }
        }

        try {
            commandData.getMethod().invoke(
                    commandData.getMethodParent(),
                    parsedArgs.toArray()
            );
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return false;
    }

}