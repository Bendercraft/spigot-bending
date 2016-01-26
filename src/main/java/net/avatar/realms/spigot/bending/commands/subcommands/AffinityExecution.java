package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.commands.BendingCommand;

public class AffinityExecution extends BendingCommand {

	public AffinityExecution() {
		super();
		this.command = "affinity";
		this.aliases.add("aff");
		this.aliases.add("affi");
		this.basePermission = "bending.command.affinity";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (args.isEmpty()) {
			printUsage(sender);
			return true;
		}
		String subCommand = args.remove(0).toUpperCase();

		if (subCommand.equals("SET")) {
			set(sender, args);
		}
		else if (subCommand.equals("ADD")) {
			add(sender, args);
		}
		else if (subCommand.equals("REMOVE") || subCommand.equals("REM")) {
			remove(sender, args);
		}
		else if (subCommand.equals("CLEAR")) {
			clear(sender, args);
		}
		else {
			printUsage(sender);
		}
		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (!(sender instanceof Player)) {
			return;
		}

		if (sender.hasPermission("bending.command.affinity.other")) {
			sender.sendMessage("/bending affinity set [player] <affinity>");
			sender.sendMessage("/bending affinity add [player] <affinity>");
			sender.sendMessage("/bending affinity remove [player] <affinity>");
			sender.sendMessage("/bending affinity clear [player]");
		}
		else if (sender.hasPermission("bending.command.affinity")) {
			sender.sendMessage("/bending affinity set <affinity>");
			sender.sendMessage("/bending affinity add <affinity>");
			sender.sendMessage("/bending affinity remove <affinity>");
			sender.sendMessage("/bending affinity clear");
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}

	private void set(CommandSender sender, List<String> args) {
		Player target = checkArgs(sender, args, 1, 2);

		if (target == null) {
			return;
		}

		BendingAffinity affinity = BendingAffinity.getType(args.get(0));
		if (affinity == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_AFFINITY);
			return;
		}

		BendingPlayer bender = BendingPlayer.getBendingPlayer(target);

		if (!bender.isBender(affinity.getElement())) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_AFFINITY_ELEMENT);
			return;
		}

		bender.setAffinity(affinity);
		String msg = Messages.AFFINITY_SET;
		msg = msg.replaceAll("\\{0\\}", affinity.name());
		target.sendMessage(msg);

		msg = Messages.YOU_SET_AFFINITY;
		msg = msg.replaceAll("\\{0\\}", affinity.name());
		msg = msg.replaceAll("\\{1\\}", target.getName());
		sender.sendMessage(msg);
	}

	private void add(CommandSender sender, List<String> args) {
		Player target = checkArgs(sender, args, 1, 2);

		if (target == null) {
			return;
		}

		BendingAffinity affinity = BendingAffinity.getType(args.get(0));
		if (affinity == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_AFFINITY);
			return;
		}

		BendingPlayer bender = BendingPlayer.getBendingPlayer(target);

		if (!bender.isBender(affinity.getElement())) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_AFFINITY_ELEMENT);
			return;
		}

		bender.addAffinity(affinity);
		String msg = Messages.AFFINITY_ADDED;
		msg = msg.replaceAll("\\{0\\}", affinity.name());
		target.sendMessage(msg);

		msg = Messages.YOU_ADDED_AFFINITY;
		msg = msg.replaceAll("\\{0\\}", affinity.name());
		msg = msg.replaceAll("\\{1\\}", target.getName());
		sender.sendMessage(msg);
	}

	private void remove(CommandSender sender, List<String> args) {
		Player target = checkArgs(sender, args, 1, 2);

		if (target == null) {
			return;
		}

		BendingAffinity affinity = BendingAffinity.getType(args.get(0));
		if (affinity == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_AFFINITY);
			return;
		}

		BendingPlayer bender = BendingPlayer.getBendingPlayer(target);

		if (!bender.isBender(affinity.getElement())) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_AFFINITY_ELEMENT);
			return;
		}

		bender.removeAffinity(affinity);
		String msg = Messages.AFFINITY_REMOVED;
		msg = msg.replaceAll("\\{0\\}", affinity.name());
		target.sendMessage(msg);

		msg = Messages.YOU_REMOVED_AFFINITY;
		msg = msg.replaceAll("\\{0\\}", affinity.name());
		msg = msg.replaceAll("\\{1\\}", target.getName());
		sender.sendMessage(msg);
	}

	private void clear(CommandSender sender, List<String> args) {
		Player target = checkArgs(sender, args, 0, 1);

		if (target == null) {
			return;
		}

		BendingPlayer bender = BendingPlayer.getBendingPlayer(target);

		bender.clearAffinities();

		String msg = Messages.AFFINITY_CLEARED;
		target.sendMessage(msg);

		msg = Messages.YOU_CLEARED_AFFINITY;
		msg = msg.replaceAll("\\{0\\}", target.getName());
		sender.sendMessage(msg);
	}

	private Player checkArgs(CommandSender sender, List<String> args, int minSize, int maxSize) {
		if (args.size() != minSize && args.size() != maxSize) {
			printUsage(sender);
			return null;
		}
		Player target;
		if (args.size() == minSize) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + Messages.CONSOLE_SPECIFY_PLAYER);
				return null;
			}
			target = (Player) sender;
		}
		else {
			target = getPlayer(args.remove(1));
		}

		if (target == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_PLAYER);
			return null;
		}
		return target;
	}

	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		// TODO Auto-generated method stub
		return null;
	}
}
