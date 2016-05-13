package net.bendercraft.spigot.bending.db;

import java.util.UUID;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;

public interface IBendingDB {
	/**
	 * Init
	 * 
	 * @param plugin
	 */
	public void init(Bending plugin);

	/**
	 * Retrieve or create a BendingPlayer using UUID as unique id
	 * 
	 * @param id
	 * @return
	 */
	public BendingPlayer get(UUID id);

	/**
	 * Permanently remove a bender from known database
	 * 
	 * @param id
	 */
	public void remove(UUID id);

	/**
	 * Update a player, update process is completely a discretion of
	 * implementation
	 * 
	 * @param id
	 * @param player
	 */
	public void set(UUID id, BendingPlayer player);

	/**
	 * Save everything
	 */
	public void save();

	/**
	 * Save a given player, some implementation might just call save all if they
	 * seem so
	 * 
	 * @param id
	 */
	public void save(UUID id);
	
	/**
	 * What to do when a player connect
	 * 
	 * @param player
	 */
	public void lease(UUID player);
	
	/**
	 * What to do when a player disconnect
	 * 
	 * @param player
	 */
	public void release(UUID player);
}
