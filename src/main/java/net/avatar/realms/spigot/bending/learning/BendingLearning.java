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
import java.util.logging.Level;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.RegisteredAbility;
import net.avatar.realms.spigot.bending.abilities.air.AirBlast;
import net.avatar.realms.spigot.bending.abilities.air.AirSpout;
import net.avatar.realms.spigot.bending.abilities.air.AirSwipe;
import net.avatar.realms.spigot.bending.abilities.arts.Dash;
import net.avatar.realms.spigot.bending.abilities.arts.HighJump;
import net.avatar.realms.spigot.bending.abilities.earth.Collapse;
import net.avatar.realms.spigot.bending.abilities.earth.EarthBlast;
import net.avatar.realms.spigot.bending.abilities.earth.EarthWall;
import net.avatar.realms.spigot.bending.abilities.fire.Blaze;
import net.avatar.realms.spigot.bending.abilities.fire.FireBlast;
import net.avatar.realms.spigot.bending.abilities.fire.HeatControl;
import net.avatar.realms.spigot.bending.abilities.water.HealingWaters;
import net.avatar.realms.spigot.bending.abilities.water.WaterManipulation;
import net.avatar.realms.spigot.bending.abilities.water.WaterSpout;
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
			Bending.getInstance().getServer().getPluginManager().registerEvents(permListener, Bending.getInstance());
			Bending.getInstance().getServer().getPluginManager().registerEvents(airListener, Bending.getInstance());
			Bending.getInstance().getServer().getPluginManager().registerEvents(earthListener, Bending.getInstance());
			Bending.getInstance().getServer().getPluginManager().registerEvents(waterListener, Bending.getInstance());
			Bending.getInstance().getServer().getPluginManager().registerEvents(fireListener, Bending.getInstance());
			Bending.getInstance().getServer().getPluginManager().registerEvents(chiListener, Bending.getInstance());

		} catch (Exception e) {
			Bending.getInstance().getLogger().log(Level.SEVERE, "Could not load Bending_Learning", e);
		}
	}

	public void onDisable() {
		Set<Player> toRemove = new HashSet<Player>(actuals.keySet());
		for (Player p : toRemove) {
			this.release(p);
		}
	}

	public boolean addPermission(Player player, String ability) {
		if (!EntityTools.hasPermission(player, ability)) {
			// Get permission attachement
			PermissionAttachment attachment = this.lease(player);
			RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(ability);
			String perm = register.getPermission();
			attachment.setPermission(perm, true);
			if (!permissions.containsKey(player.getUniqueId())) {
				permissions.put(player.getUniqueId(), new LinkedList<String>());
			}
			permissions.get(player.getUniqueId()).add(perm);
			try {
				this.save();
			} catch (Exception e) {
				Bending.getInstance().getLogger().log(Level.SEVERE, "Could not have saved permission " + perm + " for player " + player.getName() + " because : ", e);
			}
			return true;
		}
		return false;
	}

	public boolean removePermission(Player player, String ability) {
		if (EntityTools.hasPermission(player, ability)) {
			// Get permission attachement
			PermissionAttachment attachment = this.lease(player);
			RegisteredAbility register = AbilityManager.getManager().getRegisteredAbility(ability);
			attachment.unsetPermission(register.getPermission());
			if (permissions.containsKey(player.getUniqueId())) {
				permissions.get(player.getUniqueId()).remove(register.getPermission());
			}
			try {
				this.save();
			} catch (Exception e) {
				Bending.getInstance().getLogger().log(Level.SEVERE, "Could not have saved permission " + register.getPermission() + " for player " + player.getName(), e);
			}
			return true;
		}
		return false;
	}

	//TODO Rework throws as reader might not be closed in the end
	private void load() throws IOException {
		File folder = Bending.getInstance().getDataFolder();
		File permissionsFile = new File(folder, "permissions.json");

		if (permissionsFile.exists() && permissionsFile.isFile()) {
			FileReader reader = new FileReader(permissionsFile);
			LearningPermissions tmp = mapper.fromJson(reader, LearningPermissions.class);
			permissions = new HashMap<UUID, List<String>>();
			permissions.putAll(tmp.getPermissions());
			reader.close();
		}
	}

	//TODO Rework throws as writer might not be closed in the end
	private void save() throws IOException {
		File folder = Bending.getInstance().getDataFolder();
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
		PermissionAttachment attachment = p.addAttachment(Bending.getInstance());
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

	public boolean isBasicBendingAbility(String ability) {
		if(ability.equals(AirBlast.NAME) 
				|| ability.equals(AirSpout.NAME)
				|| ability.equals(AirSwipe.NAME)
				
				|| ability.equals(FireBlast.NAME)
				|| ability.equals(Blaze.NAME)
				|| ability.equals(HeatControl.NAME)
				
				|| ability.equals(EarthBlast.NAME)
				|| ability.equals(Collapse.NAME)
				|| ability.equals(EarthWall.NAME)
				
				|| ability.equals(WaterManipulation.NAME)
				|| ability.equals(HealingWaters.NAME)
				|| ability.equals(WaterSpout.NAME)
				
				|| ability.equals(Dash.NAME)
				|| ability.equals(HighJump.NAME)) {
			return true;
		}
		return false;
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