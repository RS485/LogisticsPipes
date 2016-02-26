package logisticspipes.network.packets.multiblock;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import logisticspipes.network.IReadListObject;
import logisticspipes.network.IWriteListObject;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreMultiBlockPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericSubMultiBlock;

import network.rs485.logisticspipes.world.DoubleCoordinates;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class MultiBlockCoordinatesPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private Set<DoubleCoordinates> targetPos;

	@Getter
	@Setter
	private List<CoreMultiBlockPipe.SubBlockTypeForShare> subTypes;

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeSet(targetPos, LPDataOutputStream::writeLPPosition);
		data.writeList(subTypes, LPDataOutputStream::writeEnum);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		targetPos = data.readSet(LPDataInputStream::readLPPosition);
		subTypes = data.readList(data1 -> data1.readEnum(CoreMultiBlockPipe.SubBlockTypeForShare.class));
	}

	public MultiBlockCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericSubMultiBlock block = this.getTile(player.getEntityWorld(), LogisticsTileGenericSubMultiBlock.class);
		block.setPosition(targetPos, subTypes);
	}

	@Override
	public ModernPacket template() {
		return new MultiBlockCoordinatesPacket(getId());
	}
}
