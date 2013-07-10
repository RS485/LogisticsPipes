package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.Player;

@Accessors(chain=true)
public class BufferTransfer extends ModernPacket {
	
	@Getter
	@Setter
	private byte[] content;
	
	public BufferTransfer(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new BufferTransfer(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if(MainProxy.isClient(player.worldObj)) {
			SimpleServiceLocator.clientBufferHandler.handlePacket(content);
		} else {
			SimpleServiceLocator.serverBufferHandler.handlePacket(content, (Player)player);
		}
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		content = new byte[data.readInt()];
		data.read(content);
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(content.length);
		data.write(content);
	}
}


