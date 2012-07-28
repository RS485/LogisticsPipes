package net.minecraft.src.buildcraft.krapht.pipes;

import buildcraft.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.core_LogisticsPipes;

public class PipeItemsRequestLogisticsMk2 extends PipeItemsRequestLogistics {

	public PipeItemsRequestLogisticsMk2(int itemID) {
		super(itemID);
	}

	@Override
	public boolean blockActivated(World world, int i, int j, int k,	EntityPlayer entityplayer) {
		openGui(entityplayer);
		return true;
	}

	@Override
	public int getCenterTexture() {
		return core_LogisticsPipes.LOGISTICSPIPE_REQUESTERMK2_TEXTURE;
	}
}
