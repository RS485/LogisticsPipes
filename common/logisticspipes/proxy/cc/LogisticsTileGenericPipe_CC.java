package logisticspipes.proxy.cc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.utils.AdjacentTile;
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
	
	private boolean init = false;
	private HashMap<Integer, String> commandMap = new HashMap<Integer, String>();
	private List<Method> commands = new ArrayList<Method>();
	private String typeName = "";
	
	private IComputerAccess lastPC = null;
	
	private CCType getType(Class clazz) {
		while(true) {
			CCType type = (CCType) clazz.getAnnotation(CCType.class);
			if(type != null) return type;
			if(clazz.getSuperclass() == Object.class) return null;
			clazz = clazz.getSuperclass();
		}
	}
	
	private void init() {
		if(!init) {
			init = true;
			CoreRoutedPipe pipe = getCPipe();
			if(pipe == null) return;
			CCType type = getType(pipe.getClass());
			if(type == null) return;
			typeName = type.name();
			int i = 0;
			Class clazz = pipe.getClass();
			while(true) {
				for(Method method:clazz.getDeclaredMethods()) {
					if(!method.isAnnotationPresent(CCCommand.class)) continue;
					commandMap.put(i++, method.getName());
					commands.add(method);
				}
				if(clazz.getSuperclass() == Object.class) break;
				clazz = clazz.getSuperclass();
			}
		}
	}
	
	private boolean argumentsMatch(Method method, Object[] arguments) {
		int i=0;
		for(Class args:method.getParameterTypes()) {
			if(!arguments[i].getClass().equals(args)) return false;
			i++;
		}
		return true;
	}
	
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
		/*
		if(pipe instanceof ISpecialCCPipe) {
			return "LogisticsPipes:" + ((ISpecialCCPipe)pipe).getType();
		}
		return "LogisticsPipes:Normal";
		*/
		return typeName;
	}
	
	@Override
	public String[] getMethodNames() {
		init();
		LinkedList<String> list = new LinkedList<String>();
		/*
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
		*/
		for(int i=0;i<commandMap.size();i++) {
			list.add(commandMap.get(i));
		}
		return list.toArray(new String[list.size()]);
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int methodId, Object[] arguments) throws Exception {
		if(getCPipe() == null) throw new InternalError("Pipe in not a LogisticsPipe");
		init();
		lastPC = computer;
		String name = commandMap.get(methodId);
		
		Method match = null;
		
		for(Method method:commands) {
			if(!method.getName().equalsIgnoreCase(name)) continue;
			if(!argumentsMatch(method, arguments)) continue;
			match = method;
			break;
		}
		
		if(match == null) {
			StringBuilder error = new StringBuilder();
			error.append("No such method.");
			boolean handled = false;
			for(Method method:commands) {
				if(!method.getName().equalsIgnoreCase(name)) continue;
				if(handled) {
					error.append("\n");
				}
				handled = true;
				error.append(method.getName());
				error.append("(");
				boolean a = false;
				for(Class clazz:method.getParameterTypes()) {
					if(a) {
						error.append(", ");
					}
					error.append(clazz.getName());
					a = true;
				}
				error.append(")");
			}
			if(!handled) {
				error = new StringBuilder();
				error.append("Internel Excption (Code: 1, ");
				error.append(name);
				error.append(")");
			}
			throw new UnsupportedOperationException(error.toString());
		}
		
		Object result = match.invoke(pipe, arguments);
		if(!(result instanceof Object[])) {
			if(result == null) return null;
			result = new Object[]{result};
		}
		
		return (Object[]) result;
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
	
	@Override
	public void queueEvent(String event, Object[] arguments) {
		for(IComputerAccess computer: connections.keySet()) {
			computer.queueEvent(event, arguments);
		}
	}
	
	@Override
	public void setTurtrleConnect(boolean flag) {
		turtleConnect[connections.get(lastPC).ordinal()] = flag;
		scheduleNeighborChange();
	}

	@Override
	public boolean getTurtrleConnect() {
		return turtleConnect[connections.get(lastPC).ordinal()];
	}
}
