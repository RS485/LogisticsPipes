package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.liquid.LiquidRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class PipeLiquidRequestLogistics extends LiquidRoutedPipe implements IRequestLiquid {

	public PipeLiquidRequestLogistics(int itemID) {
		super(itemID);
	}
	
	public void openGui(EntityPlayer entityplayer) {
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Liquid_Orderer_ID, this.worldObj, this.xCoord , this.yCoord, this.zCoord);
	}
	
	@Override
	public boolean blockActivated(World world, int i, int j, int k,	EntityPlayer entityplayer) {
		//if (SimpleServiceLocator.buildCraftProxy.isWrenchEquipped(entityplayer)) { //TODO Deside with or without wrench
			if (MainProxy.isServer(this.worldObj)) {
				openGui(entityplayer);
			}
		//}
		
		return super.blockActivated(world, i, j, k, entityplayer);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUID_REQUEST;
	}

}
