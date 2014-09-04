package net.avatarrealms.minecraft.bending.controller;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayerData;

public class TempBackup {
	public static final String FILE_NAME = "tempBackup.json";

	private Map<UUID, BendingPlayerData> backupPlayers;

	private File backupFile = null;
	private File dataFolder;

	public TempBackup(File file) {
		dataFolder = file;
		load();
	}

	public BendingPlayerData get(UUID id) {
		if (backupPlayers != null) {
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
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(backupFile);
			Iterator<Entry<String, JsonNode>> it = root.fields();
			while (it.hasNext()) {
				Entry<String, JsonNode> entry = it.next();
				BendingPlayerData data = mapper.readValue(entry.getValue()
						.traverse(), BendingPlayerData.class);
				UUID uuid = UUID.fromString(entry.getKey());
				backupPlayers.put(uuid, data);
			}
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
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(backupFile, backupPlayers);
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
