package logisticspipes.network.packets.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.blocks.LogisticsProgramCompilerTileEntity;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

import logisticspipes.utils.StaticResolve;

@StaticResolve
public class CompilerTriggerTaskPacket extends CoordinatesPacket {

	public CompilerTriggerTaskPacket(int id) {
		super(id);
	}

	@Getter
	@Setter
	private ResourceLocation category;

	@Getter
	@Setter
	private String type;

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsProgramCompilerTileEntity tile = this.getTile(player.world, LogisticsProgramCompilerTileEntity.class);
		tile.triggerNewTask(getCategory(), getType());
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeResourceLocation(category);
		output.writeUTF(type);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		category = input.readResourceLocation();
		type = input.readUTF();
	}

	@Override
	public ModernPacket template() {
		return new CompilerTriggerTaskPacket(getId());
	}
}
