package top.DrakGod.DgMCMod;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import top.DrakGod.DgMCMod.Utils.CommandArgs;

public interface Global {
    public Server Server = Bukkit.getServer();
    public PluginManager Plugin_Manager = Server.getPluginManager();
    public Logger Server_Logger = Server.getLogger();
    public Logger Plugin_Logger = Bukkit.getLogger();
    public CommandSender Console = Server.getConsoleSender();

    public String Plugin_Name = "§1Dg§4MC§bPlugin";

    public default <T extends JavaPlugin> T Get_Plugin(@Nonnull Class<T> clazz) {
        return Main.getPlugin(clazz);
    }

    public default Main Get_Main() {
        return Get_Plugin(Main.class);
    }

    public default boolean Get_Running() {
        return Get_Main().Get_Running();
    }

    public static boolean Get_Running_Static() {
        return Main.getPlugin(Main.class).Get_Running();
    }

    public default File Get_Data_Folder() {
        return Get_Main().getDataFolder();
    }

    public default YamlConfiguration Get_Config() {
        YamlConfiguration Config = Get_Data("config.yml");
        Save_Data(Config, "config.yml");
        return Config;
    }

    public default void Server_Log(String Mode, String Msg) {
        if (Mode.equalsIgnoreCase("INFO")) {
            Msg = "§f" + Msg;
        } else if (Mode.equalsIgnoreCase("WARN")) {
            Msg = "§e" + Msg;
        } else if (Mode.equalsIgnoreCase("ERROR")) {
            Msg = "§c" + Msg;
        }
        Console.sendMessage(Msg);
    }

    public default void Plugin_Log(String Mode, String Msg) {
        if (Mode.equalsIgnoreCase("INFO")) {
            Msg = "§f" + Msg;
        } else if (Mode.equalsIgnoreCase("WARN")) {
            Msg = "§e" + Msg;
        } else if (Mode.equalsIgnoreCase("ERROR")) {
            Msg = "§c" + Msg;
        }
        Console.sendMessage("§6[" + Plugin_Name + "§6] " + Msg);
    }

    public default void Module_Log(String Mode, String Module_Name, String Msg) {
        if (Mode.equalsIgnoreCase("INFO")) {
            Msg = "§f" + Msg;
        } else if (Mode.equalsIgnoreCase("WARN")) {
            Msg = "§e" + Msg;
        } else if (Mode.equalsIgnoreCase("ERROR")) {
            Msg = "§c" + Msg;
        }
        Console.sendMessage("§6[" + Plugin_Name + "§6] §6[§f" + Module_Name + "§6] " + Msg);
    }

    public static void Module_Log_Static(String Mode, String Module_Name, String Msg) {
        if (Mode.equalsIgnoreCase("INFO")) {
            Msg = "§f" + Msg;
        } else if (Mode.equalsIgnoreCase("WARN")) {
            Msg = "§e" + Msg;
        } else if (Mode.equalsIgnoreCase("ERROR")) {
            Msg = "§c" + Msg;
        }
        Console.sendMessage("§6[" + Plugin_Name + "§6] §6[§f" + Module_Name + "§6] " + Msg);
    }

    public default YamlConfiguration Get_Data(String File_Name) {
        try {
            YamlConfiguration Config = YamlConfiguration.loadConfiguration(new File(Get_Data_Folder(), File_Name));
            if (Config.getKeys(false).isEmpty()) {
                throw new Exception("数据文件: " + File_Name + " 错误");
            } else {
                return Config;
            }
        } catch (Exception e) {
            Plugin_Log("ERROR", "无法加载数据文件: " + File_Name + " " + e.toString());
            try {
                Files.copy(new File(Get_Data_Folder(), File_Name).toPath(),
                        new File(Get_Data_Folder(), File_Name + ".bak").toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                Plugin_Log("WARN", "已使用初始数据文件，源文件已备份至: " + File_Name + ".bak，请检查后重新加载");
            } catch (IOException ex) {
                Plugin_Log("ERROR", "无法备份数据文件: " + File_Name + " " + ex.toString());
            }

            return YamlConfiguration.loadConfiguration(
                    new InputStreamReader(
                            Get_Main().getResource("Data/" + File_Name),
                            StandardCharsets.UTF_8));
        }
    }

    public default void Save_Data(YamlConfiguration Config, String File_Name) {
        try {
            Config.save(new File(Get_Data_Folder(), File_Name));
        } catch (IOException e) {
            Plugin_Log("ERROR", "无法保存数据文件: " + File_Name + " " + e.toString());
        }
    }

    public default <T extends Event> void RegisterEvent(Consumer<T> Consumer, Class<T> Event_Class) {
        EventExecutor EventMethod = new EventExecutor() {
            public void execute(Listener Listener, Event Event) {
                if (Event_Class.isInstance(Event)) {
                    T Typed_Event = Event_Class.cast(Event);
                    Consumer.accept(Typed_Event);
                }

            }
        };
        Plugin_Manager.registerEvent(Event_Class, Get_Main().Class_Listeners, EventPriority.NORMAL, EventMethod,
                Get_Main());
    }

    public default void RegisterCommand(String Command_Name, Consumer<CommandArgs> Consumer) {
        Get_Main().getCommand(Command_Name).setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender Sender, Command Command, String Label, String[] Args) {
                Consumer.accept(new CommandArgs(Sender, Command, Label, Args));
                return true;
            }
        });
    }
}
