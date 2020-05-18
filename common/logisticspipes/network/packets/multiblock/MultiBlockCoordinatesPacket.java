package logisticspipes.network.packets.multiblock;

import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreMultiBlockPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericSubMultiBlock;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.world.DoubleCoordinates;

@StaticResolve
public class MultiBlockCoordinatesPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private Set<DoubleCoordinates> targetPos;

	@Getter
	@Setter
	private List<CoreMultiBlockPipe.SubBlockTypeForShare> subTypes;

	public MultiBlockCoordinatesPacket(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeCollection(targetPos);
		output.writeCollection(subTypes, LPDataOutput::writeEnum);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		targetPos = input.readSet(DoubleCoordinates::new);
		subTypes = input.readArrayList(data1 -> data1.readEnum(CoreMultiBlockPipe.SubBlockTypeForShare.class));
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericSubMultiBlock block = this.getTileAs(player.getEntityWorld(), LogisticsTileGenericSubMultiBlock.class);
		block.setPosition(targetPos, subTypes);
	}

	@Override
	public ModernPacket template() {
		return new MultiBlockCoordinatesPacket(getId());
	}
}
