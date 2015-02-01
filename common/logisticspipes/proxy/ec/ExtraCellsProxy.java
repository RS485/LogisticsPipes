package logisticspipes.proxy.ec;

import cpw.mods.fml.common.Loader;
import net.minecraftforge.fluids.Fluid;
import logisticspipes.proxy.interfaces.IExtraCellsProxy;

public class ExtraCellsProxy implements IExtraCellsProxy {

	@Override
	public boolean canSeeFluidInNetwork(Fluid fluid) {
		if(fluid == null)
			return true;
		try{
			Class clazz = Class.forName("extracells.api.ECApi");
			Object instance = clazz.getMethod("instance").invoke(null);
			Class clazz2 = instance.getClass();
			return (Boolean) clazz2.getDeclaredMethod("canFluidSeeInTerminal", Fluid.class).invoke(instance, fluid);
		}catch(Throwable e){}
		return true;
	}

}
