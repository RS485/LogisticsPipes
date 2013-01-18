package logisticspipes.routing;

import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.api.transport.IPassiveItemContribution;

public class RoutedEntityItemSaveHandler implements IPassiveItemContribution {
	
	public RoutedEntityItemSaveHandler() {}
	
	public RoutedEntityItemSaveHandler(RoutedEntityItem routedEntityItem) {
		this.routedEntityItem = routedEntityItem;
	}
	
	private RoutedEntityItem routedEntityItem;
	
	public int sourceint;
	public int destinationint;
	public int bufferCounter = 0;
	public boolean arrived;
	public TransportMode transportMode = TransportMode.Unknown;
	
	private void extract() {
		if(routedEntityItem != null) {
			sourceint = routedEntityItem.sourceint;
			destinationint = routedEntityItem.destinationint;
			bufferCounter = routedEntityItem.getBufferCounter();
			arrived = routedEntityItem.arrived;
			transportMode = routedEntityItem.getTransportMode();
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		if(nbttagcompound.hasKey("sourceint")) {
			sourceint = nbttagcompound.getInteger("sourceint");
		}
		if(nbttagcompound.hasKey("destinationint")) {
			destinationint = nbttagcompound.getInteger("destinationint");
		}
		arrived = nbttagcompound.getBoolean("arrived");
		bufferCounter = nbttagcompound.getInteger("bufferCounter");
		transportMode = TransportMode.values()[nbttagcompound.getInteger("transportMode")];
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		this.extract();
		if(sourceint >= 0) {
			nbttagcompound.setInteger("sourceint", sourceint);
		}
		if(destinationint >= 0) {
		}
		nbttagcompound.setBoolean("arrived", arrived);
		nbttagcompound.setInteger("bufferCounter", bufferCounter);
		nbttagcompound.setInteger("transportMode", transportMode.ordinal());
	}
}
