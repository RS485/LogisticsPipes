package logisticspipes.network.packets;

import java.util.UUID;

import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.RoutedEntityItem;
import net.minecraft.src.World;
import buildcraft.api.core.Orientations;
import buildcraft.core.network.PacketPayload;
import buildcraft.transport.EntityData;
import buildcraft.transport.network.PacketPipeTransportContent;

public class PacketPipeLogisticsContent extends PacketPipeTransportContent {
	
	public PacketPipeLogisticsContent() {
		super();
	}
	
	public PacketPipeLogisticsContent(PacketPipeTransportContent packet) {
		super();
		this.payload = packet.payload;
		this.posX = packet.posX;
		this.posY = packet.posY;
		this.posZ = packet.posZ;
	}

	public PacketPipeLogisticsContent(int x, int y, int z, EntityData data) {
		super(x,y,z,data);
		if(!(data.item instanceof IRoutedItem)) return;
		IRoutedItem routed = SimpleServiceLocator.buildCraftProxy.GetRoutedItem(data.item);
		final IRouter routerSource = SimpleServiceLocator.routerManager.getRouter(routed.getSource());
		final IRouter routerDest = SimpleServiceLocator.routerManager.getRouter(routed.getDestination());
		
		PacketPayload additions = new PacketPayload(1,0,2);
		if(routerSource != null) {
			additions.stringPayload[0] = routerSource.getId().toString();
		} else {
			additions.stringPayload[0] = "";
		}
		
		if(routerDest != null) {
			additions.stringPayload[1] = routerDest.getId().toString();
		} else {
			additions.stringPayload[1] = "";
		}
		
		additions.intPayload[0] = routed.getTransportMode().ordinal();
		
		if(super.payload == null) {
			super.payload = new PacketPayload(6, 4, 0);

			payload.intPayload[0] = data.item.getEntityId();
			payload.intPayload[1] = data.input.ordinal();
			payload.intPayload[2] = data.output.ordinal();
			payload.intPayload[3] = data.item.getItemStack().itemID;
			payload.intPayload[4] = data.item.getItemStack().stackSize;
			payload.intPayload[5] = data.item.getItemStack().getItemDamage();

			payload.floatPayload[0] = (float) data.item.getPosition().x;
			payload.floatPayload[1] = (float) data.item.getPosition().y;
			payload.floatPayload[2] = (float) data.item.getPosition().z;
			payload.floatPayload[3] = data.item.getSpeed();
		}
		super.payload.append(additions);
	}
	
	public static boolean isPacket(PacketPipeTransportContent packet) {
		if(packet.payload.stringPayload.length < 2) {
			return false;
		}
		return true;
	}
	
	public UUID getSourceUUID(World world) {
		if(this.payload.stringPayload.length < 2) {
			return null;
		}
		if(payload.stringPayload[0] == null || payload.stringPayload[0].equals("")) {
			return null;
		}
		return UUID.fromString(payload.stringPayload[0]);
	}
	
	public UUID getDestUUID(World world) {
		if(this.payload.stringPayload.length < 2) {
			return null;
		}
		if(payload.stringPayload[1] == null || payload.stringPayload[1].equals("")) {
			return null;
		}
		return UUID.fromString(payload.stringPayload[1]);
	}
	
	public TransportMode getTransportMode() {
		if(this.payload.intPayload.length < 7) {
			return TransportMode.Default;
		}
		return TransportMode.values()[this.payload.intPayload[7]];
	}
}
