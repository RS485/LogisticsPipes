package logisticspipes.network.packets.multiblock;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.LogisticsTileGenericSubMultiBlock;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class MultiBlockCoordinatesPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private int targetPosX;
	@Getter
	@Setter
	private int targetPosY;
	@Getter
	@Setter
	private int targetPosZ;

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(targetPosX);
		data.writeInt(targetPosY);
		data.writeInt(targetPosZ);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		targetPosX = data.readInt();
		targetPosY = data.readInt();
		targetPosZ = data.readInt();
	}

	public MultiBlockCoordinatesPacket setTargetLPPos(LPPosition pos) {
		setTargetPosX(pos.getX());
		setTargetPosY(pos.getY());
		setTargetPosZ(pos.getZ());
		return this;
	}

	public MultiBlockCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericSubMultiBlock block = this.getTile(player.getEntityWorld(), LogisticsTileGenericSubMultiBlock.class);
		block.setPosition(new LPPosition(targetPosX, targetPosY, targetPosZ));
	}

	@Override
	public ModernPacket template() {
		return new MultiBlockCoordinatesPacket(getId());
	}
}
