package logisticspipes.network.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
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
		if(MainProxy.isClient(player.worldObj)) {
//TODO		MainProxy.sendCompressedPacketToServer((Packet250CustomPayload)new PacketNameUpdatePacket(getIdent()).getPacket());
			MainProxy.sendCompressedPacketToServer((Packet250CustomPayload) PacketHandler.getPacket(UpdateName.class).setIdent(getIdent()).setName(getIdent().getFriendlyName()).getPacket());
		} else {
			MainProxy.proxy.updateNames(getIdent(), getName());
		}
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

