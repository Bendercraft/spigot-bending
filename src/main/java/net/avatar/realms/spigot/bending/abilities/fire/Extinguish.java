package net.avatar.realms.spigot.bending.abilities.fire;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.air.AirBlast;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.earth.LavaTrain;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@BendingAbility(name="Extinguish", element=BendingType.Fire)
public class Extinguish extends ActiveAbility {
	
	@ConfigurationParameter("Range")
	private static double RANGE = 20;

	@ConfigurationParameter("Radius")
	private static double RADIUS = 7;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 1000;
	private static byte full = AirBlast.full;
	
	
	public Extinguish (Player player) {
		super(player, null);
	}

	@SuppressWarnings ("deprecation")
	@Override
	public boolean swing () {
		switch (this.state) {
			case None:
			case CannotStart:
			case Ended:
			case Removed:
				return false;
			default:
				double range = PluginTools.firebendingDayAugment(RANGE, this.player.getWorld());

				if (BlockTools.isMeltable(EntityTools.getTargetBlock(this.player, range))) {
					new HeatMelt(this.player, this);
					return false;
				}
				
				double radius = PluginTools.firebendingDayAugment(RADIUS, this.player.getWorld());

				for (Block block : BlockTools.getBlocksAroundPoint(EntityTools.getTargetBlock(this.player, range).getLocation(),
						radius)) {
					if (ProtectionManager.isRegionProtectedFromBending(this.player, Abilities.Blaze, block.getLocation())) {
						continue;
					}
					//Do not allow firebender to completly negate lavabend
					if (LavaTrain.isLavaPart(block)) {
						continue;
					}
					if (block.getType() == Material.FIRE) {
						block.setType(Material.AIR);
						block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
					}
					else if (block.getType() == Material.STATIONARY_LAVA) {
						block.setType(Material.OBSIDIAN);
						block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
					}
					else if (block.getType() == Material.LAVA) {
						if (block.getData() == full) {
							block.setType(Material.OBSIDIAN);
						}
						else {
							block.setType(Material.COBBLESTONE);
						}
						block.getWorld().playEffect(block.getLocation(), Effect.EXTINGUISH, 0);
					}
				}

				this.bender.cooldown(Abilities.HeatControl, COOLDOWN);
				return false;
		}
	}

	@Override
	public boolean sneak() {
		Cook cook = new Cook(this.player);
		return cook.sneak();
	}
	
	public static boolean canBurn(Player player) {
		if ((EntityTools.getBendingAbility(player) == Abilities.HeatControl)
				|| FireJet.checkTemporaryImmunity(player)) {
			player.setFireTicks(0);
			return false;
		}
		
		if ((player.getFireTicks() > 80)
				&& EntityTools.canBendPassive(player, BendingType.Fire)) {
			player.setFireTicks(80);
		}
		
		return true;
	}
	

	@Override
	public Object getIdentifier () {
		return this.player;
	}
	
	@Override
	public Abilities getAbilityType () {
		return Abilities.HeatControl;
	}
}
