package logisticspipes.network.guis.module.inpipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Direction;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.gui.modules.GuiExtractor;
import logisticspipes.modules.abstractmodules.LogisticsSneakyDirectionModule;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class ExtractorModuleSlot extends ModuleCoordinatesGuiProvider {

	@Getter
	@Setter
	private Direction sneakyOrientation;

	public ExtractorModuleSlot(int id) {
		super(id);
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
		LogisticsSneakyDirectionModule module = this.getLogisticsModule(player.getEntityWorld(), LogisticsSneakyDirectionModule.class);
		if (module == null) {
			return null;
		}
		module.setSneakyDirection(sneakyOrientation);
		return new GuiExtractor(player.inventory, module);
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsSneakyDirectionModule module = this.getLogisticsModule(player.getEntityWorld(), LogisticsSneakyDirectionModule.class);
		if (module == null) {
			return null;
		}
		return new DummyContainer(player.inventory, null);
	}

	@Override
	public GuiProvider template() {
		return new ExtractorModuleSlot(getId());
	}
}
