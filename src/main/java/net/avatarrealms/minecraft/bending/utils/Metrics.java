package net.avatarrealms.minecraft.bending.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Metrics {
	private Map<String, Metrics> metrics = new HashMap<String, Metrics>();
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
	
	public List<String> toMinecraftString(int level) {
		List<String> result = new LinkedList<String>();
		
		if(value != null) {
			StringBuilder builder = this.incrementString(level);
			builder.append(value);
			result.add(builder.toString());
		}
		
		for(Entry<String, Metrics> entry : metrics.entrySet()) {
			StringBuilder builder = this.incrementString(level);
			builder.append(entry.getKey());
			result.add(builder.toString());
			result.addAll(entry.getValue().toMinecraftString(level+1));
		}
		
		return result;
	}
	
	private StringBuilder incrementString(int level) {
		StringBuilder builder = new StringBuilder();
		
		for(int i = 0 ; i < level ; i++) {
			builder.append("  ");
		}
		
		return builder;
	}
}
