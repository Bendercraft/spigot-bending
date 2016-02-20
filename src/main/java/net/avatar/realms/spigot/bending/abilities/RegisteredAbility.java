package net.avatar.realms.spigot.bending.abilities;

import java.lang.reflect.Constructor;

public class RegisteredAbility {
	private final Class<? extends BendingAbility> ability;
	private final String name;
	private final BendingElement element;
	private final BendingAffinity affinity;
	private final Constructor<? extends BendingAbility> constructor;
	private final boolean shift;

	public RegisteredAbility(String name, Class<? extends BendingAbility> ability, BendingElement element, boolean shift, Constructor<? extends BendingAbility> constructor) {
		this(name, ability, element, null, shift, constructor);
	}

	public RegisteredAbility(String name, Class<? extends BendingAbility> ability, BendingElement element, BendingAffinity affinity, boolean shift, Constructor<? extends BendingAbility> constructor) {
		this.name = name;
		this.ability = ability;
		this.element = element;
		this.affinity = affinity;
		this.constructor = constructor;
		this.shift = shift;
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

	public BendingAffinity getAffinity() {
		return this.affinity;
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

	public boolean isShift() {
		return shift;
	}
}
