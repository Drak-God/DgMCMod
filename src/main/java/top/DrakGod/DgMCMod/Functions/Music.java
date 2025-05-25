package top.DrakGod.DgMCMod.Functions;

import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.Component;
import top.DrakGod.DgMCMod.Global;
import top.DrakGod.DgMCMod.Utils.CommandArgs;

public class Music implements Global {
    private final Integer Page_Lenth = 5;

    private ConfigurationSection Music_Config;

    private Map<Player, Set<String>> Player_Resource_Packs;
    private String Music_Resourcepack;
    private String Resource_Pack_Hash;
    private UUID Resource_Pack_UUID;

    private List<Inventory> GUI_Pages;

    public Music() {
        ConfigurationSection Config = Get_Config().getConfigurationSection("music");
        Music_Config = Get_Data("music/music.yml").getConfigurationSection("music");
        Music_Resourcepack = Config.getString("resourcepack");
        Player_Resource_Packs = new HashMap<>();
        Resource_Pack_Hash = Get_Resource_Pack_Hash();

        String UUID_String = Config.getString("uuid");
        if (UUID_String == null || UUID_String.isEmpty()) {
            Resource_Pack_UUID = UUID.randomUUID();
            Config.set("uuid", Resource_Pack_UUID.toString());

            YamlConfiguration Config_File = Get_Config();
            Config_File.set("music", Config);
            Save_Data(Config_File, "config.yml");
        } else {
            Resource_Pack_UUID = UUID.fromString(UUID_String);
        }

        RegisterCommand("music", this::Open_GUI);
        RegisterEvent(this::onEntityDeath, EntityDeathEvent.class);
        RegisterEvent(this::onPlayerResourcePackStatus, PlayerResourcePackStatusEvent.class);
        RegisterEvent(this::onPlayerQuit, PlayerQuitEvent.class);
        Load_GUI();
    }

    private String Get_Resource_Pack_Hash() {
        try {
            URL Url = new URL(Music_Resourcepack);
            try (InputStream Input_Stream = Url.openStream()) {
                MessageDigest Digest = MessageDigest.getInstance("SHA-256");
                byte[] Buffer = new byte[8192];
                int Bytes_Read;
                while ((Bytes_Read = Input_Stream.read(Buffer)) != -1) {
                    Digest.update(Buffer, 0, Bytes_Read);
                }
                byte[] Hash_Bytes = Digest.digest();
                return Bytes_To_Hex(Hash_Bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Integer.toHexString(Music_Resourcepack.hashCode());
        }
    }

    private String Bytes_To_Hex(byte[] Bytes) {
        StringBuilder Hex_String = new StringBuilder(2 * Bytes.length);
        for (byte Byte : Bytes) {
            String Hex = Integer.toHexString(0xff & Byte);
            if (Hex.length() == 1) {
                Hex_String.append('0');
            }
            Hex_String.append(Hex);
        }
        return Hex_String.toString();
    }

    private void Load_GUI() {
        Integer Music_Lenth = Music_Config.getKeys(false).size();
        Integer Page_Number = (int) Math.ceil(Music_Lenth / (Page_Lenth * 9F));

        GUI_Pages = new ArrayList<>();

        Iterator<String> Music_Iterator = Music_Config.getValues(false).keySet().iterator();
        for (int c = 0; c < Page_Number; c++) {
            final int i = c;
            int Page_All_Number = (Page_Lenth + 1) * 9;

            Inventory Page = Bukkit.createInventory(null, Page_All_Number, "§1音乐列表§4-§b第" + (i + 1) + "页");
            int Page_Item_Number = Math.min(Music_Lenth - i * Page_Lenth * 9, Page_Lenth * 9);

            for (int j = 0; j <= Page_Item_Number; j++) {
                String Music_Name = Music_Iterator.next();
                ConfigurationSection Music_Section = Music_Config.getConfigurationSection(Music_Name);

                String Music_Display_Name = Music_Section.getString("name");
                String Music_Color = Music_Section.getString("color");
                List<String> Music_Lore = Music_Section.getStringList("lore");

                ItemStack Music_Item = CustomStack.getInstance("dgmcmod:" + Music_Color + "_disc").getItemStack();
                Music_Item.editMeta(Meta -> {
                    Meta.displayName(Component.text(Music_Display_Name));
                    Meta.lore(Music_Lore.stream()
                            .map(Lore -> Component.text(Lore))
                            .toList());
                });

                Page.setItem(j, Music_Item);
            }

            ItemStack Pane_Item = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
            for (int j = 0; j < 3; j++) {
                Page.setItem(Page_Lenth * 9 + j, Pane_Item);
                if (j == 0) {
                    continue;
                }
                Page.setItem(Page_All_Number - j - 1, Pane_Item);
            }

            ItemStack Last_Page_Item = new ItemStack(
                    (i == 0) ? Material.BLACK_STAINED_GLASS_PANE : Material.LIME_STAINED_GLASS_PANE);
            Last_Page_Item.editMeta(Meta -> {
                Meta.displayName(Component.text("§6上一页"));
                Meta.lore(List.of(Component.text("§e点击返回上一页")));
            });
            Page.setItem(Page_Lenth * 9 + 3, Last_Page_Item);

            ItemStack Info_Page_Item = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
            Info_Page_Item.editMeta(Meta -> {
                Meta.displayName(Component.text("§6" + (i + 1) + "§e/§6" + Page_Number));
            });
            Page.setItem(Page_Lenth * 9 + 4, Info_Page_Item);

            ItemStack Next_Page_Item = new ItemStack(
                    (i == Page_Number - 1) ? Material.BLACK_STAINED_GLASS_PANE : Material.LIME_STAINED_GLASS_PANE);
            Next_Page_Item.editMeta(Meta -> {
                Meta.displayName(Component.text("§6下一页"));
                Meta.lore(List.of(Component.text("§e点击返回下一页")));
            });
            Page.setItem(Page_Lenth * 9 + 5, Next_Page_Item);

            ItemStack Stop_Item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            Stop_Item.editMeta(Meta -> {
                Meta.displayName(Component.text("§6停止音乐"));
                Meta.lore(List.of(Component.text("§e点击停止音乐")));
            });
            Page.setItem(Page_Lenth * 9 - 1, Stop_Item);

            GUI_Pages.add(Page);
        }
    }

    public void Open_GUI(CommandArgs Args) {
        if (!(Args.Sender instanceof Player)) {
            Args.Sender.sendMessage("§c该命令只能由玩家执行");
            return;
        }
        Player Player = (Player) Args.Sender;

        if (!Player_Resource_Packs.containsKey(Player)
                || !Player_Resource_Packs.get(Player).contains(Resource_Pack_Hash)) {
            Player.addResourcePack(Resource_Pack_UUID, Music_Resourcepack, Resource_Pack_Hash.getBytes(), "DgMCMod音乐包",
                    true);

            Location Location = Player.getLocation();
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (Player.isOnline()) {
                        Player.teleport(Location);
                        Player.setGameMode(GameMode.SPECTATOR);
                        Player.sendTitle("§6正在下载资源包", "§e可能会很慢,请耐心等候", 10, 70, 20);
                        if (Player_Resource_Packs.containsKey(Player)
                                && Player_Resource_Packs.get(Player).contains(Resource_Pack_Hash)) {
                            Open_GUI(Args);
                            cancel();
                        }
                    } else {
                        cancel();
                    }
                }
            };
            return;
        }

        String[] Input_Args = Args.Args;
        Integer Page_Number = 0;

        if (Input_Args.length > 0) {
            try {
                Page_Number = Integer.valueOf(Input_Args[0]) - 1;
            } catch (NumberFormatException e) {
                Player.sendMessage("§c输入的页数不是有效的数字,回退至第一页");
            }
        }

        if (Page_Number < 0 || Page_Number > GUI_Pages.size()) {
            Player.sendMessage("§c页数错误,回退至第一页");
            Page_Number = 0;
        }
        Player.openInventory(GUI_Pages.get(Page_Number));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent Event) {
        LivingEntity Entity = Event.getEntity();
        Entity.getWorld().playSound(Entity.getLocation(), "dgmcmod:effects.pipe_fall", 10, 1);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerResourcePackStatus(PlayerResourcePackStatusEvent Event) {
        Player Player = Event.getPlayer();
        String Pack_Hash = Event.getHash();

        if (Event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            Player_Resource_Packs.computeIfAbsent(Player, K -> new HashSet<>()).add(Pack_Hash);
        } else if (Event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED ||
                Event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            Player_Resource_Packs.computeIfPresent(Player, (K, V) -> {
                V.remove(Pack_Hash);
                return V;
            });
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent Event) {
        Player Player = Event.getPlayer();
        Player_Resource_Packs.remove(Player);
    }
}