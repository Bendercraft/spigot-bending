package net.avatar.realms.spigot.bending.abilities.energy;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.FlyingPlayer;

@BendingAbility(name="Avatar State", element=BendingType.Energy)
public class AvatarState extends ActiveAbility{

	@ConfigurationParameter("Factor")
	public static double FACTOR = 4.5;

	@ConfigurationParameter("Max-Duration")
	private static long MAX_DURATION = 300000;

	@ConfigurationParameter("Cooldown-Factor")
	private static int COOLDOWN_FACTOR = 4;

	private long realDuration;

	public AvatarState (Player player) {
		super(player, null);
	}

	@Override
	public boolean swing() {

		if (this.state.isBefore(AbilityState.CanStart)) {
			return true;
		}

		if (this.state == AbilityState.Progressing) {
			setState(AbilityState.Ended);
			return false;
		}

		if (this.state == AbilityState.CanStart) {
			FlyingPlayer.addFlyingPlayer(this.player, this, getMaxMillis());
			AbilityManager.getManager().addInstance(this);
			setState(AbilityState.Progressing);
		}

		return false;
	}

	@Override
	public boolean progress () {

		if (!super.progress()) {
			return false;
		}

		if (this.state == AbilityState.Progressing) {
			addPotionEffects();
			return true;
		}

		return false;
	}

	private void addPotionEffects () {
		int duration = 70;
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 2));
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 2));
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 2));
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0));
	}

	public static boolean isAvatarState (Player player) {
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.AvatarState);

		if ((instances == null) || instances.isEmpty()) {
			return false;
		}

		if (instances.containsKey(player)) {
			return true;
		}
		return false;
	}

	public static double getValue (double value) {
		return FACTOR * value;
	}

	public static int getValue (int value) {
		return (int)FACTOR * value;
	}

	public static List<Player> getPlayers() {
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.AvatarState);
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
	public void stop () {
		FlyingPlayer.removeFlyingPlayer(this.player, this);
		long now = System.currentTimeMillis();
		this.realDuration = now - this.startedTime;
		this.bender.cooldown(Abilities.AvatarState, this.realDuration * COOLDOWN_FACTOR);
	}

	@Override
	protected long getMaxMillis() {
		return MAX_DURATION;
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.AvatarState;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}
