package logisticspipes.proxy.interfaces;

import logisticspipes.interfaces.IHUDConfig;

import net.minecraft.item.ItemStack;

public interface IModularPowersuitsProxy {

	void initModules();

	boolean isMPSHelm(ItemStack stack);

	boolean hasActiveHUDModule(ItemStack stack);

	IHUDConfig getConfigFor(ItemStack stack);

	boolean isMPSHand(ItemStack stack);

	boolean hasHelmHUDInstalled(ItemStack stack);
}
