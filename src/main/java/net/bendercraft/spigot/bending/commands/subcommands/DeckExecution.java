package net.bendercraft.spigot.bending.commands.subcommands;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.bendercraft.spigot.bending.Messages;
import net.bendercraft.spigot.bending.abilities.BendingPlayer;
import net.bendercraft.spigot.bending.commands.BendingCommand;
import net.bendercraft.spigot.bending.controller.Settings;

public class DeckExecution extends BendingCommand {

	public DeckExecution() {
		super();
		this.command = "deck";
		this.aliases.add("dk");
		this.basePermission = "bending.command.deck";
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(Messages.NOT_CONSOLE_COMMAND);
			return true;
		}

		if (args.size() == 1) {
			if (args.get(0).equalsIgnoreCase("list")) {
				listDecks(sender);
			}
			else {
				changeDeck(sender, args);
			}
		}
		else if (args.size() == 3) {
			renameDeck(sender, args);
		}
		else {
			printUsage(sender);
		}

		return true;
	}

	private void listDecks(CommandSender sender) {
		Player player = (Player) sender;
		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		sender.sendMessage("Decks : ");
		for (String deck : bender.getDecksNames()) {
			sender.sendMessage("----" + deck);
		}
	}

	private void renameDeck(CommandSender sender, List<String> args) {
		Player player = (Player) sender;
		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		String deckName = args.get(1).toLowerCase();
		String newName = args.get(2).toLowerCase();
		if (bender.getDecks().containsKey(deckName)) {
			if (bender.getDecks().containsKey(newName)) {
				sender.sendMessage(ChatColor.RED + Messages.ALREADY_DECK_NAME);
			}
			else {
				Map<Integer, String> deck = bender.getDecks().remove(deckName);
				bender.getDecks().put(newName, deck);
				sender.sendMessage(ChatColor.GREEN + Messages.DECK_RENAMED + newName);
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_DECK);
		}
	}

	private void changeDeck(CommandSender sender, List<String> args) {
		Player player = (Player) sender;
		BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
		String deckName = args.get(0).toLowerCase();
		if (bender.getDecks().containsKey(deckName)) {
			bender.setCurrentDeck(deckName);
			String msg = Messages.DECK_SET;
			msg = msg.replaceAll("\\{0\\}", deckName);
			sender.sendMessage(ChatColor.GREEN + msg);
		}
		else {
			if (bender.getDecks().size() >= Settings.MAX_DECKS_AMOUNT) {
				sender.sendMessage(ChatColor.RED + Messages.MAX_DECKS_REACHED);
			}
			else {
				Map<Integer, String> deck = new TreeMap<Integer, String>();
				bender.getDecks().put(deckName, deck);
				bender.setCurrentDeck(deckName);
				String msg = Messages.DECK_SET;
				msg = msg.replaceAll("\\{0\\}", deckName);
				sender.sendMessage(ChatColor.GREEN + msg);
			}
		}
	}


	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (sender.hasPermission("bending.command.deck")) {
			sender.sendMessage("/bending deck list");
			sender.sendMessage("/bending deck <deckname>");
			sender.sendMessage("/bending deck rename <deckname> <newname>");
		}
		else if (permission) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
		}
	}

	@Override
	public List<String> autoComplete(CommandSender sender, List<String> args) {
		List<String> values = new LinkedList<String>();
		if (!(sender instanceof Player)) {
			return values;
		}

		if (args.size() == 1 || args.size() == 2) {
			Player player = (Player) sender;
			BendingPlayer bender = BendingPlayer.getBendingPlayer(player);
			for (String value : bender.getDecksNames()) {
				values.add(value);
			}
		}

		return values;
	}
}
