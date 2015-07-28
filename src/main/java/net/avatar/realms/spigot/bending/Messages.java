package net.avatar.realms.spigot.bending;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.avatar.realms.spigot.bending.abilities.Abilities;

public class Messages {
	
	private static String FILENAME = "messages.properties";
	private static Messages messages = null;
	
	private Properties lines;
	private File languageFile;
	private InputStream input;
	
	public static Messages getMessages() {
		if (messages == null) {
			messages = new Messages();
		}
		
		return messages;
	}
	
	private Messages() {
		lines = new Properties();
		
		File folder = Bending.plugin.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
		
		languageFile = new File(folder + File.separator + FILENAME);
		if (languageFile.exists()) {
			try {
				input = new FileInputStream(languageFile);
			} catch (Exception e) {
				input = this.getClass().getClassLoader().getResourceAsStream(FILENAME);
			}
		}
		else {
			input = this.getClass().getClassLoader().getResourceAsStream(FILENAME);
		}
		
		Bending.plugin.getLogger().warning(languageFile.toString());
		Bending.plugin.getLogger().warning(input.toString());
		try {
			lines.load(input);
		} catch (IOException e) {
			e.printStackTrace();
			// Should never happen, hope so
		}
	}
	
	public String getAbilityDescription(Abilities ability) {
		return lines.getProperty("abilities."+ability.getElement().name().toLowerCase()+"."+ability.name().toLowerCase());
	}
	
}
