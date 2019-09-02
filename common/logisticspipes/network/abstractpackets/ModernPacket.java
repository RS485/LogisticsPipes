package logisticspipes.network.abstractpackets;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.packetcontent.IPacketContent;
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
	private int dimension = 0; // If the dimension is not set the packet will be handled in the main overworld

	public List<IPacketContent<?>> content = Collections.emptyList();

	public ModernPacket(int id) {
		this.id = id;
	}

	public ModernPacket setDimension(int dimension) {
		this.dimension = dimension;
		return this;
	}

	public ModernPacket setDimension(World world) {
		this.dimension = world.provider.getDimension();
		return this;
	}

	public void readData(LPDataInput input) {
		dimension = input.readInt();
		content.forEach(it -> it.readData(input));
	}

	public abstract void processPacket(EntityPlayer player);

	public void writeData(LPDataOutput output) {
		output.writeInt(dimension);
		content.forEach(it -> it.writeData(output));
	}

	public abstract ModernPacket template();

	public boolean retry() {
		return leftRetries-- > 0;
	}
}
