package logisticspipes.pipes;

import javax.annotation.Nonnull;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleItemsRequestLogisticsMk2;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

import logisticspipes.LPItems;
import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiIDs;
import logisticspipes.proxy.MainProxy;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;

public class PipeItemsRequestLogisticsMk2 extends PipeItemsRequestLogistics {

	private final ModuleItemsRequestLogisticsMk2 module;

	public PipeItemsRequestLogisticsMk2(Item item) {
		super(item);
		module = new ModuleItemsRequestLogisticsMk2();
		module.registerHandler(this, this);
		module.registerPosition(LogisticsModule.ModulePositionType.IN_PIPE, 0);
	}

	@Override
	public boolean handleClick(EntityPlayer entityplayer, SecuritySettings settings) {
		//allow using upgrade manager
		if (MainProxy.isPipeControllerEquipped(entityplayer) && !(entityplayer.isSneaking())) {
			return false;
		}
		if (MainProxy.isServer(getWorld())) {
			if (settings == null || settings.openGui) {
				openGui(entityplayer);
			} else {
				entityplayer.sendMessage(new TextComponentTranslation("lp.chat.permissiondenied"));
			}
		}
		return true;
	}

	@Override
	public void openGui(EntityPlayer entityplayer) {
		boolean flag = true;
		if (module.disk.isEmpty()) {
			if (!entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).isEmpty() && entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem().equals(LPItems.disk)) {
				module.disk.setValue(entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND));
				entityplayer.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
				flag = false;
			}
		}
		if (flag) {
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Mk2_Orderer_ID, getWorld(), getX(), getY(), getZ());
		}
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_REQUESTERMK2_TEXTURE;
	}

	@Nonnull
	public ItemStack getDisk() {
		return module.disk.getValue();
	}

	@Override
	public void onAllowedRemoval() {
		if (MainProxy.isServer(getWorld())) {
			dropDisk();
		}
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return module;
	}

	public void dropDisk() {
		if (!module.disk.isEmpty()) {
			EntityItem item = new EntityItem(getWorld(), getX(), getY(), getZ(), module.disk.getValue());
			getWorld().spawnEntity(item);
			module.disk.setValue(ItemStack.EMPTY);
		}
	}

	public void setDisk(@Nonnull ItemStack itemstack) {
		module.disk.setValue(itemstack);
	}
}
