package logisticspipes.network.packets.pipe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.PacketPayload;
import logisticspipes.network.TilePacketWrapper;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

@Accessors(chain=true)
public class PipeUpdate extends CoordinatesPacket {

	@Getter
	@Setter
	private PacketPayload payload;
	
	public PipeUpdate(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new PipeUpdate(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		TileGenericPipe tile = this.getPipe(player.worldObj);
		if(tile == null) {
			return;
		}
		if(tile.pipe == null) {
			return;
		}
		//FIXME: does the old logic at tile.pipe need to be here?!
		new TilePacketWrapper(new Class[] { TileGenericPipe.class, tile.pipe.transport.getClass(), tile.pipe.getClass() }).fromPayload(new Object[] { tile.pipe.container, tile.pipe.transport, tile.pipe }, getPayload());
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		// No payload means no data
		if (payload == null) {
			data.writeInt(0);
			data.writeInt(0);
			data.writeInt(0);
			return;
		}

		data.writeInt(payload.intPayload.length);
		data.writeInt(payload.floatPayload.length);
		data.writeInt(payload.stringPayload.length);

		for (int intData : payload.intPayload)
			data.writeInt(intData);
		for (float floatData : payload.floatPayload)
			data.writeFloat(floatData);
		for (String stringData : payload.stringPayload)
			data.writeUTF(stringData);

	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		
		payload = new PacketPayload();

		payload.intPayload = new int[data.readInt()];
		payload.floatPayload = new float[data.readInt()];
		payload.stringPayload = new String[data.readInt()];

		for (int i = 0; i < payload.intPayload.length; i++)
			payload.intPayload[i] = data.readInt();
		for (int i = 0; i < payload.floatPayload.length; i++)
			payload.floatPayload[i] = data.readFloat();
		for (int i = 0; i < payload.stringPayload.length; i++)
			payload.stringPayload[i] = data.readUTF();

	}
}

