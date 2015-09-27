package net.avatar.realms.spigot.bending.abilities.chi;

import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.BendingAbilityState;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPath;
import net.avatar.realms.spigot.bending.abilities.base.BendingActiveAbility;
import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;
import net.avatar.realms.spigot.bending.controller.ConfigurationParameter;
import net.avatar.realms.spigot.bending.utils.EntityTools;

/**
 * 
 * This ability will be modified : When you hit an entity, you deal a small
 * amount of damage to it and it gets slown. The more you hit it, the more it
 * get slown.
 *
 */
@BendingAbility(name = "Vital Point", bind = BendingAbilities.VitalPoint, element = BendingElement.ChiBlocker)
public class VitalPoint extends BendingActiveAbility {

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 1;

	@ConfigurationParameter("Damage-Increment")
	private static int DAMAGE_INCREMENT = 1;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 10000;

	@ConfigurationParameter("Duration")
	private static long DURATION = 2500;

	@ConfigurationParameter("Slown-Duration")
	private static int SLOW_DURATION = 5; // In seconds

	@ConfigurationParameter("Chiblock-Duration")
	private static long CHIBLOCK_DURATION = 3000;

	@ConfigurationParameter("Max-Range")
	private static float MAX_RANGE = 3.5f;

	private static final PotionEffectType TYPE = PotionEffectType.SLOW;

	private int damage;
	private long cooldown;
	private LivingEntity target;
	private int amplifier;

	public VitalPoint(Player player) {
		super(player, null);

		if (!this.state.isBefore(BendingAbilityState.CanStart)) {
			return;
		}

		this.amplifier = 0;
		this.damage = DAMAGE;
		this.cooldown = COOLDOWN;
		
		if(bender.hasPath(BendingPath.Seeker)) {
			this.damage *= 1.5;
			this.cooldown *= 1.4;
		}
		if(bender.hasPath(BendingPath.Restless)) {
			this.damage *= 0.6;
			this.cooldown *= 0.5;
		}
	}

	@Override
	public boolean swing() {
		switch (this.state) {
			case None:
			case CannotStart:
				return false;

			case CanStart:
				this.target = EntityTools.getTargettedEntity(this.player, MAX_RANGE);
				if (this.target == null) {
					return false;
				}

				int combo = ComboPoints.getComboPointAmount(this.player);
				if (this.player.isSneaking() && (combo > 0)) {
					this.damage += combo * DAMAGE_INCREMENT;
					this.target.damage(this.damage, this.player);
					if (combo == 5) {
						if (this.target instanceof Player) {
							EntityTools.blockChi((Player) this.target, CHIBLOCK_DURATION);
						}
						this.target.addPotionEffect(new PotionEffect(TYPE, (int) (DURATION / 20), 130));
					}
					else {
						this.amplifier = combo - 1;
						if (combo == 4) {
							if (this.target instanceof Player) {
								EntityTools.blockChi((Player) this.target, CHIBLOCK_DURATION);
							}
						}
						else if (combo == 1) {
							this.amplifier = 1;
						}
						this.target.addPotionEffect(new PotionEffect(TYPE, SLOW_DURATION / 20, this.amplifier));
					}
					ComboPoints.consume(this.player);
				}
				else {
					ComboPoints.addComboPoint(this.player, this.target);
					this.target.damage(this.damage, this.player);
					this.target.addPotionEffect(new PotionEffect(TYPE, (int) (DURATION / 20), this.amplifier));
				}

				long cooldown = this.cooldown + 1000;
				cooldown /= (6 - combo);

				this.bender.cooldown(BendingAbilities.VitalPoint, cooldown);

				setState(BendingAbilityState.Ended);
			default:
				return true;
		}
	}

	@Override
	public boolean canBeInitialized() {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (EntityTools.isWeapon(this.player.getItemInHand().getType())) {
			return false;
		}

		Map<Object, IBendingAbility> instances = AbilityManager.getManager().getInstances(BendingAbilities.VitalPoint);

		if ((instances == null) || instances.isEmpty()) {
			return true;
		}

		return !instances.containsKey(this.player);
	}

	@Override
	protected long getMaxMillis() {
		return 2000;
	}

	@Override
	public Object getIdentifier() {
		return this.player;
	}
}
