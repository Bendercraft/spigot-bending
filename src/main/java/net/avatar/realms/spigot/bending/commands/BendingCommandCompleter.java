package net.avatar.realms.spigot.bending.commands;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import net.avatar.realms.spigot.bending.abilities.Abilities;

public class BendingCommandCompleter implements TabCompleter {

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
