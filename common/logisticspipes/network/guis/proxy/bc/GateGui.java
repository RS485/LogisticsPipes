package logisticspipes.network.guis.proxy.bc;

import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class GateGui extends CoordinatesGuiProvider {
	public GateGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld());
		if(pipe.pipe == null) return null;
		if(!pipe.pipe.hasGate()) return null;
		return pipe.pipe.bcPipePart.getClientGui(player.inventory);
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld());
		if(pipe.pipe == null) return null;
		if(!pipe.pipe.hasGate()) return null;
		return pipe.pipe.bcPipePart.getGateContainer(player.inventory);
	}

	@Override
	public GuiProvider template() {
		return new GateGui(getId());
	}
}
