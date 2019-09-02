package logisticspipes.proxy.factorization;

import net.minecraft.tileentity.TileEntity;

import logisticspipes.proxy.interfaces.IFactorizationProxy;

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
