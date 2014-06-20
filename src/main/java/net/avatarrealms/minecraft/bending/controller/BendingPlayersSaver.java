package net.avatarrealms.minecraft.bending.controller;

import java.util.List;

import net.avatarrealms.minecraft.bending.model.BendingPlayer;
import net.avatarrealms.minecraft.bending.utils.PluginTools;
import net.avatarrealms.minecraft.bending.utils.Tools;

public class BendingPlayersSaver implements Runnable {

	private static int timelimitdays = 30;

	public BendingPlayersSaver() {

	}

	@Override
	public void run() {
		save();

	}

	public static void save() {
		long nowtime = System.currentTimeMillis();
		long timelimit = timelimitdays * 1000L * 60L * 60L * 24L;

		BendingPlayers config = Tools.config;
		List<BendingPlayer> players = BendingPlayer.getBendingPlayers();

		for (BendingPlayer player : players) {
			long time = player.getLastTime();
			if (nowtime - time >= timelimit && timelimitdays != 0) {
				PluginTools.verbose("Old player: " + player.getPlayerID() + " deleted.");
				player.delete();
				config.setPlayer(player.getPlayerID(), null);
			} else {
				config.setPlayer(player.getPlayerID(), player);
			}
		}

		config.save();
	}

}
