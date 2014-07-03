package net.avatarrealms.minecraft.bending.utils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Metrics {
	private Map<String, Metrics> metrics = new TreeMap<String, Metrics>();
	private String value = null;
	
	public Metrics() {
		
	}
	
	public Metrics(String value) {
		this.value = value;
	}
	
	public void put(List<String> path, String value) {
		if(path.isEmpty()) {
			this.value = value;
		} else {
			String leaf = path.remove(0);
			if(!metrics.containsKey(leaf)) {
				metrics.put(leaf, new Metrics());
			}
			metrics.get(leaf).put(path, value);
		}
	}
	
	public String toMinecraftString(String key, int level) {
		StringBuilder builder = new StringBuilder();
		builder.append(this.incrementString(level));
		builder.append(key);
		if(value != null) {
			builder.append(" - ");
			builder.append(value);
		}
		builder.append("\n");
		for(Entry<String, Metrics> entry : metrics.entrySet()) {
			builder.append(entry.getValue().toMinecraftString(entry.getKey(), level+1));
		}
		
		return builder.toString();
	}
	
	private String incrementString(int level) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0 ; i < level ; i++) {
			builder .append("  ");
		}
		
		return builder.toString();
	}
}
