package logisticspipes.network.packets.block;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.StringListPacket;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class SecurityStationAuthorizedList extends StringListPacket {

	public SecurityStationAuthorizedList(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SecurityStationAuthorizedList(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		SimpleServiceLocator.securityStationManager.setClientAuthorizationList(getStringList());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
