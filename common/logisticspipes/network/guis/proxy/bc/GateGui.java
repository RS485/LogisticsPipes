package logisticspipes.network.guis.proxy.bc;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class GateGui extends CoordinatesGuiProvider {

	@Getter
	@Setter
	private int side;

	public GateGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = getPipe(player.getEntityWorld());
		if (pipe == null || pipe.tilePart == null) {
			return null;
		}
		if (!pipe.tilePart.hasGate(ForgeDirection.getOrientation(side))) {
			return null;
		}
		return pipe.pipe.bcPipePart.getClientGui(player.inventory, side);
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = getPipe(player.getEntityWorld());
		if (pipe == null || pipe.tilePart == null) {
			return null;
		}
		if (!pipe.tilePart.hasGate(ForgeDirection.getOrientation(side))) {
			return null;
		}
		return pipe.pipe.bcPipePart.getGateContainer(player.inventory, side);
	}

	@Override
	public GuiProvider template() {
		return new GateGui(getId());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(side);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		side = data.readInt();
	}

}
