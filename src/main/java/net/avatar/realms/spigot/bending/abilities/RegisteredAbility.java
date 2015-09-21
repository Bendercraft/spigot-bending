package net.avatar.realms.spigot.bending.abilities;

import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;

public class RegisteredAbility {
	private Class<? extends IBendingAbility> ability;
	private String name;
	private BendingElement element;
	private BendingAffinity specialization;

	public RegisteredAbility(String name, Class<? extends IBendingAbility> ability, BendingElement element) {
		this(name, ability, element, null);
	}
	public RegisteredAbility(String name, Class<? extends IBendingAbility> ability, BendingElement element, BendingAffinity specialization) {
		this.name = name;
		this.ability = ability;
		this.element = element;
		this.specialization = specialization;
	}

	public Class<? extends IBendingAbility> getAbility() {
		return this.ability;
	}

	public String getName() {
		return this.name;
	}

	public BendingElement getElement() {
		return this.element;
	}

	public BendingAffinity getSpecialization() {
		return this.specialization;
	}
	public String getPermission() {
		return "bending.ability."+this.name.toLowerCase();
	}

	public String getConfigPath() {
		return this.element.name().toLowerCase() +"." + this.name.toLowerCase().replaceAll(" ", "_");
	}
}
