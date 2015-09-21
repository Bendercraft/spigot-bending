package net.avatar.realms.spigot.bending.learning;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.learning.listeners.AirListener;
import net.avatar.realms.spigot.bending.learning.listeners.ChiListener;
import net.avatar.realms.spigot.bending.learning.listeners.EarthListener;
import net.avatar.realms.spigot.bending.learning.listeners.FireListener;
import net.avatar.realms.spigot.bending.learning.listeners.PermissionListener;
import net.avatar.realms.spigot.bending.learning.listeners.WaterListener;
import net.avatar.realms.spigot.bending.utils.EntityTools;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import com.google.gson.Gson;

public class BendingLearning {
	private HashMap<UUID, List<String>> permissions = new HashMap<UUID, List<String>>();
	private HashMap<Player, PermissionAttachment> actuals = new HashMap<Player, PermissionAttachment>();

	private Gson mapper = new Gson();

	public void onEnable() {
		try {
			this.load();

			PermissionListener permListener = new PermissionListener(this);
			AirListener airListener = new AirListener(this);
			EarthListener earthListener = new EarthListener(this);
			WaterListener waterListener = new WaterListener(this);
			FireListener fireListener = new FireListener(this);
			ChiListener chiListener = new ChiListener(this);

			// Register listeners
			Bending.plugin.getServer().getPluginManager().registerEvents(permListener, Bending.plugin);
			Bending.plugin.getServer().getPluginManager().registerEvents(airListener, Bending.plugin);
			Bending.plugin.getServer().getPluginManager().registerEvents(earthListener, Bending.plugin);
			Bending.plugin.getServer().getPluginManager().registerEvents(waterListener, Bending.plugin);
			Bending.plugin.getServer().getPluginManager().registerEvents(fireListener, Bending.plugin);
			Bending.plugin.getServer().getPluginManager().registerEvents(chiListener, Bending.plugin);

		} catch (Exception e) {
			Bending.plugin.getLogger().severe("Could not load Bending_Learning : " + e.getMessage());
		}
	}

	public void onDisable() {
		Set<Player> toRemove = new HashSet<Player>(actuals.keySet());
		for (Player p : toRemove) {
			this.release(p);
		}
	}

	public boolean addPermission(Player player, BendingAbilities ability) {
		if (!EntityTools.hasPermission(player, ability)) {
			// Get permission attachement
			PermissionAttachment attachment = this.lease(player);
			String perm = EntityTools.getPermissionKey(ability);
			attachment.setPermission(perm, true);
			if (!permissions.containsKey(player.getUniqueId())) {
				permissions.put(player.getUniqueId(), new LinkedList<String>());
			}
			permissions.get(player.getUniqueId()).add(perm);
			try {
				this.save();
			} catch (Exception e) {
				Bending.plugin.getLogger().severe("Could not have saved permission " + perm + " for player " + player.getName() + " because : " + e.getMessage());
			}
			return true;
		}
		return false;
	}

	public boolean removePermission(Player player, BendingAbilities ability) {
		if (EntityTools.hasPermission(player, ability)) {
			// Get permission attachement
			PermissionAttachment attachment = this.lease(player);
			String perm = EntityTools.getPermissionKey(ability);
			attachment.unsetPermission(perm);
			if (permissions.containsKey(player.getUniqueId())) {
				permissions.get(player.getUniqueId()).remove(perm);
			}
			try {
				this.save();
			} catch (Exception e) {
				Bending.plugin.getLogger().severe("Could not have saved permission " + perm + " for player " + player.getName() + " because : " + e.getMessage());
			}
			return true;
		}
		return false;
	}

	private void load() throws IOException {
		File folder = Bending.plugin.getDataFolder();
		File permissionsFile = new File(folder, "permissions.json");

		if (permissionsFile.exists() && permissionsFile.isFile()) {
			FileReader reader = new FileReader(permissionsFile);
			LearningPermissions tmp = mapper.fromJson(reader, LearningPermissions.class);
			permissions = new HashMap<UUID, List<String>>();
			permissions.putAll(tmp.getPermissions());
			reader.close();
		}
	}

	private void save() throws IOException {
		File folder = Bending.plugin.getDataFolder();
		File permissionsFile = new File(folder, "permissions.json");

		if (!permissionsFile.exists()) {
			folder.mkdirs();
			permissionsFile.createNewFile();
		}
		FileWriter writer = new FileWriter(permissionsFile);
		LearningPermissions tmp = new LearningPermissions();
		tmp.setPermissions(permissions);
		mapper.toJson(tmp, writer);
		writer.close();
	}

	public PermissionAttachment lease(Player p) {
		if (actuals.containsKey(p)) {
			return actuals.get(p);
		}
		PermissionAttachment attachment = p.addAttachment(Bending.plugin);
		actuals.put(p, attachment);
		if (permissions.containsKey(p.getUniqueId())) {
			for (String perm : permissions.get(p.getUniqueId())) {
				attachment.setPermission(perm, true);
			}
		} else {
			permissions.put(p.getUniqueId(), new LinkedList<String>());
		}
		return attachment;
	}

	public void release(Player p) {
		if (actuals.containsKey(p)) {
			PermissionAttachment perm = actuals.get(p);
			p.removeAttachment(perm);
		}
		actuals.remove(p);
	}

	public boolean isBasicBendingAbility(BendingAbilities ability) {
		switch (ability) {
		case AirBlast:
		case AirSpout:
		case AirSwipe:
		case FireBlast:
		case Blaze:
		case HeatControl:
		case EarthBlast:
		case Collapse:
		case RaiseEarth:
		case WaterManipulation:
		case HealingWaters:
		case WaterSpout:
			return true;
		default:
			return false;
		}
	}

	private class LearningPermissions {
		private HashMap<UUID, List<String>> permissions = new HashMap<UUID, List<String>>();

		public HashMap<UUID, List<String>> getPermissions() {
			return permissions;
		}

		public void setPermissions(HashMap<UUID, List<String>> permissions) {
			this.permissions = permissions;
		}
	}
}