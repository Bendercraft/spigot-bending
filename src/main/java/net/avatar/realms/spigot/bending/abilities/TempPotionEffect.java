package net.avatar.realms.spigot.bending.abilities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

@Deprecated
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
			return this.starttime;
		}

		public PotionEffect getEffect() {
			return this.effect;
		}

	}

	public TempPotionEffect(LivingEntity entity, PotionEffect effect, long starttime) {
		this.entity = entity;
		if (instances.containsKey(entity)) {
			TempPotionEffect instance = instances.get(entity);
			instance.infos.put(instance.ID++, new PotionInfo(starttime, effect));
			// instance.effects.put(starttime, effect);
			instances.put(entity, instance);
		} else {
			// effects.put(starttime, effect);
			this.infos.put(this.ID++, new PotionInfo(starttime, effect));
			instances.put(entity, this);
		}
	}

	public TempPotionEffect(LivingEntity entity, PotionEffect effect) {
		this(entity, effect, System.currentTimeMillis());
	}

	private void addEffect(PotionEffect effect) {
		for (PotionEffect peffect : this.entity.getActivePotionEffects()) {
			if (peffect.getType().equals(effect.getType())) {
				if (peffect.getAmplifier() > effect.getAmplifier()) {

					if (peffect.getDuration() > effect.getDuration()) {
						return;
					} else {
						int dt = effect.getDuration() - peffect.getDuration();
						PotionEffect neweffect = new PotionEffect(effect.getType(), dt, effect.getAmplifier());
						new TempPotionEffect(this.entity, neweffect, System.currentTimeMillis() + peffect.getDuration() * tick);
						return;
					}

				} else {

					if (peffect.getDuration() > effect.getDuration()) {
						this.entity.removePotionEffect(peffect.getType());
						this.entity.addPotionEffect(effect);
						int dt = peffect.getDuration() - effect.getDuration();
						PotionEffect neweffect = new PotionEffect(peffect.getType(), dt, peffect.getAmplifier());
						new TempPotionEffect(this.entity, neweffect, System.currentTimeMillis() + effect.getDuration() * tick);
						return;
					} else {
						this.entity.removePotionEffect(peffect.getType());
						this.entity.addPotionEffect(effect);
						return;
					}

				}
			}
		}
		this.entity.addPotionEffect(effect);
	}

	private boolean progress() {
		List<Integer> toRemove = new LinkedList<Integer>();
		List<Integer> temp = new LinkedList<Integer>(this.infos.keySet());
		for (int id : temp) {
			PotionInfo info = this.infos.get(id);
			if (info.getTime() < System.currentTimeMillis()) {
				addEffect(info.getEffect());
				toRemove.add(id);
			}
		}
		for (int id : toRemove) {
			this.infos.remove(id);
		}

		if (this.infos.isEmpty() && instances.containsKey(this.entity)) {
			return false;
		}

		return true;
	}

	public static void progressAll() {
		List<TempPotionEffect> toRemove = new LinkedList<TempPotionEffect>();
		for (TempPotionEffect effect : instances.values()) {
			boolean keep = effect.progress();
			if (!keep) {
				toRemove.add(effect);
			}
		}
		for (TempPotionEffect effect : toRemove) {
			effect.remove();
		}
	}

	private void remove() {
		instances.remove(this.entity);
	}

}
