package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.items.RemoteOrderer;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;

public class PipeItemsRemoteOrdererLogistics extends CoreRoutedPipe implements IRequestItems {

	public PipeItemsRemoteOrdererLogistics(Item item) {
		super(item);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE;
	}

	@Override
	public boolean handleClick(EntityPlayer entityplayer, SecuritySettings settings) {
		if (entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsRemoteOrderer) {
			if (MainProxy.isServer(getWorld())) {
				if (settings == null || settings.openRequest) {
					ItemStack orderer = entityplayer.getCurrentEquippedItem();
					RemoteOrderer.connectToPipe(orderer, this);
					entityplayer.addChatComponentMessage(new ChatComponentTranslation("lp.chat.connectedtopipe"));
				} else {
					entityplayer.addChatComponentMessage(new ChatComponentTranslation("lp.chat.permissiondenied"));
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return null;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

}
