package logisticspipes.network.packets.module;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.modules.SupplierPipeMode;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.PipeItemsSupplierLogistics.PatternMode;
import logisticspipes.pipes.PipeItemsSupplierLogistics.SupplyMode;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;

public class SupplierPipeModePacket extends CoordinatesPacket {

	public SupplierPipeModePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SupplierPipeModePacket(getId());
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
		final PipeItemsSupplierLogistics logic = (PipeItemsSupplierLogistics) pipe.pipe;
		int  mode;
		if(logic.getUpgradeManager().hasPatternUpgrade()) {
			mode = logic.getPatternMode().ordinal() +1;
			if(mode >= PatternMode.values().length)
				mode=0;
			logic.setPatternMode(PatternMode.values()[mode]);
		} else {
			mode = logic.getSupplyMode().ordinal() +1;
			if(mode >= SupplyMode.values().length)
				mode=0;
			logic.setSupplyMode(SupplyMode.values()[mode]);
		}
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SupplierPipeMode.class).setHasPatternUpgrade(logic.getUpgradeManager().hasPatternUpgrade()).setInteger(mode).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), player);
	}
}

