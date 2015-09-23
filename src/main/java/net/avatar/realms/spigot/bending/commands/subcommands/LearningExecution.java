package net.avatar.realms.spigot.bending.commands.subcommands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.Messages;
import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.commands.BendingCommand;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.PluginTools;

public class LearningExecution extends BendingCommand {

	public LearningExecution() {
		super();
		this.command = "learning";
		this.aliases.add("learn");
		this.aliases.add("l");
	}

	@Override
	public boolean execute(CommandSender sender, List<String> args) {
		if (!sender.hasPermission("bending.command.learning")) {
			sender.sendMessage(ChatColor.RED + Messages.NO_PERMISSION);
			return true;
		}

		if (args.isEmpty()) {
			printUsage(sender);
			return true;
		}

		String subCommand = args.remove(0);

		if (subCommand.equalsIgnoreCase("free")) {
			free(sender, args);
		}
		else {
			unlock(sender, subCommand, args);
		}

		return true;
	}

	private void free(CommandSender sender, List<String> args) {
		Player target = null;
		if (args.size() == 3) {
			target = getPlayer(args.remove(1));
		}
		else {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + Messages.CONSOLE_SPECIFY_PLAYER);
				return;
			}
			target = (Player) sender;
		}

		if (target == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_PLAYER);
			return;
		}

		BendingPlayer bender = BendingPlayer.getBendingPlayer(target);
		String subCommand = args.remove(0);

		if (subCommand.equals("ability")) {
			freeAbility(sender, target, bender, args.get(0));
		}
		else if (subCommand.equalsIgnoreCase("affinity") || subCommand.equalsIgnoreCase("aff")) {
			String name = args.get(0);
			BendingAffinity affinity = BendingAffinity.getType(name);
			if (affinity == null) {
				sender.sendMessage(Messages.INVALID_AFFINITY);
				return;
			}
			for (BendingAbilities ability : BendingAbilities.values()) {
				if (affinity.equals(ability.getAffinity())) {
					Bending.plugin.learning.removePermission(target, ability);
				}
			}
			bender.removeAffinity(affinity);
			sender.sendMessage(ChatColor.GREEN + "Player " + target.getPlayer().getName() + " has lost " + affinity.name() + ".");
		}
		else {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_PARAMETER);
		}
	}

	private void freeAbility(CommandSender sender, Player target, BendingPlayer bender, String name) {
		BendingAbilities ability = BendingAbilities.getAbility(name);
		if (ability == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_ABILITY);
			return;
		}

		if (Bending.plugin.learning.removePermission(target, ability)) {
			bender.clearAbilities();
			sender.sendMessage(ChatColor.GREEN + "Player " + target.getPlayer().getName() + " has lost " + ability.name() + ".");
		}
	}

	private void unlock(CommandSender sender, String subCommand, List<String> args) {
		Player target = null;
		if (args.size() == 2) {
			target = getPlayer(args.remove(0));
		}
		else if (args.size() == 1) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + Messages.NOT_CONSOLE_COMMAND);
				return;
			}
			target = (Player) sender;
		}
		else {
			printUsage(sender);
			return;
		}

		if (target == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_PLAYER);
			return;
		}

		BendingPlayer bender = BendingPlayer.getBendingPlayer(target);

		if (subCommand.equalsIgnoreCase("affinity") || subCommand.equalsIgnoreCase("aff")) {
			unlockAffinity(sender, target, bender, args.get(0));
		}
		else if (subCommand.equalsIgnoreCase("ability")) {
			unlockAbility(sender, target, bender, args.get(0));
		}
		else if (subCommand.equalsIgnoreCase("avatar")) {
			unlockAvatarElement(sender, target, bender, args.get(0));
		}
		else if (subCommand.equalsIgnoreCase("unavatar")) {
			lockAvatarElements(sender, target, bender, args.get(0));
		}
	}

	private void lockAvatarElements(CommandSender sender, Player target, BendingPlayer bender, String elementName) {
		BendingElement element = BendingElement.getType(elementName);
		if (element == null) {
			sender.sendMessage(Messages.INVALID_ELEMENT);
			return;
		}
		bender.setBender(element);
		for (BendingAbilities ability : BendingAbilities.values()) {
			if (ability.getElement().equals(element) && !ability.isAffinity()) {
				Bending.plugin.learning.addPermission(target, ability);
			}
		}
		sender.sendMessage(ChatColor.DARK_GREEN + "Player " + target.getPlayer().getName() + " has lost all element except : " + element.name());
	}

	private void unlockAvatarElement(CommandSender sender, Player target, BendingPlayer bender, String elementName) {
		if (!target.hasPermission("bending.energy.avatarstate")) {
			sender.sendMessage("The target is not an avatar.");
			return;
		}

		BendingElement element = BendingElement.getType(elementName);
		if (element == null) {
			sender.sendMessage("This is not a valid element.");
			return;
		}

		if (!bender.isBender(element)) {
			bender.addBender(element);
			ChatColor color = PluginTools.getColor(Settings.getColorString(element.name()));
			String message = "Congratulations, you can now bend " + element.name();
			target.getPlayer().sendMessage(color + message);
			for (BendingAbilities ability : BendingAbilities.values()) {
				if (ability.getElement().equals(element) && !ability.isAffinity()) {
					Bending.plugin.learning.addPermission(target, ability);
					message = "You can now use " + ability.name();
					target.getPlayer().sendMessage(color + message);
				}
			}
			sender.sendMessage(ChatColor.DARK_GREEN + "Player " + target.getName() + " has unlocked element : " + element.name());
		}
	}

	private void unlockAbility(CommandSender sender, Player target, BendingPlayer bender, String abilityName) {
		BendingAbilities ability = BendingAbilities.getAbility(abilityName);
		if (ability == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_ABILITY);
			return;
		}
		if (!bender.isBender(ability.getElement())) {
			sender.sendMessage(ChatColor.RED + Messages.NOT_HAVE_ELEMENT);
			return;
		}

		if (Bending.plugin.learning.addPermission(target, ability)) {
			ChatColor color = PluginTools.getColor(Settings.getColorString(ability.getElement().name()));
			String message = Messages.ABILITY_LEARNED + ability.name();
			target.getPlayer().sendMessage(color + message);
			sender.sendMessage(ChatColor.GREEN + "Player " + target.getPlayer().getName() + " has received " + ability.name() + ".");
		}
	}

	private void unlockAffinity(CommandSender sender, Player target, BendingPlayer bender, String affinityName) {
		BendingAffinity affinity = BendingAffinity.getType(affinityName);
		if (affinity == null) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_AFFINITY);
			return;
		}
		if (!bender.isBender(affinity.getElement())) {
			sender.sendMessage(ChatColor.RED + Messages.INVALID_AFFINITY_ELEMENT);
			return;
		}
		boolean canLearn = true;
		for (BendingAffinity aff : bender.getAffinities()) {
			if (aff.getElement().equals(affinity.getElement())) {
				canLearn = false;
				break;
			}
		}
		if (canLearn) {
			bender.addAffinity(affinity);
			String msg = Messages.AFFINITY_SET;
			msg = msg.replaceAll("\\{0\\}", affinity.name());
			target.sendMessage(msg);

			msg = Messages.YOU_SET_AFFINITY;
			msg = msg.replaceAll("\\{0\\}", affinity.name());
			msg = msg.replaceAll("\\{1\\}", target.getName());

			sender.sendMessage(msg);
		}
		else {
			String msg = Messages.ALREADY_ELEMENT_AFFINITY;
			msg = msg.replaceAll("\\{0\\}", affinity.getElement().name());
			sender.sendMessage(ChatColor.RED + msg);
		}
	}

	@Override
	public void printUsage(CommandSender sender, boolean permission) {
		if (!sender.hasPermission("bending.command.afffiniy")) {
			sender.sendMessage("/bending learning affinity [player] <affinity>");
			sender.sendMessage("/bending learning ability [player] <ability>");
			sender.sendMessage("/bending learning avatar [player] <element>");
			sender.sendMessage("/bending learning unavatar [player] <element>");
			sender.sendMessage("/bending learning free ability [player] <element>");
			sender.sendMessage("/bending learning free affinity [player] <element>");
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
