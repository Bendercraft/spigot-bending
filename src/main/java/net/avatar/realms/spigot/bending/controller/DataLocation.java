package net.avatar.realms.spigot.bending.controller;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class DataLocation {
	private String world;
	private double x;
	private double y;
	private double z;
	private float yaw;
	private float pitch;

	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public static DataLocation fromLocation(Location loc) {
		if (loc == null || loc.getWorld() == null) {
			return null;
		}
		DataLocation location = new DataLocation();
		location.setWorld(loc.getWorld().getName());
		location.setX(loc.getX());
		location.setY(loc.getY());
		location.setZ(loc.getZ());
		location.setPitch(loc.getPitch());
		location.setYaw(loc.getYaw());
		return location;
	}

	public static Location toLocation(DataLocation loc) {
		if (loc == null) {
			return null;
		}
		Location location = new Location(Bukkit.getServer().getWorld(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ());
		location.setYaw(loc.getYaw());
		location.setPitch(loc.getPitch());
		return location;
	}

}