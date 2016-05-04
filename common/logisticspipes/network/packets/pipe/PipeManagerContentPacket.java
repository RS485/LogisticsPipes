package logisticspipes.network.packets.pipe;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.LogisticsOrder;
import logisticspipes.routing.order.LogisticsOrderManager;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class PipeManagerContentPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private LogisticsOrderManager<? extends LogisticsOrder, ?> manager;

	private List<IOrderInfoProvider> clientOrder;

	public PipeManagerContentPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld());
		if (pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) {
			return;
		}
		CoreRoutedPipe cPipe = (CoreRoutedPipe) pipe.pipe;
		cPipe.getClientSideOrderManager().clear();
		cPipe.getClientSideOrderManager().addAll(clientOrder);

	}

	@Override
	public ModernPacket template() {
		return new PipeManagerContentPacket(getId());
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		for (LogisticsOrder order : manager) {
			output.writeByte(1);
			output.writeOrderInfo(order);
		}
		output.writeByte(0);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		clientOrder = new LinkedList<>();
		while (input.readByte() == 1) {
			clientOrder.add(input.readOrderInfo());
		}
	}
}
