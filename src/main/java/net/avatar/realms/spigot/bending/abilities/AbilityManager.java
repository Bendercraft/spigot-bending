package net.avatar.realms.spigot.bending.abilities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class AbilityManager {
	
	private static AbilityManager manager =  null;

	private Map<Abilities, Map<Object, Ability>> abilities;
	
	public static AbilityManager getManager() {
		if (manager == null) {
			manager = new AbilityManager();
		}
		return manager;
	}
	
	private AbilityManager() {
		abilities = new HashMap<Abilities, Map<Object, Ability>>();
	}
	
	public void progressAllAbilities() {
		List<Ability> toRemove = new LinkedList<Ability>();
		for (Abilities key : abilities.keySet()) {
			for (Ability ability : abilities.get(key).values()) {
				boolean canKeep = ability.progress();
				if (!canKeep) {
					toRemove.add(ability);
				}
			}
		}
		
		for (Ability ability : toRemove) {
			ability.stop();
			ability.remove();
		}
	}
	
	public void stopAllAbilities() {
		for (Map<Object, Ability> instances : abilities.values()) {
			for (Ability ability : instances.values()) {
				ability.stop();
			}
		}
		
		clearAllAbilities();
	}
	
	private void clearAllAbilities() {
		for (Map<Object, Ability> instances : abilities.values()) {
			instances.clear();
		}
	}
	
	public void addInstance(Ability instance) {
		Map<Object, Ability> map = abilities.get(instance.getAbilityType());
		if (map == null) {
			map = new HashMap<Object, Ability>();
			abilities.put(instance.getAbilityType(), map);
		}
		map.put(instance.getIdentifier(), instance);
	}
	
	public Map<Object, Ability> getInstances(Abilities type) {
		return abilities.get(type);
	}
}
