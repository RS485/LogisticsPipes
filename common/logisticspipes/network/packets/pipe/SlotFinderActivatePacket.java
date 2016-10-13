package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.renderer.LogisticsGuiOverrenderer;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class SlotFinderActivatePacket extends ModuleCoordinatesPacket {

	@Getter
	@Setter
	private int tagetPosX;
	@Getter
	@Setter
	private int tagetPosY;
	@Getter
	@Setter
	private int tagetPosZ;
	@Getter
	@Setter
	private int slot;

	public SlotFinderActivatePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SlotFinderActivatePacket(getId());
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeInt(tagetPosX);
		output.writeInt(tagetPosY);
		output.writeInt(tagetPosZ);
		output.writeInt(slot);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		tagetPosX = input.readInt();
		tagetPosY = input.readInt();
		tagetPosZ = input.readInt();
		slot = input.readInt();
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsGuiOverrenderer.getInstance().setPipePosX(getPosX());
		LogisticsGuiOverrenderer.getInstance().setPipePosY(getPosY());
		LogisticsGuiOverrenderer.getInstance().setPipePosZ(getPosZ());
		LogisticsGuiOverrenderer.getInstance().setTargetPosX(getTagetPosX());
		LogisticsGuiOverrenderer.getInstance().setTargetPosY(getTagetPosY());
		LogisticsGuiOverrenderer.getInstance().setTargetPosZ(getTagetPosZ());
		LogisticsGuiOverrenderer.getInstance().setSlot(getSlot());
		LogisticsGuiOverrenderer.getInstance().setOverlaySlotActive(true);
		LogisticsGuiOverrenderer.getInstance().setPositionInt(getPositionInt());
		LogisticsGuiOverrenderer.getInstance().setPositionType(getType());
	}
}
