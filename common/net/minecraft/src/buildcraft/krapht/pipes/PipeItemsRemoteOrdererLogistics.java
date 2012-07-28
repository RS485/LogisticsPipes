package net.minecraft.src.buildcraft.krapht.pipes;

import java.util.Random;

import buildcraft.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.mod_LogisticsPipes;
import buildcraft.api.APIProxy;
import buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.logic.BaseRoutingLogic;
import net.minecraft.src.buildcraft.krapht.logic.TemporaryLogic;
import net.minecraft.src.buildcraft.krapht.network.NetworkConstants;
import net.minecraft.src.buildcraft.krapht.network.PacketPipeInteger;
import net.minecraft.src.buildcraft.logisticspipes.items.RemoteOrderer;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleExtractor;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleItemSink;
import net.minecraft.src.forge.DimensionManager;

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
