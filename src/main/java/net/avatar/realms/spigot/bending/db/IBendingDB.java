package net.avatar.realms.spigot.bending.db;

import java.util.Map;
import java.util.UUID;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingPlayerData;

public interface IBendingDB {
	public void init(Bending plugin);
	
	/**
	 * DUmp everything this DB knows
	 * @return
	 */
	public Map<UUID, BendingPlayerData> dump();
	
	/**
	 * Clear a DB
	 */
	public void clear();
	
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
