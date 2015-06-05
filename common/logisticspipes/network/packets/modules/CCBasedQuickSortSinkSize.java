package logisticspipes.network.packets.modules;

import java.io.IOException;

import logisticspipes.modules.ModuleCCBasedQuickSort;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class CCBasedQuickSortSinkSize extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private int sinkSize;

	public CCBasedQuickSortSinkSize(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleCCBasedQuickSort module = this.getLogisticsModule(player, ModuleCCBasedQuickSort.class);
		if (module == null) {
			return;
		}
		module.setSinkSize(sinkSize);
	}

	@Override
	public ModernPacket template() {
		return new CCBasedQuickSortSinkSize(getId());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(sinkSize);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		sinkSize = data.readInt();
	}
}
