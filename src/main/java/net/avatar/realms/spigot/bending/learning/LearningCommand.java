package net.avatar.realms.spigot.bending.learning;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingAffinity;
import net.avatar.realms.spigot.bending.abilities.BendingElement;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

public class LearningCommand {
	private BendingLearning plugin;

	private CommandSender sender;
	private BendingPlayer bPlayer;

	public LearningCommand(BendingLearning plugin, CommandSender sender, List<String> args) {
		this.plugin = plugin;
		this.sender = sender;
		if (sender instanceof Player) {
			this.bPlayer = BendingPlayer.getBendingPlayer((Player) sender);
		}

		String subCommand = args.get(0);
		String[] array = new String[3];
		array = args.toArray(array);
		if (subCommand.equals("spe")) {
			this.chooseSpe(array);
		} else if (subCommand.equals("ability")) {
			this.unlockAbility(array);
		} else if (subCommand.equals("avatar")) {
			this.unlockAvatarElement(array);
		} else if (subCommand.equals("unavatar")) {
			this.lockAvatar(array);
		} else if (subCommand.equals("free")) {
			this.free(array);
		} else {
			sender.sendMessage("Unkown subcommand : " + subCommand);
			this.usage();
		}

	}

	private void free(String[] args) {
		if (!this.sender.hasPermission("bending.command.learning")) {
			this.sender.sendMessage("You do not have this permission");
			return;
		}
		if (args.length < 3) {
			this.sender.sendMessage("Insufficient args");
			return;
		}
		String subChoice = args[1];
		if (subChoice.equals("spe")) {
			BendingPlayer target = this.bPlayer;
			if (args.length == 4) {
				target = BendingPlayer.getBendingPlayer(Bukkit.getPlayer(args[3]));
			}
			if (target == null) {
				this.sender.sendMessage("No player targeted (" + args[2] + ")");
				return;
			}
			if (target.isBender(BendingElement.ChiBlocker)) {
				BendingElement type = BendingElement.getType(args[2]);
				if (type == null) {
					return;
				}
				List<BendingElement> bends = target.getBendingTypes();
				bends.remove(type);
				target.removeBender();
				for (BendingElement bend : bends) {
					target.addBender(bend);
				}
			} else {
				BendingAffinity spe = BendingAffinity.getType(args[2]);
				if (spe == null) {
					return;
				}
				for (BendingAbilities ability : BendingAbilities.values()) {
					if ((ability.getAffinity() != null) && ability.getAffinity().equals(spe)) {
						this.plugin.removePermission(target.getPlayer(), ability);
					}
				}
				target.removeAffinity(spe);
				this.sender.sendMessage(ChatColor.GREEN + "Player " + target.getPlayer().getName() + " has lost " + spe.name() + ".");
			}
		} else if (subChoice.equals("ability")) {
			BendingPlayer target = this.bPlayer;
			if (args.length == 4) {
				target = BendingPlayer.getBendingPlayer(Bukkit.getPlayer(args[3]));
			}
			if (target == null) {
				this.sender.sendMessage("No player targeted (" + args[2] + ")");
				return;
			}
			BendingAbilities ability = BendingAbilities.getAbility(args[2]);
			if (ability == null) {
				this.sender.sendMessage(ChatColor.RED + "Ability " + ability + " is unknown");
				return;
			}
			if (this.plugin.removePermission(target.getPlayer(), ability)) {
				target.clearAbilities();
			}
			this.sender.sendMessage(ChatColor.GREEN + "Player " + target.getPlayer().getName() + " has lost " + ability.name() + ".");
		}
	}

	private void lockAvatar(String[] args) {
		if (!this.sender.hasPermission("bending.command.learning")) {
			this.sender.sendMessage("You do not have this permission");
			return;
		}
		if (args.length < 2) {
			this.sender.sendMessage("Insufficient args");
			return;
		}
		BendingPlayer target = this.bPlayer;
		if (args.length == 3) {
			target = BendingPlayer.getBendingPlayer(Bukkit.getPlayer(args[2]));
		}
		if (target == null) {
			this.sender.sendMessage("No player targeted (" + args[2] + ")");
			return;
		}
		BendingElement type = BendingElement.getType(args[1]);
		if (type == null) {
			this.sender.sendMessage("Incorrect type : " + args[1]);
			return;
		}
		// Reset bending here
		target.setBender(type);
		// Ensure it keeps all previous
		for (BendingAbilities ability : BendingAbilities.values()) {
			if (ability.getElement().equals(type) && !ability.isAffinity()) {
				this.plugin.addPermission(target.getPlayer(), ability);
			}
		}
		this.sender.sendMessage(ChatColor.DARK_GREEN + "Player " + target.getPlayer().getName() + " has lost all element except : " + type.name());
	}

	private void unlockAvatarElement(String[] args) {
		if (!this.sender.hasPermission("bending.command.learning")) {
			this.sender.sendMessage("You do not have this permission");
			return;
		}
		if (args.length < 2) {
			this.sender.sendMessage("Insufficient args");
			return;
		}
		BendingPlayer target = this.bPlayer;
		if (args.length == 3) {
			target = BendingPlayer.getBendingPlayer(Bukkit.getPlayer(args[2]));
		}
		if (target == null) {
			this.sender.sendMessage("No player targeted (" + args[2] + ")");
			return;
		}
		if (!EntityTools.canBend(target.getPlayer(), BendingAbilities.AvatarState)) {
			this.sender.sendMessage(target.getPlayer().getName() + " is not an avatar");
			return;
		}
		BendingElement type = BendingElement.getType(args[1]);
		if (type == null) {
			this.sender.sendMessage("Incorrect type : " + args[1]);
			return;
		}
		if (!target.isBender(type)) {
			target.addBender(type);
			ChatColor color = PluginTools.getColor(Settings.getColorString(type.name()));
			String message = "Congratulations, you can now bend " + type.name();
			target.getPlayer().sendMessage(color + message);
			for (BendingAbilities ability : BendingAbilities.values()) {
				if (ability.getElement().equals(type) && !ability.isAffinity()) {
					this.plugin.addPermission(target.getPlayer(), ability);
					message = "You can now use " + ability.name();
					target.getPlayer().sendMessage(color + message);
				}
			}
			this.sender.sendMessage(ChatColor.DARK_GREEN + "Player " + target.getPlayer().getName() + " has unlocked element : " + type.name());
		} else {
			this.sender.sendMessage(ChatColor.RED + "Player " + target.getPlayer().getName() + " already bend : " + type.name());
		}
	}

	private void unlockAbility(String[] args) {
		if (!this.sender.hasPermission("bending.command.learning")) {
			this.sender.sendMessage(ChatColor.RED + "You do not have this permission");
			return;
		}
		if (args.length < 2) {
			this.sender.sendMessage(ChatColor.RED + "Insufficient args");
			return;
		}

		BendingPlayer target = this.bPlayer;
		if (args.length == 3) {
			target = BendingPlayer.getBendingPlayer(Bukkit.getPlayer(args[2]));
		}
		if (target == null) {
			this.sender.sendMessage("No player targeted (" + args[2] + ")");
			return;
		}

		BendingAbilities ability = BendingAbilities.getAbility(args[1]);
		if (ability == null) {
			this.sender.sendMessage(ChatColor.RED + "Ability " + ability + " is unknown");
			return;
		}
		if (target.isBender(BendingElement.ChiBlocker) && !BendingAbilities.isChiBlocking(ability) && !this.plugin.isBasicBendingAbility(ability)) {
			this.sender.sendMessage(ChatColor.RED + "Ability " + ability + " is not available for chiblocker");
			return;
		}
		if (target.isBender(ability.getElement())) {
			if (this.plugin.addPermission(target.getPlayer(), ability)) {
				ChatColor color = PluginTools.getColor(Settings.getColorString(ability.getElement().name()));
				String message = "You can now use " + ability.name();
				target.getPlayer().sendMessage(color + message);
				this.sender.sendMessage(ChatColor.GREEN + "Player " + target.getPlayer().getName() + " has received " + ability.name() + ".");
			} else {
				this.sender.sendMessage(ChatColor.RED + target.getPlayer().getName() + " did not receive " + ability + " (permission was denied)");
			}
		} else {
			this.sender.sendMessage(ChatColor.RED + target.getPlayer().getName() + " do not bend " + ability.getElement());
		}
	}

	private void chooseSpe(String[] args) {
		if (!this.sender.hasPermission("bending.command.learning")) {
			this.sender.sendMessage("You do not have this permission");
			return;
		}
		if (args.length < 2) {
			this.sender.sendMessage("Insufficient args");
			return;
		}
		String speString = args[1];

		BendingPlayer target = this.bPlayer;
		if (args.length == 3) {
			target = BendingPlayer.getBendingPlayer(Bukkit.getPlayer(args[2]));
		}
		if (target == null) {
			this.sender.sendMessage("No player targeted (" + args[2] + ")");
			return;
		}

		BendingAffinity spe = BendingAffinity.getType(speString);
		if (spe != null) {
			if (target.isBender(spe.getElement())) {
				boolean canLearn = true;
				for (BendingAffinity speTarget : target.getSpecializations()) {
					if (speTarget.getElement().equals(spe.getElement())) {
						canLearn = false;
					}
				}
				if (canLearn) {
					target.setAffinity(spe);
					ChatColor color = PluginTools.getColor(Settings.getColorString(spe.getElement().name()));
					String message = "Congratulations, you can now use " + spe.name();
					target.getPlayer().sendMessage(color + message);
					for (BendingAbilities ability : BendingAbilities.values()) {
						if (ability.isAffinity() && ability.getAffinity().equals(spe)) {
							this.plugin.addPermission(target.getPlayer(), ability);
							message = "You can now use " + ability.name();
							target.getPlayer().sendMessage(color + message);
						}
					}
					this.sender.sendMessage(ChatColor.GREEN + "Player " + target.getPlayer().getName() + " has received " + spe.name() + ".");
				} else {
					this.sender.sendMessage(ChatColor.RED + target.getPlayer().getName() + " already have a specialization for " + spe.getElement());
				}
			} else {
				this.sender.sendMessage(ChatColor.RED + target.getPlayer().getName() + " cannot take " + spe.name() + " because cannot bend " + spe.getElement() + ".");
			}
		} else {
			this.sender.sendMessage(ChatColor.RED + speString + " is not a valid a specialization");
		}
	}

	private void usage() {
		if (!this.sender.hasPermission("bending.command.learning")) {
			this.sender.sendMessage("You do not have this permission");
			return;
		}

		this.sender.sendMessage("/blearning spe <SPECIALISATION | ELEMENT> <PLAYER>");
		this.sender.sendMessage(" -- Unlock safely a specialization for a player, chevking he has already the required element, <PLAYER> is optional");
		this.sender.sendMessage("/blearning ability <ABILITY> <PLAYER>");
		this.sender.sendMessage(" -- Unlock safely an ability for a player, checking he has already the required element, <PLAYER> is optional");
		this.sender.sendMessage("/blearning avatar <ELEMENT> <PLAYER>");
		this.sender.sendMessage(" -- Make an avatar learn an element and all bending (except specialisation) associated, checking if player is really an avatar, <PLAYER> is optional");
		this.sender.sendMessage("/blearning unavatar <ELEMENT> <PLAYER>");
		this.sender.sendMessage(" -- Make a player (not necessary an avatar in fact) forget all element except one, but keep all abilities from that element, <PLAYER> is mandatory");
		this.sender.sendMessage("/blearning free spe <ELEMENT|SPE> <PLAYER>");
		this.sender.sendMessage(" -- Make a player forget a specilization (or element for CHI-B)");
		this.sender.sendMessage("/blearning free ability <ABILITY> <PLAYER>");
		this.sender.sendMessage(" -- Make a player forget an ability");
	}
}
