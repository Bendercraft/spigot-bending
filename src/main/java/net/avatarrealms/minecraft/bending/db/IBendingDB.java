package net.avatarrealms.minecraft.bending.db;

import java.util.UUID;

import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;

public interface IBendingDB {
	public BendingPlayer get(UUID id);
	public void remove(UUID id);
	public void set(UUID id, BendingPlayer player);
	
	public void save();
	public void save(UUID id);
}
