package logisticspipes.utils;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class ModStatusHelper {
	public static boolean isModLoaded(String modId) {
		if(modId.contains("@")) {
			String version = modId.substring(modId.indexOf('@') + 1);
			modId = modId.substring(0, modId.indexOf('@'));
			if(Loader.isModLoaded(modId)) {
				ModContainer mod = Loader.instance().getIndexedModList().get(modId);
				if(mod != null) {
					return mod.getVersion().contains(version);
				}
			}
			return false;
		} else {
			return Loader.isModLoaded(modId);
		}
	}
}
