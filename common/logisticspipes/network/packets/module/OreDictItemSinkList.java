package logisticspipes.network.packets.module;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.modules.ModuleOreDictItemSink;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.NBTModuleCoordinatesPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class OreDictItemSinkList extends NBTModuleCoordinatesPacket {

	public OreDictItemSinkList(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new OreDictItemSinkList(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleOreDictItemSink module = this.getLogisticsModule(player, ModuleOreDictItemSink.class);
		if (module == null) {
			return;
		}
		module.readFromNBT(getTag());
		if (MainProxy.isServer(player.getEntityWorld()) && getType().isInWorld()) {
			module.OreListChanged();
		}
	}
}
