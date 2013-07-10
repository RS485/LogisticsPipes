package logisticspipes.network.packets.block;

import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.network.abstractpackets.InventoryCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class SolderingStationInventory extends InventoryCoordinatesPacket {

	public SolderingStationInventory(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SolderingStationInventory(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsSolderingTileEntity tile = this.getTile(player.worldObj, LogisticsSolderingTileEntity.class);
		if(tile != null) {
			for(int i = 0; i < tile.getSizeInventory(); i++) {
				if(i >= getStackList().size()) break;
				ItemStack stack = getStackList().get(i);
				tile.setInventorySlotContents(i, stack);
			}
		}
	}
}

