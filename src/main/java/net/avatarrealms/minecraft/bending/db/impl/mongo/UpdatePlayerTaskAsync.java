package net.avatarrealms.minecraft.bending.db.impl.mongo;

import java.util.UUID;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.db.impl.MongoDB;

public class UpdatePlayerTaskAsync implements Runnable {

	private Bending plugin;
	private UUID id;
	private MongoDB db;

	public UpdatePlayerTaskAsync(Bending plugin, MongoDB db, UUID id) {
		this.plugin = plugin;
		this.db = db;
		this.id = id;
	}
	
	@Override
	public void run() {
		DBObject obj = new BasicDBObject();
		obj.put(MongoDB.ID, MongoDB.fromUUID(id));
		DBObject result = db.getTable().findOne(obj);
		BendingPlayer data = null;
		if(result != null) {
			data = new BendingPlayer(MongoDB.unmarshal(result));
		}
		UpdatePlayerTaskSync sync = new UpdatePlayerTaskSync(plugin, db, id, data);
		plugin.getServer().getScheduler().runTask(plugin, sync);
	}

}
