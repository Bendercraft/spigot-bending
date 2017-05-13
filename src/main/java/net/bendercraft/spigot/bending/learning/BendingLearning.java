package net.bendercraft.spigot.bending.learning;

import org.bukkit.entity.Player;
import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.AbilityManager;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.abilities.air.AirBlast;
import net.bendercraft.spigot.bending.abilities.air.AirSlice;
import net.bendercraft.spigot.bending.abilities.air.AirSpout;
import net.bendercraft.spigot.bending.abilities.air.AirSwipe;
import net.bendercraft.spigot.bending.abilities.arts.Dash;
import net.bendercraft.spigot.bending.abilities.arts.DirectHit;
import net.bendercraft.spigot.bending.abilities.arts.HighJump;
import net.bendercraft.spigot.bending.abilities.earth.Collapse;
import net.bendercraft.spigot.bending.abilities.earth.EarthBlast;
import net.bendercraft.spigot.bending.abilities.earth.EarthLariat;
import net.bendercraft.spigot.bending.abilities.earth.EarthWall;
import net.bendercraft.spigot.bending.abilities.fire.Blaze;
import net.bendercraft.spigot.bending.abilities.fire.FireBlast;
import net.bendercraft.spigot.bending.abilities.fire.FireFerret;
import net.bendercraft.spigot.bending.abilities.fire.HeatControl;
import net.bendercraft.spigot.bending.abilities.water.HealingWaters;
import net.bendercraft.spigot.bending.abilities.water.WaterManipulation;
import net.bendercraft.spigot.bending.abilities.water.WaterSpout;

public class BendingLearning {

	public void onEnable() {
		PermissionListener permListener = new PermissionListener(this);
		AirListener airListener = new AirListener(this);
		EarthListener earthListener = new EarthListener(this);
		WaterListener waterListener = new WaterListener(this);
		FireListener fireListener = new FireListener(this);
		MasterListener chiListener = new MasterListener(this);

		// Register listeners
		Bending.getInstance().getServer().getPluginManager().registerEvents(permListener, Bending.getInstance());
		Bending.getInstance().getServer().getPluginManager().registerEvents(airListener, Bending.getInstance());
		Bending.getInstance().getServer().getPluginManager().registerEvents(earthListener, Bending.getInstance());
		Bending.getInstance().getServer().getPluginManager().registerEvents(waterListener, Bending.getInstance());
		Bending.getInstance().getServer().getPluginManager().registerEvents(fireListener, Bending.getInstance());
		Bending.getInstance().getServer().getPluginManager().registerEvents(chiListener, Bending.getInstance());
	}
	
	public boolean addPermission(Player player, String ability) {
		return addPermission(player, AbilityManager.getManager().getRegisteredAbility(ability));
	}

	public boolean addPermission(Player player, RegisteredAbility ability) {
		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		if (bender != null && !bender.hasAbility(ability)) {
			bender.addAbility(ability);
			return true;
		}
		return false;
	}
	
	public boolean removePermission(Player player, String ability) {
		return removePermission(player, AbilityManager.getManager().getRegisteredAbility(ability));
	}

	public boolean removePermission(Player player, RegisteredAbility ability) {
		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		if (bender != null && bender.hasAbility(ability)) {
			bender.removeAbility(ability);
			return true;
		}
		return false;
	}

	public boolean isBasicBendingAbility(String ability) {
		if(ability.equals(AirBlast.NAME) 
				|| ability.equals(AirSpout.NAME)
				|| ability.equals(AirSwipe.NAME)
				|| ability.equals(AirSlice.NAME)
				
				|| ability.equals(FireBlast.NAME)
				|| ability.equals(Blaze.NAME)
				|| ability.equals(HeatControl.NAME)
				|| ability.equals(FireFerret.NAME)
				
				|| ability.equals(EarthBlast.NAME)
				|| ability.equals(Collapse.NAME)
				|| ability.equals(EarthWall.NAME)
				|| ability.equals(EarthLariat.NAME)
				
				|| ability.equals(WaterManipulation.NAME)
				|| ability.equals(HealingWaters.NAME)
				|| ability.equals(WaterSpout.NAME)
				
				|| ability.equals(Dash.NAME)
				|| ability.equals(DirectHit.NAME)
				|| ability.equals(HighJump.NAME)) {
			return true;
		}
		return false;
	}
}