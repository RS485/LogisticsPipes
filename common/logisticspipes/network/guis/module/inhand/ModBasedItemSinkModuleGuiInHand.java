package logisticspipes.network.guis.module.inhand;

import logisticspipes.gui.modules.GuiModBasedItemSink;
import logisticspipes.modules.ModuleModBasedItemSink;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;
import logisticspipes.utils.item.ItemIdentifierInventory;
import net.minecraft.entity.player.EntityPlayer;


public class ModBasedItemSinkModuleGuiInHand extends ModuleInHandGuiProvider {
	
	public ModBasedItemSinkModuleGuiInHand(int id) {
		super(id);
	}
	
	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsModule module = this.getLogisticsModule(player);
		if(!(module instanceof ModuleModBasedItemSink)) return null;
		return new GuiModBasedItemSink(player.inventory, (ModuleModBasedItemSink) module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		DummyModuleContainer dummy = new DummyModuleContainer(player, getInvSlot());
		if(!(dummy.getModule() instanceof ModuleModBasedItemSink)) return null;
		dummy.setInventory(new ItemIdentifierInventory(1, "TMP", 1));
		dummy.addDummySlot(0, 0, 0);
		dummy.addNormalSlotsForPlayerInventory(0, 0);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new ModBasedItemSinkModuleGuiInHand(getId());
	}
}
