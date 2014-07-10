package logisticspipes.network.abstractpackets;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

@Accessors(chain=true)
public abstract class ModernPacket {

	@Getter
	@Setter
	private boolean isChunkDataPacket;
	
	@Getter
	@Setter
	private boolean compressable;
	/*
	@Getter
	protected String channel;
	*/
	@Getter
	private final int id;

	public ModernPacket(int id) {
		//this.channel = LogisticsPipes.LOGISTICS_PIPES_CHANNEL_NAME;
		this.id = id;
	}
/*
	public Packet250CustomPayload getPacket() {
		if(data == null) throw new RuntimeException("The packet needs to be created() first;");
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = channel;
		packet.data = this.data;
		packet.length = packet.data.length;
		packet.isChunkDataPacket = isChunkDataPacket();
		return packet;
	}

	public void create() {
		if(data != null) return; //PacketBuffer already created
		LPDataOutputStream dataStream = new LPDataOutputStream();
		try {
			dataStream.writeInt(getId());
			writeData(dataStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		data = dataStream.toByteArray();
	}
*/
	
	public abstract void readData(LPDataInputStream data) throws IOException;
	public abstract void processPacket(EntityPlayer player);
	public abstract void writeData(LPDataOutputStream data) throws IOException;
	public abstract ModernPacket template();
}