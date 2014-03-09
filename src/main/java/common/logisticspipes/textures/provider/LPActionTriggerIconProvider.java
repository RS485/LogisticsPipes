package logisticspipes.textures.provider;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LPActionTriggerIconProvider implements IIconProvider
{
	public static int actionDisablePipeIconIndex = 0;
	public static int triggerCraftingIconIndex = 1;
	public static int triggerPowerDischargingIconIndex = 2;
	public static int triggerPowerNeededIconIndex = 3;
	public static int triggerSupplierFailedIconIndex = 4;
	public static int triggerHasDestinationIconIndex = 5;
	
	private Icon icons[];
	public LPActionTriggerIconProvider() {
		icons = new Icon[6];
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int iconIndex) {
		if(iconIndex>6)
			return null;
		return icons[iconIndex];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		icons[actionDisablePipeIconIndex] = iconRegister.registerIcon("logisticspipes:actionTriggers/DisablePipe");
		icons[triggerCraftingIconIndex] = iconRegister.registerIcon("logisticspipes:actionTriggers/CraftingWaiting");
		icons[triggerPowerDischargingIconIndex] = iconRegister.registerIcon("logisticspipes:actionTriggers/PowerDischarging");
		icons[triggerPowerNeededIconIndex] = iconRegister.registerIcon("logisticspipes:actionTriggers/PowerNeeded");
		icons[triggerSupplierFailedIconIndex] = iconRegister.registerIcon("logisticspipes:actionTriggers/ActionTrigger1");
		icons[triggerHasDestinationIconIndex] = iconRegister.registerIcon("logisticspipes:actionTriggers/ActionTrigger17");
	}
}