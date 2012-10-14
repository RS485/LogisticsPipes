package logisticspipes.proxy.cc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cc.interfaces.ISpecialCCPipe;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.WorldUtil;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import buildcraft.api.core.Orientations;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class LogisticsTileGenericPipe_CC extends LogisticsTileGenericPipe implements IPeripheral {
	
	private boolean turtleConnect[] = new boolean[7];
	
	private HashMap<IComputerAccess, Orientations> connections = new HashMap<IComputerAccess, Orientations>();
	
	@Override
	public boolean isPipeConnected(TileEntity with) {
		if(SimpleServiceLocator.ccProxy.isTurtle(with) && !turtleConnect[OrientationsUtil.getOrientationOfTilewithTile(this, with).ordinal()]) return false;
		return super.isPipeConnected(with);
	}

	private CoreRoutedPipe getCPipe() {
		if(pipe instanceof CoreRoutedPipe) {
			return (CoreRoutedPipe) pipe;
		}
		return null;
	}
	
	@Override
	public String getType() {
		if(pipe instanceof ISpecialCCPipe) {
			return "LogisticsPipes:" + ((ISpecialCCPipe)pipe).getType();
		}
		return "LogisticsPipes:Normal";
	}

	@Override
	public String[] getMethodNames() {
		LinkedList<String> list = new LinkedList<String>();
		if(pipe instanceof ISpecialCCPipe) {
			list.addAll(Arrays.asList(((ISpecialCCPipe)pipe).getMethodNames()));
		}
		list.add("getRouterId");
		list.add("setTurtleConnect");
		list.add("getTurtleConnect");
		list.add("getItemID");
		list.add("getItemDamage");
		list.add("getNBTTagCompound");
		list.add("getItemIdentifierIDFor");
		return list.toArray(new String[list.size()]);
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception {
		if(getCPipe() == null) throw new InternalError("Pipe in not a LogisticsPipe");
		if(pipe instanceof ISpecialCCPipe) {
			if(((ISpecialCCPipe)pipe).getMethodNames().length > method) {
				return ((ISpecialCCPipe)pipe).callMethod(method, arguments);
			} else {
				method -= ((ISpecialCCPipe)pipe).getMethodNames().length;
			}
		}
		switch(method) {
		case 0: //getRouterId
			return new Object[]{getCPipe().getRouter().getId().toString()};
		case 1: //setTurtleConnect
			if(arguments.length != 1) throw new Exception("Wrong Arguments");
			if(!(arguments[0] instanceof Boolean)) throw new Exception("Wrong Arguments");
			turtleConnect[connections.get(computer).ordinal()] = ((Boolean)arguments[0]).booleanValue();
			scheduleNeighborChange();
			return null;
		case 2: // getTurtleConnect
			return new Object[]{turtleConnect[connections.get(computer).ordinal()]};
		case 3: // getItemID
			if(arguments.length != 1) throw new Exception("Wrong Argument count");
			if(!(arguments[0] instanceof Double)) throw new Exception("Wrong Arguments");
			ItemIdentifier item = ItemIdentifier.getForId((int)Math.floor((Double)arguments[0]));
			if(item == null) throw new Exception("Invalid ItemIdentifierID");
			return new Object[]{item.itemID};
		case 4: // getItemDamage
			if(arguments.length != 1) throw new Exception("Wrong Argument count");
			if(!(arguments[0] instanceof Double)) throw new Exception("Wrong Arguments");
			ItemIdentifier itemd = ItemIdentifier.getForId((int)Math.floor((Double)arguments[0]));
			if(itemd == null) throw new Exception("Invalid ItemIdentifierID");
			return new Object[]{itemd.itemDamage};
		case 5: // getNBTTagCompound
			if(arguments.length != 1) throw new Exception("Wrong Argument count");
			if(!(arguments[0] instanceof Double)) throw new Exception("Wrong Arguments");
			ItemIdentifier itemn = ItemIdentifier.getForId((int)Math.floor((Double)arguments[0]));
			if(itemn == null) throw new Exception("Invalid ItemIdentifierID");
			return itemn.getNBTTagCompoundAsObject();
		case 6: // getItemIdentifierIDFor
			if(arguments.length != 2) throw new Exception("Wrong Argument count");
			if(!(arguments[0] instanceof Double) || !(arguments[1] instanceof Double)) throw new Exception("Wrong Arguments");
			return new Object[]{ItemIdentifier.get((int)Math.floor((Double)arguments[0]), (int)Math.floor((Double)arguments[1]), null).getId()};
		default:return null;
		}
	}

	@Override
	public void scheduleNeighborChange() {
		super.scheduleNeighborChange();
		boolean connected[] = new boolean[6];
		WorldUtil world = new WorldUtil(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		LinkedList<AdjacentTile> adjacent = world.getAdjacentTileEntities();
		for(AdjacentTile aTile: adjacent) {
			if(SimpleServiceLocator.ccProxy.isTurtle(aTile.tile)) {
				connected[aTile.orientation.ordinal()] = true;
			}
		}
		for(int i=0; i<6;i++) {
			if(!connected[i]) {
				turtleConnect[i] = false;
			}
		}
	}

	@Override
	public boolean canAttachToSide(int side) {
		//All Sides are valid
		return true;
	}

	@Override
	public void attach(IComputerAccess computer, String computerSide) {
		Orientations ori = SimpleServiceLocator.ccProxy.getOrientation(computer, computerSide, this);
		connections.put(computer, ori);
	}

	@Override
	public void detach(IComputerAccess computer) {
		connections.remove(computer);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		for(int i=0;i<turtleConnect.length;i++) {
			nbttagcompound.setBoolean("turtleConnect_" + i, turtleConnect[i]);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		for(int i=0;i<turtleConnect.length;i++) {
			turtleConnect[i] = nbttagcompound.getBoolean("turtleConnect_" + i);
		}
	}
	
	public void queueEvent(String event, Object[] arguments) {
		for(IComputerAccess computer: connections.keySet()) {
			computer.queueEvent(event, arguments);
		}
	}
}
