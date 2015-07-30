package net.avatar.realms.spigot.bending.learning;

import java.util.List;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.abilities.BendingPlayer;
import net.avatar.realms.spigot.bending.abilities.BendingSpecializationType;
import net.avatar.realms.spigot.bending.abilities.BendingType;
import net.avatar.realms.spigot.bending.controller.Settings;
import net.avatar.realms.spigot.bending.utils.EntityTools;
import net.avatar.realms.spigot.bending.utils.PluginTools;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LearningCommand {
	private BendingLearning plugin;
	
	private CommandSender sender;
	private BendingPlayer bPlayer;

	public LearningCommand(BendingLearning plugin, CommandSender sender, String[] args) {
		this.plugin = plugin;
		this.sender = sender;
		if(sender instanceof Player) {
			this.bPlayer = BendingPlayer.getBendingPlayer((Player) sender);
		}
		
		String[] adjusted = new String[args.length-1];
		for(int i=1 ; i < args.length ; i++) {
			adjusted[i-1] = args[i];
		}
		args = adjusted;
		
		if(args.length < 1) {
			this.usage();
			return;
		}
		String subCommand = args[0];
		if(subCommand.equals("spe")) {
			this.chooseSpe(args);
		} else if(subCommand.equals("ability")) {
			this.unlockAbility(args);
		} else if(subCommand.equals("avatar")) {
			this.unlockAvatarElement(args);
		} else if(subCommand.equals("unavatar")) {
			this.lockAvatar(args);
		} else if(subCommand.equals("free")) {
			this.free(args);
		} else {
			sender.sendMessage("Unkown subcommand : "+subCommand);
			this.usage();
		}
	}
	
	private void free(String[] args) {
		if(!sender.hasPermission("bending.command.learning")) {
			sender.sendMessage("You do not have this permission");
			return;
		}
		if(args.length < 3) {
			sender.sendMessage("Insufficient args");
			return;
		}
		String subChoice = args[1];
		if(subChoice.equals("spe")) {
			BendingPlayer target = this.bPlayer;
			if(args.length == 4) {
				target = BendingPlayer.getBendingPlayer(Bukkit.getPlayer(args[3]));
			}
			if(target == null) {
				sender.sendMessage("No player targeted ("+args[2]+")");
				return;
			}
			if(target.isBender(BendingType.ChiBlocker)) {
				BendingType type = BendingType.getType(args[2]);
				if(type == null) {
					return;
				}
				List<BendingType> bends = target.getBendingTypes();
				bends.remove(type);
				target.removeBender();
				for(BendingType bend : bends) {
					target.addBender(bend);
				}
			} else {
				BendingSpecializationType spe = BendingSpecializationType.getType(args[2]);
				if(spe == null) {
					return;
				}
				for(Abilities ability : Abilities.values()) {
					if(ability.getSpecialization() != null && ability.getSpecialization().equals(spe)) {
						this.plugin.removePermission(target.getPlayer(), ability);
					}
				}
				target.removeSpecialization(spe);
				sender.sendMessage(ChatColor.GREEN+"Player "+target.getPlayer().getName()+" has lost "+spe.name()+".");
			}
		} else if(subChoice.equals("ability")) {
			BendingPlayer target = this.bPlayer;
			if(args.length == 4) {
				target = BendingPlayer.getBendingPlayer(Bukkit.getPlayer(args[3]));
			}
			if(target == null) {
				sender.sendMessage("No player targeted ("+args[2]+")");
				return;
			}
			Abilities ability = Abilities.getAbility(args[2]);
			if(ability == null) {
				sender.sendMessage(ChatColor.RED +"Ability "+ability+" is unknown");
				return;
			}
			if(this.plugin.removePermission(target.getPlayer(), ability)) {
				target.clearAbilities();
			}
			sender.sendMessage(ChatColor.GREEN+"Player "+target.getPlayer().getName()+" has lost "+ability.name()+".");
		}
	}
	
	private void lockAvatar(String[] args) {
		if(!sender.hasPermission("bending.command.learning")) {
			sender.sendMessage("You do not have this permission");
			return;
		}
		if(args.length < 2) {
			sender.sendMessage("Insufficient args");
			return;
		}
		BendingPlayer target = this.bPlayer;
		if(args.length == 3) {
			target = BendingPlayer.getBendingPlayer(Bukkit.getPlayer(args[2]));
		}
		if(target == null) {
			sender.sendMessage("No player targeted ("+args[2]+")");
			return;
		}
		BendingType type = BendingType.getType(args[1]);
		if(type == null) {
			sender.sendMessage("Incorrect type : "+args[1]);
			return;
		}
		//Reset bending here
		target.setBender(type);
		//Ensure it keeps all previous
		for(Abilities ability : Abilities.values()) {
			if(ability.getElement().equals(type) && !ability.isSpecialization()) {
				this.plugin.addPermission(target.getPlayer(), ability);
			}
		}
		sender.sendMessage(ChatColor.DARK_GREEN+"Player "+target.getPlayer().getName()+" has lost all element except : "+type.name());
	}
	
	private void unlockAvatarElement(String[] args) {
		if(!sender.hasPermission("bending.command.learning")) {
			sender.sendMessage("You do not have this permission");
			return;
		}
		if(args.length < 2) {
			sender.sendMessage("Insufficient args");
			return;
		}
		BendingPlayer target = this.bPlayer;
		if(args.length == 3) {
			target = BendingPlayer.getBendingPlayer(Bukkit.getPlayer(args[2]));
		}
		if(target == null) {
			sender.sendMessage("No player targeted ("+args[2]+")");
			return;
		}
		if(!EntityTools.canBend(target.getPlayer(), Abilities.AvatarState)) {
			sender.sendMessage(target.getPlayer().getName()+" is not an avatar");
			return;
		}
		BendingType type = BendingType.getType(args[1]);
		if(type == null) {
			sender.sendMessage("Incorrect type : "+args[1]);
			return;
		}
		if(!target.isBender(type)) {
			target.addBender(type);
			ChatColor color = PluginTools.getColor(Settings.getColorString(type.name()));
			String message = "Congratulations, you can now bend "+type.name();
			target.getPlayer().sendMessage(color+message);
			for(Abilities ability : Abilities.values()) {
				if(ability.getElement().equals(type) && !ability.isSpecialization()) {
					this.plugin.addPermission(target.getPlayer(), ability);
					message = "You can now use "+ability.name();
					target.getPlayer().sendMessage(color+message);
				}
			}
			sender.sendMessage(ChatColor.DARK_GREEN+"Player "+target.getPlayer().getName()+" has unlocked element : "+type.name());
		} else {
			sender.sendMessage(ChatColor.RED+"Player "+target.getPlayer().getName()+" already bend : "+type.name());
		}
	}
	
	private void unlockAbility(String[] args) {
		if(!sender.hasPermission("bending.command.learning")) {
			sender.sendMessage(ChatColor.RED + "You do not have this permission");
			return;
		}
		if(args.length < 2) {
			sender.sendMessage(ChatColor.RED +"Insufficient args");
			return;
		}
		
		BendingPlayer target = this.bPlayer;
		if(args.length == 3) {
			target = BendingPlayer.getBendingPlayer(Bukkit.getPlayer(args[2]));
		}
		if(target == null) {
			sender.sendMessage("No player targeted ("+args[2]+")");
			return;
		}
	
		Abilities ability = Abilities.getAbility(args[1]);
		if (ability == null) {
			sender.sendMessage(ChatColor.RED +"Ability "+ability+" is unknown");
			return;
		}
		if (target.isBender(BendingType.ChiBlocker)
				&& !Abilities.isChiBlocking(ability)
				&& !this.plugin.isBasicBendingAbility(ability)) {
			sender.sendMessage(ChatColor.RED +"Ability "+ability+" is not available for chiblocker");
			return;
		}
		if(target.isBender(ability.getElement())) {
			if(this.plugin.addPermission(target.getPlayer(), ability)) {
				ChatColor color = PluginTools.getColor(Settings.getColorString(ability.getElement().name()));
				String message = "You can now use "+ability.name();
				target.getPlayer().sendMessage(color+message);
				sender.sendMessage(ChatColor.GREEN+"Player "+target.getPlayer().getName()+" has received "+ability.name()+".");
			} else {
				sender.sendMessage(ChatColor.RED +target.getPlayer().getName()+" did not receive "+ability+" (permission was denied)");
			}
		} else {
			sender.sendMessage(ChatColor.RED +target.getPlayer().getName()+" do not bend "+ability.getElement());
		}
	}
	
	private void chooseSpe(String[] args) {
		if(!sender.hasPermission("bending.command.learning")) {
			sender.sendMessage("You do not have this permission");
			return;
		}
		if(args.length < 2) {
			sender.sendMessage("Insufficient args");
			return;
		}
		String speString = args[1];
		
		BendingPlayer target = this.bPlayer;
		if(args.length == 3) {
			target = BendingPlayer.getBendingPlayer(Bukkit.getPlayer(args[2]));
		}
		if(target == null) {
			sender.sendMessage("No player targeted ("+args[2]+")");
			return;
		}
		
		BendingSpecializationType spe = BendingSpecializationType.getType(speString);
		BendingType type = BendingType.getType(speString);
		
		if(target.isBender(BendingType.ChiBlocker)) {
			if (target.getBendingTypes().size() == 1 && target.getSpecializations().size() <= 0) {
				if (spe != null) {
					if(target.isBender(spe.getElement())) {
						boolean canLearn = true;
						for(BendingSpecializationType speTarget : target.getSpecializations()) {
							if(speTarget.getElement().equals(spe.getElement())) {
								canLearn = false;
							}
						}
						if(canLearn) {
							target.setSpecialization(spe);
							ChatColor color = PluginTools.getColor(Settings.getColorString(spe.getElement().name()));
							String message = "Congratulations, you can now use "+spe.name();
							target.getPlayer().sendMessage(color+message);
							for(Abilities ability : Abilities.values()) {
								if(ability.isSpecialization() && ability.getSpecialization().equals(spe)) {
									this.plugin.addPermission(target.getPlayer(), ability);
									message = "You can now use "+ability.name();
									target.getPlayer().sendMessage(color+message);
								}
							}
						}
					}
				} else if (type != null) {
					target.addBender(type);
					ChatColor color = PluginTools.getColor(Settings.getColorString(type.name()));
					String message = "Congratulations, you can now bend "+type.name()+" as well as "+BendingType.ChiBlocker.name();
					target.getPlayer().sendMessage(color+message);
					for(Abilities ability : Abilities.values()) {
						if(ability.getElement().equals(type)) {
							if(EntityTools.hasPermission(target.getPlayer(), ability)) {
								message = "You can now use "+ability.name();
								target.getPlayer().sendMessage(color+message);
							}
						}
					}
				} else {
					sender.sendMessage(ChatColor.RED+speString+" is not a valid a specialization/element");
				}
			} else {
				sender.sendMessage(ChatColor.RED+target.getPlayer().getName()+" already have a specialization");
			}
		} else {
			if(spe != null) {
				if(target.isBender(spe.getElement())) {
					boolean canLearn = true;
					for(BendingSpecializationType speTarget : target.getSpecializations()) {
						if(speTarget.getElement().equals(spe.getElement())) {
							canLearn = false;
						}
					}
					if(canLearn) {
						target.setSpecialization(spe);
						ChatColor color = PluginTools.getColor(Settings.getColorString(spe.getElement().name()));
						String message = "Congratulations, you can now use "+spe.name();
						target.getPlayer().sendMessage(color+message);
						for(Abilities ability : Abilities.values()) {
							if(ability.isSpecialization() && ability.getSpecialization().equals(spe)) {
								this.plugin.addPermission(target.getPlayer(), ability);
								message = "You can now use "+ability.name();
								target.getPlayer().sendMessage(color+message);
							}
						}
						sender.sendMessage(ChatColor.GREEN+"Player "+target.getPlayer().getName()+" has received "+spe.name()+".");
					} else {
						sender.sendMessage(ChatColor.RED+target.getPlayer().getName()+" already have a specialization for "+spe.getElement());
					}
				} else {
					sender.sendMessage(ChatColor.RED+target.getPlayer().getName()+" cannot take "+spe.name()+" because cannot bend "+spe.getElement()+".");
				}
			} else {
				sender.sendMessage(ChatColor.RED+speString+" is not a valid a specialization");
			}
		}
	}
	
	private void usage() {
		if(!sender.hasPermission("bending.command.learning")) {
			sender.sendMessage("You do not have this permission");
			return;
		}
		
		sender.sendMessage("/blearning spe <SPECIALISATION | ELEMENT> <PLAYER>");
		sender.sendMessage(" -- Unlock safely a specialization for a player, chevking he has already the required element, <PLAYER> is optional");
		sender.sendMessage("/blearning ability <ABILITY> <PLAYER>");
		sender.sendMessage(" -- Unlock safely an ability for a player, checking he has already the required element, <PLAYER> is optional");
		sender.sendMessage("/blearning avatar <ELEMENT> <PLAYER>");
		sender.sendMessage(" -- Make an avatar learn an element and all bending (except specialisation) associated, checking if player is really an avatar, <PLAYER> is optional");
		sender.sendMessage("/blearning unavatar <ELEMENT> <PLAYER>");
		sender.sendMessage(" -- Make a player (not necessary an avatar in fact) forget all element except one, but keep all abilities from that element, <PLAYER> is mandatory");
		sender.sendMessage("/blearning free spe <ELEMENT|SPE> <PLAYER>");
		sender.sendMessage(" -- Make a player forget a specilization (or element for CHI-B)");
		sender.sendMessage("/blearning free ability <ABILITY> <PLAYER>");
		sender.sendMessage(" -- Make a player forget an ability");
	}
}
