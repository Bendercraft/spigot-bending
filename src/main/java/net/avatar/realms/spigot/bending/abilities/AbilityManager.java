package net.avatar.realms.spigot.bending.abilities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.chi.PoisonnedDart;

public class AbilityManager {
	
	private static AbilityManager manager =  null;

	private Map<Player, PoisonnedDart> poisonnedDarts;
	
	public static AbilityManager getManager() {
		if (manager == null) {
			manager = new AbilityManager();
		}
		return manager;
	}
	
	private AbilityManager() {
		poisonnedDarts = new HashMap<Player, PoisonnedDart>();
	}
	
	public void progressAllAbilities() {
		List<Ability> abilities = new LinkedList<Ability>();
		
		compileAbilities(abilities);
		
		for (Ability ability : abilities) {
			boolean canKeep = ability.progress();
			if (!canKeep) {
				ability.stop();
				ability.remove();
			}
		}
	}
	
	public void stopAllAbilities() {
		List<Ability> abilities = new LinkedList<Ability>();
		
		compileAbilities(abilities);
		
		for (Ability ability : abilities) {
			ability.stop();
		}
		
		clearAllAbilities();
	}
	
	private void clearAllAbilities() {
		poisonnedDarts.clear();
	}
	
	private void compileAbilities(List<Ability> abilities) {
		abilities.addAll(poisonnedDarts.values());
	}
	
	public void addInstance(PoisonnedDart instance) {
		poisonnedDarts.put(instance.getPlayer(), instance);
	}

	public Map<Player, PoisonnedDart> getPoisonnedDarts() {
		return poisonnedDarts;
	}
}
