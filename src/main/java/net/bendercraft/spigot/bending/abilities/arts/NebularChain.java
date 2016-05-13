package net.bendercraft.spigot.bending.abilities.arts;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingAffinity;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.EntityTools;

@ABendingAbility(name = NebularChain.NAME, affinity = BendingAffinity.SWORD)
public class NebularChain extends BendingActiveAbility {
	public final static String NAME = "NebularChain";
	
	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 10000;
	
	@ConfigurationParameter("Range")
	public static long RANGE = 10;
	
	@ConfigurationParameter("Push")
	public static double PUSH = 2;

	public NebularChain(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean swing() {
		LivingEntity target = EntityTools.getTargetedEntity(player, RANGE);
		if(target != null) {
			target.setVelocity(player.getLocation().toVector().clone().subtract(target.getLocation().toVector()).normalize().multiply(PUSH));
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 5, 1);
			
			bender.cooldown(this, COOLDOWN);
		}
		return false;
	}

	@Override
	public boolean sneak() {
		
		return false;
	}

	@Override
	public void progress() {
		
	}
	
	@Override
	public Object getIdentifier() {
		return this.player;
	}

	@Override
	public void stop() {
		
	}

}
