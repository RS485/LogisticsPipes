package logisticspipes.network.guis.block;

import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.gui.GuiSolderingStation;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.utils.gui.DummyContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SolderingStationGui extends CoordinatesGuiProvider {

	public SolderingStationGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		final LogisticsSolderingTileEntity tile = this.getTile(player.worldObj, LogisticsSolderingTileEntity.class);
		if (tile == null) {
			return null;
		}
		GuiSolderingStation gui = new GuiSolderingStation(player, tile);
		gui.inventorySlots = getContainer(player);
		return gui;
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		final LogisticsSolderingTileEntity tile = this.getTile(player.worldObj, LogisticsSolderingTileEntity.class);
		if (tile == null) {
			return null;
		}
		DummyContainer dummy = new DummyContainer(player, tile, tile);
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				final int slotNumber = i * 3 + j;
				dummy.addRestrictedSlot(slotNumber, tile, 44 + (j * 18), 17 + (i * 18), new ISlotCheck() {

					@Override
					public boolean isStackAllowed(ItemStack itemStack) {
						return tile.checkSlot(itemStack, slotNumber);
					}
				});
			}
		}
		dummy.addRestrictedSlot(9, tile, 107, 17, Items.iron_ingot);
		dummy.addRestrictedSlot(10, tile, 141, 47, (Item) null);
		dummy.addRestrictedSlot(11, tile, 9, 9, new ISlotCheck() {

			@Override
			public boolean isStackAllowed(ItemStack itemStack) {
				return tile.getRecipeForTaget(itemStack) != null && tile.areStacksEmpty();
			}
		});
		dummy.addNormalSlotsForPlayerInventory(8, 84);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new SolderingStationGui(getId());
	}
}
