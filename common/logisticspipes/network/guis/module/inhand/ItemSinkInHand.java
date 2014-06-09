package logisticspipes.network.guis.module.inhand;

import net.minecraft.entity.player.EntityPlayer;
import logisticspipes.gui.modules.GuiItemSink;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.DummyModuleContainer;

public class ItemSinkInHand extends ModuleInHandGuiProvider {
	
	public ItemSinkInHand(int id) {
		super(id);
	}
	
	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsModule module = this.getLogisticsModule(player);
		if(!(module instanceof ModuleItemSink)) return null;
		return new GuiItemSink(player.inventory, null, (ModuleItemSink) module, -2);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		DummyContainer dummy = new DummyModuleContainer(player, getInvSlot());
		if(!(((DummyModuleContainer)dummy).getModule() instanceof ModuleItemSink)) return null;
		((DummyModuleContainer)dummy).setInventory(((ModuleItemSink)((DummyModuleContainer)dummy).getModule()).getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);
		
		//Pipe slots
	    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
	    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
	    }
	    return dummy;
	}

	@Override
	public GuiProvider template() {
		return new ItemSinkInHand(getId());
	}
}
