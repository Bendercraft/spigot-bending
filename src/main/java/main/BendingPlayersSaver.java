package main;

import java.util.List;

import tools.BendingPlayer;
import tools.Tools;

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
				Tools.verbose("Old player: " + player.getName() + " deleted.");
				player.delete();
				config.setPlayer(player.getName(), null);
			} else {
				config.setPlayer(player.getName(), player);
			}
		}

		config.save();

		// Tools.verbose("Players length: " + players.size());
	}

}
