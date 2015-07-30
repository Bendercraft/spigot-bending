package net.avatar.realms.spigot.bending.abilities.energy;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.Ability;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.controller.Flight;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@BendingAbility(name="Avatar State", element=BendingType.Energy)
public class AvatarState extends Ability{

	@ConfigurationParameter("Factor")
	private static double FACTOR = 4.5;
	
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
		
		if (state == AbilityState.CannotStart) {
			return true;
		}
		
		if (state == AbilityState.Progressing) {
			setState(AbilityState.Ended);
			return false;
		}
		
		if (state == AbilityState.CanStart) {
			AbilityManager.getManager().addInstance(this);
			setState(AbilityState.Progressing);
			new Flight(player);
		}

		return false;
	}

	@Override
	public boolean progress () {
		
		if (!super.progress()) {
			return false;
		}
		
		if (state == AbilityState.Ended) {
			return false;
		}
		
		if (state == AbilityState.Progressing) {
			addPotionEffects();
		}
		
		return true;
	}

	private void addPotionEffects () {
		int duration = 70;
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, 2));
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 2));
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, 2));
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, 0));
	}

	public static boolean isAvatarState (Player player) {
		Map<Object, Ability> instances = AbilityManager.getManager().getInstances(Abilities.AvatarState);
		
		if (instances == null || instances.isEmpty()) {
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
		Map<Object, Ability> instances = AbilityManager.getManager().getInstances(Abilities.AvatarState);
		LinkedList<Player> players = new LinkedList<Player>();
		if (instances == null || instances.isEmpty()) {
			return players;
		}
		
		for (Object obj : instances.keySet()) {
			players.add((Player) obj);
		}
		return players;
	}
	
	@Override
	public void remove() {
		long now = System.currentTimeMillis();
		realDuration = now - startedTime;
		bender.cooldown(Abilities.AvatarState, realDuration * COOLDOWN_FACTOR);
		AbilityManager.getManager().getInstances(Abilities.AvatarState).remove(player);
		super.remove();
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
		return player;
	}
}
