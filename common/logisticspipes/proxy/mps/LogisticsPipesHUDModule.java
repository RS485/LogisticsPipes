package logisticspipes.proxy.mps;

import java.util.Collections;

import logisticspipes.LogisticsPipes;
import net.machinemuse.api.IModularItem;
import net.machinemuse.api.moduletrigger.IToggleableModule;
import net.machinemuse.powersuits.common.ModularPowersuits;
import net.machinemuse.powersuits.powermodule.PowerModuleBase;
import net.machinemuse.utils.MuseCommonStrings;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;

public class LogisticsPipesHUDModule extends PowerModuleBase implements IToggleableModule {
	
	public static final String NAME = "LP-HUD";
	
	public LogisticsPipesHUDModule() {
		super(Collections.singletonList((IModularItem) ModularPowersuits.powerArmorHead));
		this.addInstallCost(new ItemStack(LogisticsPipes.LogisticsHUDArmor));
	}
	
	@Override
	public String getCategory() {
		return MuseCommonStrings.CATEGORY_SPECIAL;
	}
	
	@Override
	public String getDescription() {
		return "Add the LogisitcsPipes HUD Glasses to your helmet to see more about what your LP network is up to.";
	}
	
	@Override
	public String getDataName() {
		return NAME;
	}
	
	@Override
	public String getLocalizedName() {
		return NAME;
	}
	
	@Override
	public void registerIcon(IconRegister register) {
		icon = register.registerIcon("logisticspipes:logisticsHUDGlasses");
	}
	
	@Override
	public String getTextureFile() {
		return null;
	}
}
