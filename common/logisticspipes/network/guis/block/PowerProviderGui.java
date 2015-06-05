package logisticspipes.network.guis.block;

import logisticspipes.blocks.powertile.LogisticsPowerProviderTileEntity;
import logisticspipes.gui.GuiPowerProvider;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.utils.gui.DummyContainer;

import net.minecraft.entity.player.EntityPlayer;

public class PowerProviderGui extends CoordinatesGuiProvider {

	public PowerProviderGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsPowerProviderTileEntity tile = this.getTile(player.getEntityWorld(), LogisticsPowerProviderTileEntity.class);
		if (tile == null) {
			return null;
		}
		return new GuiPowerProvider(player, tile);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsPowerProviderTileEntity tile = this.getTile(player.getEntityWorld(), LogisticsPowerProviderTileEntity.class);
		if (tile == null) {
			return null;
		}
		DummyContainer dummy = new DummyContainer(player, null, tile);
		dummy.addNormalSlotsForPlayerInventory(8, 80);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new PowerProviderGui(getId());
	}
}
