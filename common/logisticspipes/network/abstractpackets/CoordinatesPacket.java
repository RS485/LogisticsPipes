package logisticspipes.network.abstractpackets;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.exception.TargetNotFoundException;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

@Accessors(chain = true)
@ToString
public abstract class CoordinatesPacket extends ModernPacket {

	public CoordinatesPacket(int id) {
		super(id);
	}

	@Getter
	@Setter
	private int posX;
	@Getter
	@Setter
	private int posY;
	@Getter
	@Setter
	private int posZ;

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {

		data.writeInt(posX);
		data.writeInt(posY);
		data.writeInt(posZ);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {

		posX = data.readInt();
		posY = data.readInt();
		posZ = data.readInt();

	}

	public CoordinatesPacket setTilePos(TileEntity tile) {
		this.setPosX(tile.xCoord);
		this.setPosY(tile.yCoord);
		this.setPosZ(tile.zCoord);
		return this;
	}
	/*
	public CoordinatesPacket setModulePos(LogisticsModule module) {
		this.setPosX(module.getX());
		this.setPosY(module.getY());
		this.setPosZ(module.getZ());
		return this;
	}
	*/
	
	public CoordinatesPacket setPacketPos(CoordinatesPacket packet) {
		this.posX = packet.posX;
		this.posY = packet.posY;
		this.posZ = packet.posZ;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Retrieves tileEntity at packet coordinates if any.
	 * 
	 * @param world
	 * @param clazz
	 * @return TileEntity
	 */
	public <T> T getTile(World world, Class<T> clazz) {
		if (world == null) {
			return null;
		}
		if (!world.blockExists(getPosX(), getPosY(), getPosZ())) {
			targetNotFound("Couldn't find " + clazz.getName());
			return null;
		}

		final TileEntity tile = world.getTileEntity(getPosX(), getPosY(), getPosZ());
		if(tile != null) {
			if(!(clazz.isAssignableFrom(tile.getClass()))) {
				targetNotFound("Couldn't find " + clazz.getName() + ", found " + tile.getClass());
				return null;
			}
		} else {
			targetNotFound("Couldn't find " + clazz.getName());
		}
		return (T) tile;
	}

	/**
	 * Retrieves pipe at packet coordinates if any.
	 * 
	 * @param world
	 * @return
	 */
	public LogisticsTileGenericPipe getPipe(World world) {
		return getTile(world, LogisticsTileGenericPipe.class);
	}
	
	protected void targetNotFound(String message) {
		throw new TargetNotFoundException(message, this);
	}
}
