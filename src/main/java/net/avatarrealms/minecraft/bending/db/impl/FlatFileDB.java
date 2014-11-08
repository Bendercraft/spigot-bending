package net.avatarrealms.minecraft.bending.db.impl;

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
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayerData;
import net.avatarrealms.minecraft.bending.db.IBendingDB;

import org.bukkit.plugin.java.JavaPlugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FlatFileDB implements IBendingDB {
	private Map<UUID, BendingPlayer> players = new HashMap<UUID, BendingPlayer>();
	
	private static String FILE_NAME = "benders.json";
	
	private ObjectMapper mapper = new ObjectMapper();
	private File bendingPlayersFile = null;
	private Map<UUID, BendingPlayerData> datas;
	
	private File dataFolder;
	

	@Override
	public void init(Bending plugin) {
		dataFolder = plugin.getDataFolder();
		load();
	}

	@Override
	public BendingPlayer get(UUID id) {
		//On flat file, don't bother keep RAM value syncing with file value
		if(!players.containsKey(id)) {
			if(datas.containsKey(id)) {
				//old player but not charged
				players.put(id, new BendingPlayer(datas.get(id)));
			} else {
				//New player !
				this.set(id, new BendingPlayer(id));
			}
		}
		return players.get(id);
	}

	@Override
	public void remove(UUID playerID) {
		players.remove(playerID);
		datas.remove(playerID);
		this.save();
	}

	@Override
	public void set(UUID playerID, BendingPlayer player) {
		datas.put(playerID, player.serialize());
		players.put(playerID, player);
		this.save(playerID);
	}

	@Override
	public void save() {
		try {
			mapper.writeValue(bendingPlayersFile, datas);
		} catch (Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"Could not save db to " + bendingPlayersFile, e);
		}
	}

	@Override
	public void save(UUID id) {
		//Flat file, don't care of perf
		this.save();
	}
	
	public void reload() {
		datas = new HashMap<UUID, BendingPlayerData>();
		
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
			
			JsonNode root = mapper.readTree(bendingPlayersFile);
			Iterator<Entry<String, JsonNode>> it = root.fields();
			while(it.hasNext()) {
				Entry<String, JsonNode> entry = it.next();
				BendingPlayerData data = mapper.readValue(entry.getValue().traverse(), BendingPlayerData.class);
				UUID uuid = UUID.fromString(entry.getKey());
				datas.put(uuid, data);
			}
		} catch(Exception e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"Could not load config from " + bendingPlayersFile, e);
		}
	}
	
	@Override
	public Map<UUID, BendingPlayerData> dump() {
		return datas;
	}
	
	private void load() {
		if(datas == null) {
			reload();
		}
	}

	@Override
	public void clear() {
		players.clear();
		datas.clear();
		if(bendingPlayersFile != null) {
			bendingPlayersFile.delete();
		}
	}

}
