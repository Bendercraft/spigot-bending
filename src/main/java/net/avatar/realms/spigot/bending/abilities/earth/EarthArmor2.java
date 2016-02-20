package net.avatar.realms.spigot.bending.abilities.earth;

import net.avatar.realms.spigot.bending.abilities.*;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Class that will replace the current eartharmor.
 * Duplicated until the refactor is done
 * State preparing = Blocks are moving to the player
 * State progressing = Earth Armor is placed
 *
 * State prepared = Earth Shield
 */
@ABendingAbility(name = EarthArmor2.NAME, element = BendingElement.EARTH)
public class EarthArmor2 extends BendingActiveAbility{
	public final static String NAME = "EarthArmor2";

    private static final String EARTH_LORE = "Earth Armor";
    private static final String IRON_LORE = "Iron Armor";

    private static final Color GRASS_COLOR = Color.fromRGB(0, 153, 0);
    private static final Color STONE_COLOR = Color.fromRGB(128, 128, 128);
    private static final Color SAND_COLOR = Color.fromRGB(255, 178, 102);
    private static final Color GRAVEL_COLOR = Color.fromRGB(204, 204, 255);
    private static final Color COAL_COLOR = Color.BLACK;
    private static final Color LAPIS_COLOR = Color.fromRGB(51, 153, 255);
    private static final Color REDSTONE_COLOR = Color.fromRGB(255, 51, 51);
    private static final Color DEFAULT_COLOR = Color.fromRGB(102, 51, 0);

    @ConfigurationParameter("Duration")
    private static long DURATION = 60000;

    @ConfigurationParameter("Strength")
    private static int STRENGTH = 2;

    @ConfigurationParameter("Cooldown")
    private static long COOLDOWN = 60000;

    @ConfigurationParameter("Range")
    private static int RANGE = 7;

    private BlockState headBlockSave;
    private BlockState legsBlockSave;

    private ItemStack[] oldArmors;
    private ItemStack[] earthArmors;

    public EarthArmor2(RegisteredAbility register, Player player) {
        super(register, player);
        earthArmors = new ItemStack[4];
    }

    @Override
    public boolean canBeInitialized() {
        return super.canBeInitialized();
    }

    @Override
    public boolean swing() {
        return super.swing();
    }

    @Override
    public boolean sneak() {

        return false;
    }

    @Override
    public boolean canTick() {
        if (!super.canTick()) {
            return false;
        }

        if (!getState().equals(BendingAbilityState.PREPARING)
                && !getState().equals(BendingAbilityState.PROGRESSING)
                && !getState().equals(BendingAbilityState.PREPARED)) {
            return false;
        }

        return true;
    }

    @Override
    public void progress() {
        if (getState().equals(BendingAbilityState.PREPARING)) {

        }
        else if (getState().equals(BendingAbilityState.PROGRESSING)) {

        }
        else {
            // Earth Shield

        }
    }

    @Override
    public void stop() {
        if (headBlockSave != null) {
            headBlockSave.update(true);
        }
        if (legsBlockSave != null) {
            legsBlockSave.update(true);
        }
        player.getInventory().setArmorContents(oldArmors);
    }

    public static void handlePlayerLeave(Player player) {

    }

    public static void handlePlayerDeath(Player player) {

    }

    @Override
    protected long getMaxMillis() {
        return DURATION;
    }

    @Override
    public Object getIdentifier() {
        return this.player;
    }

    @SuppressWarnings("unused")
	private void formArmor() {
        oldArmors = player.getInventory().getArmorContents();
        boolean ironArmor = false;
        if (headBlockSave != null) {
            if (BlockTools.isIronBendable(player, headBlockSave.getType())) {
                earthArmors[0] = new ItemStack(Material.IRON_HELMET, 1);
                earthArmors[1] = new ItemStack(Material.IRON_CHESTPLATE, 1);
                ironArmor = true;
                setMeta(earthArmors[0], null, IRON_LORE);
                setMeta(earthArmors[1], null, IRON_LORE);
            }
            else {
                earthArmors[0] = new ItemStack(Material.LEATHER_HELMET, 1);
                earthArmors[1] = new ItemStack(Material.IRON_CHESTPLATE, 1);
                Color color = getColor(headBlockSave);
                setMeta(earthArmors[0], color, EARTH_LORE);
                setMeta(earthArmors[1], color, EARTH_LORE);
            }
        }

        if (legsBlockSave != null) {
            if (BlockTools.isIronBendable(player, legsBlockSave.getType())) {
                earthArmors[2] = new ItemStack(Material.IRON_LEGGINGS, 1);
                earthArmors[3] = new ItemStack(Material.IRON_BOOTS, 1);
                ironArmor = true;
                setMeta(earthArmors[2], null, IRON_LORE);
                setMeta(earthArmors[3], null, IRON_LORE);
            }
            else {
                earthArmors[2] = new ItemStack(Material.LEATHER_LEGGINGS, 1);
                earthArmors[3] = new ItemStack(Material.LEATHER_BOOTS, 1);
                Color color = getColor(legsBlockSave);
                setMeta(earthArmors[2], color, EARTH_LORE);
                setMeta(earthArmors[3], color, EARTH_LORE);
            }
        }

        player.getInventory().setArmorContents(earthArmors);
        if (!ironArmor) {
            PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) DURATION / 50, STRENGTH - 1);
            this.player.addPotionEffect(resistance);
            PotionEffect slowness = new PotionEffect(PotionEffectType.SLOW, (int) DURATION / 50, 0);
            this.player.addPotionEffect(slowness);
        }

        this.startedTime = System.currentTimeMillis();
        setState(BendingAbilityState.PROGRESSING);
    }

    private Color getColor (BlockState blockSave) {

        if (blockSave.getType() == Material.GRASS) {
            return GRASS_COLOR;
        }
        else if (blockSave.getType() == Material.STONE) {
            return STONE_COLOR;
        }
        else if (blockSave.getType() == Material.SAND) {
            return SAND_COLOR;
        }
        else if (blockSave.getType() == Material.GRAVEL) {
            return GRAVEL_COLOR;
        }
        else if (blockSave.getType() == Material.COAL_ORE) {
            return COAL_COLOR;
        }
        else if (blockSave.getType() == Material.LAPIS_ORE) {
            return LAPIS_COLOR;
        }
        else if (blockSave.getType() == Material.REDSTONE_ORE) {
            return REDSTONE_COLOR;
        }
        else {
            return DEFAULT_COLOR;
        }
    }

    private void setMeta (ItemStack armor, Color color, String lore) {
        if (armor != null) {
            ItemMeta meta = armor.getItemMeta();
            meta.setDisplayName(lore);
            if (color != null) {
                LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) meta;
                leatherArmorMeta.setColor(color);
            }
            armor.setItemMeta(meta);
        }
    }

}
