package logisticspipes.proxy.specialtankhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.common.Loader;
import appeng.api.AEApi;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.util.AECableType;
import appeng.api.storage.data.IAEFluidStack;
import logisticspipes.interfaces.ISpecialTankAccessHandler;
import logisticspipes.utils.FluidIdentifier;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import buildcraft.factory.TileTank;

public class AETankHandler implements ISpecialTankAccessHandler {

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean isType(TileEntity tile) {
		return tile instanceof ITileStorageMonitorable;
	}

	@Override
	public List<TileEntity> getBaseTilesFor(TileEntity tile) {
		List<TileEntity> tiles = new ArrayList<TileEntity>(1);
		tiles.add(tile);
		return tiles;
	}

	@SuppressWarnings("unused")
	@Override
	public Map<FluidIdentifier, Long> getAvailableLiquid(TileEntity tile) {
		Map<FluidIdentifier, Long> map = new HashMap<FluidIdentifier, Long>();
		if(tile instanceof ITileStorageMonitorable){
			ITileStorageMonitorable mon = (ITileStorageMonitorable) tile;
			if(mon == null)
				return map;
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS){
				MachineSource source = new MachineSource(new LPActionHost(((IGridHost) tile).getGridNode(dir)));
				IStorageMonitorable monitor = mon.getMonitorable(dir, source);
				if(monitor == null || monitor.getFluidInventory() == null)
					continue;
				IMEMonitor<IAEFluidStack> fluids = monitor.getFluidInventory();
				for(IAEFluidStack stack : fluids.getStorageList()){
					if(isFluidVisible(stack.getFluid()))
						map.put(FluidIdentifier.get(stack.getFluid().getID(), stack.getTagCompound() != null ? stack.getTagCompound().getNBTTagCompoundCopy() : null), stack.getStackSize());
				}
				return map;
			}
		}
		return map;
	}

	@SuppressWarnings("unused")
	@Override
	public FluidStack drainFrom(TileEntity tile, FluidIdentifier ident, Integer amount, boolean drain) {
		if(tile instanceof ITileStorageMonitorable){
			ITileStorageMonitorable mon = (ITileStorageMonitorable) tile;
			if(mon == null)
				return null;
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS){
				MachineSource source = new MachineSource(new LPActionHost(((IGridHost) tile).getGridNode(dir)));
				IStorageMonitorable monitor = mon.getMonitorable(dir, source);
				if(monitor == null || monitor.getFluidInventory() == null)
					continue;
				IMEMonitor<IAEFluidStack> fluids = monitor.getFluidInventory();
				IAEFluidStack s = AEApi.instance().storage().createFluidStack(ident.makeFluidStack(amount));
				IAEFluidStack extracted = fluids.extractItems(s, drain ? Actionable.MODULATE : Actionable.SIMULATE, source);
				if(extracted == null)
					return null;
				return extracted.getFluidStack();
			}
		}
		return null;
	}
	
	private class LPActionHost implements IActionHost {
		public IGridNode node;

		public LPActionHost(IGridNode node) {
			this.node = node;
		}

		@Override
		public void securityBreak() {
		}

		@Override
		public IGridNode getGridNode(ForgeDirection paramForgeDirection) {
			return null;
		}

		@Override
		public AECableType getCableConnectionType(ForgeDirection paramForgeDirection) {
			return null;
		}

		@Override
		public IGridNode getActionableNode() {
			return node;
		}
	}
	
	
	//Disable fluids that don't show in the Fluid Terminal
	private boolean isFluidVisible(Fluid fluid){
		if(fluid == null)
			return true;
		try{
			if(Loader.isModLoaded("extracells")){
				Class clazz = Class.forName("extracells.api.ECApi");
				Object instance = clazz.getMethod("instance").invoke(null);
				Class clazz2 = instance.getClass();
				return (Boolean) clazz2.getDeclaredMethod("canFluidSeeInTerminal", Fluid.class).invoke(instance, fluid);
			}
		}catch(Throwable e){}
		return true;
	}
}
