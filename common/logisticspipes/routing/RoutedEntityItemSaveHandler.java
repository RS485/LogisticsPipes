package logisticspipes.routing;

import java.util.LinkedList;
import java.util.UUID;

import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.proxy.SimpleServiceLocator;
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
	public LinkedList<Integer> relays = new LinkedList<Integer>(); //TODO
	
	private void extract() {
		if(routedEntityItem != null) {
			sourceUUID = routedEntityItem.sourceUUID;
			destinationUUID = routedEntityItem.destinationUUID;
			bufferCounter = routedEntityItem.getBufferCounter();
			arrived = routedEntityItem.arrived;
			transportMode = routedEntityItem.getTransportMode();
			relays.clear();
			relays.addAll(routedEntityItem.relays);
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
		relays.clear();
		int size = nbttagcompound.getInteger("relaysSize");
		for(int i=0;i<size;i++) {
			//TODO: WARNING - UUID's for not-yet-loaded chunks will be -1, and cause "issues" later.
			relays.add(SimpleServiceLocator.routerManager.getIDforUUID(UUID.fromString(nbttagcompound.getString("relays" + i))));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		this.extract();
		if(sourceUUID != null) {
			nbttagcompound.setString("sourceUUID", sourceUUID.toString());
		}
		if(destinationUUID !=null) {
			nbttagcompound.setString("destinationUUID", destinationUUID.toString());
		}
		nbttagcompound.setBoolean("arrived", arrived);
		nbttagcompound.setInteger("bufferCounter", bufferCounter);
		nbttagcompound.setInteger("transportMode", transportMode.ordinal());
		nbttagcompound.setInteger("relaysSize", relays.size());
		for(int i=0;i<relays.size();i++) {
			nbttagcompound.setString("relays" + i, relays.get(i).toString());
		}
	}
}
