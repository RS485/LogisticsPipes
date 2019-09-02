package logisticspipes.network.packets;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
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
		if (MainProxy.isClient(player.world)) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(UpdateName.class).setIdent(getIdent()).setName(getIdent().getFriendlyName()));
		} else {
			MainProxy.proxy.updateNames(getIdent(), getName());
		}
	}

	@Override
	public void readData(LPDataInput input) {
		ident = input.readItemIdentifierStack().getItem();
		name = input.readUTF();
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeItemIdentifierStack(ident.makeStack(1));
		output.writeUTF(name);
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}
