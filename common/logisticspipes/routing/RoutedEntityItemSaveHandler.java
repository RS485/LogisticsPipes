package logisticspipes.routing;

import java.util.UUID;

import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.api.transport.IPassiveItemContribution;

public class RoutedEntityItemSaveHandler implements IPassiveItemContribution {
	
	public RoutedEntityItemSaveHandler() {}
	
	public RoutedEntityItemSaveHandler(RoutedEntityItem routedEntityItem) {
		this.routedEntityItem = routedEntityItem;
	}
	
	private RoutedEntityItem routedEntityItem;
	
	public UUID sourceUUID;
	public UUID destinationUUID;
	public int bufferCounter = 0;
	public boolean arrived;
	public TransportMode transportMode = TransportMode.Unknown;
	
	private void extract() {
		if(routedEntityItem != null) {
			sourceUUID = routedEntityItem.sourceUUID;
			destinationUUID = routedEntityItem.destinationUUID;
			bufferCounter = routedEntityItem.getBufferCounter();
			arrived = routedEntityItem.arrived;
			transportMode = routedEntityItem.getTransportMode();
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		if(nbttagcompound.hasKey("sourceUUID")) {
			sourceUUID = UUID.fromString(nbttagcompound.getString("sourceUUID"));
		}
		if(nbttagcompound.hasKey("destinationUUID")) {
			destinationUUID = UUID.fromString(nbttagcompound.getString("destinationUUID"));
		}
		arrived = nbttagcompound.getBoolean("arrived");
		bufferCounter = nbttagcompound.getInteger("bufferCounter");
		transportMode = TransportMode.values()[nbttagcompound.getInteger("transportMode")];
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		this.extract();
		if(sourceUUID != null) {
			nbttagcompound.setString("sourceUUID", sourceUUID.toString());
		}
		if(destinationUUID != null) {
			nbttagcompound.setString("destinationUUID", destinationUUID.toString());
		}
		nbttagcompound.setBoolean("arrived", arrived);
		nbttagcompound.setInteger("bufferCounter", bufferCounter);
		nbttagcompound.setInteger("transportMode", transportMode.ordinal());
	}
}
