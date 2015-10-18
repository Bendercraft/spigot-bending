package net.avatar.realms.spigot.bending.abilities.energy;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;

@ABendingAbility(name = "Avatar State", bind = BendingAbilities.AvatarState, element = BendingElement.Energy)
public class AvatarState extends BendingActiveAbility {

	@ConfigurationParameter("Factor")
	public static double FACTOR = 4.5;

	@ConfigurationParameter("Max-Duration")
	private static long MAX_DURATION = 300000;

	@ConfigurationParameter("Cooldown-Factor")
	private static int COOLDOWN_FACTOR = 4;

	private long realDuration;

	public AvatarState(Player player) {
		super(player);
	}

	@Override
	public boolean swing() {
		if (getState() == BendingAbilityState.Progressing) {
			setState(BendingAbilityState.Ended);
			return false;
		}

		if (getState() == BendingAbilityState.Start) {
			setState(BendingAbilityState.Progressing);
		}

		return false;
	}

	@Override
	public void progress() {
		if (getState() == BendingAbilityState.Progressing) {
			addPotionEffects();
			return;
		}

		remove();
	}

	private void addPotionEffects() {
		int duration = 70;
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 2));
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 2));
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 2));
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0));
	}

	public static boolean isAvatarState(Player player) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.AvatarState);

		if ((instances == null) || instances.isEmpty()) {
			return false;
		}

		if (instances.containsKey(player)) {
			return true;
		}
		return false;
	}

	public static double getValue(double value) {
		return FACTOR * value;
	}

	public static int getValue(int value) {
		return (int) FACTOR * value;
	}

	public static List<Player> getPlayers() {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.AvatarState);
		LinkedList<Player> players = new LinkedList<Player>();
		if ((instances == null) || instances.isEmpty()) {
			return players;
		}

		for (Object obj : instances.keySet()) {
			players.add((Player) obj);
		}
		return players;
	}

	@Override
	public void stop() {
		long now = System.currentTimeMillis();
		this.realDuration = now - this.startedTime;
		this.bender.cooldown(BendingAbilities.AvatarState, this.realDuration * COOLDOWN_FACTOR);
	}

	@Override
	protected long getMaxMillis() {
		return MAX_DURATION;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}
