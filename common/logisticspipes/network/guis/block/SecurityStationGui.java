package logisticspipes.network.guis.block;

import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.gui.GuiSecurityStation;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.utils.gui.DummyContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

public class SecurityStationGui extends CoordinatesGuiProvider {

	public SecurityStationGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsSecurityTileEntity tile = this.getTile(player.getEntityWorld(), LogisticsSecurityTileEntity.class);
		if (tile == null) {
			return null;
		}
		return new GuiSecurityStation(tile, player);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsSecurityTileEntity tile = this.getTile(player.getEntityWorld(), LogisticsSecurityTileEntity.class);
		if (tile == null) {
			return null;
		}
		DummyContainer dummy = new DummyContainer(player, null, tile);
		dummy.addRestrictedSlot(0, tile.inv, 50, 50, (Item) null);
		dummy.addNormalSlotsForPlayerInventory(10, 210);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new SecurityStationGui(getId());
	}
}
