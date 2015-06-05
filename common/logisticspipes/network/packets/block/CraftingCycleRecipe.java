package logisticspipes.network.packets.block;

import java.io.IOException;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class CraftingCycleRecipe extends CoordinatesPacket {

	public CraftingCycleRecipe(int id) {
		super(id);
	}

	@Getter
	@Setter
	private boolean down;

	@Override
	public void processPacket(EntityPlayer player) {
		TileEntity table = this.getTile(player.getEntityWorld(), TileEntity.class);
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
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(down);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		down = data.readBoolean();
	}
}
