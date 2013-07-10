package logisticspipes.network.packets;

import logisticspipes.interfaces.PlayerListReciver;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.StringListPacket;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;

@Accessors(chain=true)
public class PlayerList extends StringListPacket {

	public PlayerList(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new PlayerList(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof PlayerListReciver) {
			((PlayerListReciver)FMLClientHandler.instance().getClient().currentScreen).recivePlayerList(getStringList());
		}
	}
}

