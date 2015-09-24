package net.avatar.realms.spigot.bending.db.impl;

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
import com.google.gson.Gson;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingPlayerData;
import net.avatar.realms.spigot.bending.db.IBendingDB;

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
			Bending.log.log(Level.SEVERE, "Could not load file " + file, e);
			try {
				File save = getFile(id, 0);
				int i = 1;
				while (save.exists()) {
					save = getFile(id, i);
					i++;
				}
				FileUtils.moveFile(file, save);
			} catch (IOException e1) {

			}
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					
				}
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
			Bending.log.warning("Tried to save player "+id+" but not loaded.");
			return;
		}
		FileWriter writer = null;
		try {
			File file = getFile(id);
			writer = new FileWriter(file, false);
			mapper.toJson(players.get(id).serialize(), writer);
			writer.close();
		} catch (Exception e) {
			Bending.log.log(Level.SEVERE, "Could not save player " + id, e);
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					
				}
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
