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

import org.apache.commons.io.FileUtils;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingPlayerData;
import net.avatar.realms.spigot.bending.db.IBendingDB;

public class FlatFileDB implements IBendingDB {
	private Map<UUID, BendingPlayer> players = new HashMap<UUID, BendingPlayer>();

	private static String FILE_NAME = "benders.json";

	private Gson mapper = new Gson();
	private File bendingPlayersFile = null;
	private Map<UUID, BendingPlayerData> datas;

	private File dataFolder;

	@Override
	public void init(Bending plugin) {
		this.dataFolder = plugin.getDataFolder();
		load();
	}

	@Override
	public BendingPlayer get(UUID id) {
		// On flat file, don't bother keep RAM value syncing with file value
		if (!this.players.containsKey(id)) {
			if (this.datas.containsKey(id)) {
				// old player but not charged
				this.players.put(id, new BendingPlayer(this.datas.get(id)));
			} else {
				// New player !
				this.set(id, new BendingPlayer(id));
			}
		}
		return this.players.get(id);
	}

	@Override
	public void remove(UUID playerID) {
		this.players.remove(playerID);
		this.datas.remove(playerID);
		this.save();
	}

	@Override
	public void set(UUID playerID, BendingPlayer player) {
		this.datas.put(playerID, player.serialize());
		this.players.put(playerID, player);
		this.save(playerID);
	}

	@Override
	public void save() {
		try {
			FileWriter writer = new FileWriter(this.bendingPlayersFile, false);
			BendingPlayerDatas temp = new BendingPlayerDatas();
			temp.setDatas(this.datas);
			this.mapper.toJson(temp, writer);
			writer.close();
		} catch (Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save db to " + this.bendingPlayersFile, e);
		}
	}

	@Override
	public void save(UUID id) {
		// Flat file, don't care of perf
		this.save();
	}

	public void reload() {
		this.datas = new HashMap<UUID, BendingPlayerData>();

		if (this.bendingPlayersFile == null) {
			this.bendingPlayersFile = new File(this.dataFolder, FILE_NAME);
		}
		try {
			if (!this.bendingPlayersFile.exists()) {
				this.bendingPlayersFile.createNewFile();
				FileWriter content = new FileWriter(this.bendingPlayersFile);
				content.write("{}");
				content.close();
			}

			FileReader reader = new FileReader(this.bendingPlayersFile);
			BendingPlayerDatas temp = this.mapper.fromJson(reader, BendingPlayerDatas.class);
			if (temp != null && temp.getDatas() != null) {
				this.datas.putAll(temp.getDatas());
			} else {
				Bending.plugin.getLogger().warning("Bending Player Data was NULL !");
			}

			reader.close();
		} catch (Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not load config from " + this.bendingPlayersFile, e);
			try {
				File save = new File(this.dataFolder, FILE_NAME + ".save.0");
				int i = 1;
				while (save.exists()) {
					save = new File(this.dataFolder, FILE_NAME + ".save." + i);
					i++;
				}
				FileUtils.moveFile(this.bendingPlayersFile, save);
			} catch (IOException e1) {

			}
		}
	}

	@Override
	public Map<UUID, BendingPlayerData> dump() {
		return this.datas;
	}

	private void load() {
		if (this.datas == null) {
			reload();
		}
	}

	@Override
	public void clear() {
		this.players.clear();
		this.datas.clear();
		if (this.bendingPlayersFile != null) {
			this.bendingPlayersFile.delete();
		}
	}

	private class BendingPlayerDatas {
		private Map<UUID, BendingPlayerData> datas;

		public Map<UUID, BendingPlayerData> getDatas() {
			return this.datas;
		}

		public void setDatas(Map<UUID, BendingPlayerData> datas) {
			this.datas = datas;
		}
	}

}
