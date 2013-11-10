package logisticspipes.proxy.mps;

import java.util.Collections;

import logisticspipes.LogisticsPipes;
import logisticspipes.items.ItemHUDArmor;
import logisticspipes.network.GuiIDs;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import net.machinemuse.api.IModularItem;
import net.machinemuse.api.moduletrigger.IRightClickModule;
import net.machinemuse.powersuits.common.ModularPowersuits;
import net.machinemuse.powersuits.powermodule.PowerModuleBase;
import net.machinemuse.utils.MuseCommonStrings;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class LogisticsPipesHUDConfigModule extends PowerModuleBase implements IRightClickModule {
	
	public static final String NAME = "LP-HUD-Config";
	
	public LogisticsPipesHUDConfigModule() {
		super(Collections.singletonList((IModularItem) ModularPowersuits.powerTool));
		this.addInstallCost(new ItemStack(ModularPowersuits.components, 1, 14));
	}
	
	@Override
	public String getCategory() {
		return MuseCommonStrings.CATEGORY_SPECIAL;
	}
	
	@Override
	public String getDescription() {
		return "Add the LogisitcsPipes HUD Config tool to configure your HUD glasses settings.";
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

	@Override
	public void onItemUse(ItemStack itemstack, EntityPlayer player, World world, int i, int j, int k, int l, float f, float f1, float f2) {}

	@Override
	public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer player, World world, int i, int j, int k, int l, float f, float f1, float f2) {
		useItem(player, world);
		if(MainProxy.isClient(world)) return false;
		return true;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer entityplayer, int i) {}

	@Override
	public void onRightClick(EntityPlayer player, World world, ItemStack itemstack) {
		if(MainProxy.isClient(world)) return;
		useItem(player, world);
	}
	
	private void useItem(EntityPlayer player, World world) {
		if((player.inventory.armorItemInSlot(3) != null && player.inventory.armorItemInSlot(3).getItem() instanceof ItemHUDArmor) || (SimpleServiceLocator.mpsProxy.isMPSHelm(player.inventory.armorItemInSlot(3)) && SimpleServiceLocator.mpsProxy.hasHelmHUDInstalled(player.inventory.armorItemInSlot(3)))) {
			player.openGui(LogisticsPipes.instance, GuiIDs.GUI_HUD_Settings, world, player.inventory.currentItem, -1, 0);			
		}
	}
}
