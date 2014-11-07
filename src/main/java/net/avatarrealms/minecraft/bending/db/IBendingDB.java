package net.avatarrealms.minecraft.bending.db;

import java.util.UUID;

import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;

public interface IBendingDB {
	/**
	 * Retrieve or create a BendingPlayer using UUID as unique id
	 * @param id
	 * @return
	 */
	public BendingPlayer get(UUID id);
	
	/**
	 * Permanently remove a bender from known database
	 * @param id
	 */
	public void remove(UUID id);
	
	/**
	 * Update a player, update process is completly a discretion of implementation
	 * @param id
	 * @param player
	 */
	public void set(UUID id, BendingPlayer player);
	
	/**
	 * Save everything
	 */
	public void save();
	/**
	 * Save a given player, some implementation might just call save all if they seem so
	 * @param id
	 */
	public void save(UUID id);
}
