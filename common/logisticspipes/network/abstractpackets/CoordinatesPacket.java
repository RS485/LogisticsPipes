package logisticspipes.network.abstractpackets;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.exception.TargetNotFoundException;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@ToString
public abstract class CoordinatesPacket extends ModernPacket {

	public enum LTGPCompletionCheck {
		NONE,
		PIPE,
		TRANSPORT;
	}

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
		setPosX(tile.xCoord);
		setPosY(tile.yCoord);
		setPosZ(tile.zCoord);
		return this;
	}

	public CoordinatesPacket setLPPos(LPPosition pos) {
		setPosX(pos.getX());
		setPosY(pos.getY());
		setPosZ(pos.getZ());
		return this;
	}

	public CoordinatesPacket setPacketPos(CoordinatesPacket packet) {
		posX = packet.posX;
		posY = packet.posY;
		posZ = packet.posZ;
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
			targetNotFound("World was null");
			return null;
		}
		if (!world.blockExists(getPosX(), getPosY(), getPosZ())) {
			targetNotFound("Couldn't find " + clazz.getName());
			return null;
		}

		final TileEntity tile = world.getTileEntity(getPosX(), getPosY(), getPosZ());
		if (tile != null) {
			if (!(clazz.isAssignableFrom(tile.getClass()))) {
				targetNotFound("Couldn't find " + clazz.getName() + ", found " + tile.getClass());
				return null;
			}
		} else {
			targetNotFound("Couldn't find " + clazz.getName());
		}
		return (T) tile;
	}

	@SuppressWarnings("unchecked")
	/**
	 * Retrieves tileEntity or CoreUnroutedPipe at packet coordinates if any.
	 * 
	 * @param world
	 * @param clazz
	 * @return TileEntity
	 */
	public <T> T getTileOrPipe(World world, Class<T> clazz) {
		if (world == null) {
			targetNotFound("World was null");
			return null;
		}
		if (!world.blockExists(getPosX(), getPosY(), getPosZ())) {
			targetNotFound("Couldn't find " + clazz.getName());
			return null;
		}

		final TileEntity tile = world.getTileEntity(getPosX(), getPosY(), getPosZ());
		if (tile != null) {
			if (clazz.isAssignableFrom(tile.getClass())) {
				return (T) tile;
			}
			if (tile instanceof LogisticsTileGenericPipe) {
				if (((LogisticsTileGenericPipe) tile).pipe != null && clazz.isAssignableFrom(((LogisticsTileGenericPipe) tile).pipe.getClass())) {
					return (T) ((LogisticsTileGenericPipe) tile).pipe;
				}
				targetNotFound("Couldn't find " + clazz.getName() + ", found pipe with " + tile.getClass());
				return null;
			}
		} else {
			targetNotFound("Couldn't find " + clazz.getName());
			return null;
		}
		targetNotFound("Couldn't find " + clazz.getName() + ", found " + tile.getClass());
		return null;
	}

	/**
	 * Retrieves pipe at packet coordinates if any.
	 * 
	 * @param world
	 * @return
	 */
	public LogisticsTileGenericPipe getPipe(World world) {
		return getPipe(world, LTGPCompletionCheck.NONE);
	}

	public LogisticsTileGenericPipe getPipe(World world, LTGPCompletionCheck check) {
		LogisticsTileGenericPipe pipe = getTile(world, LogisticsTileGenericPipe.class);
		if (check == LTGPCompletionCheck.PIPE || check == LTGPCompletionCheck.TRANSPORT) {
			if (pipe.pipe == null) {
				targetNotFound("The found pipe didn't have a loaded pipe field");
			}
		}
		if (check == LTGPCompletionCheck.TRANSPORT) {
			if (pipe.pipe.transport == null) {
				targetNotFound("The found pipe didn't have a loaded transport field");
			}
		}
		return pipe;
	}

	protected void targetNotFound(String message) {
		throw new TargetNotFoundException(message, this);
	}
}
