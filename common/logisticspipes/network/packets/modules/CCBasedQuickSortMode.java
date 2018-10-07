package logisticspipes.network.packets.modules;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.modules.ModuleCCBasedQuickSort;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

import logisticspipes.utils.StaticResolve;

@StaticResolve
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
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeInt(timeOut);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		timeOut = input.readInt();
	}
}
