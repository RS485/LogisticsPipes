package logisticspipes.network.guis.module.inpipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.gui.modules.GuiOreDictItemSink;
import logisticspipes.modules.ModuleOreDictItemSink;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.NBTModuleCoordinatesGuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.item.ItemIdentifierInventory;

@StaticResolve
public class OreDictItemSinkModuleSlot extends NBTModuleCoordinatesGuiProvider {

	public OreDictItemSinkModuleSlot(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		ModuleOreDictItemSink module = this.getLogisticsModule(player.getEntityWorld(), ModuleOreDictItemSink.class);
		if (module == null) {
			return null;
		}
		module.readFromNBT(getNbt());
		return new GuiOreDictItemSink(player.inventory, module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		ModuleOreDictItemSink module = this.getLogisticsModule(player.getEntityWorld(), ModuleOreDictItemSink.class);
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
		return new OreDictItemSinkModuleSlot(getId());
	}
}
