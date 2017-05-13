package net.bendercraft.spigot.bending.commands.subcommands;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.Messages;
import net.bendercraft.spigot.bending.commands.BendingCommand;
import net.bendercraft.spigot.bending.db.MySQLDB;

public class CredentialsExecution extends BendingCommand {

	public CredentialsExecution() {
		super();
		this.command = "credentials";
		this.aliases.add("c");
		this.basePermission = "bending.command.credentials";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + Messages.NOT_CONSOLE_COMMAND);
			return true;
		}
		
		Player player = (Player) sender;
		String token = RandomStringUtils.randomAlphanumeric((int) (6 + Math.random()*3));
		MySQLDB.credentials(player.getUniqueId(), player.getName(), token);
		
		String prefix = ChatColor.GREEN+"["+ChatColor.AQUA+"Bending"+ChatColor.GREEN+"] ";
		
		sender.sendMessage(prefix+"Adresse : "+ChatColor.GOLD+"https://talents.avatar-horizon.world");
		sender.sendMessage(prefix+"Nom d'utilisateur : "+ChatColor.GOLD+player.getName());
		sender.sendMessage(prefix+"Mot de passe : "+ChatColor.GOLD+token);

		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.command.credentials")) {
			sender.sendMessage("/bending credentials");
		} else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}

	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		return new LinkedList<String>();
	}
}
