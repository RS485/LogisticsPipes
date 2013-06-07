package logisticspipes.network.abstractpackets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.transport.TileGenericPipe;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
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
	public void writeData(DataOutputStream data) throws IOException {

		data.writeInt(posX);
		data.writeInt(posY);
		data.writeInt(posZ);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {

		posX = data.readInt();
		posY = data.readInt();
		posZ = data.readInt();

	}

	@SuppressWarnings("unchecked")
	/**
	 * Retrieves tileEntity at packet coordinates if any.
	 * 
	 * @param world
	 * @param clazz
	 * @return TileEntity
	 */
	public <T extends TileEntity> T getTile(World world, Class<T> clazz) {
		if (world == null) {
			return null;
		}
		if (!world.blockExists(getPosX(), getPosY(), getPosZ())) {
			return null;
		}

		final TileEntity tile = world.getBlockTileEntity(getPosX(), getPosY(), getPosZ());
		if(!(clazz.isAssignableFrom(tile.getClass()))) return null;
		return (T) tile;
	}

	/**
	 * Retrieves pipe at packet coordinates if any.
	 * 
	 * @param world
	 * @return
	 */
	public TileGenericPipe getPipe(World world) {
		return getTile(world, TileGenericPipe.class);
	}
}
