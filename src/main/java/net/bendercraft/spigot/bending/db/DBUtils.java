package net.bendercraft.spigot.bending.db;

public class DBUtils {
	private DBUtils() {
		
	}
	
	public static IBendingDB choose(String key) {
		// TODO rework this awful statement
		if ("flatfile".equals(key)) {
			return new FlatFileDB();
		}
		return null;
	}
}
