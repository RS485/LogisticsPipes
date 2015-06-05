package logisticspipes.network.packets;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
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
		if (MainProxy.isClient(player.worldObj)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(UpdateName.class).setIdent(getIdent()).setName(getIdent().getFriendlyName()));
		} else {
			MainProxy.proxy.updateNames(getIdent(), getName());
		}
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		ident = data.readItemIdentifierStack().getItem();
		name = data.readUTF();
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeItemIdentifierStack(ident.makeStack(0));
		data.writeUTF(name);
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
