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
public class CCBasedQuickSortMode extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private int timeOut;

	public CCBasedQuickSortMode(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleCCBasedQuickSort module = this.getLogisticsModule(player, ModuleCCBasedQuickSort.class);
		if (module == null) {
			return;
		}
		module.setTimeout(timeOut);
	}

	@Override
	public ModernPacket template() {
		return new CCBasedQuickSortMode(getId());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(timeOut);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		timeOut = data.readInt();
	}
}
