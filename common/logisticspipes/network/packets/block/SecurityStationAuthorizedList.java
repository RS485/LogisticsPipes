package logisticspipes.network.packets.block;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.StringListPacket;
import logisticspipes.proxy.SimpleServiceLocator;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

@Accessors(chain=true)
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

