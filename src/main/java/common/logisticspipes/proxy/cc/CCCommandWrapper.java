package logisticspipes.proxy.cc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCQueued;
import logisticspipes.security.PermissionException;
import logisticspipes.ticks.QueuedTasks;
import dan200.computer.core.ILuaObject;

public class CCCommandWrapper implements ILuaObject {
	
	private CCInfos info;
	private Object object;

	public CCCommandWrapper(CCInfos info2, Object object2) {
		info = info2;
		object = object2;
	}

	@Override
	public String[] getMethodNames() {
		LinkedList<String> list = new LinkedList<String>();
		list.add("help");
		list.add("commandHelp");
		list.add("getType");
		for(int i=0;i<info.commandMap.size();i++) {
			list.add(info.commandMap.get(i));
		}
		return list.toArray(new String[list.size()]);
	}

	@Override
	public Object[] callMethod(int methodId, Object[] arguments) throws Exception {
		if(methodId == 0) {
			StringBuilder help = new StringBuilder();
			StringBuilder head = new StringBuilder();
			StringBuilder head2 = new StringBuilder();
			head.append("Type: ");
			head.append(info.type);
			head.append("\n");
			head2.append("Commands: \n");
			for(Integer num:info.commands.keySet()) {
				Method method = info.commands.get(num);
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
		if(methodId == 0) {
			if(arguments.length != 1) return new Object[]{"Wrong Argument Count"};
			if(!(arguments[0] instanceof Double)) return new Object[]{"Wrong Argument Type"};
			Integer number = (int) Math.floor(((Double)arguments[0]));
			if(!info.commands.containsKey(number)) return new Object[]{"No command with that index"};
			Method method = info.commands.get(number);
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
		if(methodId == 0) {
			return CCHelper.createArray(CCHelper.getAnswer(info.type));
		}
		methodId--;
		String name = info.commandMap.get(methodId);
		
		Method match = null;
		
		for(Method method:info.commands.values()) {
			if(!method.getName().equalsIgnoreCase(name)) continue;
			if(!argumentsMatch(method, arguments)) continue;
			match = method;
			break;
		}
		
		if(match == null) {
			StringBuilder error = new StringBuilder();
			error.append("No such method.");
			boolean handled = false;
			for(Method method:info.commands.values()) {
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
		
		if(match.getAnnotation(CCQueued.class) != null) {
			final Method m = match;
			String prefunction = null;
			if(!(prefunction = match.getAnnotation(CCQueued.class).prefunction()).equals("")) {
				if(object != null) {
					Class<?> clazz = object.getClass();
					while(true) {
						for(Method method:clazz.getDeclaredMethods()) {
							if(method.getName().equals(prefunction)) {
								if(method.getParameterTypes().length > 0) {
									throw new InternalError("Internal Excption (Code: 3)");
								}
								try {
									method.invoke(object, new Object[]{});
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
						Object result = m.invoke(object, a);
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
				new Exception("Took too long (" + m.getName() + "," + object.getClass().getName() + ")").printStackTrace();
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
			result = match.invoke(object, arguments);
		} catch(InvocationTargetException e) {
			if(e.getTargetException() instanceof Exception) {
				throw (Exception) e.getTargetException();
			}
			throw e;
		}
		return CCHelper.createArray(CCHelper.getAnswer(result));
	}
	
	private boolean argumentsMatch(Method method, Object[] arguments) {
		int i=0;
		for(Class<?> args:method.getParameterTypes()) {
			if(!arguments[i].getClass().equals(args)) return false;
			i++;
		}
		return true;
	}
}
