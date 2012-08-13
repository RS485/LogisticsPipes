package net.minecraft.src.buildcraft.krapht.pipes;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.logic.TemporaryLogic;
import net.minecraft.src.buildcraft.logisticspipes.items.RemoteOrderer;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;

public class PipeItemsRemoteOrdererLogistics extends RoutedPipe implements IRequestItems {

	public PipeItemsRemoteOrdererLogistics(int itemID) {
		super(new TemporaryLogic(), itemID);
	}

	@Override
	public int getCenterTexture() {
		return mod_LogisticsPipes.LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE;
	}

	@Override
	public boolean blockActivated(World world, int i, int j, int k,	EntityPlayer entityplayer) {
		if(entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() == mod_LogisticsPipes.LogisticsRemoteOrderer) {
			ItemStack orderer = entityplayer.getCurrentEquippedItem();
			RemoteOrderer.connectToPipe(orderer, this);
			return true;
		} 
		return super.blockActivated(world, i, j, k, entityplayer);
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return null;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

}
