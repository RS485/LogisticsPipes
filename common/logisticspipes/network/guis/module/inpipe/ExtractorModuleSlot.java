package logisticspipes.network.guis.module.inpipe;

import java.io.IOException;

import logisticspipes.gui.modules.GuiExtractor;
import logisticspipes.modules.abstractmodules.LogisticsSneakyDirectionModule;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.utils.gui.DummyContainer;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
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
