package net.avatar.realms.spigot.bending.abilities.chi;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.Ability;

/**
 * 
 * This ability hit the first entity in front of you powerfully driving to a knockback
 * You must be sneaking when clicking to activate this technique.
 *
 */
public class PowerfulHit extends Ability{

	public PowerfulHit(Player player) {
		super(player, null);
	}	

	@Override
	public boolean swing() {
		// TODO Auto-generated method stub
		
		//Vector v = loc1.toVector().subtract(loc2.toVector())
		return super.swing();
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}
		
		return false;
	}

	@Override
	protected int getMaxMillis() {
		return 1;
	}

	@Override
	public void remove() {
		
		super.remove();
	}

	@Override
	public Abilities getAbilityType() {
		return Abilities.PowerfulHit;
	}

	@Override
	public Object getIdentifier() {
		return player;
	}

}
