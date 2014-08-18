package logisticspipes.asm;

import java.io.File;
import java.util.Map;

import logisticspipes.LogisticsPipes;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class LogisticsPipesCoreLoader implements IFMLLoadingPlugin {
	
	public LogisticsPipesCoreLoader() throws Exception	 {
		if(LogisticsPipes.DEBUG && new File(".classpath").exists()) {//Only in eclipse debug env
			DevEnvHelper.detectCoreModInEclipseSettings(); //Load CoreMods out of the class path
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
