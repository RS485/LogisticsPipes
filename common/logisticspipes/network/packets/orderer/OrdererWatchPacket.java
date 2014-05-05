package logisticspipes.network.packets.orderer;

import java.io.IOException;

import logisticspipes.interfaces.IRequestWatcher;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.utils.item.ItemIdentifierStack;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

@Accessors(chain=true)
public class OrdererWatchPacket extends IntegerCoordinatesPacket {
	
	@Getter
	@Setter
	private ItemIdentifierStack stack;
	
	@Getter
	@Setter
	private LinkedLogisticsOrderList orders;
	
	public OrdererWatchPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeItemIdentifierStack(stack);
		data.writeLinkedLogisticsOrderList(orders);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		stack = data.readItemIdentifierStack();
		orders = data.readLinkedLogisticsOrderList();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe tile = this.getPipe(player.worldObj);
		if(tile.pipe instanceof IRequestWatcher) {
			((IRequestWatcher)tile.pipe).handleClientSideListInfo(getInteger(), getStack(), getOrders());
		}
	}
	
	@Override
	public ModernPacket template() {
		return new OrdererWatchPacket(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
