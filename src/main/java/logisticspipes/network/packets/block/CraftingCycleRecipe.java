package logisticspipes.network.packets.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class CraftingCycleRecipe extends CoordinatesPacket {

	@Getter
	@Setter
	private boolean down;

	public CraftingCycleRecipe(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		TileEntity table = this.getTileAs(player.getEntityWorld(), TileEntity.class);
		if (table instanceof LogisticsCraftingTableTileEntity) {
			((LogisticsCraftingTableTileEntity) table).cycleRecipe(down);
		} else if (table instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) table).pipe instanceof PipeBlockRequestTable) {
			((PipeBlockRequestTable) ((LogisticsTileGenericPipe) table).pipe).cycleRecipe(down);
		}
	}

	@Override
	public ModernPacket template() {
		return new CraftingCycleRecipe(getId());
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeBoolean(down);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		down = input.readBoolean();
	}
}
