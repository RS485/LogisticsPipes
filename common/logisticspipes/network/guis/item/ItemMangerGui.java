package logisticspipes.network.guis.item;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.LPItems;
import logisticspipes.gui.GuiCardManager;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.items.ItemModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.utils.CardManagmentInventory;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;

@StaticResolve
public class ItemMangerGui extends GuiProvider {

	public ItemMangerGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		return new GuiCardManager(player);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		final CardManagmentInventory Cinv = new CardManagmentInventory();
		DummyContainer dummy = new DummyContainer(player, Cinv, new IGuiOpenControler() {

			@Override
			public void guiOpenedByPlayer(EntityPlayer player) {}

			@Override
			public void guiClosedByPlayer(EntityPlayer player) {
				Cinv.close(player, (int) player.posX, (int) player.posY, (int) player.posZ);
			}
		});
		for (int i = 0; i < 2; i++) {
			dummy.addRestrictedSlot(i, Cinv, 0, 0, ItemModule.class);
		}
		dummy.addRestrictedSlot(2, Cinv, 0, 0, itemStack -> false);
		dummy.addRestrictedSlot(3, Cinv, 0, 0, LPItems.itemCard);
		for (int i = 4; i < 10; i++) {
			dummy.addColorSlot(i, Cinv, 0, 0);
		}
		dummy.addNormalSlotsForPlayerInventory(0, 0);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new ItemMangerGui(getId());
	}
}
