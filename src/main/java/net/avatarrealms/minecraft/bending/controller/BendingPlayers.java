package net.avatarrealms.minecraft.bending.controller;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.model.data.BendingPlayerData;
import net.avatarrealms.minecraft.bending.model.data.BendingPlayerOldData;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BendingPlayers {
	public static String FILE_NAME = "benders.json";
	
	@Deprecated
	private File bendingPlayersOldFile = null;
	@Deprecated
	private Map<String, BendingPlayerOldData> oldBendingPlayers;
	
	private File bendingPlayersFile = null;
	private Map<UUID, BendingPlayerData> bendingPlayers;
	
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

	public BendingPlayer getBendingPlayer(Player player) {
		if(bendingPlayers != null) {
			if(bendingPlayers.containsKey(player.getUniqueId())) {
				//OK nice, this player has been already converted
				return BendingPlayer.deserialize(bendingPlayers.get(player.getUniqueId()));
			} else {
				//Try to find it from old save file
				if(oldBendingPlayers != null) {
					if(this.convert(player)) {
						//OK nice, this player has been just converted
						return BendingPlayer.deserialize(bendingPlayers.get(player.getUniqueId()));
					}
				}
			}
		}
		return null;
	}
	
	@Deprecated
	public boolean convert(Player player) {
		try {
			if(oldBendingPlayers != null) {
				if(oldBendingPlayers.containsKey(player.getName())) {
					BendingPlayerOldData oldData = oldBendingPlayers.get(player.getName());
					
					UUID uuid = player.getUniqueId();
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
					
					//Add new freshly converted
					bendingPlayers.put(uuid, data);
					//Remove old save
					oldBendingPlayers.remove(player.getName());
					
					//Translation is done, so save it !
					this.save();
					
					Bending.plugin.getLogger().info("Player: "+player.getName()+" with UUID : "+uuid+" was converted from old file to new one");
					return true;
				}
			}
		} catch(Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"Could not load config from " + bendingPlayersOldFile, e);
		}
		return false;
	}

	
	public void reload() {
		this.reloadOld();
		this.reloadNew();
	}
	
	public void reloadNew() {
		bendingPlayers = new HashMap<UUID, BendingPlayerData>();
		
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
	
	@Deprecated
	public void reloadOld() {
		oldBendingPlayers = new HashMap<String, BendingPlayerOldData>();
		
		if (bendingPlayersOldFile == null) {
			bendingPlayersOldFile = new File(dataFolder, FILE_NAME+".old");
		}
		try {
			if(bendingPlayersOldFile.exists()) {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode root = mapper.readTree(bendingPlayersOldFile);
				Iterator<Entry<String, JsonNode>> it = root.fields();
				while(it.hasNext()) {
					Entry<String, JsonNode> entry = it.next();
					BendingPlayerOldData data = mapper.readValue(entry.getValue().traverse(), BendingPlayerOldData.class);
					oldBendingPlayers.put(entry.getKey(), data);
				}
			}
		} catch(Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"Could not load config from " + bendingPlayersFile, e);
		}
	}

	private void load() {
		if (bendingPlayers == null || oldBendingPlayers == null) {
			reload();
		}
	}

	public void save() {
		this.saveOld();
		this.saveNew();
	}
	
	private void saveNew() {
		if (bendingPlayers == null || bendingPlayersFile == null) {
			return;
		}
		try {
			if(!bendingPlayersFile.exists()) {
				bendingPlayersFile.createNewFile();
			}
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(bendingPlayersFile, bendingPlayers);
		} catch(Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"Could not save config to " + bendingPlayersFile, e);
		}
	}
	
	@Deprecated
	private void saveOld() {
		if (oldBendingPlayers == null || bendingPlayersOldFile == null) {
			return;
		}
		try {
			if(!bendingPlayersOldFile.exists()) {
				bendingPlayersOldFile.createNewFile();
			}
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(bendingPlayersOldFile, oldBendingPlayers);
		} catch(Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"Could not save config to " + bendingPlayersOldFile, e);
		}
	}

	public void close() {
		save();
		bendingPlayers = null;
		bendingPlayersFile = null;
		oldBendingPlayers = null;
		bendingPlayersOldFile = null;
	}

}
