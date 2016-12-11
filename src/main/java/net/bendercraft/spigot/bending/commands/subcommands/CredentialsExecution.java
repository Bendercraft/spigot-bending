package net.bendercraft.spigot.bending.commands.subcommands;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.gson.Gson;

import net.bendercraft.spigot.bending.Bending;
import net.bendercraft.spigot.bending.Messages;
import net.bendercraft.spigot.bending.commands.BendingCommand;
import net.bendercraft.spigot.bending.db.Credentials;

public class CredentialsExecution extends BendingCommand {
	
	private static Gson mapper = new Gson();

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
		
		File folder = new File(Bending.getInstance().getDataFolder(), "credentials");
		folder.mkdirs();
		
		File file = new File(folder, player.getName()+".json");
		try {
			file.createNewFile();
		} catch (IOException e) {
			
		}
		Credentials credentials = new Credentials();
		credentials.name = player.getName();
		credentials.uuid = player.getUniqueId().toString();
		credentials.token = RandomStringUtils.randomAlphanumeric((int) (6 + Math.random()*3));
		
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			mapper.toJson(credentials, writer);
		} catch (IOException e) {
			
		} finally {
			IOUtils.closeQuietly(writer);
		}
		
		String prefix = ChatColor.GREEN+"["+ChatColor.AQUA+"Bending"+ChatColor.GREEN+"] ";
		
		sender.sendMessage(prefix+"Adresse : "+ChatColor.GOLD+"https://craft.bendercraft.net");
		sender.sendMessage(prefix+"Nom d'utilisateur : "+ChatColor.GOLD+credentials.name);
		sender.sendMessage(prefix+"Mot de passe : "+ChatColor.GOLD+credentials.token);

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
