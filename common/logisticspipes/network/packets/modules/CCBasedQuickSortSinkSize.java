package logisticspipes.network.packets.modules;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.modules.ModuleCCBasedQuickSort;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

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
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeInt(sinkSize);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		sinkSize = input.readInt();
	}
}
