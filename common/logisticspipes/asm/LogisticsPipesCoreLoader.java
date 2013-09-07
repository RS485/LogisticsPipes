package logisticspipes.asm;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;

@MCVersion(value="1.6.2")
public class LogisticsPipesCoreLoader implements IFMLLoadingPlugin {
	@Override
	public String[] getLibraryRequestClass() {
		return null;
	}
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{"logisticspipes.asm.LogisticsClassTransformer"};
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
