package net.avatar.realms.spigot.bending.commands;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.commands.subcommands.AddExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.AffinityExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.AvailableExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.BindExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.ChooseExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.ClearExecution;
import net.avatar.realms.spigot.bending.commands.subcommands.CooldownExecution;
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

	private List<IBendingCommand> commands;

	public BendingCommandExecutor() {
		this.bind = new BindExecution();
		this.choose = new ChooseExecution();
		this.remove = new RemoveExecution();
		this.learning = new LearningExecution();
		this.version = new VersionExecution();
		this.toggle = new ToggleExecution();
		this.who = new WhoExecution();
		this.cooldown = new CooldownExecution();
		this.add = new AddExecution();
		// TODO :
		this.display = new DisplayExecution();
		this.clear = new ClearExecution();
		this.affinity = new AffinityExecution();
		this.path = new PathExecution();
		this.reload = new ReloadExecution();
		this.help = new HelpExecution();
		this.available = new AvailableExecution();

		this.commands = new LinkedList<IBendingCommand>();
		this.commands.add(this.bind);
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

		List<String> argList = Arrays.asList(args);
		String subCommand = argList.remove(0);

		for (IBendingCommand command : this.commands) {
			if (command.isCommand(subCommand)) {
				return command.execute(sender, argList);
			}
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return null;
	}

	private String autoCompleteParameter(String start, Abilities[] abilities) {
		List<String> values = new LinkedList<String>();
		for (Abilities ability : abilities) {
			values.add(ability.name());
		}
		return autoCompleteParameter(start, values);
	}

	private String autoCompleteParameter(String start, List<String> values) {
		if (start == null || start.isEmpty()) {
			return " ";
		}
		int length = start.length();

		List<String> valids = new LinkedList<String>();
		for (String value : values) {
			String temp = value.substring(0, length);
			if (temp.equalsIgnoreCase(start)) {
				valids.add(value);
			}
		}
		if (valids.size() < 1) {
			return start;
		}
		else if (valids.size() == 1) {
			return valids.get(0);
		}
		else {
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
