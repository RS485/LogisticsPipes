package logisticspipes.pipes;

import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.items.RemoteOrderer;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class PipeItemsRemoteOrdererLogistics extends CoreRoutedPipe implements IRequestItems {

	public PipeItemsRemoteOrdererLogistics(int itemID) {
		super(new TemporaryLogic(), itemID);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_REMOTE_ORDERER_TEXTURE;
	}

	@Override
	public boolean handleClick(World world, int i, int j, int k, EntityPlayer entityplayer, SecuritySettings settings) {
		if(entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsRemoteOrderer) {
			if(MainProxy.isServer(world)) {
				if (settings == null || settings.openRequest) {
					ItemStack orderer = entityplayer.getCurrentEquippedItem();
					RemoteOrderer.connectToPipe(orderer, this);
					entityplayer.sendChatToPlayer("Connected to pipe");
				} else {
					entityplayer.sendChatToPlayer("Permission denied");
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return null;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}


	@Override
	public int getIconIndex(ForgeDirection direction) {
		// TODO Auto-generated method stub
		return 0;
	}

}
