package net.avatarrealms.minecraft.bending.abilities.chi;

import java.util.HashMap;
import java.util.Map;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.AvatarState;
import net.avatarrealms.minecraft.bending.abilities.BendingType;
import net.avatarrealms.minecraft.bending.abilities.IAbility;
import net.avatarrealms.minecraft.bending.controller.ConfigManager;
import net.avatarrealms.minecraft.bending.utils.EntityTools;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Paralyze implements IAbility {
	private static Map<Entity, Long> entities = new HashMap<Entity, Long>();
	private static Map<Entity, Long> cooldowns = new HashMap<Entity, Long>();

	private static final long cooldown = ConfigManager.paralyzeCooldown;
	private static final long duration = ConfigManager.paralyzeDuration;
	private IAbility parent;

	public Paralyze(Player sourceplayer, Entity targetentity, IAbility parent) {
		this.parent = parent;
		if (targetentity != null && sourceplayer != null) {
			if (EntityTools.isBender(sourceplayer, BendingType.ChiBlocker)
				&& EntityTools.getBendingAbility(sourceplayer) == Abilities.Paralyze
				&& EntityTools.canBend(sourceplayer, Abilities.Paralyze)) {
			if (cooldowns.containsKey(targetentity)) {
				if (System.currentTimeMillis() < cooldowns.get(targetentity)
						+ cooldown) {
					return;
				} else {
					cooldowns.remove(targetentity);
				}
			}
			paralyze(targetentity);
			cooldowns.put(targetentity, System.currentTimeMillis());
			}
		}	
	}

	private static void paralyze(Entity entity) {
		entities.put(entity, System.currentTimeMillis());
		if (entity instanceof Creature) {
			((Creature) entity).setTarget(null);
		}
	}

	public static boolean isParalyzed(Entity entity) {
		if (entity instanceof Player) {
			if (AvatarState.isAvatarState((Player) entity))
				return false;
		}
		if (entities.containsKey(entity)) {
			if (System.currentTimeMillis() < entities.get(entity) + duration) {
				return true;
			}
			entities.remove(entity);
		}
		return false;

	}

	public static String getDescription() {
		return "Paralyzes the target, making them unable to do anything for a short "
				+ "period of time. This ability has a long cooldown.";
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}
