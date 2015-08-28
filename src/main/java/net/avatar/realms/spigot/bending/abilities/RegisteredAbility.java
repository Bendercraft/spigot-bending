package net.avatar.realms.spigot.bending.abilities;

import net.avatar.realms.spigot.bending.abilities.base.IAbility;

public class RegisteredAbility {
	private Class<? extends IAbility> ability;
	private String name;
	private BendingType element;
	private BendingSpecializationType specialization;

	public RegisteredAbility(String name, Class<? extends IAbility> ability, BendingType element) {
		this(name, ability, element, null);
	}
	public RegisteredAbility(String name, Class<? extends IAbility> ability, BendingType element, BendingSpecializationType specialization) {
		this.name = name;
		this.ability = ability;
		this.element = element;
		this.specialization = specialization;
	}

	public Class<? extends IAbility> getAbility() {
		return this.ability;
	}

	public String getName() {
		return this.name;
	}

	public BendingType getElement() {
		return this.element;
	}

	public BendingSpecializationType getSpecialization() {
		return this.specialization;
	}
	public String getPermission() {
		return "bending.ability."+this.name.toLowerCase();
	}

	public String getConfigPath() {
		return this.element.name().toLowerCase() +"." + this.name.toLowerCase().replaceAll(" ", "_");
	}
}
