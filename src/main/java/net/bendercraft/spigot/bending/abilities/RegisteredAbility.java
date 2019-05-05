package net.bendercraft.spigot.bending.abilities;

import java.lang.reflect.Constructor;
import java.util.Objects;


public class RegisteredAbility {

	private final Class<? extends BendingAbility> ability;
	private final String name;
	private final BendingElement element;
	private final BendingAffinity affinity;
	private final Constructor<? extends BendingAbility> constructor;
	private final boolean shift;
	private final boolean passive;
	private final boolean useWithTools;

	public RegisteredAbility(String name, Class<? extends BendingAbility> ability, BendingElement element, boolean shift, boolean passive, boolean tools, Constructor<? extends BendingAbility> constructor) {
		this(name, ability, element, null, shift, passive, tools,  constructor);
	}

	public RegisteredAbility(String name, Class<? extends BendingAbility> ability, BendingElement element, BendingAffinity affinity, boolean shift, boolean passive, boolean tools, Constructor<? extends BendingAbility> constructor) {
		this.name = name;
		this.ability = ability;
		this.element = element;
		this.affinity = affinity;
		this.constructor = constructor;
		this.shift = shift;
		this.passive = passive;
		this.useWithTools = tools;
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

	public String getConfigPath() {
		return this.element.name().toLowerCase() + "." + this.name.toLowerCase().replaceAll(" ", "_");
	}

	public Constructor<? extends BendingAbility> getConstructor() {
		return constructor;
	}

	public boolean isShift() {
		return shift;
	}

	public boolean isPassive() {
		return passive;
	}

	public boolean canBeUsedWithTools() {
		return useWithTools;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }
		RegisteredAbility that = (RegisteredAbility) o;
		return ability.equals(that.ability) && name.equals(that.name) && element == that.element && affinity == that.affinity;
	}

	@Override
	public int hashCode() {
		return Objects.hash(ability);
	}
}
