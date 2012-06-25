package net.minecraft.src.buildcraft.krapht.pipes;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.buildcraft.krapht.gui.GuiOrderer;

public class PipeItemsRequestLogisticsMK2 extends PipeItemsRequestLogistics {

	public PipeItemsRequestLogisticsMK2(int itemID) {
		super(itemID);
	}

	@Override
	public boolean blockActivated(World world, int i, int j, int k,	EntityPlayer entityplayer) {
		ModLoader.getMinecraftInstance().displayGuiScreen(new GuiOrderer(this, entityplayer));
		return true;
	}

	@Override
	public int getCenterTexture() {
		return core_LogisticsPipes.LOGISTICSPIPE_REQUESTERMK2_TEXTURE;
	}
}
