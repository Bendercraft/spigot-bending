package net.avatarrealms.minecraft.bending.abilities.water;

import net.avatarrealms.minecraft.bending.model.IAbility;

public class IceSwipe implements IAbility{
	
	private IAbility parent;
	
	//TODO : Not to forget to check for the protected region
	//TODO : Almost the same that the airswipe but with a different skin

	
	@Override
	public int getBaseExperience() {
		return 4;
	}

	@Override
	public IAbility getParent() {
		return parent;
	}
}
