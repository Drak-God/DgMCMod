package top.DrakGod.DgMCMod.Functions;

import java.util.List;
import java.util.Map;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import top.DrakGod.DgMCMod.Global;

public class Music implements Global {
    private ConfigurationSection Music_Config;

    public Music() {
        Music_Config = Get_Data("music/music.yml").getConfigurationSection("music");
    }

    private void Play_Music(Player Player, String Music_Name) {
        Player.playSound()
    }
}
