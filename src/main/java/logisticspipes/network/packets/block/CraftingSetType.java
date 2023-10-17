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
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class CraftingSetType extends CoordinatesPacket {

	@Getter
	@Setter
	private ItemIdentifier targetType;

	public CraftingSetType(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		TileEntity table = this.getTileAs(player.getEntityWorld(), TileEntity.class);
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
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeItemIdentifier(targetType);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		targetType = input.readItemIdentifier();
	}
}
