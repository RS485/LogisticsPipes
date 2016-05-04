package logisticspipes.network.packets.cpipe;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class CPipeSatelliteId extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private int pipeId;

	public CPipeSatelliteId(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CPipeSatelliteId(getId());
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeInt(pipeId);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		pipeId = input.readInt();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleCrafter module = this.getLogisticsModule(player, ModuleCrafter.class);
		if (module == null) {
			return;
		}
		module.setSatelliteId(pipeId, -1);
	}
}
