package net.avatar.realms.spigot.bending.abilities.fire;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.AbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.abilities.base.ActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.BlockTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

@BendingAbility(name="Illumination", element=BendingType.Fire)
public class Illumination extends ActiveAbility {
	//	private static Map<Block, Player> blocks = new HashMap<Block, Player>();

	@ConfigurationParameter("Range")
	private static final int RANGE = 5;

	@ConfigurationParameter("Cooldown")
	public static long COOLDOWN = 0;

	private Block block;
	private BlockState blockState;

	public Illumination (Player player) {
		super(player, null);
	}

	@Override
	public boolean swing () {
		switch (this.state) {
			case None:
			case CannotStart:
				return false;
			case CanStart:
				set();
				AbilityManager.getManager().addInstance(this);
				setState(AbilityState.Progressing);
				return false;
			case Preparing:
			case Prepared:
			case Progressing:
				setState(AbilityState.Ended);
			case Ending:
			case Ended:
			case Removed:
			default:
				return false;
		}
	}

	private void set() {
		Block standingblock = this.player.getLocation().getBlock();
		Block standblock = standingblock.getRelative(BlockFace.DOWN);
		if ((FireStream.isIgnitable(this.player, standingblock) && (standblock.getType() != Material.LEAVES))
				&& (this.block == null) && !isIlluminated(standblock)) {
			this.block = standingblock;
			this.blockState = this.block.getState();
			this.block.setType(Material.TORCH);
		}
		else if ((FireStream.isIgnitable(this.player, standingblock) && (standblock.getType() != Material.LEAVES))
				&& !this.block.equals(standblock) && !isIlluminated(standblock) && BlockTools.isSolid(standblock)) {
			revert();
			this.block = standingblock;
			this.blockState = this.block.getState();
			this.block.setType(Material.TORCH);
		}
		else if (this.block == null) {
			return;
		}
		else if (this.player.getWorld() != this.block.getWorld()) {
			revert();
		}
		else if (this.player.getLocation().distance(this.block.getLocation()) >
		PluginTools.firebendingDayAugment(RANGE, this.player.getWorld())) {
			revert();
		}
	}
	
	@Override
	public boolean progress () {
		if (!super.progress()) {
			return false;
		}

		set();

		return true;
	}

	@Override
	public void stop () {
		revert();
	}

	private void revert() {
		if (this.block != null) {
			this.blockState.update(true);
		}
	}

	@Override
	public void remove () {
		this.bender.cooldown(Abilities.Illumination, COOLDOWN);
		super.remove();
	}

	public static boolean isIlluminated (Block block) {
		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.Illumination);
		for (Object o : instances.keySet()) {
			Illumination ill = (Illumination) instances.get(o);
			if (ill.block.equals(block)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Object getIdentifier () {
		return this.player;
	}
	
	@Override
	public Abilities getAbilityType () {
		return Abilities.FireBlast;
	}

}
