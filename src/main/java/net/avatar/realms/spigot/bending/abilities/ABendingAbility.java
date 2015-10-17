package net.avatar.realms.spigot.bending.abilities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ABendingAbility {
	public String name();

	public BendingAbilities bind();

	public BendingElement element();

	public BendingAffinity affinity() default BendingAffinity.None;
}
