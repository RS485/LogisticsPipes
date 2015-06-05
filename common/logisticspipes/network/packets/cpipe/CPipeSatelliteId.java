package logisticspipes.network.packets.cpipe;

import java.io.IOException;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
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
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(pipeId);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		pipeId = data.readInt();
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
