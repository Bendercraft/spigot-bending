package net.avatar.realms.spigot.bending.abilities;

public class RegisteredAbility {
	private Class<? extends Ability> ability;
	private String name;
	private BendingType element;
	private BendingSpecializationType specialization;
	
	public RegisteredAbility(String name, Class<? extends Ability> ability, BendingType element) {
		this(name, ability, element, null);
	}
	public RegisteredAbility(String name, Class<? extends Ability> ability, BendingType element, BendingSpecializationType specialization) {
		this.name = name;
		this.ability = ability;
		this.element = element;
		this.specialization = specialization;
	}

	public Class<? extends Ability> getAbility() {
		return ability;
	}

	public String getName() {
		return name;
	}
	
	public BendingType getElement() {
		return element;
	}
	
	public BendingSpecializationType getSpecialization() {
		return specialization;
	}
	public String getPermission() {
		return "bending.ability."+name.toLowerCase();
	}
	
	public String getConfigPath() {
		return element.name().toLowerCase() +"." + name.toLowerCase().replaceAll(" ", "_");
	}
}
