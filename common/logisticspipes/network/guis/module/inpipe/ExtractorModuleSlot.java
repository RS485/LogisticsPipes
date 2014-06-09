package logisticspipes.network.guis.module.inpipe;

import java.io.IOException;

import logisticspipes.gui.modules.GuiExtractor;
import logisticspipes.interfaces.ISneakyDirectionReceiver;
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
import net.minecraftforge.common.ForgeDirection;

@Accessors(chain=true)
public class ExtractorModuleSlot extends ModuleCoordinatesGuiProvider {
	
	@Getter
	@Setter
	private ForgeDirection sneakyOrientation;
	
	public ExtractorModuleSlot(int id) {
		super(id);
	}
	
	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeForgeDirection(sneakyOrientation);
	}
	
	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		sneakyOrientation = data.readForgeDirection();
	}
	
	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld());
		if(pipe.pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(getSlot()) instanceof ISneakyDirectionReceiver)) return null;
		((ISneakyDirectionReceiver)((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(getSlot())).setSneakyDirection(sneakyOrientation);
		return new GuiExtractor(player.inventory, (CoreRoutedPipe) pipe.pipe, (ISneakyDirectionReceiver) ((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(getSlot()), getSlot() + 1);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld());
		if(pipe == null || !(pipe.pipe instanceof CoreRoutedPipe) || !(((CoreRoutedPipe)pipe.pipe).getLogisticsModule().getSubModule(getSlot()) instanceof ISneakyDirectionReceiver)) return null;
		return new DummyContainer(player.inventory, null);
	}

	@Override
	public GuiProvider template() {
		return new ExtractorModuleSlot(getId());
	}
}
