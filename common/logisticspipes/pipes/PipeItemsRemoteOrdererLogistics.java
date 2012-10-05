package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Textures;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.items.RemoteOrderer;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.World;

public class PipeItemsRemoteOrdererLogistics extends RoutedPipe implements IRequestItems {

	public PipeItemsRemoteOrdererLogistics(int itemID) {
		super(new TemporaryLogic(), itemID);
	}

	@Override
	public int getCenterTexture() {
		return Textures.LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE;
	}

	@Override
	public boolean blockActivated(World world, int i, int j, int k,	EntityPlayer entityplayer) {
		if(entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsRemoteOrderer && MainProxy.isServer()) {
			ItemStack orderer = entityplayer.getCurrentEquippedItem();
			RemoteOrderer.connectToPipe(orderer, this);
			entityplayer.sendChatToPlayer("Connected to pipe");
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
