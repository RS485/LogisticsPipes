package logisticspipes.proxy.forestry;

import java.lang.reflect.Field;

import logisticspipes.proxy.interfaces.IGenericProgressProvider;

import net.minecraft.tileentity.TileEntity;

import forestry.core.tiles.TilePowered;

public class ForestryProgressProvider implements IGenericProgressProvider {

	private Field workCounter;

	public ForestryProgressProvider() throws NoSuchFieldException, SecurityException {
		workCounter = TilePowered.class.getDeclaredField("workCounter");
		workCounter.setAccessible(true);
	}

	@Override
	public boolean isType(TileEntity tile) {
		return tile instanceof TilePowered;
	}

	@Override
	public byte getProgress(TileEntity tile) {
		try {
			return (byte) Math.max(0, Math.min(((Integer) workCounter.get(tile)).intValue() * 4, 100));
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
