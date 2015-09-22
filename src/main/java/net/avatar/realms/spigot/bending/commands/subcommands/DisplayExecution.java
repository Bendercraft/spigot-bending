package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.commands.BendingCommand;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.PluginTools;

public class DisplayExecution extends BendingCommand {

	public DisplayExecution() {
		super();
		this.command = "display";
		this.aliases.add("d");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + Messages.NOT_CONSOLE_COMMAND);
			return true;
		}

		if (!sender.hasPermission("bending.command.display")) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
			return true;
		}

		// If list slots
		if (args.isEmpty()) {
			Player player = (Player) sender;
			BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
			Map<Integer, BendingAbilities> abilities = bender.getAbilities();
			player.sendMessage("Slots :");
			if (abilities != null && !abilities.isEmpty()) {
				ChatColor white = ChatColor.WHITE;
				for (Entry<Integer, BendingAbilities> slot : abilities.entrySet()) {
					ChatColor color = PluginTools.getColor(Settings.getColorString(slot.getValue().getElement().name()));
					player.sendMessage("--" + color + slot.getKey() + white + " : " + color + slot.getValue().name());
				}
			} else {
				player.sendMessage("-" + Messages.NOTHING_BOUND);
			}
		} else {
			BendingElement element = getElement(args.get(0));
			if (element == null) {
				sender.sendMessage(ChatColor.RED + Messages.INVALID_ELEMENT);
				return true;
			}
			ChatColor color = PluginTools.getColor(Settings.getColorString(element.name()));
			sender.sendMessage(color + element.name() + ":");
			for (BendingAbilities ability : BendingAbilities.getElementAbilities(element)) {
				sender.sendMessage(color + ability.name());
			}
		}

		return true;
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.command.display")) {
			sender.sendMessage("/bending display [element]");
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}

	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		// TODO Auto-generated method stub
		return null;
	}
}
