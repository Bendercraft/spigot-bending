package net.avatar.realms.spigot.bending.db;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingPlayerData;
import net.avatar.realms.spigot.bending.db.impl.FlatFileDB;

public class DBUtils {
	public static IBendingDB choose(String key) {
		// TODO rework this awful statement
		if (key.equals("flatfile")) {
			return new FlatFileDB();
		}
		return null;
	}

	public static void convert(IBendingDB src, IBendingDB dest) {
		Map<UUID, BendingPlayerData> dump = src.dump();
		dest.clear();
		for (Entry<UUID, BendingPlayerData> entry : dump.entrySet()) {
			dest.set(entry.getKey(), new BendingPlayer(entry.getValue()));
		}
	}
}
