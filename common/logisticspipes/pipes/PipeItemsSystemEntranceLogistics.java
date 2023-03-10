package logisticspipes.pipes;

import java.util.UUID;

import javax.annotation.Nullable;

import lombok.Getter;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

import logisticspipes.LogisticsPipes;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.modules.ModuleItemsSystemEntranceLogistics;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.EntrencsTransport;

public class PipeItemsSystemEntranceLogistics extends CoreRoutedPipe {
	@Getter
	private final ModuleItemsSystemEntranceLogistics module;

	public PipeItemsSystemEntranceLogistics(Item item) {
		super(new EntrencsTransport(), item);
		((EntrencsTransport) transport).pipe = this;
		module = new ModuleItemsSystemEntranceLogistics();
		module.registerHandler(this, this);
		module.registerPosition(LogisticsModule.ModulePositionType.IN_PIPE, 0);
	}

	public UUID getLocalFreqUUID() {
		if (module.inv.getStackInSlot(0) == null) {
			return null;
		}
		if (!module.inv.getStackInSlot(0).hasTagCompound()) {
			return null;
		}
		if (!module.inv.getStackInSlot(0).getTagCompound().hasKey("UUID")) {
			return null;
		}
		spawnParticle(Particles.WhiteParticle, 2);
		return UUID.fromString(module.inv.getStackInSlot(0).getTagCompound().getString("UUID"));
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_ENTRANCE_TEXTURE;
	}

	@Override
	public @Nullable LogisticsModule getLogisticsModule() {
		return module;
	}

	@Override
	public void onAllowedRemoval() {
		dropFreqCard();
	}

	private void dropFreqCard() {
		if (module.inv.getStackInSlot(0) == null) {
			return;
		}
		EntityItem item = new EntityItem(getWorld(), getX(), getY(), getZ(), module.inv.getStackInSlot(0));
		getWorld().spawnEntity(item);
		module.inv.clearInventorySlotContents(0);
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Freq_Card_ID, getWorld(), getX(), getY(), getZ());
	}
}
