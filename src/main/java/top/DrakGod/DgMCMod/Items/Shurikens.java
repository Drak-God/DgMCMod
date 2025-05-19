package top.DrakGod.DgMCMod.Items;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import dev.lone.itemsadder.api.CustomStack;
import top.DrakGod.DgMCMod.Global;

public class Shurikens implements Global {
    private static final double DAMAGE_IRON = 5.0;
    private static final double DAMAGE_GOLD = 8.0;
    private static final double DAMAGE_DIAMOND = 10.0;

    public Shurikens() {
        RegisterEvent(this::onPlayerInteract, PlayerInteractEvent.class);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent Event) {
        if (Event.getAction().isRightClick() && Event.hasItem()) {
            ItemStack Item = Event.getItem();
            if (Item != null && isShuriken(Item)) {
                Event.setCancelled(true);
                if (Item.getAmount() >= 1) {
                    Launch_Shuriken(Event.getPlayer(), Item);
                    Item.setAmount(Item.getAmount() - 1);
                }
            }
        }
    }

    private boolean isShuriken(ItemStack Item) {
        CustomStack Custom_Item = CustomStack.byItemStack(Item);
        if (Custom_Item == null) {
            return false;
        }
        return Custom_Item.getId().equals("iron_shuriken") ||
                Custom_Item.getId().equals("gold_shuriken") ||
                Custom_Item.getId().equals("diamond_shuriken");
    }

    private double getShurikenDamage(ItemStack Item) {
        CustomStack Custom_Item = CustomStack.byItemStack(Item);
        if (Custom_Item != null) {
            return switch (Custom_Item.getId()) {
                case "iron_shuriken" -> DAMAGE_IRON;
                case "gold_shuriken" -> DAMAGE_GOLD;
                case "diamond_shuriken" -> DAMAGE_DIAMOND;
                default -> 0;
            };
        }
        return 0;
    }

    private void Launch_Shuriken(Player Player, ItemStack Item) {
        CustomStack Shuriken = CustomStack.byItemStack(Item);
        if (Shuriken == null) {
            return;
        }

        Vector Direction = Player.getLocation().getDirection().normalize();
        Arrow IArrow = Player.launchProjectile(Arrow.class);
        IArrow.setDamage(getShurikenDamage(Item));
        IArrow.setVisibleByDefault(false);
        IArrow.setVisualFire(false);
        IArrow.setVelocity(Direction.multiply(1.5));
        IArrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);

        ArmorStand Armor_Stand = Player.getWorld().spawn(Player.getEyeLocation(), ArmorStand.class);
        Armor_Stand.setVisible(false);
        Armor_Stand.setGravity(false);
        Armor_Stand.setSmall(true);
        Armor_Stand.setArms(false);
        Armor_Stand.setBasePlate(false);
        Armor_Stand.getEquipment().setHelmet(Shuriken.getItemStack());

        new BukkitRunnable() {
            private World Player_World = Player.getWorld();
            private double Rotation = 0;

            @Override
            public void run() {
                if (IArrow.isOnGround() || IArrow.isDead()) {
                    IArrow.remove();
                    Armor_Stand.remove();

                    if (IArrow.isOnGround()) {
                        ItemStack NShuriken = Shuriken.getItemStack().clone();
                        NShuriken.setAmount(1);
                        Player_World.dropItem(IArrow.getLocation(), NShuriken);
                    }

                    this.cancel();
                    return;
                }

                Location Arrow_Location = IArrow.getLocation();
                Vector Arrow_Velocity = IArrow.getVelocity();

                double Yaw = Math.toDegrees(Math.atan2(Arrow_Velocity.getX(), Arrow_Velocity.getZ())) - 90;

                Armor_Stand.teleport(Arrow_Location.setDirection(Arrow_Velocity));
                Armor_Stand.setHeadPose(new EulerAngle(
                        Math.toRadians(0),
                        Math.toRadians(Yaw),
                        Rotation));

                Rotation += 0.1;
                if (Rotation >= 2 * Math.PI) {
                    Rotation -= 2 * Math.PI;
                }
            }
        }.runTaskTimer(Get_Main(), 0L, 1L);
    }
}
