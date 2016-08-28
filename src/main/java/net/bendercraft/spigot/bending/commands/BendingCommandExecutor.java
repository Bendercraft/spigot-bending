package net.bendercraft.spigot.bending.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import net.bendercraft.spigot.bending.Messages;
import net.bendercraft.spigot.bending.commands.subcommands.AddExecution;
import net.bendercraft.spigot.bending.commands.subcommands.AffinityExecution;
import net.bendercraft.spigot.bending.commands.subcommands.AvailableExecution;
import net.bendercraft.spigot.bending.commands.subcommands.BindExecution;
import net.bendercraft.spigot.bending.commands.subcommands.ChooseExecution;
import net.bendercraft.spigot.bending.commands.subcommands.ClearExecution;
import net.bendercraft.spigot.bending.commands.subcommands.CooldownExecution;
import net.bendercraft.spigot.bending.commands.subcommands.DebugExecution;
import net.bendercraft.spigot.bending.commands.subcommands.DeckExecution;
import net.bendercraft.spigot.bending.commands.subcommands.DisplayExecution;
import net.bendercraft.spigot.bending.commands.subcommands.HelpExecution;
import net.bendercraft.spigot.bending.commands.subcommands.LearningExecution;
import net.bendercraft.spigot.bending.commands.subcommands.PathExecution;
import net.bendercraft.spigot.bending.commands.subcommands.ReloadExecution;
import net.bendercraft.spigot.bending.commands.subcommands.RemoveExecution;
import net.bendercraft.spigot.bending.commands.subcommands.ToggleExecution;
import net.bendercraft.spigot.bending.commands.subcommands.VersionExecution;

public class BendingCommandExecutor implements CommandExecutor, TabCompleter {

	private List<IBendingCommand> commands;

	public BendingCommandExecutor() {
		this.commands = new LinkedList<IBendingCommand>();

		this.commands.add(new BindExecution());
		this.commands.add(new ChooseExecution());
		this.commands.add(new RemoveExecution());
		this.commands.add(new LearningExecution());
		this.commands.add(new VersionExecution());
		this.commands.add(new ToggleExecution());
		this.commands.add(new CooldownExecution());
		this.commands.add(new AddExecution());
		this.commands.add(new ReloadExecution());
		this.commands.add(new DisplayExecution());
		this.commands.add(new ClearExecution());
		this.commands.add(new AffinityExecution());
		this.commands.add(new PathExecution());
		this.commands.add(new DeckExecution());
		this.commands.add(new AvailableExecution());
		this.commands.add(new DebugExecution());
		this.commands.add(new HelpExecution(this.commands));
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
