package logisticspipes.network.packets.pipe;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.renderer.LogisticsGuiOverrenderer;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
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
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(tagetPosX);
		data.writeInt(tagetPosY);
		data.writeInt(tagetPosZ);
		data.writeInt(slot);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		tagetPosX = data.readInt();
		tagetPosY = data.readInt();
		tagetPosZ = data.readInt();
		slot = data.readInt();
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
