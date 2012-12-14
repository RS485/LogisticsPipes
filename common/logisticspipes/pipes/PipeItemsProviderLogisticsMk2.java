package logisticspipes.pipes;

import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;

public class PipeItemsProviderLogisticsMk2 extends PipeItemsProviderLogistics {

	public PipeItemsProviderLogisticsMk2(int itemID) {
		super(itemID);
	}

	@Override
	public TextureType getCenterTexture() {
		if(SimpleServiceLocator.buildCraftProxy.checkMaxItems()) {
			return Textures.LOGISTICSPIPE_PROVIDERMK2_TEXTURE;
		} else {
			return Textures.LOGISTICSPIPE_PROVIDERMK2_TEXTURE_DIS;
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if(MainProxy.isClient()) return;
		
		if (!_orderManager.hasOrders() || worldObj.getWorldTime() % 6 != 0) return;
		for(int i = 0; i < 16; i++) {
			if(_orderManager.hasOrders()) {
				if(!useEnergy(2)) return;
				Pair<ItemIdentifierStack,IRequestItems> order = _orderManager.getNextRequest();
				int sent = sendItem(order.getValue1().getItem(), order.getValue1().stackSize, order.getValue2().getRouter().getId());
				MainProxy.proxy.spawnGenericParticle("VioletParticle", this.xCoord, this.yCoord, this.zCoord, 3);
				if (sent > 0){
					_orderManager.sendSuccessfull(sent);
				}
				else {
					_orderManager.sendFailed();
				}
				if(!SimpleServiceLocator.buildCraftProxy.checkMaxItems()) {
					break;
				}
			}
		}
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Fast;
	}
}
