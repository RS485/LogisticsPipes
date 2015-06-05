package logisticspipes.network.packets.block;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.network.abstractpackets.Integer2CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

public class CraftingTableFuzzyFlagsModifyPacket extends Integer2CoordinatesPacket {

	public CraftingTableFuzzyFlagsModifyPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsCraftingTableTileEntity tile = this.getTile(player.worldObj, LogisticsCraftingTableTileEntity.class);
		if (tile == null) {
			return;
		}
		if (!tile.isFuzzy()) {
			return;
		}
		tile.handleFuzzyFlagsChange(getInteger(), getInteger2(), player);
	}

	@Override
	public ModernPacket template() {
		return new CraftingTableFuzzyFlagsModifyPacket(getId());
	}
}
