package logisticspipes.network.packets.module;

import logisticspipes.modules.ModuleThaumicAspectSink;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.NBTModuleCoordinatesPacket;
import logisticspipes.proxy.MainProxy;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

@Accessors(chain=true)
public class ThaumicAspectsSinkList extends NBTModuleCoordinatesPacket {

	public ThaumicAspectsSinkList(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ThaumicAspectsSinkList(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleThaumicAspectSink module = this.getLogisticsModule(player, ModuleThaumicAspectSink.class);
		if(module == null) return;
		module.readFromNBT(getTag());
		if(MainProxy.isServer(player.getEntityWorld()) && this.getType().isInWorld()) {
			module.aspectListChanged();
		}
	}
}

