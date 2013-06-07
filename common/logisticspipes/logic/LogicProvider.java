/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logic;

import logisticspipes.LogisticsPipes;
import logisticspipes.logisticspipes.ExtractionMode;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.oldpackets.PacketPipeInteger;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.Player;

public class LogicProvider extends BaseRoutingLogic{

	private SimpleInventory dummyInventory = new SimpleInventory(9, "Items to provide (or empty for all)", 1);
	private boolean _filterIsExclude;
	private ExtractionMode _extractionMode = ExtractionMode.Normal;

	@Override
	public void destroy() {}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		if(MainProxy.isServer(entityplayer.worldObj)) {
			//GuiProxy.openGuiProviderPipe(entityplayer.inventory, dummyInventory, this);
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_ProviderPipe_ID, worldObj, xCoord, yCoord, zCoord);
			MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_PIPE_MODE_CONTENT, xCoord, yCoord, zCoord, getExtractionMode().ordinal()).getPacket(), (Player)entityplayer);
			MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.PROVIDER_PIPE_INCLUDE_CONTENT, xCoord, yCoord, zCoord, isExcludeFilter() ? 1 : 0).getPacket(), (Player)entityplayer);
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
		_extractionMode = ExtractionMode.getMode(nbttagcompound.getInteger("extractionMode"));
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
		return !dummyInventory.isEmpty();
	}
	
	public boolean itemIsFiltered(ItemIdentifier item){
		return dummyInventory.containsItem(item);
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

	public void setExtractionMode(int id) {
		_extractionMode = ExtractionMode.getMode(id);
	}

	public void nextExtractionMode() {
		_extractionMode = _extractionMode.next();
	}
}
