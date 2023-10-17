package logisticspipes.network.guis.module.inpipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

import logisticspipes.gui.modules.GuiSneakyConfigurator;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.module.SneakyDirection;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class SneakyModuleInSlotGuiProvider extends ModuleCoordinatesGuiProvider {

	private EnumFacing sneakyOrientation;

	public SneakyModuleInSlotGuiProvider(int id) {
		super(id);
	}

	public SneakyModuleInSlotGuiProvider setSneakyOrientation(EnumFacing sneakyOrientation) {
		this.sneakyOrientation = sneakyOrientation;
		return this;
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeFacing(sneakyOrientation);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		sneakyOrientation = input.readFacing();
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsModule module = this.getLogisticsModule(player.getEntityWorld(), LogisticsModule.class);
		if (!(module instanceof SneakyDirection && module instanceof Gui)) {
			return null;
		}
		((SneakyDirection) module).setSneakyDirection(sneakyOrientation);
		return new GuiSneakyConfigurator(player.inventory, module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsModule module = this.getLogisticsModule(player.getEntityWorld(), LogisticsModule.class);
		if (!(module instanceof SneakyDirection && module instanceof Gui)) {
			return null;
		}
		return new DummyContainer(player.inventory, null);
	}

	@Override
	public GuiProvider template() {
		return new SneakyModuleInSlotGuiProvider(getId());
	}
}
