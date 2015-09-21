package net.avatar.realms.spigot.bending.abilities;

import java.lang.reflect.Constructor;

import net.avatar.realms.spigot.bending.abilities.base.IBendingAbility;

public class RegisteredAbility {
	private Class<? extends IBendingAbility> ability;
	private String name;
	private BendingElement element;
	private BendingAffinity specialization;
	private Constructor<? extends IBendingAbility> constructor;

	public RegisteredAbility(String name, Class<? extends IBendingAbility> ability, BendingElement element, Constructor<? extends IBendingAbility> constructor) {
		this(name, ability, element, null, constructor);
	}

	public RegisteredAbility(String name, Class<? extends IBendingAbility> ability, BendingElement element, BendingAffinity specialization, Constructor<? extends IBendingAbility> constructor) {
		this.name = name;
		this.ability = ability;
		this.element = element;
		this.specialization = specialization;
		this.constructor = constructor;
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
		return "bending.ability." + this.name.toLowerCase();
	}

	public String getConfigPath() {
		return this.element.name().toLowerCase() + "." + this.name.toLowerCase().replaceAll(" ", "_");
	}

	public Constructor<? extends IBendingAbility> getConstructor() {
		return constructor;
	}
}
