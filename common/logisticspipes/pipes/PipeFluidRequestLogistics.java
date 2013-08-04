package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.FluidIdentifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;

public class PipeFluidRequestLogistics extends FluidRoutedPipe implements IRequestFluid {

	public PipeFluidRequestLogistics(int itemID) {
		super(itemID);
	}
	
	public void openGui(EntityPlayer entityplayer) {
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Fluid_Orderer_ID, this.getWorld(), this.getX() , this.getY(), this.getZ());
	}
	
	@Override
	public boolean wrenchClicked(EntityPlayer entityplayer, SecuritySettings settings) {
		if(MainProxy.isServer(getWorld())) {
			if (settings == null || settings.openRequest) {
				openGui(entityplayer);
			} else {
				entityplayer.sendChatToPlayer(ChatMessageComponent.func_111066_d("Permission denied"));
			}
		}
		return true;
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUID_REQUEST;
	}

	@Override
	public void sendFailed(FluidIdentifier value1, Integer value2) {
		//Request Pipe doesn't handle this.
	}

	@Override
	public boolean canInsertToTanks() {
		return true;
	}

	@Override
	public boolean canInsertFromSideToTanks() {
		return true;
	}

}
