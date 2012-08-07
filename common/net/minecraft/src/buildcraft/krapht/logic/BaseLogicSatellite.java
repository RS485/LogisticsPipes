/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.logic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.krapht.IRequireReliableTransport;
import net.minecraft.src.buildcraft.krapht.LogisticsManager;
import net.minecraft.src.buildcraft.krapht.LogisticsRequest;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.krapht.ItemIdentifier;

public class BaseLogicSatellite extends BaseRoutingLogic implements IRequireReliableTransport {

	public static HashSet<BaseLogicSatellite> AllSatellites = new HashSet<BaseLogicSatellite>();

	protected final LinkedList<ItemIdentifier> _lostItems = new LinkedList<ItemIdentifier>();

	@TileNetworkData
	public int satelliteId;

	public BaseLogicSatellite() {
		throttleTime = 40;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		satelliteId = nbttagcompound.getInteger("satelliteid");
		ensureAllSatelliteStatus();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("satelliteid", satelliteId);
		super.writeToNBT(nbttagcompound);
	}

	protected int findId(int increment) {
		int potentialId = satelliteId;
		boolean conflict = true;
		while (conflict) {
			potentialId += increment;
			if (potentialId < 0) {
				return 0;
			}
			conflict = false;
			for (final BaseLogicSatellite sat : AllSatellites) {
				if (sat.satelliteId == potentialId) {
					conflict = true;
					break;
				}
			}
		}
		return potentialId;
	}

	protected void ensureAllSatelliteStatus() {
		if (satelliteId == 0 && AllSatellites.contains(this)) {
			AllSatellites.remove(this);
		}
		if (satelliteId != 0 && !AllSatellites.contains(this)) {
			AllSatellites.add(this);
		}
	}

	public void setNextId() {
		satelliteId = findId(1);
		ensureAllSatelliteStatus();
	}

	public void setPrevId() {
		satelliteId = findId(-1);
		ensureAllSatelliteStatus();
	}

	@Override
	public void destroy() {
		if (AllSatellites.contains(this)) {
			AllSatellites.remove(this);
		}
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
	}

	@Override
	public void throttledUpdateEntity() {
		super.throttledUpdateEntity();
		if (_lostItems.isEmpty()) {
			return;
		}

		final Iterator<ItemIdentifier> iterator = _lostItems.iterator();
		while (iterator.hasNext()) {
			final LogisticsRequest request = new LogisticsRequest(iterator.next(), 1, getRoutedPipe());
			if (LogisticsManager.Request(request, ((RoutedPipe) container.pipe).getRouter().getRoutersByCost(), null)) {
				iterator.remove();
			}
		}
	}

	@Override
	public void itemLost(ItemIdentifier item) {
		_lostItems.add(item);
	}

	@Override
	public void itemArrived(ItemIdentifier item) {
	}
}
