package logisticspipes.utils.gui;

import logisticspipes.LogisticsPipes;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.LogisticsModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class DummyModuleContainer extends DummyContainer {
	
	private ItemStack moduleStack;
	private LogisticsModule module;
	private int slot;
	
	public DummyModuleContainer(EntityPlayer player, int slot) {
		super(player.inventory, null);
		this.slot = slot;
		moduleStack = player.inventory.mainInventory[slot];
		module = LogisticsPipes.ModuleItem.getModuleForItem(moduleStack, null, null, null, null, null);
		module.registerSlot(-1-slot);
		ItemModuleInformationManager.readInformation(moduleStack, module);
	}
	
	public LogisticsModule getModule() {
		return module;
	}

	public void setInventory(IInventory inv) {
		_dummyInventory = inv;
	}
	
	@Override
	protected Slot addSlotToContainer(Slot par1Slot) {
		if(par1Slot != null && par1Slot.getSlotIndex() == slot && par1Slot.inventory == this._playerInventory) {
			return super.addSlotToContainer(new UnmodifiableSlot(par1Slot));
		}
        return super.addSlotToContainer(par1Slot);
    }

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		super.onContainerClosed(par1EntityPlayer);
		ItemModuleInformationManager.saveInfotmation(par1EntityPlayer.inventory.mainInventory[slot], module);
		par1EntityPlayer.inventory.onInventoryChanged();
	}
}