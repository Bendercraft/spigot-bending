package net.avatarrealms.minecraft.bending.db.impl;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.bukkit.Material;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.Abilities;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayerData;
import net.avatarrealms.minecraft.bending.abilities.BendingSpecializationType;
import net.avatarrealms.minecraft.bending.abilities.BendingType;
import net.avatarrealms.minecraft.bending.db.IBendingDB;

public class MongoDB implements IBendingDB {
	private static long MAX_NO_REFRESH = 1000; //ms
	private static String ID = "_id";
	
	private Map<UUID, Long> timestamps = new HashMap<UUID, Long>();
	private Map<UUID, BendingPlayer> players = new HashMap<UUID, BendingPlayer>();
	
	private MongoClient mongoClient;
	private DB db;
	private DBCollection table;

	@Override
	public void init(Bending plugin) {
		try {
			mongoClient = new MongoClient("localhost" , 27017);
			db = mongoClient.getDB("minecraft");
			table = db.getCollection("benders");
		} catch (UnknownHostException e) {
			Bending.log.log(Level.SEVERE, "Could not initialize MongoDB", e);
		}
	}
	
	@Override
	public Map<UUID, BendingPlayerData> dump() {
		Map<UUID, BendingPlayerData> result = new HashMap<UUID, BendingPlayerData>();
		
		DBCursor cursor = table.find();
		while(cursor.hasNext()) {
			DBObject obj = cursor.next();
			BendingPlayerData data = unmarshal(obj);
			result.put(data.getPlayer(), data);
		}
		
		return result;
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
		
		DBObject obj = new BasicDBObject();
		obj.put(ID, new ObjectId(id.toString()));
		DBObject result = table.findOne(obj);
		if(result != null) {
			players.put(id, new BendingPlayer(unmarshal(result)));
		} else {
			//No, not the petrol company
			BendingPlayer bp = new BendingPlayer(id);
			this.set(id, bp);
		}
		timestamps.put(id, System.currentTimeMillis());
		
		return players.get(id);
	}

	@Override
	public void remove(UUID id) {
		timestamps.remove(id);
		players.remove(id);
		DBObject obj = new BasicDBObject();
		obj.put(ID, new ObjectId(id.toString()));
		DBObject result = table.findOne(obj);
		if(result != null) {
			table.remove(result);
		}
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
			DBObject obj = marshal(bp.serialize());
			objs.add(obj);
		}
		table.drop();
		table.insert(objs);
	}

	@Override
	public void save(UUID id) {
		this.set(id, players.get(id));
	}

	private static DBObject marshal(BendingPlayerData data) {
		DBObject result = new BasicDBObject();
		
		result.put(ID, new ObjectId(data.getPlayer().toString()));
		
		result.put("player", data.getPlayer().toString());
		result.put("language", data.getLanguage());
		result.put("lastTime", data.getLastTime());
		
		BasicDBList bendings = new BasicDBList();
		for(BendingType type : data.getBendings()) {
			bendings.add(type.name());
		}
		result.put("bendings", bendings);
		
		BasicDBObject items = new BasicDBObject();
		for(Entry<Material, Abilities> entry : data.getItemAbilities().entrySet()) {
			if(entry.getKey() != null && entry.getValue() != null) {
				items.put(entry.getKey().name(), entry.getValue().name());
			}
		}
		result.put("items", items);
		
		BasicDBObject slots = new BasicDBObject();
		for(Entry<Integer, Abilities> entry : data.getSlotAbilities().entrySet()) {
			if(entry.getKey() != null && entry.getValue() != null) {
				slots.put("m"+entry.getKey().toString(), entry.getValue().name());
			}
		}
		result.put("slots", slots);
		
		BasicDBList specializations = new BasicDBList();
		for(BendingSpecializationType spe : data.getSpecialization()) {
			if(spe != null) {
				specializations.add(spe.name());
			}
		}
		result.put("specializations", specializations);
		
		return result;
	}
	
	private static BendingPlayerData unmarshal(DBObject obj) {
		BendingPlayerData result = new BendingPlayerData();
		
		result.setPlayer(UUID.fromString((String) obj.get("player")));
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

	@Override
	public void clear() {
		timestamps.clear();
		players.clear();
		table.drop();
	}

}
