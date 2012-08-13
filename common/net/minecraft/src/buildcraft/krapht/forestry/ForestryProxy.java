package net.minecraft.src.buildcraft.krapht.forestry;

import java.lang.reflect.Field;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.IBee;
import forestry.api.genetics.AlleleManager;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.krapht.ItemIdentifier;

public class ForestryProxy implements IForestryProxy {
	
	public ForestryProxy() {
		try {
			tileMachine = Class.forName("forestry.core.gadgets.TileMachine");
			machine_in_TileMachine = tileMachine.getDeclaredField("machine");
			machine_in_TileMachine.setAccessible(true);
			analyserClass = Class.forName("forestry.apiculture.MachineAnalyzer");
			has_all = true;
		} catch(Exception e) {}
	}
	
	Class<?> tileMachine;
	Field machine_in_TileMachine;
	Class<?> analyserClass;
	boolean has_all;

	@Override
	public boolean isBee(ItemIdentifier item) {
		return isBee(item.makeNormalStack(1));
	}

	@Override
	public boolean isBee(ItemStack item) {
		return BeeManager.beeInterface.isBee(item);
	}

	@Override
	public boolean isAnalysedBee(ItemIdentifier item) {
		return isAnalysedBee(item.makeNormalStack(1));
	}

	@Override
	public boolean isAnalysedBee(ItemStack item) {
		if(!BeeManager.beeInterface.isBee(item)) {
			return false;
		}
		return BeeManager.beeInterface.getBee(item).isAnalyzed();
	}
	
	public int getBeeAlleleCount() {
		return AlleleManager.alleleList.length;
	}
	
	@Override
	public boolean isTileAnalyser(TileEntity tile) {
		if(!has_all) {
			return false;
		}
		try {
			if(tileMachine.isAssignableFrom(tile.getClass())) {
				Object obj = machine_in_TileMachine.get(tile);
				if(analyserClass.isAssignableFrom(obj.getClass())) {
					return true;
				}
			}
		} catch (Exception e) {}
		return false;
	}

	@Override
	public boolean forestryEnabled() {
		return has_all;
	}
}
