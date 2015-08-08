package logisticspipes.network.packets.chassis;

import java.io.IOException;

import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class ChassisGUI extends CoordinatesPacket {

	@Getter
	@Setter
	private int buttonID;

	public ChassisGUI(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeInt(buttonID);
		super.writeData(data);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		buttonID = data.readInt();
		super.readData(data);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = getPipe(player.worldObj);
		if (pipe == null || !(pipe.pipe instanceof PipeLogisticsChassi)) {
			return;
		}

		final PipeLogisticsChassi chassisPipe = (PipeLogisticsChassi) pipe.pipe;

		if (!(chassisPipe.getLogisticsModule().getSubModule(getButtonID()) instanceof LogisticsGuiModule)) {
			return;
		}

		((LogisticsGuiModule) chassisPipe.getLogisticsModule().getSubModule(getButtonID())).getPipeGuiProviderForModule().setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()).open(player);
	}

	@Override
	public ModernPacket template() {
		return new ChassisGUI(getId());
	}

}
