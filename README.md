[![license](https://img.shields.io/github/license/mashape/apistatus.svg) ](LICENSE)
[![](https://jitpack.io/v/haq/ample.svg)](https://jitpack.io/#haq/ample)

# ample

A basic annotation based command system for Spigot. You do no need to declare the commands in plugin.yml as it will
register the commands for you.

## Todo

* Subcommands

## Usage

```java
public class TestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        new Ample(this) // passing in java plugin, so it can get plugin name
                .register(this); // the objects that contain the command annotated methods
    }

    //every command method needs this annotation
    @Command(
            value = "Test",
            description = "Test command",
            usage = "test <int> <double> <string>",
            alias = {"tst", "t"}
    )
    @PlayerOnly // tag a method with this annotation if you want that command to be run by a player only
    @Permission("example.perm") // tag a method with this if the command has a permission requirement
    public void test(CommandSender commandSender, int arg1, double arg2, String arg3) {
        System.out.println("TEST COMMAND!");
    }
}
```

## Download

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

```xml
<dependency>
    <groupId>com.github.haq</groupId>
    <artifactId>ample</artifactId>
    <version>VERSION</version>
</dependency>
```