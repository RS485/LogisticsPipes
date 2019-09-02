package logisticspipes.utils;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.versioning.ComparableVersion;

public class ModStatusHelper {

	public static boolean isModLoaded(String modId) {
		if (modId.contains("@")) {
			String version = modId.substring(modId.indexOf('@') + 1);
			modId = modId.substring(0, modId.indexOf('@'));
			if (Loader.isModLoaded(modId)) {
				ModContainer mod = Loader.instance().getIndexedModList().get(modId);
				if (mod != null) {
					return mod.getVersion().startsWith(version);
				}
			}
			return false;
		} else if (Loader.isModLoaded(modId)) {
			return true;
		} else {
			return ModAPIManager.INSTANCE.hasAPI(modId);
		}
	}

	public static boolean areModsLoaded(String modIds) {
		if (modIds.contains("+")) {
			for (String modId : modIds.split("\\+")) {
				if (!isModLoaded(modId)) {
					return false;
				}
			}
			return true;
		} else {
			return isModLoaded(modIds);
		}
	}

	public static boolean isModVersionEqualsOrHigher(String modId, String version) {
		ComparableVersion v1 = new ComparableVersion(version);
		ModContainer mod = Loader.instance().getIndexedModList().get(modId);
		if (mod != null) {
			ComparableVersion v2 = new ComparableVersion(mod.getVersion());
			return v1.compareTo(v2) <= 0;
		}
		return false;
	}
}
