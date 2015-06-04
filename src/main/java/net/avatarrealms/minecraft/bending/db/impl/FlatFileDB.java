package net.avatarrealms.minecraft.bending.db.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayerData;
import net.avatarrealms.minecraft.bending.db.IBendingDB;

import org.apache.commons.io.IOUtils;
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
			String json = mapper.toJson(datas);
			FileWriter writer = new FileWriter(bendingPlayersFile);
			writer.write(json);
			writer.close();
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
	
	@SuppressWarnings("unchecked")
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
			
			FileReader reader = new FileReader(bendingPlayersFile);
			List<String> lines = IOUtils.readLines(reader);
			
			if(lines.isEmpty()) {
				return;
			}
			datas = mapper.fromJson(lines.get(0), datas.getClass());
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
