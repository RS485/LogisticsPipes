package logisticspipes.network.packets.chassis;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class ChassisGUI extends CoordinatesPacket {

	@Getter
	@Setter
	private int buttonID;

	public ChassisGUI(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeInt(buttonID);
		super.writeData(output);
	}

	@Override
	public void readData(LPDataInput input) {
		buttonID = input.readInt();
		super.readData(input);
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

		((LogisticsGuiModule) chassisPipe.getLogisticsModule().getSubModule(getButtonID())).getPipeGuiProviderForModule().setPosX(getPosX()).setPosY(getPosY())
				.setPosZ(getPosZ()).open(player);
	}

	@Override
	public ModernPacket template() {
		return new ChassisGUI(getId());
	}

}
