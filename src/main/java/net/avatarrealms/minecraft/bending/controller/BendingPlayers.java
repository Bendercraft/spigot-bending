package net.avatarrealms.minecraft.bending.controller;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.data.BendingPlayerData;
import net.avatarrealms.minecraft.bending.model.data.BendingPlayerOldData;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BendingPlayers {
	public static String FILE_NAME = "benders.json";
	
	private Map<UUID, BendingPlayerData> bendingPlayers;
	private File bendingPlayersFile = null;
	private File dataFolder;

	public BendingPlayers(File file) {
		dataFolder = file;
		load();
	}
	
	public BendingPlayerData get(UUID id) {
		if (bendingPlayers != null) {
			return bendingPlayers.get(id);
		}
		return null;
	}

	public void setPlayer(UUID playerID, BendingPlayer player) {
		if (bendingPlayers != null) {
			bendingPlayers.put(playerID, player.serialize());
			this.save();
		}
		return;
	}

	public BendingPlayer getBendingPlayer(UUID playerID) {
		if (bendingPlayers != null) {
			if (bendingPlayers.containsKey(playerID)) {
				return BendingPlayer.deserialize(bendingPlayers.get(playerID));
			}
		}
		return null;
	}

	public Set<UUID> getSavedPlayers() {
		Set<UUID> result = new HashSet<UUID>();
		if (bendingPlayers != null) {
			result = bendingPlayers.keySet();
		}
		return result;
	}

	
	public void reload() {
		bendingPlayers = new HashMap<UUID, BendingPlayerData>();
		//First load old one -- THIS FRAGMENT WILL BE REMOVED after 1.8
		File bendingPlayersOldFile = new File(dataFolder, FILE_NAME+".old");
		try {
			//If this file does not exist, does not matter, it is just a fallback
			if(bendingPlayersOldFile.exists()) {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode root = mapper.readTree(bendingPlayersOldFile);
				Iterator<Entry<String, JsonNode>> it = root.fields();
				while(it.hasNext()) {
					Entry<String, JsonNode> entry = it.next();
					BendingPlayerOldData oldData = mapper.readValue(entry.getValue().traverse(), BendingPlayerOldData.class);
					//Transform player name into UUID
					String playerName = entry.getKey();
					UUID uuid = Bukkit.getServer().getPlayer(playerName).getUniqueId();
					//Transformat OLD DATA into new one
					BendingPlayerData data = new BendingPlayerData();
					data.setBendings(oldData.getBendings());
					data.setBendToItem(oldData.isBendToItem());
					data.setItemAbilities(oldData.getItemAbilities());
					data.setLanguage(oldData.getLanguage());
					data.setLastTime(oldData.getLastTime());
					data.setPermaRemoved(oldData.isPermaRemoved());
					data.setPlayer(uuid);
					data.setSlotAbilities(oldData.getSlotAbilities());
					
					bendingPlayers.put(uuid, data);
				}
				
				//Since we have loaded and translated old file, no longer necessary to keep this one
				bendingPlayersOldFile.renameTo(new File(dataFolder, FILE_NAME+".old.used"));
			}
		} catch(Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"Could not load config from " + bendingPlayersOldFile, e);
		}
		
		
		
		
		//Then, look at file with UUID (most recent one)
		if (bendingPlayersFile == null) {
			bendingPlayersFile = new File(dataFolder, FILE_NAME);
		}
		try {
			if(!bendingPlayersFile.exists()) {
				bendingPlayersFile.createNewFile();
				FileWriter content = new FileWriter(bendingPlayersFile);
				content.write("{}");
				content.close();
			}
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(bendingPlayersFile);
			Iterator<Entry<String, JsonNode>> it = root.fields();
			while(it.hasNext()) {
				Entry<String, JsonNode> entry = it.next();
				BendingPlayerData data = mapper.readValue(entry.getValue().traverse(), BendingPlayerData.class);
				UUID uuid = UUID.fromString(entry.getKey());
				bendingPlayers.put(uuid, data);
			}
		} catch(Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"Could not load config from " + bendingPlayersFile, e);
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
			bendingPlayersFile = new File(dataFolder, FILE_NAME);
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
