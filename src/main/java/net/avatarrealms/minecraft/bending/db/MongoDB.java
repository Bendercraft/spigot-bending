package net.avatarrealms.minecraft.bending.db;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Material;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayerData;
import net.avatarrealms.minecraft.bending.abilities.BendingSpecializationType;
import net.avatarrealms.minecraft.bending.abilities.BendingType;

public class MongoDB implements IBendingDB {
	private static long MAX_NO_REFRESH = 1000; //ms
	
	private Map<UUID, Long> timestamps = new HashMap<UUID, Long>();
	private Map<UUID, BendingPlayer> players = new HashMap<UUID, BendingPlayer>();
	
	private MongoClient mongoClient;
	private DB db;
	private DBCollection table;

	public MongoDB() {
		try {
			mongoClient = new MongoClient( "localhost" , 27017 );
			db = mongoClient.getDB("minecraft");
			table = db.getCollection("benders");
		} catch (UnknownHostException e) {
			
		}
	}

	@Override
	public BendingPlayer get(UUID id) {
		if(timestamps.containsKey(id)) {
			if(System.currentTimeMillis() - timestamps.get(id) < MAX_NO_REFRESH) {
				if(players.containsKey(id)) {
					return players.get(id);
				} else {
					//Ok wierd here, we have a timestamp but no reference
				}
			}
		}
		
		DBObject obj = new BasicDBObject("player", id);
		DBObject result = table.findOne(obj);
		if(result != null) {
			players.put(id, new BendingPlayer(unmarshal(result)));
		} else {
			//No, not the petrol company
			BendingPlayer bp = new BendingPlayer(id);
			this.set(id, bp);
		}
		timestamps.put(id, System.currentTimeMillis());
		
		// TODO Auto-generated method stub
		return players.get(id);
	}

	@Override
	public void remove(UUID playerID) {
		timestamps.remove(playerID);
		players.remove(playerID);
		DBObject obj = new BasicDBObject("player", playerID);
		table.remove(obj);
	}

	@Override
	public void set(UUID playerID, BendingPlayer player) {
		this.remove(playerID);
		DBObject obj = marshal(player.serialize());
		table.insert(obj);
		timestamps.put(playerID, System.currentTimeMillis());
		players.put(playerID, player);
	}

	@Override
	public void save() {
		List<DBObject> objs = new LinkedList<DBObject>();
		for(BendingPlayer bp : players.values()) {
			objs.add(marshal(bp.serialize()));
		}
		table.drop();
		table.insert(objs);
	}

	@Override
	public void save(UUID id) {
		this.set(id, players.get(id));
	}

	public static DBObject marshal(BendingPlayerData data) {
		DBObject result = new BasicDBObject();
		
		result.put("player", data.getPlayer());
		result.put("language", data.getLanguage());
		result.put("lastTime", data.getLastTime());
		
		BasicDBList bendings = new BasicDBList();
		for(BendingType type : data.getBendings()) {
			bendings.add(type.name());
		}
		result.put("bendings", bendings);
		
		BasicDBObject items = new BasicDBObject();
		for(Entry<Material, Abilities> entry : data.getItemAbilities().entrySet()) {
			items.put(entry.getKey().name(), entry.getValue().name());
		}
		result.put("items", items);
		
		BasicDBObject slots = new BasicDBObject();
		for(Entry<Integer, Abilities> entry : data.getSlotAbilities().entrySet()) {
			slots.put("m"+entry.getKey().toString(), entry.getValue().name());
		}
		result.put("slots", slots);
		
		BasicDBList specializations = new BasicDBList();
		for(BendingSpecializationType spe : data.getSpecialization()) {
			bendings.add(spe.name());
		}
		result.put("specializations", specializations);
		
		return result;
	}
	
	public static BendingPlayerData unmarshal(DBObject obj) {
		BendingPlayerData result = new BendingPlayerData();
		
		result.setPlayer((UUID) obj.get("player"));
		result.setLanguage((String) obj.get("language"));
		result.setLastTime((Long) obj.get("lastTime"));
		
		List<BendingType> bendings = new LinkedList<BendingType>();
		BasicDBList temp0 = (BasicDBList) obj.get("bendings");
		for(Object entry : temp0) {
			bendings.add(BendingType.getType((String) entry));
		}
		result.setBendings(bendings);
		
		Map<Material, Abilities> items = new HashMap<Material, Abilities>();
		BasicDBObject temp1 = (BasicDBObject) obj.get("items");
		for(Entry<String, Object> entry : temp1.entrySet()) {
			items.put(Material.getMaterial(((String)entry.getKey())), Abilities.getAbility((String)entry.getValue()));
		}
		result.setItemAbilities(items);
		
		Map<Integer, Abilities> slots = new HashMap<Integer, Abilities>();
		BasicDBObject temp2 = (BasicDBObject) obj.get("slots");
		for(Entry<String, Object> entry : temp2.entrySet()) {
			slots.put(Integer.parseInt(entry.getKey().substring(1)), Abilities.getAbility((String)entry.getValue()));
		}
		result.setSlotAbilities(slots);
		
		List<BendingSpecializationType> specializations = new LinkedList<BendingSpecializationType>();
		BasicDBList temp3 = (BasicDBList) obj.get("specializations");
		for(Object entry : temp3) {
			specializations.add(BendingSpecializationType.getType((String) entry));
		}
		result.setSpecialization(specializations);
		
		return result;
	}
}
