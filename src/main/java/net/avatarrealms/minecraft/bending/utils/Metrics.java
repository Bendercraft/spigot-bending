package net.avatarrealms.minecraft.bending.utils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Metrics {
	public static Metrics ROOT = new Metrics();

	private Map<String, Metrics> metrics = new TreeMap<String, Metrics>();
	private String value = null;

	public Metrics() {
	}

	public Metrics(String value) {
		this.value = value;
	}

	public void put(List<String> path, String value) {
		if (path.isEmpty()) {
			this.value = value;
		} else {
			String leaf = path.remove(0);
			if (!metrics.containsKey(leaf)) {
				metrics.put(leaf, new Metrics());
			}
			metrics.get(leaf).put(path, value);
		}
	}

	public String toMinecraftString(String key, int level) {
		StringBuilder builder = new StringBuilder();

		if (value == null && !metrics.isEmpty()) {
			builder.append(this.incrementString(level));
			builder.append(key);
		} else if (metrics.isEmpty()) {
			if (value != null) {
				try {
					int val = Integer.parseInt(value);
					if (val >= 1) {
						builder.append(this.incrementString(level));
						builder.append(key);
						builder.append(" - ");
						builder.append(value);
					}
				} catch (Exception e) {
					builder.append(this.incrementString(level));
					builder.append(key);
					builder.append(" - ");
					builder.append(value);
				}
			}
		}

		builder.append("\n");
		for (Entry<String, Metrics> entry : metrics.entrySet()) {
			builder.append(entry.getValue().toMinecraftString(entry.getKey(),
					level + 1));
		}

		return builder.toString();
	}

	private String incrementString(int level) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < level; i++) {
			builder.append("  ");
		}

		return builder.toString();
	}
}
