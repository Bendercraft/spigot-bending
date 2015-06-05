package net.avatar.realms.spigot.bending;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.avatar.realms.spigot.bending.abilities.Abilities;
import net.avatar.realms.spigot.bending.controller.BendingManager;
import net.avatar.realms.spigot.bending.utils.PluginTools;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class Language {

	private HashMap<String, HashMap<String, String>> descriptions = new HashMap<String, HashMap<String, String>>();
	private HashMap<String, String> defaultdescriptions = new HashMap<String, String>();
	public static final String on_air_choose = "As an airbender, you now take no falling damage, have faster sprinting and higher "
			+ "jumps. Additionally, daily activities are easier for you - your food meter decays at a "
			+ "much slower rate";
	public static final String on_earth_choose = "As an earthbender, upon landing on bendable earth, you will briefly turn the "
			+ "area to soft sand, negating any fall damage you would have otherwise taken.";
	public static final String on_water_choose = "As a waterbender, you no longer take any fall damage when landing on ice, snow "
			+ "or even 1-block-deep water. Additionally, sneaking in the water with a bending ability "
			+ "selected that does not utilize sneak (or no ability at all)"
			+ " will give you accelerated swimming. "
			+ "Lastly, you can pull water from plants with your abilities.";
	public static final String on_fire_choose = "As a firebender, you now more quickly smother yourself when you catch on fire.";
	public static final String on_chi_choose = "As a chiblocker, you have no active abilities to bind. Instead, you have improved "
			+ "sprinting and jumping, have a dodge chance and deal more damage with your fists. "
			+ "Additionally, punching a bender will block his/her chi for a few seconds, preventing "
			+ "him/her from bending (and even stopping their passive!).";
	private List<String> supportedlanguages = new ArrayList<String>();
	private String defaultlanguage = "en";
	FileConfiguration config = new YamlConfiguration();

	public void load (File file) {
		try {
			if (file.exists()) {
				this.config.load(file);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		this.supportedlanguages.add(this.defaultlanguage);
		this.defaultlanguage = this.config.getString("DefaultLanguage", this.defaultlanguage);
		this.config.set("DefaultLanguage", this.defaultlanguage);
		if (this.config.contains("SupportedLanguages")) {
			this.supportedlanguages = this.config.getStringList("SupportedLanguages");
		}
		this.config.set("SupportedLanguages", this.supportedlanguages);
		Set<String> indexList = new HashSet<String>();
		for (Abilities a : Abilities.values()) {
			indexList.add(a.name());
			this.defaultdescriptions.put(a.name(), a.getDescription());
		}
		this.defaultdescriptions.put("AirChoose", on_air_choose);
		this.defaultdescriptions.put("ChiChoose", on_chi_choose);
		this.defaultdescriptions.put("EarthChoose", on_earth_choose);
		this.defaultdescriptions.put("FireChoose", on_fire_choose);
		this.defaultdescriptions.put("WaterChoose", on_water_choose);
		for (String language : this.supportedlanguages) {
			HashMap<String, String> langdescriptions = new HashMap<String, String>();
			for (String index : indexList) {
				String element;
				if (Abilities.isAirbending(Abilities.getAbility(index))) {
					element = "Air";
				}
				else if (Abilities.isWaterbending(Abilities.getAbility(index))) {
					element = "Water";
				}
				else if (Abilities.isFirebending(Abilities.getAbility(index))) {
					element = "Fire";
				}
				else if (Abilities.isEarthbending(Abilities.getAbility(index))) {
					element = "Earth";
				}
				else if (Abilities.isChiBlocking(Abilities.getAbility(index))) {
					element = "Chiblocker";
				}
				else {
					element = "Other";
				}
				String key = language + "." + element + "." + index;
				langdescriptions.put(key, this.config.getString(key, this.defaultdescriptions.get(index)));
			}

			langdescriptions.put(language + ".Air.AirChoose", this.config.getString(language
					+ ".Air.AirChoose", this.defaultdescriptions.get("AirChoose")));
			langdescriptions.put(language + ".Fire.FireChoose", this.config.getString(language
					+ ".Fire.FireChoose", this.defaultdescriptions.get("FireChoose")));
			langdescriptions.put(language + ".Earth.EarthChoose", this.config.getString(language
					+ ".Earth.EarthChoose", this.defaultdescriptions.get("EarthChoose")));
			langdescriptions.put(language + ".Water.WaterChoose", this.config.getString(language
					+ ".Water.WaterChoose", this.defaultdescriptions.get("WaterChoose")));
			langdescriptions.put(language + ".Chiblocker.ChiChoose", this.config.getString(language
					+ ".Chiblocker.ChiChoose", this.defaultdescriptions.get("ChiChoose")));
			HashMap<String, String> messages = new HashMap<String, String>();
			messages.put(language + ".Messages.Sunrise", this.config.getString(language + "."
					+ "Messages.Sunrise", BendingManager.defaultsunrisemessage));
			messages.put(language + ".Messages.Sunset", this.config.getString(language + "."
					+ "Messages.Sunset", BendingManager.defaultsunsetmessage));
			messages.put(language + ".Messages.Moonrise", this.config.getString(language + "."
					+ "Messages.Moonrise", BendingManager.defaultmoonrisemessage));
			messages.put(language + ".Messages.Moonset", this.config.getString(language + "."
					+ "Messages.Moonset", BendingManager.defaultmoonsetmessage));
			HashMap<String, String> general = new HashMap<String, String>();
			general.put(language + ".General.usage", this.config
					.getString(language + ".General.usage", BendingCommand.usage));
			general.put(language + ".General.the_server", this.config.getString(language
					+ ".General.the_server", BendingCommand.the_server));
			general.put(language + ".General.who_usage", this.config.getString(language
					+ ".General.who_usage", BendingCommand.who_usage));
			general.put(language + ".General.who_not_on_server", this.config.getString(language
					+ ".General.who_not_on_server", BendingCommand.who_not_on_server));
			general.put(language + ".General.who_player_usage", this.config.getString(language
					+ ".General.who_player_usage", BendingCommand.who_player_usage));
			general.put(language + ".General.choose_usage", this.config.getString(language
					+ ".General.choose_usage", BendingCommand.choose_usage));
			general.put(language + ".General.choose_player_usage", this.config.getString(language
					+ ".General.choose_player_usage", BendingCommand.choose_player_usage));
			general.put(language + ".General.no_perms_air", this.config.getString(language
					+ ".General.no_perms_air", BendingCommand.no_perms_air));
			general.put(language + ".General.no_perms_fire", this.config.getString(language
					+ ".General.no_perms_fire", BendingCommand.no_perms_fire));
			general.put(language + ".General.no_perms_earth", this.config.getString(language
					+ ".General.no_perms_earth", BendingCommand.no_perms_earth));
			general.put(language + ".General.no_perms_water", this.config.getString(language
					+ ".General.no_perms_water", BendingCommand.no_perms_water));
			general.put(language + ".General.no_perms_chi", this.config.getString(language
					+ ".General.no_perms_chi", BendingCommand.no_perms_chi));
			general.put(language + ".General.other_no_perms_air", this.config.getString(language
					+ ".General.other_no_perms_air", BendingCommand.other_no_perms_air));
			general.put(language + ".General.other_no_perms_fire", this.config.getString(language
					+ ".General.other_no_perms_fire", BendingCommand.other_no_perms_fire));
			general.put(language + ".General.other_no_perms_earth", this.config.getString(language
					+ ".General.other_no_perms_earth", BendingCommand.other_no_perms_earth));
			general.put(language + ".General.other_no_perms_water", this.config.getString(language
					+ ".General.other_no_perms_water", BendingCommand.other_no_perms_water));
			general.put(language + ".General.other_no_perms_chi", this.config.getString(language
					+ ".General.other_no_perms_chi", BendingCommand.other_no_perms_chi));
			general.put(language + ".General.choosen_air", this.config.getString(language
					+ ".General.choosen_air", BendingCommand.choosen_air));
			general.put(language + ".General.choosen_fire", this.config.getString(language
					+ ".General.choosen_fire", BendingCommand.choosen_fire));
			general.put(language + ".General.choosen_earth", this.config.getString(language
					+ ".General.choosen_earth", BendingCommand.choosen_earth));
			general.put(language + ".General.choosen_water", this.config.getString(language
					+ ".General.choosen_water", BendingCommand.choosen_water));
			general.put(language + ".General.choosen_chi", this.config.getString(language
					+ ".General.choosen_chi", BendingCommand.choosen_chi));
			general.put(language + ".General.you_changed", this.config.getString(language
					+ ".General.you_changed", BendingCommand.you_changed));
			general.put(language + ".General.changed_you", this.config.getString(language
					+ ".General.changed_you", BendingCommand.changed_you));
			general.put(language + ".General.import_usage", this.config.getString(language
					+ ".General.import_usage", BendingCommand.import_usage));
			general.put(language + ".General.import_noSQL", this.config.getString(language
					+ ".General.import_noSQL", BendingCommand.import_noSQL));
			general.put(language + ".General.import_success", this.config.getString(language
					+ ".General.import_success", BendingCommand.import_success));
			general.put(language + ".General.no_execute_perms", this.config.getString(language
					+ ".General.no_execute_perms", BendingCommand.no_execute_perms));
			general.put(language + ".General.no_bind_perms", this.config.getString(language
					+ ".General.no_bind_perms", BendingCommand.no_bind_perms));
			general.put(language + ".General.no_use_perms", this.config.getString(language
					+ ".General.no_use_perms", BendingCommand.no_use_perms));
			general.put(language + ".General.reload_usage", this.config.getString(language
					+ ".General.reload_usage"));
			general.put(language + ".General.reload_success", this.config.getString(language
					+ ".General.reload_success", BendingCommand.reload_success));
			general.put(language + ".General.display_usage", this.config.getString(language
					+ ".General.display_usage", BendingCommand.display_usage));
			general.put(language + ".General.display_element_usage", this.config.getString(language
					+ ".General.display_element_usage", BendingCommand.display_element_usage));
			general.put(language + ".General.slot", this.config
					.getString(language + ".General.slot", BendingCommand.slot));
			general.put(language + ".General.display_no_abilities", this.config.getString(language
					+ ".General.display_no_abilities", BendingCommand.display_no_abilities));
			general.put(language + ".General.toggle_usage", this.config.getString(language
					+ ".General.toggle_usage", BendingCommand.toggle_usage));
			general.put(language + ".General.toggle_off", this.config.getString(language
					+ ".General.toggle_off", BendingCommand.toggle_off));
			general.put(language + ".General.toggle_on", this.config.getString(language
					+ ".General.toggle_on", BendingCommand.toggle_on));
			general.put(language + ".General.admin_toggle_off", this.config.getString(language
					+ ".General.admin_toggle_off", BendingCommand.admin_toggle_off));
			general.put(language + ".General.admin_toggle_on", this.config.getString(language
					+ ".General.admin_toggle_on", BendingCommand.admin_toggle_on));
			general.put(language + ".General.admin_toggle", this.config.getString(language
					+ ".General.admin_toggle", BendingCommand.admin_toggle));
			general.put(language + ".General.not_from_console", this.config.getString(language
					+ ".General.not_from_console", BendingCommand.not_from_console));
			general.put(language + ".General.permaremove_message", this.config.getString(language
					+ ".General.permaremove_message", BendingCommand.permaremove_message));
			general.put(language + ".General.you_permaremove", this.config.getString(language
					+ ".General.you_permaremove", BendingCommand.you_permaremove));
			general.put(language + ".General.permaremove_you", this.config.getString(language
					+ ".General.permaremove_you", BendingCommand.permaremove_you));
			general.put(language + ".General.remove_usage", this.config.getString(language
					+ ".General.remove_usage", BendingCommand.remove_usage));
			general.put(language + ".General.remove_you", this.config.getString(language
					+ ".General.remove_you", BendingCommand.remove_you));
			general.put(language + ".General.you_remove", this.config.getString(language
					+ ".General.you_remove", BendingCommand.you_remove));
			general.put(language + ".General.add_self", this.config
					.getString(language + ".General.add_self", BendingCommand.add_self));

			general.put(language + ".General.add_other", this.config.getString(language
					+ ".General.add_other", BendingCommand.add_other));
			general.put(language + ".General.clear_all", this.config.getString(language
					+ ".General.clear_all", BendingCommand.clear_all));
			general.put(language + ".General.clear_slot", this.config.getString(language
					+ ".General.clear_slot", BendingCommand.clear_slot));
			general.put(language + ".General.clear_item", this.config.getString(language
					+ ".General.clear_item", BendingCommand.clear_item));
			general.put(language + ".General.cleared_message", this.config.getString(language
					+ ".General.cleared_message", BendingCommand.cleared_message));
			general.put(language + ".General.slot_item_cleared", this.config.getString(language
					+ ".General.slot_item_cleared", BendingCommand.slot_item_cleared));
			general.put(language + ".General.bind_slot", this.config.getString(language
					+ ".General.bind_slot", BendingCommand.bind_slot));
			general.put(language + ".General.bind_to_slot", this.config.getString(language
					+ ".General.bind_to_slot", BendingCommand.bind_to_slot));
			general.put(language + ".General.bind_item", this.config.getString(language
					+ ".General.bind_item", BendingCommand.bind_item));
			general.put(language + ".General.bind_to_item", this.config.getString(language
					+ ".General.bind_to_item", BendingCommand.bind_to_item));
			general.put(language + ".General.not_air", this.config
					.getString(language + ".General.not_air", BendingCommand.not_air));
			general.put(language + ".General.not_earth", this.config.getString(language
					+ ".General.not_earth", BendingCommand.not_earth));
			general.put(language + ".General.not_fire", this.config
					.getString(language + ".General.not_fire", BendingCommand.not_fire));
			general.put(language + ".General.not_water", this.config.getString(language
					+ ".General.not_water", BendingCommand.not_water));
			general.put(language + ".General.not_chi", this.config
					.getString(language + ".General.not_chi", BendingCommand.not_chi));
			general.put(language + ".General.bound_to", this.config
					.getString(language + ".General.bound_to", BendingCommand.bound_to));
			general.put(language + ".General.bound_to_slot", this.config.getString(language
					+ ".General.bound_to_slot", BendingCommand.bound_to_slot));
			general.put(language + ".General.help_list", this.config.getString(language
					+ ".General.help_list", BendingCommand.help_list));
			general.put(language + ".General.command_list", this.config.getString(language
					+ ".General.command_list", BendingCommand.command_list));
			general.put(language + ".General.ability_list", this.config.getString(language
					+ ".General.ability_list", BendingCommand.ability_list));
			general.put(language + ".General.player", this.config
					.getString(language + ".General.player", BendingCommand.player));
			general.put(language + ".General.element", this.config
					.getString(language + ".General.element", BendingCommand.element));
			general.put(language + ".General.language_success", this.config.getString(language
					+ ".General.language_success", BendingCommand.language_success));
			general.put(language + ".General.language_not_supported", this.config.getString(language
					+ ".General.language_not_supported", BendingCommand.language_not_supported));
			general.put(language + ".General.your_language", this.config.getString(language
					+ ".General.your_language", BendingCommand.your_language));
			general.put(language + ".General.default_language", this.config.getString(language
					+ ".General.default_language", BendingCommand.default_language));
			general.put(language + ".General.supported_languages", this.config.getString(language
					+ ".General.supported_languages", BendingCommand.supported_languages));
			general.put(language + ".General.language_usage", this.config.getString(language
					+ ".General.language_usage", BendingCommand.language_usage));
			general.put(language + ".General.language_change_usage", this.config.getString(language
					+ ".General.language_change_usage", BendingCommand.language_change_usage));
			general.put(language + ".General.you_already_air", this.config.getString(language
					+ ".General.you_already_air", BendingCommand.you_already_air));
			general.put(language + ".General.you_already_earth", this.config.getString(language
					+ ".General.you_already_earth", BendingCommand.you_already_earth));
			general.put(language + ".General.you_already_fire", this.config.getString(language
					+ ".General.you_already_fire", BendingCommand.you_already_fire));
			general.put(language + ".General.you_already_water", this.config.getString(language
					+ ".General.you_already_water", BendingCommand.you_already_water));
			general.put(language + ".General.you_already_chi", this.config.getString(language
					+ ".General.you_already_chi", BendingCommand.you_already_chi));
			general.put(language + ".General.they_already_air", this.config.getString(language
					+ ".General.they_already_air", BendingCommand.they_already_air));
			general.put(language + ".General.they_already_earth", this.config.getString(language
					+ ".General.they_already_earth", BendingCommand.they_already_earth));
			general.put(language + ".General.they_already_fire", this.config.getString(language
					+ ".General.they_already_fire", BendingCommand.they_already_fire));
			general.put(language + ".General.they_already_water", this.config.getString(language
					+ ".General.they_already_water", BendingCommand.they_already_water));
			general.put(language + ".General.they_already_chi", this.config.getString(language
					+ ".General.they_already_chi", BendingCommand.they_already_chi));
			general.put(language + ".General.add_air", this.config
					.getString(language + ".General.add_air", BendingCommand.add_air));
			general.put(language + ".General.add_earth", this.config.getString(language
					+ ".General.add_earth", BendingCommand.add_earth));
			general.put(language + ".General.add_fire", this.config
					.getString(language + ".General.add_fire", BendingCommand.add_fire));
			general.put(language + ".General.add_water", this.config.getString(language
					+ ".General.add_water", BendingCommand.add_water));
			general.put(language + ".General.add_chi", this.config
					.getString(language + ".General.add_chi", BendingCommand.add_chi));

			general.put(language + ".General.add_you_air", this.config.getString(language
					+ ".General.add_you_air", BendingCommand.add_you_air));
			general.put(language + ".General.add_you_earth", this.config.getString(language
					+ ".General.add_you_earth", BendingCommand.add_you_earth));
			general.put(language + ".General.add_you_fire", this.config.getString(language
					+ ".General.add_you_fire", BendingCommand.add_you_fire));
			general.put(language + ".General.add_you_water", this.config.getString(language
					+ ".General.add_you_water", BendingCommand.add_you_water));
			general.put(language + ".General.add_you_chi", this.config.getString(language
					+ ".General.add_you_chi", BendingCommand.add_you_chi));
			general.put(language + ".General.you_add_air", this.config.getString(language
					+ ".General.you_add_air", BendingCommand.you_add_air));
			general.put(language + ".General.you_add_earth", this.config.getString(language
					+ ".General.you_add_earth", BendingCommand.you_add_earth));
			general.put(language + ".General.you_add_fire", this.config.getString(language
					+ ".General.you_add_fire", BendingCommand.you_add_fire));
			general.put(language + ".General.you_add_water", this.config.getString(language
					+ ".General.you_add_water", BendingCommand.you_add_water));
			general.put(language + ".General.you_add_chi", this.config.getString(language
					+ ".General.you_add_chi", BendingCommand.you_add_chi));
			general.put(language + ".General.bind_mode_usage", this.config.getString(language
					+ ".General.bind_mode_usage", BendingCommand.bind_mode_usage));
			general.put(language + ".General.bind_mode_change_usage", this.config.getString(language
					+ ".General.bind_mode_change_usage", BendingCommand.bind_mode_change_usage));

			general.put(language + ".General.bind_mode_change", this.config.getString(language
					+ ".General.bind_mode_change", BendingCommand.bind_mode_change));
			general.put(language + ".General.your_bind_mode", this.config.getString(language
					+ ".General.your_bind_mode", BendingCommand.your_bind_mode));
			general.put(language + ".General.server_bind_mode", this.config.getString(language
					+ ".General.server_bind_mode", BendingCommand.server_bind_mode));
			general.put(language + ".General.version_usage", this.config.getString(language
					+ ".General.version_usage", BendingCommand.version_usage));
			HashMap<String, String> total = new HashMap<String, String>();

			for (String index : messages.keySet()) {
				if (total.containsKey(index)) {
					PluginTools.verbose("Duplicate values in the code!");
					return;
				}
				total.put(index, messages.get(index));
				this.config.set(index, messages.get(index));
			}

			for (String index : general.keySet()) {
				if (total.containsKey(index)) {
					PluginTools.verbose("Duplicate values in the code!");
					return;
				}
				total.put(index, general.get(index));
				this.config.set(index, general.get(index));
			}

			for (String index : langdescriptions.keySet()) {
				if (total.containsKey(index)) {
					PluginTools.verbose("Duplicate values in the code!");
					return;
				}
				total.put(index, langdescriptions.get(index));
				this.config.set(index, langdescriptions.get(index));
			}

			this.descriptions.put(language, total);
		}
		try {
			this.config.save(file);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getMessage (String language, String key) {
		String index = language.toLowerCase() + "." + key;
		if (!this.descriptions.containsKey(language)) {
			PluginTools.verbose("Language '" + language + "' not supported!");
			return "Language not supported!";
		}
		if (!this.descriptions.get(language).containsKey(index)) {
			PluginTools.verbose("'" + index + "' doesn't exist?");
			return "There was an error...";
		}
		return this.descriptions.get(language).get(index);
	}

	public List<String> getSupportedLanguages () {
		return this.supportedlanguages;
	}

	public String getDefaultLanguage () {
		return this.defaultlanguage;
	}
}
