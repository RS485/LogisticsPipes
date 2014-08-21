package logisticspipes.asm;

import java.util.Map;

import net.minecraft.launchwrapper.Launch;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class LogisticsPipesCoreLoader implements IFMLLoadingPlugin {
	
	public LogisticsPipesCoreLoader() throws Exception {
		byte[] bs = Launch.classLoader.getClassBytes("net.minecraft.world.World");
		if(bs != null) {
			Launch.classLoader.findClass("logisticspipes.asm.DevEnvHelper").getMethod("detectCoreModInEclipseSettings").invoke(null);
		}
	}
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{
				"logisticspipes.asm.LogisticsClassTransformer"
				};
	}

	@Override
	public String getAccessTransformerClass() {
		return "logisticspipes.asm.LogisticsAccessTransformer";
	}
	
	@Override
	public String getModContainerClass() {
		return null;
	}
	
	@Override
	public String getSetupClass() {
		return null;
	}
	
	@Override
	public void injectData(Map<String, Object> data) {
		
	}
}
