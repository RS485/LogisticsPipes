package logisticspipes.network.packets.modules;

import java.io.IOException;

import logisticspipes.gui.GuiSupplierPipe;
import logisticspipes.modules.ModuleActiveSupplier;
import logisticspipes.modules.ModuleActiveSupplier.PatternMode;
import logisticspipes.modules.ModuleActiveSupplier.SupplyMode;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.IntegerModuleCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.client.FMLClientHandler;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class SupplierPipeMode extends IntegerModuleCoordinatesPacket {

	@Getter
	@Setter
	private boolean hasPatternUpgrade;

	public SupplierPipeMode(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SupplierPipeMode(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleActiveSupplier module = this.getLogisticsModule(player, ModuleActiveSupplier.class);
		if (module == null) {
			return;
		}
		if (hasPatternUpgrade) {
			module.setPatternMode(PatternMode.values()[getInteger()]);
		} else {
			module.setSupplyMode(SupplyMode.values()[getInteger()]);
		}
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiSupplierPipe) {
			((GuiSupplierPipe) FMLClientHandler.instance().getClient().currentScreen).refreshMode();
		}
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		hasPatternUpgrade = data.readBoolean();
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(hasPatternUpgrade);
	}

}
