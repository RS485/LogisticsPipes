package logisticspipes.textures.provider;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.renderer.IIconProvider;

public class LPActionTriggerIconProvider implements IIconProvider {

	public static int actionDisablePipeIconIndex = 0;
	public static int triggerCraftingIconIndex = 1;
	public static int triggerPowerDischargingIconIndex = 2;
	public static int triggerPowerNeededIconIndex = 3;
	public static int triggerSupplierFailedIconIndex = 4;
	public static int triggerHasDestinationIconIndex = 5;
	public static int actionRobotRoutingIconIndex = 6;

	@SideOnly(Side.CLIENT)
	private TextureAtlasSprite[] icons;

	public LPActionTriggerIconProvider() {
		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			icons = new TextureAtlasSprite[7];
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIcon(int iconIndex) {
		if (iconIndex > 6) {
			return null;
		}
		return icons[iconIndex];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(Object icon) {
		TextureMap iconRegister = (TextureMap) icon;
		icons[LPActionTriggerIconProvider.actionDisablePipeIconIndex] = iconRegister.registerSprite(new ResourceLocation("logisticspipes:actionTriggers/DisablePipe"));
		icons[LPActionTriggerIconProvider.triggerCraftingIconIndex] = iconRegister.registerSprite(new ResourceLocation("logisticspipes:actionTriggers/CraftingWaiting"));
		icons[LPActionTriggerIconProvider.triggerPowerDischargingIconIndex] = iconRegister.registerSprite(new ResourceLocation("logisticspipes:actionTriggers/PowerDischarging"));
		icons[LPActionTriggerIconProvider.triggerPowerNeededIconIndex] = iconRegister.registerSprite(new ResourceLocation("logisticspipes:actionTriggers/PowerNeeded"));
		icons[LPActionTriggerIconProvider.triggerSupplierFailedIconIndex] = iconRegister.registerSprite(new ResourceLocation("logisticspipes:actionTriggers/ActionTrigger1"));
		icons[LPActionTriggerIconProvider.triggerHasDestinationIconIndex] = iconRegister.registerSprite(new ResourceLocation("logisticspipes:actionTriggers/ActionTrigger17"));
		icons[LPActionTriggerIconProvider.actionRobotRoutingIconIndex] = iconRegister.registerSprite(new ResourceLocation("logisticspipes:actionTriggers/RobotRouting"));
	}
}
