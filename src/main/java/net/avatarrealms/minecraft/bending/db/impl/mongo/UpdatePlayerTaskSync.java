package net.avatarrealms.minecraft.bending.db.impl.mongo;

import java.util.UUID;

import net.avatarrealms.minecraft.bending.Bending;
import net.avatarrealms.minecraft.bending.abilities.BendingPlayer;
import net.avatarrealms.minecraft.bending.db.impl.MongoDB;

public class UpdatePlayerTaskSync implements Runnable {

	private Bending plugin;
	private BendingPlayer data;
	private UUID id;
	private MongoDB db;

	public UpdatePlayerTaskSync(Bending plugin, MongoDB db, UUID id, BendingPlayer data) {
		this.plugin = plugin;
		this.db = db;
		this.id = id;
		this.data = data;
	}
	
	@Override
	public void run() {
		//If data not null, update, else just quit
		if(data != null) {
			db.set(data.getPlayerID(), data);
		}
		db.finishUpdateTask(id);
	}

}
