package logisticspipes.network.guis.module.inpipe;

import java.io.IOException;

import logisticspipes.gui.modules.GuiItemSink;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.gui.DummyContainer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

@Accessors(chain=true)
public class ItemSinkSlot extends ModuleCoordinatesGuiProvider {
	
	@Getter
	@Setter
	private boolean isDefaultRoute;
	
	public ItemSinkSlot(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(isDefaultRoute);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		isDefaultRoute = data.readBoolean();
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) return null;
		ModuleItemSink module;
		int slot = 0;
		if(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleItemSink) {
			module = (ModuleItemSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule();
		} else if (((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(getSlot()) instanceof ModuleItemSink) {
			slot = getSlot() + 1;
			module = (ModuleItemSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(getSlot());
		} else {
			return null;
		}
		module.setDefaultRoute(isDefaultRoute);
		return new GuiItemSink(player.inventory, (CoreRoutedPipe) pipe.pipe, module, slot);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null || pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) return null;
		ModuleItemSink module;
		if(((CoreRoutedPipe)pipe.pipe).getLogisticsModule() instanceof ModuleItemSink) {
			module = (ModuleItemSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule();
		} else if(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(getSlot()) instanceof ModuleItemSink) {
			module = (ModuleItemSink) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(getSlot());
		} else {
			return null;
		}
		DummyContainer dummy = new DummyContainer(player.inventory, module.getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);

		//Pipe slots
	    for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
	    	dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
	    }
	    
	    return dummy;
	}

	@Override
	public GuiProvider template() {
		return new ItemSinkSlot(getId());
	}
}
