package logisticspipes.proxy.mps;

import logisticspipes.interfaces.IHUDConfig;
import logisticspipes.proxy.interfaces.IModularPowersuitsProxy;
import net.machinemuse.api.ModuleManager;
import net.machinemuse.powersuits.item.ItemPowerArmorHelmet;
import net.machinemuse.powersuits.item.ItemPowerFist;
import net.minecraft.item.ItemStack;

public class ModularPowersuitsProxy implements IModularPowersuitsProxy {

	@Override
	public void initModules() {
		ModuleManager.addModule(new LogisticsPipesHUDModule());
		ModuleManager.addModule(new LogisticsPipesHUDConfigModule());
	}

	@Override
	public boolean isMPSHelm(ItemStack stack) {
		if(stack == null) return false;
		return stack.getItem() instanceof ItemPowerArmorHelmet;
	}

	@Override
	public boolean isMPSHand(ItemStack stack) {
		if(stack == null) return false;
		return stack.getItem() instanceof ItemPowerFist;
	}

	@Override
	public boolean hasActiveHUDModule(ItemStack stack) {
		return ModuleManager.itemHasActiveModule(stack, LogisticsPipesHUDModule.NAME);
	}

	@Override
	public boolean hasHelmHUDInstalled(ItemStack stack) {
		return ModuleManager.itemHasModule(stack, LogisticsPipesHUDModule.NAME);
	}

	IHUDConfig dummy = new IHUDConfig() {
		@Override public boolean isHUDSatellite() {return false;}
		@Override public boolean isHUDProvider() {return false;}
		@Override public boolean isHUDPowerJunction() {return false;}
		@Override public boolean isHUDInvSysCon() {return false;}
		@Override public boolean isHUDCrafting() {return false;}
		@Override public boolean isHUDChassie() {return false;}
		@Override public void setHUDChassie(boolean state) {}
		@Override public void setHUDCrafting(boolean state) {}
		@Override public void setHUDInvSysCon(boolean state) {}
		@Override public void setHUDPowerJunction(boolean state) {}
		@Override public void setHUDProvider(boolean state) {}
		@Override public void setHUDSatellite(boolean state) {}
	};
	
	@Override
	public IHUDConfig getConfigFor(ItemStack stack) {
		if(stack == null) return dummy;
		if(stack.getTagCompound() == null) return dummy;
		return new MPSHUDConfig(stack.getTagCompound());
	}
}
