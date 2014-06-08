package logisticspipes.modules;

import java.util.Map;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsCraftingLogisticsMk3;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.BufferMode;
import logisticspipes.utils.item.ItemIdentifier;

public class ModuleCrafterMK2 extends ModuleCrafter {

	public ModuleCrafterMK2() {
	}

	public ModuleCrafterMK2(
			PipeItemsCraftingLogistics parentPipe) {
		super(parentPipe);
	}


	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleCrafterMK2");
	}


	@Override
	protected int neededEnergy() {
		return 15;
	}

	@Override
	protected int itemsToExtract() {
		return 64;
	}
	
	@Override
	protected int stacksToExtract() {
		return 1;
	}


}
