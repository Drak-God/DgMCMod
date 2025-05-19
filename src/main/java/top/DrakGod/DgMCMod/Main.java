package top.DrakGod.DgMCMod;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import top.DrakGod.DgMCMod.Items.ItemRegister;

public class Main extends JavaPlugin implements Global {
    public Listeners Class_Listeners;
    public ItemRegister Item_Register;

    public String Version;

    @Override
    public void onEnable() {
        Version = getPluginMeta().getVersion();
        Server_Log("INFO", "");
        Server_Log("INFO", "§1┏━━  ┏━━┓ §4┏┓┏┓ ┏━━┓   §fDgMCMod");
        Server_Log("INFO", "§1┃  ┃ ┃ ━┓ §4┃┗┛┃ ┃      §fv" + Version);
        Server_Log("INFO", "§1┗━━  ┗━━┛ §4┗  ┛ ┗━━┛   §fBy DrakGod");
        Server_Log("INFO", "");

        Class_Listeners = new Listeners();
        Item_Register = new ItemRegister();

        Plugin_Log("INFO", "§1Dg§4MC§bMod§a已启用!");
    }

    @Override
    public void onDisable() {
        Plugin_Log("INFO", "§1Dg§4MC§bMod§4已禁用!");
    }
}