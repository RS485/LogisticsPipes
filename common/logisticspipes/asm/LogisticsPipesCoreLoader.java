package logisticspipes.asm;

import java.util.Map;

import net.minecraft.launchwrapper.Launch;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

//@IFMLLoadingPlugin.SortingIndex(1001) TODO: For next MC update. Changing this now, will change ASM check sums as well.
public class LogisticsPipesCoreLoader implements IFMLLoadingPlugin {

	public LogisticsPipesCoreLoader() throws Exception {
		Launch.classLoader.addTransformerExclusion("logisticspipes.asm.");
		Launch.classLoader.findClass("logisticspipes.LPConstants").getMethod("loadedCoremod").invoke(null);
		byte[] bs = Launch.classLoader.getClassBytes("net.minecraft.world.World");
		if (bs != null) {
			//Launch.classLoader.findClass("logisticspipes.asm.DevEnvHelper").getMethod("detectCoreModInDevEnv").invoke(null);
			//Launch.classLoader.findClass("logisticspipes.asm.DevEnvHelper").getMethod("handleSpecialClassTransformer").invoke(null);
		}
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[] { "logisticspipes.asm.LogisticsClassTransformer" };
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

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}
