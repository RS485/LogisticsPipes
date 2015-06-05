package logisticspipes.network.guis.module.inpipe;

import logisticspipes.gui.modules.GuiThaumicAspectSink;
import logisticspipes.modules.ModuleThaumicAspectSink;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.NBTModuleCoordinatesGuiProvider;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.item.ItemIdentifierInventory;

import net.minecraft.entity.player.EntityPlayer;

public class ThaumicAspectSinkModuleSlot extends NBTModuleCoordinatesGuiProvider {

	public ThaumicAspectSinkModuleSlot(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		ModuleThaumicAspectSink module = this.getLogisticsModule(player.getEntityWorld(), ModuleThaumicAspectSink.class);
		if (module == null) {
			return null;
		}
		module.readFromNBT(getNbt());
		return new GuiThaumicAspectSink(player.inventory, module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		ModuleThaumicAspectSink module = this.getLogisticsModule(player.getEntityWorld(), ModuleThaumicAspectSink.class);
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
		return new ThaumicAspectSinkModuleSlot(getId());
	}
}
