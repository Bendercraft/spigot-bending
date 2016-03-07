package net.avatar.realms.spigot.bending.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.commands.subcommands.AddExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.AffinityExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.AvailableExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.BindExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.ChooseExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.ClearExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.CooldownExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.DebugExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.DeckExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.DisplayExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.HelpExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.LearningExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.PathExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.ReloadExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.RemoveExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.ToggleExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.VersionExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.WhoExecution;

public class BendingCommandExecutor implements CommandExecutor, TabCompleter {

	private IBendingCommand bind;
	private IBendingCommand choose;
	private IBendingCommand remove;
	private IBendingCommand learning;
	private IBendingCommand version;
	private IBendingCommand toggle;
	private IBendingCommand who;
	private IBendingCommand display;
	private IBendingCommand cooldown;
	private IBendingCommand clear;
	private IBendingCommand add;
	private IBendingCommand affinity;
	private IBendingCommand path;
	private IBendingCommand reload;
	private IBendingCommand help;
	private IBendingCommand available;
	private IBendingCommand deck;
	private IBendingCommand debug;

	private List<IBendingCommand> commands;

	public BendingCommandExecutor() {
		this.commands = new LinkedList<IBendingCommand>();

		this.bind = new BindExecution();
		this.choose = new ChooseExecution();
		this.remove = new RemoveExecution();
		this.learning = new LearningExecution();
		this.version = new VersionExecution();
		this.toggle = new ToggleExecution();
		this.who = new WhoExecution();
		this.cooldown = new CooldownExecution();
		this.add = new AddExecution();
		this.reload = new ReloadExecution();
		this.display = new DisplayExecution();
		this.clear = new ClearExecution();
		this.affinity = new AffinityExecution();
		this.path = new PathExecution();
		this.help = new HelpExecution(this.commands);
		// TODO :
		this.deck = new DeckExecution();
		this.available = new AvailableExecution();
		this.debug = new DebugExecution();

		this.commands.add(this.bind);
		this.commands.add(this.deck);
		this.commands.add(this.debug);
		this.commands.add(this.choose);
		this.commands.add(this.remove);
		this.commands.add(this.add);
		this.commands.add(this.affinity);
		this.commands.add(this.path);
		this.commands.add(this.learning);
		this.commands.add(this.available);
		this.commands.add(this.help);
		this.commands.add(this.version);
		this.commands.add(this.toggle);
		this.commands.add(this.who);
		this.commands.add(this.display);
		this.commands.add(this.cooldown);
		this.commands.add(this.clear);
		this.commands.add(this.reload);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender == null) {
			return false;
		}
		if (args.length < 1) {
			return false;
		}

		List<String> argList = new LinkedList<>(Arrays.asList(args));
		String subCommand = argList.remove(0);

		for (IBendingCommand command : this.commands) {
			if (command.isCommand(subCommand)) {
				if (command.hasBasePermission(sender)) {
					return command.execute(sender, argList);
				}
				else {
					sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> result = new ArrayList<String>();
		if (args.length == 0) {
			result.add("bending");
		}
		else if (args.length == 1) {
			List<String> values = new LinkedList<String>();
			for (IBendingCommand command : this.commands) {
				values.add(command.getCommand());
			}
			result.add(autoCompleteParameter(args[0], values));
		}
		else {
			List<String> argList = new LinkedList<String>(Arrays.asList(args));
			String sub = argList.remove(0);
			for (IBendingCommand command : this.commands) {
				if (command.isCommand(sub)) {
					result.add(autoCompleteParameter(argList.get(0), command.autoComplete(sender, argList)));
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Choose the best available value for the autocompletion
	 * 
	 * @param start
	 *            What was the parameter sent
	 * @param values
	 *            What are the possible values
	 * @return the best value
	 */
	private String autoCompleteParameter(String start, List<String> values) {
		if (start == null || start.isEmpty()) {
			return " ";
		}

		List<String> valids = new LinkedList<String>();
		if (values == null) {
			return start;
		}

		for (String value : values) {
			if (value.toLowerCase().startsWith(start.toLowerCase())) {
				valids.add(value);
			}
		}
		if (valids.size() < 1) {
			return start;
		} else if (valids.size() == 1) {
			return valids.get(0);
		} else {
			String base = valids.get(0);
			valids.remove(0);
			StringBuilder builder = new StringBuilder();
			int i = 0;
			boolean done = false;
			while (!done) {
				if (i >= base.length()) {
					break;
				}
				boolean same = true;
				char c = base.charAt(i);
				for (String other : valids) {
					if (other.charAt(i) != c) {
						same = false;
						done = true;
						break;
					}
				}
				if (same) {
					builder.append(c);
				}
				i++;
			}
			return builder.toString();
		}
	}
}
