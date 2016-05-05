package net.bendercraft.spigot.bending.db.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.abilities.BendingPlayerData;
import net.bendercraft.spigot.bending.db.IBendingDB;

public class FlatFileDB implements IBendingDB {
	private static String DIR_NAME = "benders";

	private Gson mapper = new Gson();
	private Map<UUID, BendingPlayer> players = new HashMap<UUID, BendingPlayer>();
	private File dataFolder;

	@Override
	public void init(Bending plugin) {
		this.dataFolder = new File(plugin.getDataFolder(), DIR_NAME);
		this.dataFolder.mkdirs();
	}

	@Override
	public BendingPlayer get(UUID id) {
		//Already loaded ? Return that version !
		if (this.players.containsKey(id)) {
			return this.players.get(id);
		}
		
		
		File file = getFile(id);
		//File does not exist ? New player !
		if(!file.exists()) {
			BendingPlayer result = new BendingPlayer(id);
			this.set(id, result);
			return result;
		}
		
		//File exist ? Load it !
		FileReader reader = null;
		BendingPlayer result = null;
		try {
			reader = new FileReader(file);
			BendingPlayerData temp = mapper.fromJson(reader, BendingPlayerData.class);
			result = new BendingPlayer(temp);
			this.set(id, result);
		} catch (FileNotFoundException e) {
			Bending.getInstance().getLogger().log(Level.SEVERE, "Could not load file " + file, e);
			File save = getFile(id, 0);
			try {
				int i = 1;
				while (save.exists()) {
					save = getFile(id, i);
					i++;
				}
				FileUtils.moveFile(file, save);
			} catch (IOException e1) {
				Bending.getInstance().getLogger().log(Level.SEVERE, "Could not move file " + file + " to "+save, e1);
			}
		} catch(Exception e) {
			Bending.getInstance().getLogger().log(Level.SEVERE, "Could not load file SEVERE " + file, e);
		} finally {
			if(reader != null) {
				IOUtils.closeQuietly(reader);
			}
		}
		
		return result;
	}

	@Override
	public void remove(UUID playerID) {
		this.players.remove(playerID);
		File file = getFile(playerID);
		if(file.exists()) {
			file.delete();
		}
	}

	@Override
	public void set(UUID playerID, BendingPlayer player) {
		this.players.put(playerID, player);
		this.save(playerID);
	}

	@Override
	public void save() {
		for(UUID id : players.keySet()) {
			save(id);
		}
	}

	@Override
	public void save(UUID id) {
		if(!players.containsKey(id)) {
			Bending.getInstance().getLogger().warning("Tried to save player "+id+" but not loaded.");
			return;
		}
		FileWriter writer = null;
		try {
			File file = getFile(id);
			writer = new FileWriter(file, false);
			mapper.toJson(players.get(id).serialize(), writer);
			writer.close();
		} catch (Exception e) {
			Bending.getInstance().getLogger().log(Level.SEVERE, "Could not save player " + id, e);
		} finally {
			if(writer != null) {
				IOUtils.closeQuietly(writer);
			}
		}
	}

	@Override
	public void lease(UUID player) {
		this.get(player); // Will do everything
	}

	@Override
	public void release(UUID player) {
		this.save(player);
		this.players.remove(player);
	}
	
	private File getFile(UUID id) {
		return new File(dataFolder, id.toString()+".json");
	}
	
	private File getFile(UUID id, int save) {
		return new File(dataFolder, id.toString()+".json.save."+save);
	}

}
