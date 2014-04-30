package logisticspipes.network.packets.orderer;

import java.io.IOException;
import java.util.List;

import logisticspipes.interfaces.IRequestWatcher;
import logisticspipes.network.IReadListObject;
import logisticspipes.network.IWriteListObject;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.routing.LogisticsOrder;
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
	private List<LogisticsOrder> orders;
	
	public OrdererWatchPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeItemIdentifierStack(stack);
		data.writeList(orders, new IWriteListObject<LogisticsOrder>() {
			@Override
			public void writeObject(LPDataOutputStream data, LogisticsOrder order) throws IOException {
				data.writeOrder(order);
			}});
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		stack = data.readItemIdentifierStack();
		orders = data.readList(new IReadListObject<LogisticsOrder>() {
			@Override
			public LogisticsOrder readObject(LPDataInputStream data) throws IOException {
				return data.readOrder();
			}});
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
