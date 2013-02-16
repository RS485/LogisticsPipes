package logisticspipes.proxy.cc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCQueued;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.security.PermissionException;
import logisticspipes.ticks.QueuedTasks;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.WorldUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import dan200.computer.api.IComputerAccess;
import dan200.computer.api.IPeripheral;

public class LogisticsTileGenericPipe_CC extends LogisticsTileGenericPipe implements IPeripheral {
	
	private boolean turtleConnect[] = new boolean[7];
	
	private HashMap<IComputerAccess, ForgeDirection> connections = new HashMap<IComputerAccess, ForgeDirection>();
	
	private boolean init = false;
	private HashMap<Integer, String> commandMap = new HashMap<Integer, String>();
	private Map<Integer, Method> commands = new LinkedHashMap<Integer, Method>();
	private String typeName = "";
	
	private IComputerAccess lastPC = null;
	
	private int lastCheckedSide = 0;
	
	private CCType getType(Class<?> clazz) {
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
			Class<?> clazz = pipe.getClass();
			while(true) {
				for(Method method:clazz.getDeclaredMethods()) {
					if(!method.isAnnotationPresent(CCCommand.class)) continue;
					for(Class<?> param:method.getParameterTypes()) {
						if(!param.getName().startsWith("java")) {
							throw new InternalError("Internal Excption (Code: 2)");
						}
					}
					commandMap.put(i, method.getName());
					commands.put(i, method);
					i++;
				}
				if(clazz.getSuperclass() == Object.class) break;
				clazz = clazz.getSuperclass();
			}
		}
	}
	
	private boolean argumentsMatch(Method method, Object[] arguments) {
		int i=0;
		for(Class<?> args:method.getParameterTypes()) {
			if(!arguments[i].getClass().equals(args)) return false;
			i++;
		}
		return true;
	}
	
	@Override
	public boolean isPipeConnected(TileEntity with, ForgeDirection dir) {
		if(SimpleServiceLocator.ccProxy.isTurtle(with) && !turtleConnect[OrientationsUtil.getOrientationOfTilewithTile(this, with).ordinal()]) return false;
		return super.isPipeConnected(with, dir);
	}

	private CoreRoutedPipe getCPipe() {
		if(pipe instanceof CoreRoutedPipe) {
			return (CoreRoutedPipe) pipe;
		}
		return null;
	}
	
	@Override
	public String getType() {
		return typeName;
	}
	
	@Override
	public String[] getMethodNames() {
		init();
		LinkedList<String> list = new LinkedList<String>();
		list.add("help");
		list.add("commandHelp");
		for(int i=0;i<commandMap.size();i++) {
			list.add(commandMap.get(i));
		}
		return list.toArray(new String[list.size()]);
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, int methodId, Object[] arguments) throws Exception {
		if(getCPipe() == null) throw new InternalError("Pipe is not a LogisticsPipe");
		init();
		lastPC = computer;
		if(methodId == 0) {
			StringBuilder help = new StringBuilder();
			StringBuilder head = new StringBuilder();
			StringBuilder head2 = new StringBuilder();
			head.append("PipeType: ");
			head.append(typeName);
			head.append("\n");
			head2.append("Commands: \n");
			for(Integer num:commands.keySet()) {
				Method method = commands.get(num);
				StringBuilder command = new StringBuilder();
				if(help.length() != 0) {
					command.append("\n");
				}
				int number = num.intValue();
				if(number < 10) {
					command.append(" ");
				}
				command.append(number);
				if(method.isAnnotationPresent(CCQueued.class)) {
					command.append(" Q");
				} else {
					command.append("  ");
				}
				command.append(": ");
				command.append(method.getName());
				StringBuilder param = new StringBuilder();
				param.append("(");
				boolean a = false;
				for(Class<?> clazz:method.getParameterTypes()) {
					if(a) {
						param.append(", ");
					}
					param.append(clazz.getSimpleName());
					a = true;
				}
				param.append(")");
				int sub = 0;
				if(param.toString().length() + command.length() > 36) {
					command.append("\n      ---");
					sub = command.length() - 5;
				}
				command.append(param.toString());
				StringBuilder event = new StringBuilder();
				if(method.isAnnotationPresent(CCQueued.class)) {
					CCQueued queued = method.getAnnotation(CCQueued.class);
					if(queued != null && queued.event() != null && !queued.event().equals("")) {
						command.append(": ");
						event.append(queued.event());
					}
				}
				if(event.length() + command.length() - sub > 36) {
					if(sub == 0) {
						command.append("\n      ---");
					} else {
						command.append("\n         ---");
					}
				}
				command.append(event.toString());
				help.append(command.toString());
			}
			String commands = help.toString();
			String[] lines = commands.split("\n");
			if(lines.length > 10) {
				int pageNumber = 1;
				if(arguments.length > 0) {
					if(arguments[0] instanceof Double) {
						pageNumber = (int) Math.floor((Double)arguments[0]);
						if(pageNumber < 1) {
							pageNumber = 1;
						}
					}
				}
				StringBuilder page = new StringBuilder();
				page.append(head.toString());
				page.append("Page ");
				page.append(pageNumber);
				page.append(" of ");
				page.append((int)(Math.floor(lines.length / 10) + (lines.length % 10 == 0 ? 0:1)));
				page.append("\n");
				page.append(head2.toString());
				pageNumber--;
				int from = pageNumber * 11;
				int to = pageNumber * 11 + 10;
				for(int i=from;i<to;i++) {
					if(i < lines.length) {
						page.append(lines[i]);
					}
					if(i < to - 1) {
						page.append("\n");
					}
				}
				return new Object[]{page.toString()};
			}
			return new Object[]{new StringBuilder().append(head).append(head2).append(help).toString()};
		}
		methodId--;
		if(methodId == 0) {
			if(arguments.length != 1) return new Object[]{"Wrong Argument Count"};
			if(!(arguments[0] instanceof Double)) return new Object[]{"Wrong Argument Type"};
			Integer number = (int) Math.floor(((Double)arguments[0]));
			if(!commands.containsKey(number)) return new Object[]{"No command with that index"};
			Method method = commands.get(number);
			StringBuilder help = new StringBuilder();
			help.append("---------------------------------\n");
			help.append("Command: ");
			help.append(method.getName());
			help.append("\n");
			help.append("Parameter: ");
			if(method.getParameterTypes().length > 0) {
				help.append("\n");
				boolean a = false;
				for(Class<?> clazz:method.getParameterTypes()) {
					if(a) {
						help.append(", ");
					}
					help.append(clazz.getSimpleName());
					a = true;
				}
				help.append("\n");
			} else {
				help.append("NONE\n");
			}
			help.append("Return Type: ");
			if(method.isAnnotationPresent(CCQueued.class)) {
				help.append("Event\n");
				help.append("Event Name: ");
				help.append(method.getAnnotation(CCQueued.class).event());
			} else {
				help.append(method.getReturnType().getName());
			}
			help.append("\n");
			help.append("Description: \n");
			help.append(method.getAnnotation(CCCommand.class).description());
			return new Object[]{help.toString()};
		}
		
		methodId--;
		String name = commandMap.get(methodId);
		
		Method match = null;
		
		for(Method method:commands.values()) {
			if(!method.getName().equalsIgnoreCase(name)) continue;
			if(!argumentsMatch(method, arguments)) continue;
			match = method;
			break;
		}
		
		if(match == null) {
			StringBuilder error = new StringBuilder();
			error.append("No such method.");
			boolean handled = false;
			for(Method method:commands.values()) {
				if(!method.getName().equalsIgnoreCase(name)) continue;
				if(handled) {
					error.append("\n");
				}
				handled = true;
				error.append(method.getName());
				error.append("(");
				boolean a = false;
				for(Class<?> clazz:method.getParameterTypes()) {
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
				error.append("Internal Excption (Code: 1, ");
				error.append(name);
				error.append(")");
			}
			throw new UnsupportedOperationException(error.toString());
		}
		
		if(match.getAnnotation(CCQueued.class) != null && match.getAnnotation(CCQueued.class).realQueue()) {
			final Method m = match;
			String prefunction = null;
			if(!(prefunction = match.getAnnotation(CCQueued.class).prefunction()).equals("")) {
				//CoreRoutedPipe pipe = getCPipe();
				if(pipe != null) {
					Class<?> clazz = pipe.getClass();
					while(true) {
						for(Method method:clazz.getDeclaredMethods()) {
							if(method.getName().equals(prefunction)) {
								if(method.getParameterTypes().length > 0) {
									throw new InternalError("Internal Excption (Code: 3)");
								}
								try {
									method.invoke(pipe, new Object[]{});
								} catch(InvocationTargetException e) {
									if(e.getTargetException() instanceof Exception) {
										throw (Exception) e.getTargetException();
									}
									throw e;
								}
								break;
							}
						}
						if(clazz.getSuperclass() == Object.class) break;
						clazz = clazz.getSuperclass();
					}
				}
			}
			final Object[] a = arguments;
			QueuedTasks.queueTask(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					try {
						Object result = m.invoke(pipe, a);
						if(result != null) {
							CCQueued method = m.getAnnotation(CCQueued.class);
							String event = method.event();
							if(event != null && !event.equals("")) {
								queueEvent(event, CCHelper.createArray(CCHelper.getAnswer(result)));
							}
						}
					} catch (InvocationTargetException e) {
						if(e.getTargetException() instanceof PermissionException) {
							CCQueued method = m.getAnnotation(CCQueued.class);
							String event = method.event();
							if(event != null && !event.equals("")) {
								queueEvent(event, new Object[]{"Permission denied"});
							}
						} else {
							throw e;
						}
					}
					return null;
				}
			});
			return null;
		}
		Object result;
		try {
			result = match.invoke(pipe, arguments);
		} catch(InvocationTargetException e) {
			if(e.getTargetException() instanceof Exception) {
				throw (Exception) e.getTargetException();
			}
			throw e;
		}
		return CCHelper.createArray(CCHelper.getAnswer(result));
	}

	@Override
	public void scheduleNeighborChange() {
		super.scheduleNeighborChange();
		boolean connected[] = new boolean[6];
		WorldUtil world = new WorldUtil(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		LinkedList<AdjacentTile> adjacent = world.getAdjacentTileEntities(false);
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
		lastCheckedSide = side;
		return true;
	}

	@Override
	public void attach(IComputerAccess computer) {
		ForgeDirection ori = SimpleServiceLocator.ccProxy.getOrientation(computer, lastCheckedSide, this);
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
