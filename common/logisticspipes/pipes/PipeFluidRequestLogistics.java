package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.FluidIdentifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ChatComponentText;

public class PipeFluidRequestLogistics extends FluidRoutedPipe implements IRequestFluid {

	public PipeFluidRequestLogistics(Item item) {
		super(item);
	}

	public void openGui(EntityPlayer entityplayer) {
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Fluid_Orderer_ID, getWorld(), getX(), getY(), getZ());
	}

	@Override
	public boolean handleClick(EntityPlayer entityplayer, SecuritySettings settings) {
		if (SimpleServiceLocator.toolWrenchHandler.isWrenchEquipped(entityplayer) && SimpleServiceLocator.toolWrenchHandler.canWrench(entityplayer, getX(), getY(), getZ())) {
			if (MainProxy.isServer(getWorld())) {
				if (settings == null || settings.openRequest) {
					openGui(entityplayer);
				} else {
					entityplayer.addChatMessage(new ChatComponentText("Permission denied"));
				}
			}
			SimpleServiceLocator.toolWrenchHandler.wrenchUsed(entityplayer, getX(), getY(), getZ());
			return true;
		}
		return false;
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

	@Override
	public boolean canReceiveFluid() {
		return false;
	}
}
