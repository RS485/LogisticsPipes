package logisticspipes.network.abstractguis;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@ToString
public abstract class CoordinatesGuiProvider extends GuiProvider {

	@Getter
	@Setter
	private int posX;
	@Getter
	@Setter
	private int posY;
	@Getter
	@Setter
	private int posZ;

	public CoordinatesGuiProvider(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {

		output.writeInt(posX);
		output.writeInt(posY);
		output.writeInt(posZ);
	}

	@Override
	public void readData(LPDataInput input) {

		posX = input.readInt();
		posY = input.readInt();
		posZ = input.readInt();

	}

	public CoordinatesGuiProvider setTilePos(TileEntity tile) {
		setPosX(tile.getPos().getX());
		setPosY(tile.getPos().getY());
		setPosZ(tile.getPos().getZ());
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
		if (world.isAirBlock(new BlockPos(getPosX(), getPosY(), getPosZ()))) {
			if (LogisticsPipes.isDEBUG()) {
				LogisticsPipes.log.fatal(toString());
				new RuntimeException("Couldn't find " + clazz.getName()).printStackTrace();
			}
			return null;
		}

		final TileEntity tile = world.getTileEntity(new BlockPos(getPosX(), getPosY(), getPosZ()));
		if (tile != null) {
			if (!(clazz.isAssignableFrom(tile.getClass()))) {
				if (LogisticsPipes.isDEBUG()) {
					LogisticsPipes.log.fatal(toString());
					new RuntimeException("Couldn't find " + clazz.getName() + ", found " + tile.getClass()).printStackTrace();
				}
				return null;
			}
		} else {
			if (LogisticsPipes.isDEBUG()) {
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
