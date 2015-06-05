package logisticspipes.proxy.factorization;

import logisticspipes.proxy.interfaces.IFactorizationProxy;

import net.minecraft.tileentity.TileEntity;

public class FactorizationProxy implements IFactorizationProxy {

	public static final String barelClassPath = "factorization.weird.TileEntityDayBarrel";
	private Class<?> barrelClass;

	public FactorizationProxy() throws ClassNotFoundException {
		barrelClass = Class.forName(FactorizationProxy.barelClassPath);
	}

	@Override
	public boolean isBarral(TileEntity tile) {
		return barrelClass.isAssignableFrom(tile.getClass());
	}
}
