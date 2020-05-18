package logisticspipes.network.guis.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.gui.GuiSecurityStation;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;

@StaticResolve
public class SecurityStationGui extends CoordinatesGuiProvider {

	public SecurityStationGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		return new GuiSecurityStation(getTileAs(player.world, LogisticsSecurityTileEntity.class), player);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsSecurityTileEntity securityStation = getTileAs(player.world, LogisticsSecurityTileEntity.class);
		DummyContainer dummy = new DummyContainer(player, null, securityStation);
		dummy.addRestrictedSlot(0, securityStation.inv, 50, 50, (Item) null);
		dummy.addNormalSlotsForPlayerInventory(10, 210);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new SecurityStationGui(getId());
	}
}
