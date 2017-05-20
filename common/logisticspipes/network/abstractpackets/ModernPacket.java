package logisticspipes.network.abstractpackets;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public abstract class ModernPacket {

	@Getter
	@Setter
	private boolean isChunkDataPacket;

	/**
	 * Usually means "to compress".
	 * The getter {@link #isCompressable()} can be overridden to always disable/enable compression though.
	 */
	@Getter
	@Setter
	private boolean compressable;
	/*
	@Getter
	protected String channel;
	 */
	@Getter
	private final int id;

	@Getter
	@Setter
	private int debugId = 0;

	protected int leftRetries = 5;

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

	public abstract void readData(LPDataInputStream inStream) throws IOException;

	public abstract void processPacket(EntityPlayer player);

	/**
	 * Serializes packet out to the given stream.
	 * Must be thread-safe to main thread for compressable packets!
	 *
	 * @param outStream the stream to write to
	 * @throws IOException when there are problems with serializing
	 */
	public abstract void writeData(LPDataOutputStream outStream) throws IOException;

	public abstract ModernPacket template();

	public boolean retry() {
		return leftRetries-- > 0;
	}
}
