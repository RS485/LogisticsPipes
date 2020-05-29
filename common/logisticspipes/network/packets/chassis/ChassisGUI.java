package logisticspipes.network.packets.chassis;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
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
		final LogisticsTileGenericPipe pipe = getPipe(player.world);
		if (pipe == null || !(pipe.pipe instanceof PipeLogisticsChassi)) {
			return;
		}

		final PipeLogisticsChassi chassisPipe = (PipeLogisticsChassi) pipe.pipe;

		LogisticsModule subModule = chassisPipe.getSubModule(getButtonID());
		if (!(subModule instanceof Gui)) {
			return;
		}

		Gui.getPipeGuiProvider((Gui) subModule).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()).open(player);
	}

	@Override
	public ModernPacket template() {
		return new ChassisGUI(getId());
	}

}
