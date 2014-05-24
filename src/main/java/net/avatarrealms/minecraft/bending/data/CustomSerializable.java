package net.avatarrealms.minecraft.bending.data;

import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface CustomSerializable extends ConfigurationSerializable {

	Map<String, Object> serialize();

}
