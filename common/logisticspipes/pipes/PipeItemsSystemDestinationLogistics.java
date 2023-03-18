package logisticspipes.pipes;

import java.util.Objects;
import java.util.UUID;

import lombok.Getter;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import logisticspipes.LogisticsPipes;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleItemsSystemDestinationLogistics;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.item.ItemIdentifierStack;

public class PipeItemsSystemDestinationLogistics extends CoreRoutedPipe {
	@Getter
	private final ModuleItemsSystemDestinationLogistics module;

	public PipeItemsSystemDestinationLogistics(Item item) {
		super(item);
		module = new ModuleItemsSystemDestinationLogistics();
		module.registerHandler(this, this);
		module.registerPosition(LogisticsModule.ModulePositionType.IN_PIPE, 0);
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_DESTINATION_TEXTURE;
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return module;
	}

	public Object getTargetUUID() {
		final ItemIdentifierStack itemident = module.inv.getIDStackInSlot(0);
		if (itemident == null) {
			return null;
		}
		final ItemStack stack = itemident.makeNormalStack();
		if (!stack.hasTagCompound()) {
			return null;
		}
		if (!Objects.requireNonNull(stack.getTagCompound()).hasKey("UUID")) {
			return null;
		}
		spawnParticle(Particles.WhiteParticle, 2);
		return UUID.fromString(stack.getTagCompound().getString("UUID"));
	}

	@Override
	public void onAllowedRemoval() {
		dropFreqCard();
	}

	private void dropFreqCard() {
		final ItemIdentifierStack itemident = module.inv.getIDStackInSlot(0);
		if (itemident == null) {
			return;
		}
		EntityItem item = new EntityItem(getWorld(), getX(), getY(), getZ(), itemident.makeNormalStack());
		getWorld().spawnEntity(item);
		module.inv.clearInventorySlotContents(0);
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Freq_Card_ID, getWorld(), getX(), getY(), getZ());
	}
}
