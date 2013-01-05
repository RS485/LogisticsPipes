/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.transport;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import logisticspipes.LogisticsPipes;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.pipes.basic.liquid.LogisitcsLiquidConnectionTransport;
import logisticspipes.pipes.upgrades.UpgradeManager;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.Pair;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.EntityPassiveItem;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import buildcraft.transport.EntityData;
import buildcraft.transport.IItemTravelingHook;
import buildcraft.transport.PipeTransport;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

public class PipeTransportLogistics extends PipeTransportItems {

	private final int _bufferTimeOut = 20 * 2; //2 Seconds
	private RoutedPipe _pipe = null;
	private final HashMap<ItemStack,Pair<Integer /* Time */, Integer /* BufferCounter */>> _itemBuffer = new HashMap<ItemStack, Pair<Integer, Integer>>(); 
	private Method reverseItem = null;
	private Field toRemove = null;
	private Set<Integer> notToRemove = new HashSet<Integer>();
	
	public PipeTransportLogistics() {
		allowBouncing = true;
		try {
			reverseItem = PipeTransportItems.class.getDeclaredMethod("reverseItem", new Class[]{EntityData.class});
			reverseItem.setAccessible(true);
			toRemove = PipeTransportItems.class.getDeclaredField("toRemove");
			toRemove.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		travelHook = new IItemTravelingHook() {
			@SuppressWarnings("unchecked")
			@Override
			public void endReached(PipeTransportItems pipe, EntityData data, TileEntity tile) {
				try {
					Set<Integer> toRemoveList = (Set<Integer>) toRemove.get(PipeTransportLogistics.this);
					toRemoveList.add(data.item.getEntityId());
					handleTileReached(data, tile);
					if(!toRemoveList.contains(data.item.getEntityId())) {
						notToRemove.add(data.item.getEntityId());
						toRemoveList.add(data.item.getEntityId());
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void drop(PipeTransportItems pipe, EntityData data) {
				//Reduce Drop Speed
				data.item.setSpeed(0.0F);
			}

			@Override
			public void centerReached(PipeTransportItems pipe, EntityData data) {
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void performRemoval() {
		try {
			if(!notToRemove.isEmpty()){
				Set<Integer> toRemoveList = (Set<Integer>) toRemove.get(PipeTransportLogistics.this);
				toRemoveList.removeAll(notToRemove);
				notToRemove.clear();
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		super.performRemoval();
	}

	private RoutedPipe getPipe() {
		if (_pipe == null){
			_pipe = (RoutedPipe) container.pipe;
		}
		return _pipe;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if (!_itemBuffer.isEmpty()){
			List<IRoutedItem> toAdd = new LinkedList<IRoutedItem>();
			Iterator<ItemStack> iterator = _itemBuffer.keySet().iterator();
			while (iterator.hasNext()){
				ItemStack next = iterator.next();
				int currentTimeOut = _itemBuffer.get(next).getValue1();
				if (currentTimeOut > 0){
					_itemBuffer.get(next).setValue1(currentTimeOut - 1);
				} else {
					EntityPassiveItem item = new EntityPassiveItem(container.pipe.worldObj, this.xCoord + 0.5F, this.yCoord + Utils.getPipeFloorOf(next) - 0.1, this.zCoord + 0.5, next);
					IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(container.pipe.worldObj, item);
					routedItem.setDoNotBuffer(true);
					routedItem.setBufferCounter(_itemBuffer.get(next).getValue2() + 1);
					toAdd.add(routedItem);
					iterator.remove();
				}
			}
			for(IRoutedItem item:toAdd) {
				this.entityEntering(item.getEntityPassiveItem(), ForgeDirection.UP);
			}
		}
	}
	
	public void dropBuffer(){
		Iterator<ItemStack> iterator = _itemBuffer.keySet().iterator();
		while (iterator.hasNext()){
			ItemStack next = iterator.next();
			SimpleServiceLocator.buildCraftProxy.dropItems(this.container.worldObj, next, this.xCoord, this.yCoord, this.zCoord);
			iterator.remove();
		}
	}
	
	@Override
	public void unscheduleRemoval(IPipedItem item) {
		super.unscheduleRemoval(item);
		if(item instanceof IRoutedItem) {
			IRoutedItem routed = (IRoutedItem)item;
			routed.changeDestination(null);
			EntityData data = travelingEntities.get(item.getEntityId());
			IRoutedItem newRoute = routed.getNewUnRoutedItem();
			data.item = newRoute.getEntityPassiveItem();
			newRoute.setReRoute(true);
			newRoute.addToJamList(getPipe().getRouter());
			newRoute.setBufferCounter(routed.getBufferCounter());
		}
	}
	
	@Override
	public ForgeDirection resolveDestination(EntityData data) {
		
		if(data.item != null && data.item.getItemStack() != null) {
			getPipe().relayedItem(data.item.getItemStack().stackSize);
		}
		
		IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.GetOrCreateRoutedItem(getPipe().worldObj, data);
		ForgeDirection value = getPipe().getRouteLayer().getOrientationForItem(routedItem);
		routedItem.setReRoute(false);
		if (value == null && MainProxy.isClient()) {
			routedItem.getItemStack().stackSize = 0;
			scheduleRemoval(data.item);
			return ForgeDirection.UNKNOWN;
		} else if (value == null) {
			LogisticsPipes.log.severe("THIS IS NOT SUPPOSED TO HAPPEN!");
			return ForgeDirection.UNKNOWN;
		}
		if (value == ForgeDirection.UNKNOWN && !routedItem.getDoNotBuffer() && routedItem.getBufferCounter() < 5) {
			_itemBuffer.put(routedItem.getItemStack().copy(), new Pair<Integer,Integer>(20 * 2, routedItem.getBufferCounter()));
			routedItem.getItemStack().stackSize = 0;	//Hack to make the item disappear
			scheduleRemoval(data.item);
			return ForgeDirection.UNKNOWN;
		}
		
		readjustSpeed(routedItem.getEntityPassiveItem());
		
		return value;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);


		_itemBuffer.clear();
		
        NBTTagList nbttaglist = nbttagcompound.getTagList("buffercontents");
        for(int i = 0; i < nbttaglist.tagCount(); i++) {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
            _itemBuffer.put(ItemStack.loadItemStackFromNBT(nbttagcompound1), new Pair<Integer, Integer>(_bufferTimeOut, 0));
        }
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		NBTTagList nbttaglist = new NBTTagList();
        //ItemStack[] offspring = spawn.toArray(new ItemStack[spawn.size()]);

		for (ItemStack stack : _itemBuffer.keySet()){
			NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            stack.writeToNBT(nbttagcompound1);
            nbttaglist.appendTag(nbttagcompound1);
		}
        nbttagcompound.setTag("buffercontents", nbttaglist);
		
	}
	
	@Override
	public void readjustSpeed(IPipedItem item) {	
		if (SimpleServiceLocator.buildCraftProxy.isRoutedItem(item)){
			
			IRoutedItem routedItem = SimpleServiceLocator.buildCraftProxy.GetRoutedItem(item); 
			float defaultBoost = 1F;
			
			switch (routedItem.getTransportMode()){
			case Default:
				defaultBoost = 10F;
				break;
			case Passive:
				defaultBoost = 20F;
				break;
			case Active:
				defaultBoost = 30F;
				break;
			case Unknown:
				break;
			default:
				break;
			
			}

			float multiplyerSpeed = 1.0F + (0.2F * getPipe().getUpgradeManager().getSpeedUpgradeCount());
			float multiplyerPower = 1.0F + (0.3F * getPipe().getUpgradeManager().getSpeedUpgradeCount());
			
			float add = Math.max(item.getSpeed(), Utils.pipeNormalSpeed * defaultBoost * multiplyerPower) - item.getSpeed();
			if(getPipe().useEnergy(Math.round(add * 25))) {
				item.setSpeed(Math.min(Math.max(item.getSpeed(), Utils.pipeNormalSpeed * defaultBoost * multiplyerSpeed), 1.0F));
			}
		}
	}
	
	//BC copy
	private void handleTileReached(EntityData data, TileEntity tile) {
		if (tile instanceof IPipeEntry)
			((IPipeEntry) tile).entityEntering(data.item, data.output);
		else if (tile instanceof TileGenericPipe && ((TileGenericPipe) tile).pipe.transport instanceof PipeTransportItems) {
			TileGenericPipe pipe = (TileGenericPipe) tile;
			((PipeTransportItems) pipe.pipe.transport).entityEntering(data.item, data.output);
		} else if (tile instanceof IInventory) {
			if (!CoreProxy.proxy.isRenderWorld(worldObj)) {
				//LogisticsPipes start
				UpgradeManager manager = getPipe().getUpgradeManager();
				ForgeDirection insertion = data.output.getOpposite();
				if(manager.hasSneakyUpgrade()) {
					insertion = manager.getSneakyUpgrade().getSneakyOrientation();
					if(insertion == null) {
						insertion = data.output.getOpposite();
					}
				}
				ItemStack added = InventoryHelper.getTransactorFor(tile).add(data.item.getItemStack(), insertion, true);
				
				data.item.getItemStack().stackSize -= added.stackSize;
				
				//For InvSysCon
				if(data.item instanceof IRoutedItem) {
					IRoutedItem routed = (IRoutedItem) data.item;
					IRoutedItem newItem = routed.getCopy();
					newItem.setItemStack(added);
					EntityData addedData = new EntityData(newItem.getEntityPassiveItem(), data.input);
					insertedItemStack(addedData, tile);
				}
				//LogisticsPipes end

				if(data.item.getItemStack().stackSize > 0) {
					reverseItem(data);
				}
			}
		} else {
			if (travelHook != null)
				travelHook.drop(this, data);

			EntityItem dropped = data.item.toEntityItem(data.output);

			if (dropped != null)
				// On SMP, the client side doesn't actually drops
				// items
				onDropped(dropped);
		}
	}
	//BC copy end
	
	protected void reverseItem(EntityData data) {
		if(reverseItem != null) {
			try {
				reverseItem.invoke(this, data);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				reverseItem = null;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				reverseItem = null;
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				reverseItem = null;
			}
		} else {
			throw new UnsupportedOperationException("Failed calling reverseItem(EntityItem);");
		}
	}

	protected void insertedItemStack(EntityData data, TileEntity tile) {}
}
