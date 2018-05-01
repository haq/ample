package me.ihaq.ample.data;

import java.lang.reflect.Method;

public class CommandData {

    private String name;
    private String subName;
    private String description;
    private String usage;
    private String[] alias;
    private String permission;
    private boolean playerOnly;
    private Method method;
    private Object methodParent;

    public CommandData(String name, String description, String usage, String[] alias, String permission, boolean playerOnly, Method method, Object methodParent) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.alias = alias;
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.method = method;
        this.methodParent = methodParent;

        if (this.name.contains(".")) {
            String[] splitString = this.name.split("\\.");
            this.name = splitString[0];
            this.subName = splitString[1];
        }
    }

    public String getName() {
        return name;
    }

    public String getSubName() {
        return subName;
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