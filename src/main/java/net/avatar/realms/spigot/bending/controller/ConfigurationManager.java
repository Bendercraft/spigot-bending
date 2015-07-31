package net.avatar.realms.spigot.bending.controller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.avatar.realms.spigot.bending.Bending;

public abstract class ConfigurationManager {
	
	public static void generateDefaultConfigFile(File configFile, Map<String, Field> fields) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		configFile.delete();
		try {
			configFile.createNewFile();
		} catch (IOException e) {
			Bending.plugin.getLogger().severe("Couldn't create default config file");
			e.printStackTrace();
		}
		if(configFile.exists()) {
			FileWriter writer = null;
			try {
				writer = new FileWriter(configFile);
				JsonObject root = new JsonObject();
				for(String path : fields.keySet()) {
					Field f = fields.get(path);
					ConfigurationParameter a = f.getAnnotation(ConfigurationParameter.class);
					if(a != null) {
						f.setAccessible(true);
						try {
							if(f.getType().equals(int.class)) {
								getLast(root, path).addProperty(getLastKey(path), f.getInt(null));
							} 
							else if(f.getType().equals(double.class)) {
								getLast(root, path).addProperty(getLastKey(path), f.getDouble(null));
							} 
							else if(f.getType().equals(short.class)) {
								getLast(root, path).addProperty(getLastKey(path), f.getShort(null));
							} 
							else if(f.getType().equals(long.class)) {
								getLast(root, path).addProperty(getLastKey(path), f.getLong(null));
							} 
							else if(f.getType().equals(float.class)) {
								getLast(root, path).addProperty(getLastKey(path), f.getFloat(null));
							} 
							else if(f.getType().equals(boolean.class)) {
								getLast(root, path).addProperty(getLastKey(path), f.getBoolean(null));
							} 
							else if(f.getType().equals(String.class)) {
								getLast(root, path).addProperty(getLastKey(path), (String)f.get(null));
							} 
							else if (f.getType().equals(String[].class)) {
								getLast(root, path).add(getLastKey(path), gson.toJsonTree(f.get(null), String[].class));
							}
							else {
								Bending.plugin.getLogger().warning("Config variable "+f.getName()+" found with annoted param "+path+" and in config file but is of unknown type "+f.getType());
							}
						} catch (IllegalAccessException e) {
							Bending.plugin.getLogger().severe("IllegalAccessException on config param "+path);
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							Bending.plugin.getLogger().severe("IllegalArgument type on "+a.value());
							e.printStackTrace();
						}
					}
				}
				gson.toJson(root, writer);
			} catch (IOException e) {
				Bending.plugin.getLogger().severe("Error while writing default config file data");
				e.printStackTrace();
			}
			if(writer != null) {
    			try {
    				writer.close();
				} catch (IOException e) {
					
				}
    		}
		}
	}
	
	public static void applyConfiguration(File configFile, Map<String, Field> fields) {
		Gson gson = new GsonBuilder().create();
        
        //Load groups
        if(configFile.exists()) {
        	FileReader reader = null;
        	try {
				reader = new FileReader(configFile);
				JsonObject config = gson.fromJson(reader, JsonObject.class);
				
				for(String path : fields.keySet()) {
					Field f = fields.get(path);
					ConfigurationParameter a = f.getAnnotation(ConfigurationParameter.class);
					if(a != null) {
						f.setAccessible(true);
						try {
							JsonElement param = getLast(config, path).get(getLastKey(path));
							if(param != null) {
								if(f.getType().equals(int.class)) {
									f.setInt(null, param.getAsInt());
								} 
								else if(f.getType().equals(double.class)) {
									f.setDouble(null, param.getAsDouble());
								} 
								else if(f.getType().equals(short.class)) {
									f.setShort(null, param.getAsShort());
								} 
								else if(f.getType().equals(long.class)) {
									f.setLong(null, param.getAsLong());
								} 
								else if(f.getType().equals(float.class)) {
									f.set(null, param.getAsFloat());
								} 
								else if(f.getType().equals(boolean.class)) {
									f.setBoolean(null, param.getAsBoolean());
								} 
								else if(f.getType().equals(String.class)) {
									f.set(null, param.getAsString());
								} 
								else if (f.getType().equals(String[].class)) {
									f.set(null, gson.fromJson(param ,String[].class));
								}
								else {
									Bending.plugin.getLogger().warning("Config variable "+f.getName()+" found with annoted param '"+path+"' and in config file but is of unknown type "+f.getType());
								}
							} else {
								Bending.plugin.getLogger().warning("Config variable "+f.getName()+" found with annoted param '"+path+"' but not in config file");
							}
						} catch (IllegalAccessException e) {
							Bending.plugin.getLogger().severe("IllegalAccessException on config param "+a.value());
							e.printStackTrace();
						} catch (IllegalArgumentException e) {
							Bending.plugin.getLogger().severe("IllegalArgument type on "+a.value());
							e.printStackTrace();
						}
					}
				}
			} catch (Exception e) {
				Bending.plugin.getLogger().severe("Error while loading config file data");
				e.printStackTrace();
			}
        	if(reader != null) {
    			try {
					reader.close();
				} catch (IOException e) {
					
				}
    		}
        } else {
        	Bending.plugin.getLogger().warning("Config file is missing, should be at "+configFile.getPath());
        	File defaultConfigFile = new File(configFile.getParentFile(), configFile.getName()+".default");
        	generateDefaultConfigFile(defaultConfigFile, fields);
        }
	}
	
	private static JsonObject getLast(JsonObject base, String key) {
		if(key.lastIndexOf(".") == -1) {
			return base;
		}
		String subkey = key.substring(0, key.lastIndexOf("."));
		String leftover = key.substring(key.lastIndexOf(".")+1);
		if(base.get(subkey) == null) {
			base.add(subkey, new JsonObject());
		}
		return getLast(base.get(subkey).getAsJsonObject(), leftover);
	}
	
	private static String getLastKey(String whole) {
		return whole.substring(whole.lastIndexOf(".")+1, whole.length());
	}
}
