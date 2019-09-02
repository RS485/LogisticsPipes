package logisticspipes.proxy.interfaces;

import net.minecraft.item.ItemStack;

import logisticspipes.interfaces.IHUDConfig;

public interface IModularPowersuitsProxy {

	void initModules();

	boolean isMPSHelm(ItemStack stack);

	boolean hasActiveHUDModule(ItemStack stack);

	IHUDConfig getConfigFor(ItemStack stack);

	boolean isMPSHand(ItemStack stack);

	boolean hasHelmHUDInstalled(ItemStack stack);
}
