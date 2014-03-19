package logisticspipes.routing;

import java.util.UUID;

import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import net.minecraft.nbt.NBTTagCompound;

public class RoutedEntityItemSaveHandler {
	
	public RoutedEntityItemSaveHandler(RoutedEntityItem routedEntityItem) {
		this.routedEntityItem = routedEntityItem;
	}
	
	private RoutedEntityItem routedEntityItem;
	
	public UUID destinationUUID;
	public int bufferCounter = 0;
	public boolean arrived;
	public TransportMode transportMode = TransportMode.Unknown;
	
	private void extract() {
		if(routedEntityItem != null) {
			destinationUUID = routedEntityItem.destinationUUID;
			bufferCounter = routedEntityItem.getBufferCounter();
			arrived = routedEntityItem.arrived;
			transportMode = routedEntityItem.getTransportMode();
		}
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		if(nbttagcompound.hasKey("destinationUUID")) {
			destinationUUID = UUID.fromString(nbttagcompound.getString("destinationUUID"));
		}
		arrived = nbttagcompound.getBoolean("arrived");
		bufferCounter = nbttagcompound.getInteger("bufferCounter");
		transportMode = TransportMode.values()[nbttagcompound.getInteger("transportMode")];
	}

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		this.extract();
		if(destinationUUID != null) {
			nbttagcompound.setString("destinationUUID", destinationUUID.toString());
		}
		nbttagcompound.setBoolean("arrived", arrived);
		nbttagcompound.setInteger("bufferCounter", bufferCounter);
		nbttagcompound.setInteger("transportMode", transportMode.ordinal());
	}
}
