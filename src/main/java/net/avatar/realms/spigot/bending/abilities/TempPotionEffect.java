package net.avatar.realms.spigot.bending.abilities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

public class TempPotionEffect {
	private static Map<LivingEntity, TempPotionEffect> instances = new HashMap<LivingEntity, TempPotionEffect>();

	private static final long tick = 21;

	private int ID = Integer.MIN_VALUE;
	
	private Map<Integer, PotionInfo> infos = new HashMap<Integer, PotionInfo>();
	private LivingEntity entity;

	private class PotionInfo {

		private long starttime;
		private PotionEffect effect;

		public PotionInfo(long starttime, PotionEffect effect) {
			this.starttime = starttime;
			this.effect = effect;
		}

		public long getTime() {
			return starttime;
		}

		public PotionEffect getEffect() {
			return effect;
		}

	}

	public TempPotionEffect(LivingEntity entity, PotionEffect effect,
			long starttime) {
		this.entity = entity;
		if (instances.containsKey(entity)) {
			TempPotionEffect instance = instances.get(entity);
			instance.infos
					.put(instance.ID++, new PotionInfo(starttime, effect));
			// instance.effects.put(starttime, effect);
			instances.put(entity, instance);
		} else {
			// effects.put(starttime, effect);
			infos.put(ID++, new PotionInfo(starttime, effect));
			instances.put(entity, this);
		}
	}

	public TempPotionEffect(LivingEntity entity, PotionEffect effect) {
		this(entity, effect, System.currentTimeMillis());
	}

	private void addEffect(PotionEffect effect) {
		for (PotionEffect peffect : entity.getActivePotionEffects()) {
			if (peffect.getType().equals(effect.getType())) {
				if (peffect.getAmplifier() > effect.getAmplifier()) {

					if (peffect.getDuration() > effect.getDuration()) {
						return;
					} else {
						int dt = effect.getDuration() - peffect.getDuration();
						PotionEffect neweffect = new PotionEffect(
								effect.getType(), dt, effect.getAmplifier());
						new TempPotionEffect(entity, neweffect,
								System.currentTimeMillis()
										+ peffect.getDuration() * tick);
						return;
					}

				} else {

					if (peffect.getDuration() > effect.getDuration()) {
						entity.removePotionEffect(peffect.getType());
						entity.addPotionEffect(effect);
						int dt = peffect.getDuration() - effect.getDuration();
						PotionEffect neweffect = new PotionEffect(
								peffect.getType(), dt, peffect.getAmplifier());
						new TempPotionEffect(entity, neweffect,
								System.currentTimeMillis()
										+ effect.getDuration() * tick);
						return;
					} else {
						entity.removePotionEffect(peffect.getType());
						entity.addPotionEffect(effect);
						return;
					}

				}
			}
		}
		entity.addPotionEffect(effect);
	}

	private boolean progress() {
		List<Integer> toRemove = new LinkedList<Integer>();
		List<Integer> temp = new LinkedList<Integer>(infos.keySet());
		for (int id : temp) {
			PotionInfo info = infos.get(id);
			if (info.getTime() < System.currentTimeMillis()) {
				addEffect(info.getEffect());
				toRemove.add(id);
			}
		}
		for(int id : toRemove) {
			infos.remove(id);
		}
		
		if (infos.isEmpty() && instances.containsKey(entity))
			return false;
		
		return true;
	}

	public static void progressAll() {
		List<TempPotionEffect> toRemove = new LinkedList<TempPotionEffect>();
		for (TempPotionEffect effect : instances.values()) {
			boolean keep = effect.progress();
			if(!keep) {
				toRemove.add(effect);
			}
		}
		for (TempPotionEffect effect : toRemove) {
			effect.remove();
		}
	}

	private void remove() {
		instances.remove(entity);
	}

}
