package logisticspipes.utils.gui;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class DummyModuleContainer extends DummyContainer {
	
	private ItemStack moduleStack;
	private ILogisticsModule module;
	private int slot;
	
	public DummyModuleContainer(EntityPlayer player, int slot) {
		super(player.inventory, null);
		this.slot = slot;
		moduleStack = player.inventory.mainInventory[slot];
		module = LogisticsPipes.ModuleItem.getModuleForItem(moduleStack, null, null, null, null, null);
		module.registerPosition(0, -1, slot, 20);
		ItemModuleInformationManager.readInformation(moduleStack, module);
	}
	
	public ILogisticsModule getModule() {
		return module;
	}

	public void setInventory(IInventory inv) {
		_dummyInventory = inv;
	}
	
	protected Slot addSlotToContainer(Slot par1Slot) {
		if(par1Slot != null && par1Slot.getSlotIndex() == slot && par1Slot.inventory == this._playerInventory) {
			return super.addSlotToContainer(new UnmodifiableSlot(par1Slot));
		}
        return super.addSlotToContainer(par1Slot);
    }

	@Override
	public void onCraftGuiClosed(EntityPlayer par1EntityPlayer) {
		super.onCraftGuiClosed(par1EntityPlayer);
		ItemModuleInformationManager.saveInfotmation(par1EntityPlayer.inventory.mainInventory[slot], module);
		par1EntityPlayer.inventory.onInventoryChanged();
	}
}