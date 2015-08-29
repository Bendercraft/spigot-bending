package net.avatar.realms.spigot.bending.db.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingPlayerData;
import net.avatar.realms.spigot.bending.db.IBendingDB;

import org.apache.commons.io.FileUtils;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;

public class FlatFileDB implements IBendingDB {
	private Map<UUID, BendingPlayer> players = new HashMap<UUID, BendingPlayer>();
	
	private static String FILE_NAME = "benders.json";
	
	private Gson mapper = new Gson();
	private File bendingPlayersFile = null;
	private Map<UUID, BendingPlayerData> datas;
	
	private File dataFolder;
	
	@Override
	public void init (Bending plugin) {
		dataFolder = plugin.getDataFolder();
		load();
	}
	
	@Override
	public BendingPlayer get (UUID id) {
		//On flat file, don't bother keep RAM value syncing with file value
		if (!players.containsKey(id)) {
			if (datas.containsKey(id)) {
				//old player but not charged
				players.put(id, new BendingPlayer(datas.get(id)));
			}
			else {
				//New player !
				this.set(id, new BendingPlayer(id));
			}
		}
		return players.get(id);
	}
	
	@Override
	public void remove (UUID playerID) {
		players.remove(playerID);
		datas.remove(playerID);
		this.save();
	}
	
	@Override
	public void set (UUID playerID, BendingPlayer player) {
		datas.put(playerID, player.serialize());
		players.put(playerID, player);
		this.save(playerID);
	}
	
	@Override
	public void save () {
		try {
			FileWriter writer = new FileWriter(bendingPlayersFile, false);
			BendingPlayerDatas temp = new BendingPlayerDatas();
			temp.setDatas(datas);
			mapper.toJson(temp, writer);
			writer.close();
		}
		catch (Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save db to " + bendingPlayersFile, e);
		}
	}
	
	@Override
	public void save (UUID id) {
		//Flat file, don't care of perf
		this.save();
	}
	
	public void reload () {
		datas = new HashMap<UUID, BendingPlayerData>();
		
		if (bendingPlayersFile == null) {
			bendingPlayersFile = new File(dataFolder, FILE_NAME);
		}
		try {
			if (!bendingPlayersFile.exists()) {
				bendingPlayersFile.createNewFile();
				FileWriter content = new FileWriter(bendingPlayersFile);
				content.write("{}");
				content.close();
			}
			
			FileReader reader = new FileReader(bendingPlayersFile);
			BendingPlayerDatas temp = mapper.fromJson(reader, BendingPlayerDatas.class);
			if (temp != null) {
				datas.putAll(temp.getDatas());
			}
			else {
				Bending.plugin.getLogger().warning("Bending Player Data was NULL !");
			}
			
			reader.close();
		}
		catch (Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not load config from " + bendingPlayersFile, e);
			try {
				File save = new File(dataFolder, FILE_NAME + ".save.0");
				int i = 1;
				while (save.exists()) {
					save = new File(dataFolder, FILE_NAME + ".save." + i);
					i++;
				}
				FileUtils.moveFile(bendingPlayersFile, save);
			}
			catch (IOException e1) {
			
			}
		}
	}
	
	@Override
	public Map<UUID, BendingPlayerData> dump () {
		return datas;
	}
	
	private void load () {
		if (datas == null) {
			reload();
		}
	}
	
	@Override
	public void clear () {
		players.clear();
		datas.clear();
		if (bendingPlayersFile != null) {
			bendingPlayersFile.delete();
		}
	}
	
	private class BendingPlayerDatas {
		private Map<UUID, BendingPlayerData> datas;
		
		public Map<UUID, BendingPlayerData> getDatas () {
			return datas;
		}
		
		public void setDatas (Map<UUID, BendingPlayerData> datas) {
			this.datas = datas;
		}
	}
	
}
