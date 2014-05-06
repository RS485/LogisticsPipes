package logisticspipes.asm;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class LogisticsPipesCoreLoader implements IFMLLoadingPlugin {
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
