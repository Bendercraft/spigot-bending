package net.bendercraft.spigot.bending.abilities.fire;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.bendercraft.spigot.bending.abilities.ABendingAbility;
import net.bendercraft.spigot.bending.abilities.BendingActiveAbility;
import net.bendercraft.spigot.bending.abilities.BendingElement;
import net.bendercraft.spigot.bending.abilities.RegisteredAbility;
import net.bendercraft.spigot.bending.controller.ConfigurationParameter;
import net.bendercraft.spigot.bending.utils.EntityTools;

@ABendingAbility(name = Illumination.NAME, element = BendingElement.FIRE, shift=false)
public class Illumination extends BendingActiveAbility {
	public final static String NAME = "Illumination";

	@ConfigurationParameter("Range")
	private static int RANGE = 5;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 0;

	public Illumination(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean sneak() {
		EntityTools.giveItemInOffHand(player, new ItemStack(Material.TORCH, 1));
		bender.cooldown(NAME, COOLDOWN);
		return false;
	}

	@Override
	public void progress() {
		
	}

	@Override
	public void stop() {
		
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}
