package net.avatar.realms.spigot.bending.abilities.chi;


import java.util.Map;

import org.bukkit.Effect;
import org.bukkit.entity.LivingEntity;
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
import net.avatar.realms.spigot.bending.utils.EntityTools;

/**
 * 
 * This ability will be modified :
 * When you hit an entity, you deal a small amount of damage to it and it gets slown.
 * The more you hit it, the more it get slown.
 *
 */
@BendingAbility(name="Vital Point", element=BendingType.ChiBlocker)
public class VitalPoint extends ActiveAbility {

	@ConfigurationParameter("Damage")
	private static int DAMAGE = 1;

	@ConfigurationParameter("Cooldown")
	private static long COOLDOWN = 10000;

	@ConfigurationParameter("Interval")
	private static long INTERVAL = 500;

	@ConfigurationParameter("Punch-Interval")
	private static int PUNCH_INTERVAL = 2;

	@ConfigurationParameter("Duration")
	private static long DURATION = 2500;

	@ConfigurationParameter("Slown-Duration")
	private static int SLOW_DURATION = 5; // In seconds

	@ConfigurationParameter("Chiblock-Duration")
	private static long CHIBLOCK_DURATION = 1500;

	@ConfigurationParameter("Max-Duration")
	private static long MAX_DURATION = 60 * 1000;

	@ConfigurationParameter("Max-Range")
	private static float MAX_RANGE = 3.5f;

	private static final PotionEffectType TYPE = PotionEffectType.SLOW;

	private long time;
	private int cptPunches;
	private int damage;
	private LivingEntity target;
	private int amplifier;

	public VitalPoint(Player player) {
		super(player, null);

		if (!this.state.isBefore(AbilityState.CanStart)) {
			return;
		}

		this.time = this.startedTime;
		this.cptPunches = 0;
		this.amplifier = 0;
		this.damage = DAMAGE;
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
				setState(AbilityState.Progressing);
				AbilityManager.getManager().addInstance(this);

			case Preparing:
			case Prepared:
			case Progressing:
				long now = System.currentTimeMillis();
				if (((now - this.time) < INTERVAL) && (this.cptPunches > 0)) {
					return false;
				}

				LivingEntity temp = EntityTools.getTargettedEntity(this.player, MAX_RANGE);
				if (!temp.equals(this.target)) {
					this.cptPunches = 0;
					this.amplifier = 0;
					this.target = temp;
				}

				this.cptPunches++;
				if ((this.cptPunches % PUNCH_INTERVAL) == 0)  {
					this.amplifier ++;
				}

				this.target.damage(this.damage, this.player);
				boolean isSlown = false;
				for (PotionEffect pe : this.target.getActivePotionEffects()) {
					if (pe.getType().equals(TYPE)) {
						if ((pe.getDuration() < SLOW_DURATION) || (pe.getAmplifier() < this.amplifier)) {
							isSlown = true;
						}
					}
				}
				if (isSlown) {
					// We have to remove it first, else it will not be added
					this.target.removePotionEffect(TYPE);
				}
				this.target.addPotionEffect(new PotionEffect(TYPE, SLOW_DURATION, this.amplifier));
				if (this.target instanceof Player) {
					EntityTools.blockChi((Player) this.target, now + CHIBLOCK_DURATION);
					((Player)this.target).playEffect(this.player.getLocation(), Effect.GHAST_SHRIEK, null);
				}
				this.time = System.currentTimeMillis();
				return false;

			case Ending:
			case Ended:
			case Removed:
			default:
				return true;
		}
	}

	@Override
	public boolean progress() {
		if (!super.progress()) {
			return false;
		}

		long now = System.currentTimeMillis();
		if (now >= (this.time + DURATION)) {
			return false;
		}

		return true;
	}

	@Override
	public void remove() {
		this.bender.cooldown(Abilities.VitalPoint, COOLDOWN);
		super.remove();
	}

	@Override
	public boolean canBeInitialized () {
		if (!super.canBeInitialized()) {
			return false;
		}

		if (EntityTools.isWeapon(this.player.getItemInHand().getType())) {
			return false;
		}

		Map<Object, IAbility> instances = AbilityManager.getManager().getInstances(Abilities.VitalPoint);

		if ((instances == null) || instances.isEmpty()) {
			return true;
		}

		return !instances.containsKey(this.player);
	}

	@Override
	protected long getMaxMillis () {
		return MAX_DURATION;
	}

	@Override
	public Abilities getAbilityType () {
		return Abilities.VitalPoint;
	}

	@Override
	public Object getIdentifier () {
		return this.player;
	}
}
