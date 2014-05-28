package net.avatarrealms.minecraft.bending.data;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.BendingPlayerData;

import org.bukkit.plugin.java.JavaPlugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BendingPlayers {
	private Map<String, BendingPlayerData> bendingPlayers;
	private File bendingPlayersFile = null;
	private File dataFolder;

	public BendingPlayers(File file) {
		dataFolder = file;
		load();
	}

	public BendingPlayerData get(String s) {
		if (bendingPlayers != null) {
			return bendingPlayers.get(s);
		}
		return null;
	}

	public void setPlayer(String playername, BendingPlayer player) {
		if (bendingPlayers != null) {
			bendingPlayers.put(playername, player.serialize());
			this.save();
		}
		return;
	}

	public BendingPlayer getBendingPlayer(String playername) {
		if (bendingPlayers != null) {
			if (bendingPlayers.containsKey(playername)) {
				return BendingPlayer.deserialize(bendingPlayers.get(playername));
			}
		}
		return null;
	}

	public Set<String> getSavedPlayers() {
		Set<String> result = new HashSet<String>();
		if (bendingPlayers != null) {
			result = bendingPlayers.keySet();
		}
		return result;
	}

	public void reload() {
		if (bendingPlayersFile == null) {
			bendingPlayersFile = new File(dataFolder, "benders.json");
		}
		try {
			if(!bendingPlayersFile.exists()) {
				bendingPlayersFile.createNewFile();
				FileWriter content = new FileWriter(bendingPlayersFile);
				content.write("{}");
				content.close();
			}
			bendingPlayers = new HashMap<String, BendingPlayerData>();
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(bendingPlayersFile);
			Iterator<Entry<String, JsonNode>> it = root.fields();
			while(it.hasNext()) {
				Entry<String, JsonNode> entry = it.next();
				BendingPlayerData data = mapper.readValue(entry.getValue().traverse(), BendingPlayerData.class);
				bendingPlayers.put(entry.getKey(), data);
			}
		} catch(Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"Could not load config from " + bendingPlayersFile, e);
			bendingPlayers = new HashMap<String, BendingPlayerData>();
		}
	}

	private void load() {
		if (bendingPlayers == null) {
			reload();
		}
	}

	public void save() {
		if (bendingPlayers == null || bendingPlayersFile == null) {
			return;
		}
		if (bendingPlayersFile == null) {
			bendingPlayersFile = new File(dataFolder, "benders.json");
		}
		try {
			if(!bendingPlayersFile.exists()) {
				bendingPlayersFile.createNewFile();
			}
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(bendingPlayersFile, bendingPlayers);
		} catch(Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"Could not save config from " + bendingPlayersFile, e);
		}
	}

	public void close() {
		save();
		bendingPlayers = null;
		bendingPlayersFile = null;
	}

}
