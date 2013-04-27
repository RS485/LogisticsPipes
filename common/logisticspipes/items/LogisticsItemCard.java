package logisticspipes.items;

import java.util.List;
import java.util.UUID;

import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsItemCard extends LogisticsItem implements IItemAdvancedExistance {

	public static final int FREQ_CARD = 0;
	public static final int SEC_CARD = 1;
	
	public LogisticsItemCard(int i) {
		super(i);
		this.hasSubtypes = true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean flag) {
		super.addInformation(itemStack, player, list, flag);
		if(!itemStack.hasTagCompound()) {
			list.add("This is no valid Card");
		} else {
			if(itemStack.getTagCompound().hasKey("UUID")) {
				if(itemStack.getItemDamage() == FREQ_CARD) {
					list.add("Freq. Card");
				} else if(itemStack.getItemDamage() == SEC_CARD) {
					list.add("Sec. Card");
				}
				if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
					list.add("Id: " + itemStack.getTagCompound().getString("UUID"));
					if (itemStack.getItemDamage() == SEC_CARD) {
						UUID id = UUID.fromString(itemStack.getTagCompound().getString("UUID"));
						list.add("Authorization: " + (SimpleServiceLocator.securityStationManager.isAuthorized(id)? "Authorized" : "Deauthorized"));
					}
				}
			}
		}
	}

	@Override
	public boolean getShareTag() {
		return true;
	}

	@Override
	public int getItemStackLimit() {
		return 64;
	}

	@Override
	public boolean canExistInNormalInventory(ItemStack stack) {
		return true;
	}

	@Override
	public boolean canExistInWorld(ItemStack stack) {
		if(stack.getItemDamage() == SEC_CARD) {
			return false;
		}
		return true;
	}
}
