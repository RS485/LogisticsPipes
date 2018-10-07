package logisticspipes.network.packets.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
public class CompilerStatusPacket extends CoordinatesPacket {

	public CompilerStatusPacket(int id) {
		super(id);
	}

	@Getter
	@Setter
	private ResourceLocation category;

	@Getter
	@Setter
	private double progress;

	@Getter
	@Setter
	private ItemStack disk;

	@Getter
	@Setter
	private ItemStack programmer;

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsProgramCompilerTileEntity tile = this.getTile(player.world, LogisticsProgramCompilerTileEntity.class);
		tile.setStateOnClient(this);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeResourceLocation(category);
		output.writeDouble(progress);
		output.writeItemStack(disk);
		output.writeItemStack(programmer);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		category = input.readResourceLocation();
		progress = input.readDouble();
		disk = input.readItemStack();
		programmer = input.readItemStack();
	}

	@Override
	public ModernPacket template() {
		return new CompilerStatusPacket(getId());
	}
}
