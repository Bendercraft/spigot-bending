package net.avatar.realms.spigot.bending.integrations.worldguard.protocollib;

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
			
			boolean overriden = false;
			Map<Object, BendingAbility> marks = AbilityManager.getManager().getInstances(Mark.NAME);
			if(marks != null && !marks.isEmpty()) {
				for(BendingAbility raw : marks.values()) {
					Mark ability = (Mark) raw;
					if(ability.getTarget() != null && ability.getTarget().getEntityId() == entityID) {
						overriden = true;
						break;
					}
				}
			}
			
			if(!overriden) {
				boolean suppress = false;
				Map<Object, BendingAbility> tremorsenses = AbilityManager.getManager().getInstances(TremorSense.NAME);
				if(tremorsenses != null && !tremorsenses.isEmpty()) {
					for(BendingAbility raw : tremorsenses.values()) {
						TremorSense ability = (TremorSense) raw;
						if(ability.getEntities().containsKey(entityID) && event.getPlayer() != ability.getPlayer()) {
							suppress = true;
							break;
						}
					}
				}
				
				if(suppress) {
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
						} catch(FieldAccessException e) {
							
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
