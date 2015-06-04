package net.avatarrealms.minecraft.bending.controller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;

import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayerData;

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

	@SuppressWarnings("unchecked")
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
			List<String> lines = IOUtils.readLines(reader);
			
			if(lines.isEmpty()) {
				return;
			}
			backupPlayers = mapper.fromJson(lines.get(0), backupPlayers.getClass());
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
			String json = mapper.toJson(backupPlayers);
			FileWriter writer = new FileWriter(backupFile);
			writer.write(json);
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

}
