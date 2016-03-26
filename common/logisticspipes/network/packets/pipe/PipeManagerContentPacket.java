package logisticspipes.network.packets.pipe;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.LogisticsOrder;
import logisticspipes.routing.order.LogisticsOrderManager;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
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
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		for (LogisticsOrder order : manager) {
			data.writeByte(1);
			data.writeOrderInfo(order);
		}
		data.writeByte(0);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		clientOrder = new LinkedList<IOrderInfoProvider>();
		while (data.readByte() == 1) {
			clientOrder.add(data.readOrderInfo());
		}
	}
}
