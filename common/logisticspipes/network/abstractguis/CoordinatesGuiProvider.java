package logisticspipes.network.abstractguis;

import java.io.IOException;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@ToString
public abstract class CoordinatesGuiProvider extends GuiProvider {

	public CoordinatesGuiProvider(int id) {
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

	public CoordinatesGuiProvider setTilePos(TileEntity tile) {
		setPosX(tile.xCoord);
		setPosY(tile.yCoord);
		setPosZ(tile.zCoord);
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
			if (LPConstants.DEBUG) {
				LogisticsPipes.log.fatal(toString());
				new RuntimeException("Couldn't find " + clazz.getName()).printStackTrace();
			}
			return null;
		}

		final TileEntity tile = world.getTileEntity(getPosX(), getPosY(), getPosZ());
		if (tile != null) {
			if (!(clazz.isAssignableFrom(tile.getClass()))) {
				if (LPConstants.DEBUG) {
					LogisticsPipes.log.fatal(toString());
					new RuntimeException("Couldn't find " + clazz.getName() + ", found " + tile.getClass()).printStackTrace();
				}
				return null;
			}
		} else {
			if (LPConstants.DEBUG) {
				LogisticsPipes.log.fatal(toString());
				new RuntimeException("Couldn't find " + clazz.getName()).printStackTrace();
			}
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
}
