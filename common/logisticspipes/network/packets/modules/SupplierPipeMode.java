package logisticspipes.network.packets.modules;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.gui.GuiSupplierPipe;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.PipeItemsSupplierLogistics.PatternMode;
import logisticspipes.pipes.PipeItemsSupplierLogistics.SupplyMode;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;

@Accessors(chain=true)
public class SupplierPipeMode extends IntegerCoordinatesPacket {

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
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if (pipe == null) {
			return;
		}
		if (!(pipe.pipe instanceof PipeItemsSupplierLogistics)) {
			return;
		}
		if(hasPatternUpgrade) {
			((PipeItemsSupplierLogistics) pipe.pipe).setPatternMode(PatternMode.values()[getInteger()]);
		} else {
			((PipeItemsSupplierLogistics) pipe.pipe).setSupplyMode(SupplyMode.values()[getInteger()]);
		}
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiSupplierPipe) {
			((GuiSupplierPipe) FMLClientHandler.instance().getClient().currentScreen).refreshMode();
		}
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		hasPatternUpgrade = data.readBoolean();
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(hasPatternUpgrade);
	}
	
}