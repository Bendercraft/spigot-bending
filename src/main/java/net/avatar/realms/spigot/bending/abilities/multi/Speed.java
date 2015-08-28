package net.avatar.realms.spigot.bending.abilities.multi;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.TempPotionEffect;
import net.avatar.realms.spigot.bending.abilities.base.IAbility;
import net.avatar.realms.spigot.bending.abilities.base.PassiveAbility;
import net.avatar.realms.spigot.bending.controller.Flight;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.ProtectionManager;

@BendingAbility(name="Speed", element=BendingType.None)
public class Speed extends PassiveAbility {

	public Speed(Player player) {
		super(player, null);

		if (this.state.isBefore(AbilityState.CanStart)) {
			return;
		}
		// Yes, this is useless
	}

	public void sprint () {
		// I've separate it from constructor in case where a player bind Speed
		// (as it became an Abilities enum) and make fun by clicking...
		new Flight(this.player);
		this.player.setAllowFlight(true);
		AbilityManager.getManager().addInstance(this);
		setState(AbilityState.Progressing);
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		if (this.player.isSprinting()) {
			if (this.bender.isBender(BendingType.ChiBlocker)) {
				applySpeed();
				return true;
			}
			else if (this.bender.isBender(BendingType.Air)) {
				if (EntityTools.canBendPassive(this.player, BendingType.Air)) {
					applySpeed();
					return true;
				}
			}
		}

		return false;
	}

	private void applySpeed() {
		int factor = 0;
		if (EntityTools.isBender(this.player, BendingType.Air)
				&& EntityTools.canBendPassive(this.player, BendingType.Air)) {
			factor = 1;
		}
		int jumpfactor = factor + 1;
		PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 70,
				factor);
		PotionEffect jump = new PotionEffect(PotionEffectType.JUMP, 70,
				jumpfactor);

		new TempPotionEffect(this.player, speed);
		if (EntityTools.getBendingAbility(this.player) != Abilities.AirScooter) {
			new TempPotionEffect(this.player, jump);
		}

	}

	public static List<Player> getPlayers () {
		List<Player> players = new LinkedList<Player>();
		List<Object> toRemove = new LinkedList<Object>();

		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.AirPassive);
		if ((instances == null) || instances.isEmpty()) {
			return players;
		}

		for (Entry<Object, IAbility> entry : instances.entrySet()) {
			Player player = entry.getValue().getPlayer();
			if (player.isSprinting()) {
				players.add(player);
			}
			else {
				toRemove.add(entry.getKey());
			}
		}

		for (Object o : toRemove) {
			instances.get(o).consume();
		}

		return players;
	}

	@Override
	public Object getIdentifier () {
		return this.player;
	}

	@Override
	public boolean canBeInitialized () {
		if ((this.player == null) || (this.bender == null)) {
			return false;
		}

		if (!(this.bender.isBender(BendingType.Air) || this.bender.isBender(BendingType.ChiBlocker))) {
			return false;
		}

		if (ProtectionManager.isRegionProtectedFromBendingPassives(this.player, this.player.getLocation())) {
			return false;
		}

		return true;
	}

	@Override
	public Abilities getAbilityType () {
		return Abilities.AirPassive;
	}
}
