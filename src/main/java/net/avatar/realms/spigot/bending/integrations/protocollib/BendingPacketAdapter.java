package net.avatar.realms.spigot.bending.integrations.protocollib;

import java.util.List;
import java.util.Map;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import net.avatar.realms.spigot.bending.Bending;
import net.avatar.realms.spigot.bending.abilities.AbilityManager;
import net.avatar.realms.spigot.bending.abilities.BendingAbility;
import net.avatar.realms.spigot.bending.abilities.arts.Mark;
import net.avatar.realms.spigot.bending.abilities.earth.TremorSense;
import org.bukkit.entity.Player;

public class BendingPacketAdapter extends PacketAdapter {

	public BendingPacketAdapter(Bending plugin) {
		super(plugin, PacketType.Play.Server.ENTITY_METADATA);
	}
	
	 @Override
     public void onPacketSending(PacketEvent event) {
		 PacketContainer packet = event.getPacket();
		// Because we listen for "ENTITY_METADATA", packet we should have is "PacketPlayOutEntityMetadata"
		// it has one integer filed : entity id
		int entityID = packet.getIntegers().readSafely(0);
		 //If the entity is marked, there is no need to filter the packet
		if(!Mark.isMarked(entityID)) {
			//If the entity is tremorsensed by the player, there is no need to filter glowing
			if(!TremorSense.isEntityTremorsensedByPlayer(entityID, event.getPlayer())) {
				//This packet also have a collection of datawatcher with only one on it
				List<WrappedWatchableObject> metadatas = packet.getWatchableCollectionModifier().readSafely(0);
				WrappedWatchableObject status = null;
				for (WrappedWatchableObject metadata : metadatas) {
					//See http://wiki.vg/Entities for explanation on why index 0
					try {
						if (metadata.getIndex() == 0) {
							status = metadata;
			                break;
			            }
					}
					catch(FieldAccessException e) {
					}
				}
				if(status != null) {
					byte mask = (byte) status.getValue(); //0x40 = Glowing effect mask
					mask &= ~0x40;//0x40 = Glowing effect mask
					status.setValue(mask);
				}
			}
		}
     }
}
