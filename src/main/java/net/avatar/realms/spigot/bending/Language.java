package net.avatar.realms.spigot.bending;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.avatar.realms.spigot.bending.utils.PluginTools;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;


public class Language {

	private HashMap<String, HashMap<String, String>> descriptions = new HashMap<String, HashMap<String, String>>();
	private HashMap<String, String> defaultdescriptions = new HashMap<String, String>();
	public static final String on_air_choose = Messages.getString("general.on_air_choose"); //$NON-NLS-1$
	public static final String on_earth_choose = Messages.getString("general.on_earth_choose"); //$NON-NLS-1$
	public static final String on_water_choose = Messages.getString("general.on_water_choose"); //$NON-NLS-1$
	public static final String on_fire_choose = Messages.getString("general.on_fire_choose"); //$NON-NLS-1$
	public static final String on_chi_choose = Messages.getString("general.on_chi_choose"); //$NON-NLS-1$
	private List<String> supportedlanguages = new ArrayList<String>();
	private String defaultlanguage = "en"; //$NON-NLS-1$
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
		this.defaultlanguage = this.config.getString("DefaultLanguage", this.defaultlanguage); //$NON-NLS-1$
		this.config.set("DefaultLanguage", this.defaultlanguage); //$NON-NLS-1$
		if (this.config.contains("SupportedLanguages")) { //$NON-NLS-1$
			this.supportedlanguages = this.config.getStringList("SupportedLanguages"); //$NON-NLS-1$
		}
		this.config.set("SupportedLanguages", this.supportedlanguages); //$NON-NLS-1$

		this.defaultdescriptions.put("AirChoose", on_air_choose); //$NON-NLS-1$
		this.defaultdescriptions.put("ChiChoose", on_chi_choose); //$NON-NLS-1$
		this.defaultdescriptions.put("EarthChoose", on_earth_choose); //$NON-NLS-1$
		this.defaultdescriptions.put("FireChoose", on_fire_choose); //$NON-NLS-1$
		this.defaultdescriptions.put("WaterChoose", on_water_choose); //$NON-NLS-1$
	}

	public String getMessage (String language, String key) {
		String index = language.toLowerCase() + "." + key; //$NON-NLS-1$
		if (!this.descriptions.containsKey(language)) {
			PluginTools.verbose("Language '" + language + "' not supported!"); //$NON-NLS-1$ //$NON-NLS-2$
			return "Language not supported!"; //$NON-NLS-1$
		}
		if (!this.descriptions.get(language).containsKey(index)) {
			PluginTools.verbose("'" + index + "' doesn't exist?"); //$NON-NLS-1$ //$NON-NLS-2$
			return Messages.getString("general.errors"); //$NON-NLS-1$
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
