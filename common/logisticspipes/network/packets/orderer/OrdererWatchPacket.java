package logisticspipes.network.packets.orderer;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.interfaces.IRequestWatcher;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.request.resources.IResource;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class OrdererWatchPacket extends IntegerCoordinatesPacket {

	@Getter
	@Setter
	private IResource stack;

	@Getter
	@Setter
	private LinkedLogisticsOrderList orders;

	public OrdererWatchPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeResource(stack);
		output.writeLinkedLogisticsOrderList(orders);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		stack = input.readResource();
		orders = input.readLinkedLogisticsOrderList();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe tile = this.getPipe(player.worldObj);
		if (tile.pipe instanceof IRequestWatcher) {
			((IRequestWatcher) tile.pipe).handleClientSideListInfo(getInteger(), getStack(), getOrders());
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
