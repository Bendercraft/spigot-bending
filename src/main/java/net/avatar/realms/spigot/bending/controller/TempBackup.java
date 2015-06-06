package net.avatar.realms.spigot.bending.controller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;

import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingPlayerData;

public class TempBackup {
	public static final String FILE_NAME = "tempBackup.json";
	
	private static Gson mapper = new Gson();

	private Map<UUID, BendingPlayerData> backupPlayers;

	private File backupFile = null;
	private File dataFolder;

	public TempBackup(File file) {
		dataFolder = file;
		load();
	}

	public BendingPlayerData get(UUID id) {
		if (backupPlayers != null && backupPlayers.containsKey(id)) {
			return backupPlayers.get(id);
		}
		return null;
	}

	public void setPlayer(UUID playerID, BendingPlayer player) {
		if (backupPlayers != null) {
			backupPlayers.put(playerID, player.serialize());
			this.save();
		}
		return;
	}

	public void reload() {
		backupPlayers = new HashMap<UUID, BendingPlayerData>();

		if (backupFile == null) {
			backupFile = new File(dataFolder, FILE_NAME);
		}
		try {
			if (!backupFile.exists()) {
				backupFile.createNewFile();
				FileWriter content = new FileWriter(backupFile);
				content.write("{}");
				content.close();
			}
			FileReader reader = new FileReader(backupFile);
			Backups tmp = mapper.fromJson(reader, Backups.class);
			if(tmp == null || tmp.getBackupPlayers() == null) {
				Logger.getLogger(JavaPlugin.class.getName()).log(Level.WARNING,
						"No config data found in " + backupFile);
				return;
			}
			backupPlayers.putAll(tmp.getBackupPlayers());
			reader.close();
		} catch (Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"Could not load config from " + backupFile, e);
		}
	}

	private void load() {
		if (backupPlayers == null) {
			reload();
		}
	}

	public void save() {
		if (backupPlayers == null || backupFile == null) {
			return;
		}
		try {
			if (!backupFile.exists()) {
				backupFile.createNewFile();
			}
			FileWriter writer = new FileWriter(backupFile);
			Backups tmp = new Backups();
			tmp.setBackupPlayers(backupPlayers);
			mapper.toJson(tmp, writer);
			writer.close();
		} catch (Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"Could not save config to " + backupFile, e);
		}
	}

	public void close() {
		save();
		backupPlayers = null;
		backupFile = null;
	}
	
	public void remove (Player pl) {
		backupPlayers.remove(pl.getUniqueId());
		this.save();
	}
	
	private class Backups {
		private Map<UUID, BendingPlayerData> backupPlayers;

		public Map<UUID, BendingPlayerData> getBackupPlayers() {
			return backupPlayers;
		}

		public void setBackupPlayers(Map<UUID, BendingPlayerData> backupPlayers) {
			this.backupPlayers = backupPlayers;
		}
	}
}
