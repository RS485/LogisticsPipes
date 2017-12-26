package logisticspipes.network.abstractpackets;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class ModernPacket {

	/*
	@Getter
	protected String channel;
	 */
	@Getter
	private final int id;
	protected int leftRetries = 5;
	@Getter
	@Setter
	private boolean isChunkDataPacket;
	@Getter
	@Setter
	private boolean compressable;
	//@Getter
	//private byte[] data = null;
	@Getter
	@Setter
	private int debugId = 0;

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
				dataStream.writeInt(debugId);
				writeData(dataStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
			data = dataStream.toByteArray();
		}
	 */

	public abstract void readData(LPDataInput input);

	public abstract void processPacket(EntityPlayer player);

	public abstract void writeData(LPDataOutput output);

	public abstract ModernPacket template();

	public boolean retry() {
		return leftRetries-- > 0;
	}
}
