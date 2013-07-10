package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.oldpackets.PacketNameUpdatePacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet250CustomPayload;

@Accessors(chain=true)
public class UpdateName extends ModernPacket {

	@Getter
	@Setter
	private ItemIdentifier ident;

	@Getter
	@Setter
	private String name;
	
	public UpdateName(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new UpdateName(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		MainProxy.sendCompressedPacketToServer((Packet250CustomPayload)new PacketNameUpdatePacket(getIdent()).getPacket());
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		ident = ItemIdentifier.get(data.readInt(), data.readInt(), null);
		name = data.readUTF();
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(ident.itemID);
		data.writeInt(ident.itemDamage);
		data.writeUTF(name);
	}
}

