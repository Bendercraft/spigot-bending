package net.avatar.realms.spigot.bending.abilities;

import java.lang.reflect.Constructor;

public class RegisteredAbility {
	private Class<? extends BendingAbility> ability;
	private String name;
	private BendingElement element;
	private BendingAffinity specialization;
	private Constructor<? extends BendingAbility> constructor;

	public RegisteredAbility(String name, Class<? extends BendingAbility> ability, BendingElement element, Constructor<? extends BendingAbility> constructor) {
		this(name, ability, element, null, constructor);
	}

	public RegisteredAbility(String name, Class<? extends BendingAbility> ability, BendingElement element, BendingAffinity specialization, Constructor<? extends BendingAbility> constructor) {
		this.name = name;
		this.ability = ability;
		this.element = element;
		this.specialization = specialization;
		this.constructor = constructor;
	}

	public Class<? extends BendingAbility> getAbility() {
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
		return "bending.ability." + this.name.toLowerCase();
	}

	public String getConfigPath() {
		return this.element.name().toLowerCase() + "." + this.name.toLowerCase().replaceAll(" ", "_");
	}

	public Constructor<? extends BendingAbility> getConstructor() {
		return constructor;
	}
}
