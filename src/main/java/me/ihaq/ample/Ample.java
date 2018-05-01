package me.ihaq.ample;

import me.ihaq.ample.data.CommandData;
import me.ihaq.ample.data.annotation.Command;
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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

            if (!isMethodValid(method))
                return;

            // getting Command annotation
            Command commandAnnotation = method.getAnnotation(Command.class);

            // making CommandData object out of the annotations
            CommandData commandData = new CommandData(commandAnnotation.value(), commandAnnotation.description(), commandAnnotation.usage(), commandAnnotation.alias(),
                    method.isAnnotationPresent(Permission.class) ? method.getAnnotation(Permission.class).value() : null,
                    method.isAnnotationPresent(PlayerOnly.class), method, object);

            // making BukkitCommand to register in CommandMap
            BukkitCommand bukkitCommand = makeBukkitCommand(commandData, method);

            if (commandMap.getCommand(bukkitCommand.getName()) == null) {
                commandMap.register(plugin.getName(), bukkitCommand);

                // registering the command in bukkit's HelpMap
                HelpTopic helpTopic = new GenericCommandHelpTopic(bukkitCommand);
                plugin.getServer().getHelpMap().addTopic(new IndexHelpTopic(plugin.getName(), null, null, Collections.singletonList(helpTopic)));
            }

            commandDataList.add(commandData);
        }));
    }

    private boolean onCommand(CommandSender commandSender, String label, String[] args) {

        for (CommandData commandData : getCommandData(label)) {

            if (commandData == null)
                return false;

            if (commandData.isPlayerOnly() && !(commandSender instanceof Player)) {
                commandSender.sendMessage("Only players can perform this command.");
                return true;
            }

            if (commandData.getPermission() != null && !commandSender.hasPermission(commandData.getPermission())) {
                commandSender.sendMessage("You do not have permission to use this command.");
                return true;
            }

            // handling subcommand validation
            if (commandData.getSubName() != null) {
                if (args.length <= 0)
                    return false;

                if (!commandData.getSubName().equalsIgnoreCase(args[0]) && Arrays.stream(commandData.getAlias()).noneMatch(s -> s.equalsIgnoreCase(args[0])))
                    return false;
            }

            try {
                commandData.getMethod().invoke(commandData.getMethodParent(), commandData, commandSender, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return false;
    }


    /**
     * @param method the method to validate
     * @return returns whether the method is valid or not
     */
    private boolean isMethodValid(Method method) {

        method.setAccessible(true);

        // checking if the method has the proper annotation
        if (!method.isAnnotationPresent(Command.class))
            return false;

        // checking if the command has only 3 parameter
        if (method.getParameterCount() != 3)
            return false;

        if (!CommandData.class.isAssignableFrom(method.getParameterTypes()[0]))
            return false;

        if (!CommandSender.class.isAssignableFrom(method.getParameterTypes()[1]))
            return false;

        if (!String[].class.isAssignableFrom(method.getParameterTypes()[2]))
            return false;

        return true;
    }
    
    private BukkitCommand makeBukkitCommand(CommandData commandData, Method method) {

        BukkitCommand bukkitCommand = new BukkitCommand(commandData.getName()) {
            @Override
            public boolean execute(CommandSender commandSender, String s, String[] strings) {
                return onCommand(commandSender, s, strings);
            }
        };

        if (!commandData.getDescription().isEmpty())
            bukkitCommand.setDescription(commandData.getDescription());

        if (!commandData.getUsage().isEmpty())
            bukkitCommand.setUsage("/" + commandData.getUsage());

        if (commandData.getAlias().length != 0)
            bukkitCommand.setAliases(Arrays.asList(commandData.getAlias()));

        if (method.isAnnotationPresent(Permission.class)) {
            bukkitCommand.setPermission(method.getAnnotation(Permission.class).value());
            bukkitCommand.setPermissionMessage("You do not have permission to use this command.");
        }
        return bukkitCommand;
    }

    /**
     * @param label the name of the command to search for
     * @return returns a list of CommandData objects that matched
     */
    private List<CommandData> getCommandData(String label) {
        return commandDataList.stream()
                .filter(commandData -> {
                    if (commandData.getSubName() == null) {
                        return commandData.getName().equalsIgnoreCase(label) || Arrays.stream(commandData.getAlias()).anyMatch(s -> s.equalsIgnoreCase(label));
                    } else {
                        return commandData.getName().equalsIgnoreCase(label);
                    }
                })
                .collect(Collectors.toList());
    }

}