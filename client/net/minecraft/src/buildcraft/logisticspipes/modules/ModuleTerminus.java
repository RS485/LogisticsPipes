package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.IInventory;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.logisticspipes.modules.SinkReply.FixedPriority;
import net.minecraft.src.krapht.InventoryUtil;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.SimpleInventory;

public class ModuleTerminus implements ILogisticsModule {

	private final SimpleInventory _filterInventory = new SimpleInventory(9, "Terminated items", 1);

	public IInventory getFilterInventory(){
		return _filterInventory;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		_filterInventory.readFromNBT(nbttagcompound, "");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
    	_filterInventory.writeToNBT(nbttagcompound, "");
    }

	@Override
	public boolean displayGui(EntityPlayer entityplayer, GuiScreen previousGui) {
		ModLoader.getMinecraftInstance().displayGuiScreen(new GuiTerminus(entityplayer.inventory, this, previousGui));
		return true;
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item) {
		InventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(_filterInventory);
		if (invUtil.containsItem(item)){
			SinkReply reply = new SinkReply();
			reply.fixedPriority = FixedPriority.Terminus;
			reply.isPassive = true;
			return reply;
		}

		return null;
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void tick() {}

}
