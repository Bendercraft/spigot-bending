package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.ABendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

@ABendingAbility(name = Illumination.NAME, element = BendingElement.FIRE, shift=false)
public class Illumination extends BendingActiveAbility {
	public final static String NAME = "Illumination";

	@ConfigurationParameter("Range")
	private static final int RANGE = 5;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 0;

	private Block block;
	private BlockState blockState;

	public Illumination(RegisteredAbility register, Player player) {
		super(register, player);
	}

	@Override
	public boolean sneak() {
		if(getState() == BendingAbilityState.START) {
			setState(BendingAbilityState.PROGRESSING);
		} else if(getState() == BendingAbilityState.PROGRESSING) {
			remove();
		}
		return false;
	}

	private void set() {
		Block standingblock = this.player.getLocation().getBlock();
		Block standblock = standingblock.getRelative(BlockFace.DOWN);
		if ((FireStream.isIgnitable(this.player, standingblock) && (standblock.getType() != Material.LEAVES)) && (this.block == null) && !isIlluminated(standblock)) {
			this.block = standingblock;
			this.blockState = this.block.getState();
			this.block.setType(Material.TORCH);
		} else if ((FireStream.isIgnitable(this.player, standingblock) && (standblock.getType() != Material.LEAVES)) && !this.block.equals(standblock) && !isIlluminated(standblock) && BlockTools.isSolid(standblock)) {
			revert();
			this.block = standingblock;
			this.blockState = this.block.getState();
			this.block.setType(Material.TORCH);
		} else if (this.block == null) {
			return;
		} else if (this.player.getWorld() != this.block.getWorld()) {
			revert();
		} else if (this.player.getLocation().distance(this.block.getLocation()) > PluginTools.firebendingDayAugment(RANGE, this.player.getWorld())) {
			revert();
		}
	}

	@Override
	public void progress() {
		set();
	}

	@Override
	public void stop() {
		revert();
		this.bender.cooldown(NAME, COOLDOWN);
	}

	private void revert() {
		if (this.block != null) {
			this.blockState.update(true);
		}
	}

	@Override
	protected long getMaxMillis() {
		return 1000 * 60 * 15;
	}

	public static boolean isIlluminated(Block block) {
		Map<Object, BendingAbility> instances = AbilityManager.getManager().getInstances(NAME);
		if (instances == null) {
			return false;
		}
		for (Object o : instances.keySet()) {
			Illumination ill = (Illumination) instances.get(o);
			if (ill.block != null && ill.block.equals(block)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}

}
