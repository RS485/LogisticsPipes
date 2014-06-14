package logisticspipes.network.packets.cpipe;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.Player;

public class CPipeCleanupToggle extends CoordinatesPacket {
	
	public CPipeCleanupToggle(int id) {
		super(id);
	}
	
	@Override
	public ModernPacket template() {
		return new CPipeCleanupToggle(getId());
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		
		if( !(pipe.pipe instanceof PipeItemsCraftingLogistics)) {
			return;
		}
		
		((PipeItemsCraftingLogistics) pipe.pipe).toogleCleaupMode();
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CPipeCleanupStatus.class).setMode(((PipeItemsCraftingLogistics) pipe.pipe).cleanupModeIsExclude).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), (Player) player);
	}
}

