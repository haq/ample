[ ![Download](https://api.bintray.com/packages/ihaq/maven/ample/images/download.svg) ](https://bintray.com/ihaq/maven/ample/_latestVersion)
[![license](https://img.shields.io/github/license/mashape/apistatus.svg) ](LICENSE)

# Ample
A basic annotation based command system for Bukkit/Spigot. You do need to declare the commands in plugin.yml as it will register the commands using reflection.

## Todo
* Subcommands

## Declaration

```java
public class TestPlugin extends JavaPlugin {

    @Command(value = "Test", description = "Test command", usage = "Example usage.", alias = {"tst","t"}) //every command method needs this annotation
    @PlayerOnly // tag a method with this annotation if you want that command to be run by a player only
    @Permission("example.perm") // tag a method with this if the command has a permission requirement
    public void test(CommandData commandData, CommandSender commandSender, String[] args) {
        // handle command
    }

}
```

## Usage
```java
public class TestPlugin extends JavaPlugin {

    @Command(value = "Test", description = "Test command", usage = "Example usage.", alias = {"tst","t"}) //every command method needs this annotation
    @PlayerOnly // tag a method with this annotation if you want that command to be run by a player only
    @Permission("example.perm") // tag a method with this if the command has a permission requirement
    public void test(CommandData commandData, CommandSender commandSender, String[] args) {
        System.out.println("TEST COMMAND!");
    }

    @Override
    public void onEnable() {
        new Ample(this) // passing in java plugin, so it can get plugin name
            .register(this); // the objects that contain the command annotated methods
    }

}
```


## Download
[ ![Download](https://api.bintray.com/packages/ihaq/maven/ample/images/download.svg) ](https://bintray.com/ihaq/maven/ample/_latestVersion)

Replace VERSION with the verion above.

#### Maven
```xml
<repository>
    <id>jcenter</id>
    <name>jcenter-bintray</name>
    <url>http://jcenter.bintray.com</url>
</repository>

<dependency>
    <groupId>me.ihaq.ample</groupId>
    <artifactId>ample</artifactId>
    <version>VERSION</version>
</dependency>
```

#### Gradle
```gradle
dependencies {
    compile 'me.ihaq.ample:ample:VERSION'
}

repositories {
    jcenter()
}
```