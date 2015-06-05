package logisticspipes.network.packets.block;

import java.io.IOException;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class CraftingSetType extends CoordinatesPacket {

	public CraftingSetType(int id) {
		super(id);
	}

	@Getter
	@Setter
	private ItemIdentifier targetType;

	@Override
	public void processPacket(EntityPlayer player) {
		TileEntity table = this.getTile(player.getEntityWorld(), TileEntity.class);
		if (table instanceof LogisticsCraftingTableTileEntity) {
			((LogisticsCraftingTableTileEntity) table).targetType = targetType;
			((LogisticsCraftingTableTileEntity) table).cacheRecipe();
		} else if (table instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) table).pipe instanceof PipeBlockRequestTable) {
			((PipeBlockRequestTable) ((LogisticsTileGenericPipe) table).pipe).targetType = targetType;
			((PipeBlockRequestTable) ((LogisticsTileGenericPipe) table).pipe).cacheRecipe();
		}
	}

	@Override
	public ModernPacket template() {
		return new CraftingSetType(getId());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeItemIdentifier(targetType);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		targetType = data.readItemIdentifier();
	}
}
