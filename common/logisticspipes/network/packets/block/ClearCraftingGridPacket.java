package logisticspipes.network.packets.block;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class ClearCraftingGridPacket extends CoordinatesPacket {

	public ClearCraftingGridPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		TileEntity table = this.getTile(player.getEntityWorld(), TileEntity.class);
		if (table instanceof LogisticsCraftingTableTileEntity) {
		} else if (table instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) table).pipe instanceof PipeBlockRequestTable) {
			((PipeBlockRequestTable)((LogisticsTileGenericPipe) table).pipe).matrix.clearGrid();
			((PipeBlockRequestTable)((LogisticsTileGenericPipe) table).pipe).cacheRecipe();
		}
	}

	@Override
	public ModernPacket template() {
		return new ClearCraftingGridPacket(getId());
	}
}
