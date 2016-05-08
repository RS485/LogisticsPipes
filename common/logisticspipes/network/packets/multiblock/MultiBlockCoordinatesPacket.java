package logisticspipes.network.packets.multiblock;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreMultiBlockPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericSubMultiBlock;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class MultiBlockCoordinatesPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private Set<DoubleCoordinates> targetPos;

	@Getter
	@Setter
	private List<CoreMultiBlockPipe.SubBlockTypeForShare> subTypes;

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeCollection(targetPos, LPDataOutput::writeLPPosition);
		output.writeCollection(subTypes, LPDataOutput::writeEnum);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		targetPos = input.readSet(LPDataInput::readLPPosition);
		subTypes = input.readArrayList(data1 -> data1.readEnum(CoreMultiBlockPipe.SubBlockTypeForShare.class));
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
