package logisticspipes.routing;

import java.util.LinkedList;
import java.util.UUID;

import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.proxy.SimpleServiceLocator;
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
	public LinkedList<Integer> relays = new LinkedList<Integer>(); //TODO
	
	private void extract() {
		if(routedEntityItem != null) {
			destinationUUID = routedEntityItem.destinationUUID;
			bufferCounter = routedEntityItem.getBufferCounter();
			arrived = routedEntityItem.arrived;
			transportMode = routedEntityItem.getTransportMode();
			relays.clear();
			relays.addAll(routedEntityItem.relays);
		}
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
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

	public void writeToNBT(NBTTagCompound nbttagcompound) {
		this.extract();
		if(destinationUUID !=null) {
			nbttagcompound.setString("destinationUUID", destinationUUID.toString());
		}
		nbttagcompound.setBoolean("arrived", arrived);
		nbttagcompound.setInteger("bufferCounter", bufferCounter);
		nbttagcompound.setInteger("transportMode", transportMode.ordinal());
		nbttagcompound.setInteger("relaysSize", relays.size());
		for(int i=0;i<relays.size();i++) {
			nbttagcompound.setString("relays" + i, SimpleServiceLocator.routerManager.getRouter(relays.get(i)).getId().toString());
		}
	}
}
