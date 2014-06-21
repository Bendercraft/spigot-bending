package net.avatarrealms.minecraft.bending.abilities.water;

import java.util.Arrays;

import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.model.Abilities;
import net.avatarrealms.minecraft.bending.model.BendingType;
import net.avatarrealms.minecraft.bending.model.TempBlock;
import net.avatarrealms.minecraft.bending.utils.BlockTools;
import net.avatarrealms.minecraft.bending.utils.EntityTools;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class FastSwimming {

	private static double factor = ConfigManager.fastSwimmingFactor;

	private static final Abilities[] shiftabilities = {
			Abilities.WaterManipulation, Abilities.Surge,
			Abilities.HealingWaters, Abilities.PhaseChange,
			Abilities.Bloodbending, Abilities.IceSpike, Abilities.OctopusForm,
			Abilities.Torrent, Abilities.AirBlast, Abilities.AirBurst,
			Abilities.AirShield, Abilities.AirSuction, Abilities.AirSwipe,
			Abilities.Blaze, Abilities.Collapse, Abilities.EarthBlast,
			Abilities.EarthTunnel, Abilities.FireBlast, Abilities.FireBurst,
			Abilities.FireShield, Abilities.Lightning, Abilities.RaiseEarth,
			Abilities.Shockwave, Abilities.Tornado, Abilities.Tremorsense };

	public static void HandleSwim(Server server) {
		for (Player player : server.getOnlinePlayers()) {
			Abilities ability = EntityTools.getBendingAbility(player);
			if (EntityTools.isBender(player, BendingType.Water)
					&& EntityTools.canBendPassive(player, BendingType.Water)
					&& player.isSneaking()
					&& BlockTools.isWater(player.getLocation().getBlock())
					&& !TempBlock.isTempBlock(player.getLocation().getBlock())
					&& !(Arrays.asList(shiftabilities).contains(ability))) {
				player.setVelocity(player.getEyeLocation().getDirection()
						.clone().normalize().multiply(factor));

			}
		}
		
	}
}
