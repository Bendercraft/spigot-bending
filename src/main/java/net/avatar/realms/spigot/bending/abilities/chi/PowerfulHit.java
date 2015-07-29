package net.avatar.realms.spigot.bending.abilities.chi;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.Ability;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.utils.EntityTools;

/**
 * 
 * This ability hit the first entity in front of you powerfully driving to a knockback
 * You must be sneaking when clicking to activate this technique.
 *
 */
@BendingAbility(name="Powerful Hit", element=BendingType.ChiBlocker)
public class PowerfulHit extends Ability{
	
	public static final long DAMAGE = Bending.plugin.configuration.getLongAttribute(configPrefix +"Chi.PowerfulHit.Damage");
	public static final long KNOCKBACK = Bending.plugin.configuration.getLongAttribute(configPrefix +"Chi.PowerfulHit.Knockback");
	public static final long RANGE = Bending.plugin.configuration.getLongAttribute(configPrefix +"Chi.PowerfulHit.Range");
	public static final long COOLDOWN = Bending.plugin.configuration.getLongAttribute(configPrefix +"Chi.PowerfulHit.Cooldown");

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
	protected long getMaxMillis() {
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

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}
		
		if (EntityTools.isWeapon(player.getItemInHand().getType())) {
			return false;
		}
		
		return true;
	}

}
