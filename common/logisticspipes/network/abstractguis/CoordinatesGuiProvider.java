package logisticspipes.network.abstractguis;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.UtilWorld;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;

@Accessors(chain = true)
@ToString
public abstract class CoordinatesGuiProvider extends GuiProvider {

	public CoordinatesGuiProvider(int id) {
		super(id);
	}

	@Getter
	@Setter
	public BlockPos blockPos;
	@Getter
	@Setter
	private int Xcoord = blockPos.getX();
	@Getter
	@Setter
	private int Ycoord = blockPos.getY();
	@Getter
	@Setter
	private int Zcoord = blockPos.getZ();



	@Override
	public void writeData(LPDataOutputStream data) throws IOException {

		data.writeInt(blockPos.getX());
		data.writeInt(blockPos.getY());
		data.writeInt(blockPos.getZ());
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {

		Xcoord = data.readInt();
		Ycoord = data.readInt();
		Zcoord = data.readInt();

	}

	public CoordinatesGuiProvider setTilePos(TileEntity tile) {
		setXcoord(tile.getPos().getX());
		setYcoord(tile.getPos().getY());
		setZcoord(tile.getPos().getZ());
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
		if (!UtilWorld.blockExists(blockPos,world)) {
			if (LPConstants.DEBUG) {
				LogisticsPipes.log.fatal(toString());
				new RuntimeException("Couldn't find " + clazz.getName()).printStackTrace();
			}
			return null;
		}

		final TileEntity tile = world.getTileEntity((getBlockPos()));
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
