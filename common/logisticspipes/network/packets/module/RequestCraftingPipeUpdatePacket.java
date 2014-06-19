package logisticspipes.network.packets.module;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;
import logisticspipes.network.packets.cpipe.CPipeSatelliteImportBack;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.Player;

public class RequestCraftingPipeUpdatePacket extends ModuleCoordinatesPacket {

	public RequestCraftingPipeUpdatePacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new RequestCraftingPipeUpdatePacket(getId());
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		ModuleCrafter module = this.getLogisticsModule(player, ModuleCrafter.class);
		if(module == null) return;
		MainProxy.sendPacketToPlayer(module.getCPipePacket(), (Player) player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CPipeSatelliteImportBack.class).setInventory(module.getDummyInventory()).setModulePos(module), (Player) player);
	}
}

