package logisticspipes.network.guis.module.inpipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.gui.modules.GuiStringBasedItemSink;
import logisticspipes.interfaces.IStringBasedModule;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.NBTModuleCoordinatesGuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.item.ItemIdentifierInventory;

@StaticResolve
public class StringBasedItemSinkModuleGuiSlot extends NBTModuleCoordinatesGuiProvider {

	public StringBasedItemSinkModuleGuiSlot(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsModule module = this.getLogisticsModule(player.getEntityWorld(), LogisticsModule.class);
		if (!(module instanceof IStringBasedModule)) {
			return null;
		}
		module.readFromNBT(getNbt());
		return new GuiStringBasedItemSink(player.inventory, module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		IStringBasedModule module = this.getLogisticsModule(player.getEntityWorld(), IStringBasedModule.class);
		if (module == null) {
			return null;
		}
		DummyContainer dummy = new DummyContainer(player.inventory, new ItemIdentifierInventory(1, "TMP", 1));
		dummy.addDummySlot(0, 0, 0);
		dummy.addNormalSlotsForPlayerInventory(0, 0);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new StringBasedItemSinkModuleGuiSlot(getId());
	}
}
