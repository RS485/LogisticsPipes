package logisticspipes.proxy.cc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;

import logisticspipes.LogisticsPipes;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCDirectCall;
import logisticspipes.proxy.cc.interfaces.CCQueued;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.security.PermissionException;
import logisticspipes.ticks.QueuedTasks;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;

public class LPTilePipeWrapper implements ILuaObject {
	
	protected final LogisticsTileGenericPipe pipe;
	
	private boolean init = false;
	private HashMap<Integer, String> commandMap = new HashMap<Integer, String>();
	private Map<Integer, Method> commands = new LinkedHashMap<Integer, Method>();
	protected String typeName = "";
	
	public LPTilePipeWrapper(LogisticsTileGenericPipe pipe) {
		this.pipe = pipe;
	}

	private CCType getType(Class<?> clazz) {
		while(true) {
			CCType type = clazz.getAnnotation(CCType.class);
			if(type != null) return type;
			if(clazz.getSuperclass() == Object.class) return null;
			clazz = clazz.getSuperclass();
		}
	}
	
	private void init() {
		if(!init) {
			init = true;
			CoreRoutedPipe pipe = this.pipe.getCPipe();
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
							throw new InternalError("Internal Excption (Code: 2), [" + param.getName() + "]");
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
		Class<?> args[] = method.getParameterTypes();
		if(arguments.length != args.length) return false;
		for(int i=0; i<arguments.length; i++) {
			if(args[i].equals(Object.class)) continue; //Object can take anything
			if(!arguments[i].getClass().equals(args[i])) return false;
		}
		return true;
	}
	
	public String getType() {
		init();
		return typeName;
	}
	
	@Override
	@ModDependentMethod(modId="ComputerCraft@1.6")
	public String[] getMethodNames() {
		init();
		LinkedList<String> list = new LinkedList<String>();
		list.add("help");
		list.add("commandHelp");
		list.add("getType");
		for(int i=0;i<commandMap.size();i++) {
			list.add(commandMap.get(i));
		}
		return list.toArray(new String[list.size()]);
	}
	
	@Override
	@ModDependentMethod(modId="ComputerCraft@1.6")
	public Object[] callMethod(ILuaContext context, int methodId, Object[] arguments) throws Exception {
		if(this.pipe.getCPipe() == null) throw new InternalError("Pipe is not a LogisticsPipe");
		init();
		
		//help
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
				//if(method.isAnnotationPresent(CCDirectCall.class)) continue;
				StringBuilder command = new StringBuilder();
				if(help.length() != 0) {
					command.append("\n");
				}
				int number = num.intValue();
				if(number < 10) {
					command.append(" ");
				}
				command.append(number);
				command.append(" ");
				if(method.isAnnotationPresent(CCDirectCall.class)) {
					command.append("D");
				} else {
					command.append(" ");
				}
				if(method.isAnnotationPresent(CCQueued.class)) {
					command.append("Q");
				} else {
					command.append(" ");
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
				if(param.toString().length() + command.length() > 36) {
					command.append("\n      ---");
				}
				command.append(param.toString());
				help.append(command.toString());
			}
			String commands = help.toString();
			String[] lines = commands.split("\n");
			if(lines.length > 16) {
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
				int to = pageNumber * 11 + 11;
				for(int i=from;i<to;i++) {
					if(i < lines.length) {
						page.append(lines[i]);
					}
					if(i < to - 1) {
						page.append("\n");
					}
				}
				return new Object[]{page.toString()};
			} else {
				for(int i=0;i<16-lines.length;i++) {
					String buffer = head.toString();
					head = new StringBuilder();
					head.append("\n").append(buffer);
				}
			}
			return new Object[]{new StringBuilder().append(head).append(head2).append(help).toString()};
		}
		methodId--;
		
		//commandHelp
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
			help.append(method.getReturnType().getName());
			help.append("\n");
			help.append("Description: \n");
			help.append(method.getAnnotation(CCCommand.class).description());
			return new Object[]{help.toString()};
		}
		methodId--;
		
		//getType
		if(methodId == 0) {
			return CCHelper.createArray(CCHelper.getAnswer(getType()));
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

		if(match.getAnnotation(CCDirectCall.class) != null) {
			if(this.pipe.currentPC == null) {
				throw new PermissionException();
			}
		}
		
		if(match.getAnnotation(CCCommand.class).needPermission()) {
			this.pipe.getCPipe().checkCCAccess();
		}
		
		if(match.getAnnotation(CCQueued.class) != null) {
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
			final Object[] resultArray = new Object[1];
			final Boolean[] booleans = new Boolean[2];
			booleans[0] = false;
			booleans[1] = false;
			QueuedTasks.queueTask(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					try {
						Object result = m.invoke(pipe, a);
						if(result != null) {
							resultArray[0] = result;
						}
					} catch (InvocationTargetException e) {
						if(e.getTargetException() instanceof PermissionException) {
							booleans[1] = true;
							resultArray[0] = e.getTargetException();
						} else {
							booleans[0] = true;
							throw e;
						}
					}
					booleans[0] = true;
					return null;
				}
			});
			int count = 0;
			while(!booleans[0] && count < 200) {
				Thread.sleep(10);
				count++;
			}
			if(count >= 199) {
				CoreRoutedPipe pipe = this.pipe.getCPipe();
				LogisticsPipes.log.warning("CC call " + m.getName() + " on " + pipe.getClass().getName() + " at (" + this.pipe.xCoord + "," + this.pipe.yCoord + "," + this.pipe.zCoord + ") took too long.");
				throw new Exception("Took too long");
			}
			if(m.getReturnType().equals(Void.class)) {
				return null;
			}
			if(booleans[1]) {
				//PermissionException
				throw ((Exception)resultArray[0]);
			}
			return CCHelper.createArray(CCHelper.getAnswer(resultArray[0]));
		}
		Object result;
		try {
			result = match.invoke(this.pipe.pipe, arguments);
		} catch(InvocationTargetException e) {
			if(e.getTargetException() instanceof Exception) {
				throw (Exception) e.getTargetException();
			}
			throw e;
		}
		return CCHelper.createArray(CCHelper.getAnswer(result));
	}
}
