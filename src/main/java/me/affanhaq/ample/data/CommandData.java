package me.affanhaq.ample.data;

import java.lang.reflect.Method;

public class CommandData {

    private final String name;
    private final String description;
    private final String usage;
    private final String[] alias;
    private final Class<?>[] args;
    private final String permission;
    private final boolean playerOnly;
    private final Method method;
    private final Object methodParent;

    public CommandData(
            String name,
            String description,
            String usage,
            String[] alias,
            Class<?>[] args,
            String permission,
            boolean playerOnly,
            Method method,
            Object methodParent
    ) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.alias = alias;
        this.args = args;
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.method = method;
        this.methodParent = methodParent;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public String[] getAlias() {
        return alias;
    }

    public Class<?>[] getArgs() {
        return args;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isPlayerOnly() {
        return playerOnly;
    }

    public Method getMethod() {
        return method;
    }

    public Object getMethodParent() {
        return methodParent;
    }
}