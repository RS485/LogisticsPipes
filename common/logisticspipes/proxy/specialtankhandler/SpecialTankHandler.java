package logisticspipes.proxy.specialtankhandler;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ISpecialTankHandler;
import net.minecraft.tileentity.TileEntity;

import com.google.common.collect.Lists;

public class SpecialTankHandler {
	private List<ISpecialTankHandler> handlers = new ArrayList<ISpecialTankHandler>();
	
	public void registerHandler(ISpecialTankHandler handler) {
		try {
			if(handler.init()) {
				handlers.add(handler);
				LogisticsPipes.log.info("Loaded SpecialTankHandler: " + handler.getClass().getName());
			} else {
				LogisticsPipes.log.warning("Didn't load SpecialTankHandler: " + handler.getClass().getName());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<TileEntity> getBaseTileFor(TileEntity tile) {
		for(ISpecialTankHandler handler:handlers) {
			if(handler.isType(tile)) {
				return handler.getBaseTilesFor(tile);
			}
		}
		return Lists.newArrayList(tile);
	}
}
