package logisticspipes.network.abstractpackets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

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
	@Getter
	@Setter
	private int dimension = 0; // If the dimension is not set the packet will be handled in the main overworld

	public ModernPacket(int id) {
		//this.channel = LogisticsPipes.LOGISTICS_PIPES_CHANNEL_NAME;
		this.id = id;
	}

	public void setDimension(World world) {
		dimension = world.provider.getDimension();
	}

	public void readData(LPDataInput input) {
		dimension = input.readInt();
	}

	public abstract void processPacket(EntityPlayer player);

	public void writeData(LPDataOutput output) {
		output.writeInt(dimension);
	}

	public abstract ModernPacket template();

	public boolean retry() {
		return leftRetries-- > 0;
	}
}
