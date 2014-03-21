package logisticspipes.network.packets.pipe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.renderer.LogisticsGuiOverrenderer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

@Accessors(chain=true)
public class SlotFinderActivatePacket extends CoordinatesPacket {

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
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(tagetPosX);
		data.writeInt(tagetPosY);
		data.writeInt(tagetPosZ);
		data.writeInt(slot);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
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
		LogisticsGuiOverrenderer.getInstance().setActive(true);
	}
}
