package logisticspipes.proxy.interfaces;

import logisticspipes.interfaces.IHUDConfig;

import net.minecraft.item.ItemStack;

public interface IModularPowersuitsProxy {

	public void initModules();

	public boolean isMPSHelm(ItemStack stack);

	public boolean hasActiveHUDModule(ItemStack stack);

	public IHUDConfig getConfigFor(ItemStack stack);

	public boolean isMPSHand(ItemStack stack);

	public boolean hasHelmHUDInstalled(ItemStack stack);
}
