package net.avatar.realms.spigot.bending;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.avatar.realms.spigot.bending.abilities.BendingAbilities;
import net.avatar.realms.spigot.bending.controller.LanguageParameter;

// TODO : Load all messages. Note that there will be custom messages in the Plugin Data folder and
// original english messages in the .jar file
public class Messages {

	private static final String FILENAME = "messages.properties";

	@LanguageParameter("command.error.not_console")
	public static String NOT_CONSOLE_COMMAND = "This command cannot be executed by the console.";

	@LanguageParameter("command.error.no_permission")
	public static String NO_PERMISSION = "You do not have the permission to execute that command.";

	@LanguageParameter("command.error.invalid_ability")
	public static String INVALID_ABILITY = "This is an invalid ability.";

	@LanguageParameter("command.error.not_element")
	public static String NOT_HAVE_ELEMENT = "You do not have the proper element : ";

	@LanguageParameter("command.ability_bound")
	public static String ABILITY_BOUND = "{0} bound to slot {1}.";

	@LanguageParameter("command.error.console_specify_player")
	public static String CONSOLE_SPECIFY_PLAYER = "You must specify a player if you are a console.";

	@LanguageParameter("command.error.invalid_player")
	public static String INVALID_PLAYER = "Invalid player (may not be connected on server).";

	@LanguageParameter("command.error.invalid_element")
	public static String INVALID_ELEMENT = "Invalid element";

	@LanguageParameter("command.error.choose_other")
	public static String ERROR_CHOOSE_OTHER = "You do not have the permission to change another one's bending element";

	@LanguageParameter("command.error.change_element")
	public static String ERROR_CHANGE_ELEMENT = "You do not have the permission to change your bending element";

	@LanguageParameter("command.other_change_you")
	public static String OTHER_CHANGE_YOU = "Your bending bending element has been set to {0}.";

	@LanguageParameter("command.you_change_other")
	public static String YOU_CHANGE_OTHER = "You've set the bending element of {0} to {1}";

	@LanguageParameter("command.element_set")
	public static String SET_ELEMENT = "You've set your bending element to : {0}.";

	@LanguageParameter("command.you_remove_other")
	public static String YOU_REMOVE_OTHER = "You've removed the bending of {0}.";

	@LanguageParameter("command.other_remove_you")
	public static String OTHER_REMOVE_YOU = "Your bending has been removed.";

	@LanguageParameter("command.you_no_exist")
	public static String YOU_NO_EXIST = "You do not exist.";

	@LanguageParameter("bending.toggle_on")
	public static String TOGGLE_ON = "You activated your bending.";

	@LanguageParameter("bending.toggle_off")
	public static String TOGGLE_OFF = "You deactivated your bending.";

	@LanguageParameter("bending.toggle_spe_on")
	public static String TOGGLE_SPE_ON = "You activated your bending specialization.";

	@LanguageParameter("bending.toggle_spe_off")
	public static String TOGGLE_SPE_OFF = "You deactivated your bending specialization.";

	@LanguageParameter("command.no_bending")
	public static String NO_BENDING = "No bending";

	@LanguageParameter("command.who.is_bending")
	public static String WHO_IS_BENDING = "{0} is a {1} bender.";

	@LanguageParameter("command.who_is_chi")
	public static String WHO_IS_CHI = "{0} is a chi-blocker.";

	@LanguageParameter("command.not_element_able")
	public static String NOT_ELEMENT_ABLE = "The target is not able to use the {0} bending.";

	@LanguageParameter("command.already_element")
	public static String ALREADY_ELEMENT = "The target already has this bending.";

	@LanguageParameter("command.you_were_added")
	public static String YOU_WERE_ADDED = "You now can use {0}.";

	@LanguageParameter("command.you_added")
	public static String YOU_ADDED = "You added {0} to {1}.";

	@LanguageParameter("command.reloaded")
	public static String RELOADED = "Reload done";

	@LanguageParameter("command.nothing_bound")
	public static String NOTHING_BOUND = "Nothing bound";

	private static Properties lines;
	private static File languageFile;
	private static InputStream input;

	public static void loadMessages() {
		lines = new Properties();

		File folder = Bending.plugin.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}

		languageFile = new File(folder + File.separator + FILENAME);
		if (languageFile.exists()) {
			try {
				input = new FileInputStream(languageFile);
			}
			catch (Exception e) {
				input = Messages.class.getClassLoader().getResourceAsStream(FILENAME);
			}
		}
		else {
			input = Messages.class.getClassLoader().getResourceAsStream(FILENAME);
		}

		try {
			lines.load(input);
		}
		catch (IOException e) {
			e.printStackTrace();
			// Should never happen, hope so
		}

	}

	public static String getAbilityDescription(BendingAbilities ability) {
		return lines.getProperty("abilities." + ability.getElement().name().toLowerCase() + "." + ability.name().toLowerCase());
	}

	public static String getString(String string) {
		return lines.getProperty(string);
	}

	public static void sendMessage(Player player, String key) {
		sendMessage(player, key, ChatColor.WHITE);
	}

	public static void sendMessage(Player player, String key, ChatColor color) {
		if (player == null) {
			Bending.plugin.getLogger().info(color + getString(key));
		}
		else {
			player.sendMessage(color + getString(key));
		}
	}

	public static void sendMessage(Player player, String key, Map<String, String> params) {
		sendMessage(player, key, ChatColor.WHITE, params);
	}

	public static void sendMessage(Player player, String key, ChatColor color, Map<String, String> params) {
		String msg = getString(key);
		for (String k : params.keySet()) {
			msg.replaceAll("%" + k + "%", params.get(k));
		}
		if (player == null) {
			Bending.plugin.getLogger().info(color + msg);
		}
		else {
			player.sendMessage(color + msg);
		}
	}

}
