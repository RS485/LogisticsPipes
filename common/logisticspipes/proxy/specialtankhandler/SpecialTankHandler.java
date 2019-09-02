package logisticspipes.proxy.specialtankhandler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;

import com.google.common.collect.Lists;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ISpecialTankHandler;

public class SpecialTankHandler {

	private List<ISpecialTankHandler> handlers = new ArrayList<>();

	public void registerHandler(ISpecialTankHandler handler) {
		try {
			if (handler.init()) {
				handlers.add(handler);
				LogisticsPipes.log.info("Loaded SpecialTankHandler: " + handler.getClass().getName());
			} else {
				LogisticsPipes.log.warn("Didn't load SpecialTankHandler: " + handler.getClass().getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<TileEntity> getBaseTileFor(TileEntity tile) {
		for (ISpecialTankHandler handler : handlers) {
			if (handler.isType(tile)) {
				return handler.getBaseTilesFor(tile);
			}
		}
		return Lists.newArrayList(tile);
	}

	public boolean hasHandlerFor(TileEntity tile) {
		if (tile == null) {
			return false;
		}
		for (ISpecialTankHandler handler : handlers) {
			if (handler.isType(tile)) {
				return true;
			}
		}
		return false;
	}

	public ISpecialTankHandler getTankHandlerFor(TileEntity tile) {
		for (ISpecialTankHandler handler : handlers) {
			if (handler.isType(tile)) {
				return handler;
			}
		}
		String name = "null";
		if (tile != null) {
			name = tile.getClass().getName();
		}
		throw new RuntimeException("Unknwon TankTileEntity Request, '" + name + "'");
	}
}
