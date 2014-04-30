package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import tools.BendingPlayer;

public class BendingPlayers {

	private FileConfiguration bendingPlayers = null;
	private File bendingPlayersFile = null;

	private File dataFolder;

	private int version = 1;

	// private InputStream defConfigStream;

	// public BendingPlayers(File file, InputStream inputStream) {
	// load();
	// dataFolder = file;
	// defConfigStream = inputStream;
	// }

	public BendingPlayers(File file) {
		load();
		dataFolder = file;
	}

	public String getKey(String s) {
		if (!(bendingPlayers == null))
			return bendingPlayers.getString(s, "");
		return "";
	}

	public Object get(String s) {
		if (bendingPlayers != null) {
			return bendingPlayers.get(s);
		}
		return null;
	}

	public void setPlayer(String playername, BendingPlayer player) {
		if (bendingPlayers != null) {
			bendingPlayers.set(playername, player);
			// Tools.verbose(playername + ": " + player.serialize());
		}
		return;
	}

	public BendingPlayer getBendingPlayer(String playername) {
		if (bendingPlayers != null) {
			if (bendingPlayers.contains(playername))
				return new BendingPlayer(bendingPlayers
						.getConfigurationSection(playername).getValues(false));
		}
		return null;
	}

	public List<String> getSavedPlayers() {
		List<String> list = new ArrayList<String>();
		if (bendingPlayers != null) {
			list = new ArrayList<String>(bendingPlayers.getKeys(false));
			list.remove("version");
		}
		return list;
	}

	// public void purgeOldPlayers(long timelimitdays) {
	// if (timelimitdays == 0)
	// return;
	// long nowtime = System.currentTimeMillis();
	// long timelimit = timelimitdays * (1000 * 60 * 60 * 24);
	// for (BendingPlayer player : BendingPlayer.getBendingPlayers()) {
	// long time = player.getLastTime();
	// if (nowtime - time >= timelimit) {
	// player.delete();
	// if (bendingPlayers != null) {
	// bendingPlayers.set(player.getName(), null);
	// }
	// }
	// }
	// }

	public Boolean checkKeys(String s) {
		if (!(bendingPlayers == null))
			return bendingPlayers.getKeys(false).contains(s);
		return false;
	}

	public Set<String> getKeys() {
		return bendingPlayers.getKeys(false);
	}

	public void setKey(String key, String field) {
		if (!(bendingPlayers == null)) {
			bendingPlayers.set(key, field);
			save();
		}
		// if (bendingPlayers == null)
		// Tools.verbose("Uh oh?");
		// Tools.verbose(key);
		// Tools.verbose(field);
	}

	public void reload() {
		if (bendingPlayersFile == null) {
			bendingPlayersFile = new File(dataFolder, "bendingPlayers.yml");
		}
		bendingPlayers = YamlConfiguration
				.loadConfiguration(bendingPlayersFile);
		if (bendingPlayers.contains("version")) {
			if (bendingPlayers.getInt("version", 0) == version) {
				return;
			}
		}
		bendingPlayers = new YamlConfiguration();
		bendingPlayers.set("version", version);
		save();
		return;
	}

	private void load() {
		if (bendingPlayers == null) {
			reload();
		}
	}

	public void save() {
		if (bendingPlayers == null || bendingPlayersFile == null) {
			return;
		}
		try {
			bendingPlayers.save(bendingPlayersFile);
		} catch (IOException ex) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE,
					"Could not save config to " + bendingPlayersFile, ex);
		}
	}

	public void close() {
		save();
		bendingPlayers = null;
		bendingPlayersFile = null;
	}

}
