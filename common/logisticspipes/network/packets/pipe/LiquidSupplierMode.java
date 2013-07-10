package logisticspipes.network.packets.pipe;

import logisticspipes.logic.LogicLiquidSupplier;
import logisticspipes.logic.LogicLiquidSupplierMk2;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsLiquidSupplier;
import logisticspipes.pipes.PipeLiquidSupplierMk2;
import logisticspipes.proxy.MainProxy;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class LiquidSupplierMode extends IntegerCoordinatesPacket {

	public LiquidSupplierMode(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new LiquidSupplierMode(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if(MainProxy.isClient(player.worldObj)) {
			if(pipe.pipe instanceof PipeItemsLiquidSupplier && pipe.pipe.logic instanceof LogicLiquidSupplier) {
				((LogicLiquidSupplier)pipe.pipe.logic).setRequestingPartials((getInteger() % 10) == 1);
			}
			if(pipe.pipe instanceof PipeLiquidSupplierMk2 && pipe.pipe.logic instanceof LogicLiquidSupplierMk2) {
				((LogicLiquidSupplierMk2)pipe.pipe.logic).setRequestingPartials((getInteger() % 10) == 1);
			}
		} else {
			if(pipe.pipe instanceof PipeItemsLiquidSupplier) {
				PipeItemsLiquidSupplier liquid = (PipeItemsLiquidSupplier) pipe.pipe;
				((LogicLiquidSupplier)liquid.logic).setRequestingPartials((getInteger() % 10) == 1);
			}
			if(pipe.pipe instanceof PipeLiquidSupplierMk2) {
				PipeLiquidSupplierMk2 liquid = (PipeLiquidSupplierMk2) pipe.pipe;
				((LogicLiquidSupplierMk2)liquid.logic).setRequestingPartials((getInteger() % 10) == 1);
			}
		}
	}
}

