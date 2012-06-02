package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.logisticspipes.GuiID;
import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;
import net.minecraft.src.buildcraft.logisticspipes.modules.SinkReply.FixedPriority;
import net.minecraft.src.krapht.InventoryUtil;
import net.minecraft.src.krapht.ItemIdentifier;

public class ModulePolymorphicItemSink implements ILogisticsModule {
	
	private final IInventoryProvider _invProvider;
	
	public ModulePolymorphicItemSink(IInventoryProvider invProvider) {
		_invProvider = invProvider;
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item) {
		IInventory targetInventory = _invProvider.getInventory();
		if (targetInventory == null) return null;
		
		InventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(targetInventory);
		if (!invUtil.containsItem(item)) return null;
		
		SinkReply reply = new SinkReply();
		reply.fixedPriority = FixedPriority.ItemSink;
		reply.isDefault = false;
		reply.isPassive = true;
		//reply.speedBoost = 20F;
		return reply;
		
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {}

	public GuiID getGuiID() {
		return null;
	}
	
	/*@Override
	public boolean displayGui(EntityPlayer entityplayer, GuiScreen previousGui) {return false;}*/

	@Override
	public ILogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void tick() {}
}
