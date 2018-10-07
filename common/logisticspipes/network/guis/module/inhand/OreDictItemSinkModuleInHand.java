package logisticspipes.network.guis.module.inhand;

import logisticspipes.gui.modules.GuiOreDictItemSink;
import logisticspipes.modules.ModuleOreDictItemSink;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;
import logisticspipes.utils.item.ItemIdentifierInventory;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.utils.StaticResolve;

@StaticResolve
public class OreDictItemSinkModuleInHand extends ModuleInHandGuiProvider {

	public OreDictItemSinkModuleInHand(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsModule module = getLogisticsModule(player);
		if (!(module instanceof ModuleOreDictItemSink)) {
			return null;
		}
		return new GuiOreDictItemSink(player.inventory, (ModuleOreDictItemSink) module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		DummyModuleContainer dummy = new DummyModuleContainer(player, getInvSlot());
		if (!(dummy.getModule() instanceof ModuleOreDictItemSink)) {
			return null;
		}
		dummy.setInventory(new ItemIdentifierInventory(1, "TMP", 1));
		dummy.addDummySlot(0, 0, 0);
		dummy.addNormalSlotsForPlayerInventory(0, 0);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new OreDictItemSinkModuleInHand(getId());
	}
}
