/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.buildcraft.krapht.logic;

import logisticspipes.mod_LogisticsPipes;
import logisticspipes.buildcraft.krapht.GuiIDs;
import logisticspipes.buildcraft.krapht.network.NetworkConstants;
import logisticspipes.buildcraft.krapht.network.PacketPipeInteger;
import logisticspipes.buildcraft.krapht.proxy.MainProxy;
import logisticspipes.buildcraft.logisticspipes.ExtractionMode;
import logisticspipes.krapht.InventoryUtil;
import logisticspipes.krapht.InventoryUtilFactory;
import logisticspipes.krapht.ItemIdentifier;
import logisticspipes.krapht.SimpleInventory;
import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NBTTagCompound;

public class LogicProvider extends BaseRoutingLogic{

	private SimpleInventory dummyInventory = new SimpleInventory(9, "Items to provide (or empty for all)", 1);
	private boolean _filterIsExclude;
	private ExtractionMode _extractionMode = ExtractionMode.Normal;

	private final InventoryUtilFactory _invUtilFactory;
	private final InventoryUtil _dummyInvUtil;
	
	
	public LogicProvider(){
		this(new InventoryUtilFactory());
	}
	
	public LogicProvider (InventoryUtilFactory invUtilFactory){
		_invUtilFactory = invUtilFactory;
		_dummyInvUtil = _invUtilFactory.getInventoryUtil(dummyInventory);
	}

	@Override
	public void destroy() {}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		if(MainProxy.isServer()) {
			//GuiProxy.openGuiProviderPipe(entityplayer.inventory, dummyInventory, this);
			entityplayer.openGui(mod_LogisticsPipes.instance, GuiIDs.GUI_ProviderPipe_ID, worldObj, xCoord, yCoord, zCoord);
			PacketDispatcher.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_PIPE_MODE_CONTENT, xCoord, yCoord, zCoord, getExtractionMode().ordinal()).getPacket(), entityplayer);
			PacketDispatcher.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_PIPE_INCLUDE_CONTENT, xCoord, yCoord, zCoord, isExcludeFilter() ? 1 : 0).getPacket(), entityplayer);
		}	
	}
	
	/*** GUI ***/
	public SimpleInventory getDummyInventory() {
		return dummyInventory;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		dummyInventory.readFromNBT(nbttagcompound, "");
		_filterIsExclude = nbttagcompound.getBoolean("filterisexclude");
		_extractionMode = ExtractionMode.values()[nbttagcompound.getInteger("extractionMode")];
    }

	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);
    	dummyInventory.writeToNBT(nbttagcompound, "");
    	nbttagcompound.setBoolean("filterisexclude", _filterIsExclude);
    	nbttagcompound.setInteger("extractionMode", _extractionMode.ordinal());
    }
	
	/** INTERFACE TO PIPE **/
	public boolean hasFilter(){
		return _dummyInvUtil.getItemsAndCount().size() > 0;
	}
	
	public boolean itemIsFiltered(ItemIdentifier item){
		return _dummyInvUtil.getItemsAndCount().containsKey(item);
	}
	
	public boolean isExcludeFilter(){
		return _filterIsExclude;
	}
	
	public void setFilterExcluded(boolean isExcluded){
		_filterIsExclude = isExcluded;
	}
	
	public ExtractionMode getExtractionMode(){
		return _extractionMode;
	}

	public void nextExtractionMode() {
		_extractionMode = _extractionMode.next();
	}
}
