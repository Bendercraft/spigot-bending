package net.avatar.realms.spigot.bending.abilities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/* Not really used yet */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BendingAbility {
	public String name();

	public BendingAbilities bind();

	public BendingElement element();

	public BendingAffinity affinity() default BendingAffinity.None;
}
