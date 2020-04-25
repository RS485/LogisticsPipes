package logisticspipes.asm.wrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LogWrapper;

import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import logisticspipes.LogisticsPipes;
import logisticspipes.asm.IgnoreDisabledProxy;
import logisticspipes.proxy.DontLoadProxy;
import logisticspipes.proxy.VersionNotSupportedException;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.proxy.interfaces.IGenericProgressProvider;
import logisticspipes.proxy.interfaces.ILPPipeConfigToolWrapper;
import logisticspipes.utils.ModStatusHelper;

public class LogisticsWrapperHandler {

	private static final boolean DUMP = false;

	private static Map<String, Class<?>> lookupMap = new HashMap<>();
	public static List<AbstractWrapper> wrapperController = new ArrayList<>();

	private static Method m_defineClass = null;

	private LogisticsWrapperHandler() {}

	public static ILPPipeConfigToolWrapper getWrappedPipeConfigToolWrapper(String clazz, String name, Class<? extends ILPPipeConfigToolWrapper> providerClass) {
		ILPPipeConfigToolWrapper wrapper = null;
		Throwable e = null;
		try {
			Class.forName(clazz);
			try {
				wrapper = providerClass.newInstance();
			} catch (Exception e1) {
				if (e1 instanceof VersionNotSupportedException) {
					throw (VersionNotSupportedException) e1;
				}
				e1.printStackTrace();
				e = e1;
			} catch (NoClassDefFoundError e1) {
				e1.printStackTrace();
				e = e1;
			}
		} catch (NoClassDefFoundError | ClassNotFoundException ignored) {}
		GenericLPPipeConfigToolWrapper instance = new GenericLPPipeConfigToolWrapper(wrapper, name);
		if (wrapper != null) {
			LogisticsPipes.log.info("Loaded " + name + " PipeConfigToolWrapper");
		} else {
			if (e != null) {
				instance.setState(WrapperState.Exception);
				instance.setReason(e);
				LogisticsPipes.log.info("Couldn't load " + name + " PipeConfigToolWrapper");
			} else {
				LogisticsPipes.log.info("Didn't load " + name + " PipeConfigToolWrapper");
				instance.setState(WrapperState.ModMissing);
			}
		}
		LogisticsWrapperHandler.wrapperController.add(instance);
		return instance;
	}

	public static IGenericProgressProvider getWrappedProgressProvider(String modId, String name, Class<? extends IGenericProgressProvider> providerClass) {
		IGenericProgressProvider provider = null;
		Throwable e = null;
		if (ModStatusHelper.isModLoaded(modId)) {
			try {
				provider = providerClass.newInstance();
			} catch (Exception e1) {
				if (e1 instanceof VersionNotSupportedException) {
					throw (VersionNotSupportedException) e1;
				}
				e1.printStackTrace();
				e = e1;
			} catch (NoClassDefFoundError e1) {
				e1.printStackTrace();
				e = e1;
			}
		}
		GenericProgressProviderWrapper instance = new GenericProgressProviderWrapper(provider, modId + ": " + name);
		if (provider != null) {
			LogisticsPipes.log.info("Loaded " + modId + ", " + name + " ProgressProvider");
		} else {
			if (e != null) {
				instance.setState(WrapperState.Exception);
				instance.setReason(e);
				LogisticsPipes.log.info("Couldn't load " + modId + ", " + name + " ProgressProvider");
			} else {
				LogisticsPipes.log.info("Didn't load " + modId + ", " + name + " ProgressProvider");
				instance.setState(WrapperState.ModMissing);
			}
		}
		instance.setModId(modId);
		LogisticsWrapperHandler.wrapperController.add(instance);
		return instance;
	}

	public static ICraftingRecipeProvider getWrappedRecipeProvider(String modId, String name, Class<? extends ICraftingRecipeProvider> providerClass) {
		ICraftingRecipeProvider provider = null;
		Throwable e = null;
		if (ModStatusHelper.isModLoaded(modId)) {
			try {
				provider = providerClass.newInstance();
			} catch (Exception e1) {
				if (e1 instanceof VersionNotSupportedException) {
					throw (VersionNotSupportedException) e1;
				}
				e1.printStackTrace();
				e = e1;
			} catch (NoClassDefFoundError e1) {
				e1.printStackTrace();
				e = e1;
			}
		}
		CraftingRecipeProviderWrapper instance = new CraftingRecipeProviderWrapper(provider, name);
		if (provider != null) {
			LogisticsPipes.log.info("Loaded " + name + " RecipeProvider");
		} else {
			if (e != null) {
				instance.setState(WrapperState.Exception);
				instance.setReason(e);
				LogisticsPipes.log.info("Couldn't load " + name + " RecipeProvider");
			} else {
				LogisticsPipes.log.info("Didn't load " + name + " RecipeProvider");
				instance.setState(WrapperState.ModMissing);
			}
		}
		instance.setModId(modId);
		LogisticsWrapperHandler.wrapperController.add(instance);
		return instance;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getWrappedProxy(String modId, Class<T> interfaze, Class<? extends T> proxyClazz, T dummyProxy, Class<?>... wrapperInterfaces) throws SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException,
			InvocationTargetException, NoSuchMethodException {
		String proxyName = interfaze.getSimpleName().substring(1);
		if (!proxyName.endsWith("Proxy")) {
			throw new RuntimeException("UnuportedProxyName: " + proxyName);
		}
		proxyName = proxyName.substring(0, proxyName.length() - 5);
		String className = "logisticspipes/asm/wrapper/generated/" + proxyName + "ProxyWrapper";
		boolean ignoreModLoaded = false;
		if (modId.startsWith("!")) {
			ignoreModLoaded = true;
			modId = modId.substring(1);
		}
		List<Class<?>> wrapperInterfacesList = Arrays.asList(wrapperInterfaces);
		Class<?> clazz;
		synchronized (lookupMap) {
			clazz = LogisticsWrapperHandler.lookupMap.get(className);
			if (clazz == null) {
				String fieldName = interfaze.getName().replace('.', '/');
				//String classFile = interfaze.getSimpleName().substring(1) + "Wrapper.java";
				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

				cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null, "logisticspipes/asm/wrapper/AbstractWrapper", new String[] { fieldName });

				cw.visitSource(".LP|ASM.dynamic", null);

				{
					FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, "proxy", "L" + fieldName + ";", null, null);
					fv.visitEnd();
				}
				{
					FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "dummyProxy", "L" + fieldName + ";", null, null);
					fv.visitEnd();
				}
				{
					MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(L" + fieldName + ";L" + fieldName + ";)V", null, null);
					mv.visitCode();
					Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitLineNumber(11, l0);
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "logisticspipes/asm/wrapper/AbstractWrapper", "<init>", "()V");
					Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitLineNumber(12, l1);
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitVarInsn(Opcodes.ALOAD, 1);
					mv.visitFieldInsn(Opcodes.PUTFIELD, className, "dummyProxy", "L" + fieldName + ";");
					Label l2 = new Label();
					mv.visitLabel(l2);
					mv.visitLineNumber(13, l2);
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitVarInsn(Opcodes.ALOAD, 2);
					mv.visitFieldInsn(Opcodes.PUTFIELD, className, "proxy", "L" + fieldName + ";");
					Label l3 = new Label();
					mv.visitLabel(l3);
					mv.visitLineNumber(14, l3);
					mv.visitInsn(Opcodes.RETURN);
					Label l4 = new Label();
					mv.visitLabel(l4);
					mv.visitLocalVariable("this", "L" + className + ";", null, l0, l4, 0);
					mv.visitLocalVariable("dProxy", "L" + fieldName + ";", null, l0, l4, 1);
					mv.visitLocalVariable("iProxy", "L" + fieldName + ";", null, l0, l4, 2);
					mv.visitMaxs(2, 3);
					mv.visitEnd();
				}
				int lineAddition = 100;
				for (Method method : interfaze.getMethods()) {
					LogisticsWrapperHandler.addProxyMethod(cw, method, fieldName, className, lineAddition, !wrapperInterfacesList.contains(method.getReturnType()));
					lineAddition += 10;
				}
				LogisticsWrapperHandler.addGetName(cw, className, proxyName);
				LogisticsWrapperHandler.addGetTypeName(cw, className, "Proxy");
				cw.visitEnd();

				String lookfor = className.replace('/', '.');

				byte[] bytes = cw.toByteArray();

				if (LogisticsPipes.isDEBUG()) {
					if (LogisticsWrapperHandler.DUMP) {
						LogisticsWrapperHandler.saveGeneratedClass(bytes, lookfor, "LP_WRAPPER_CLASSES");
					}
					ClassReader cr = new ClassReader(bytes);
					org.objectweb.asm.util.CheckClassAdapter.verify(cr, Launch.classLoader, false, new PrintWriter(System.err));
				}

				try {
					clazz = LogisticsWrapperHandler.loadClass(bytes, lookfor);
				} catch (LinkageError e) {
					try {
						if (e.getMessage().contains("attempted") && e.getMessage().contains("duplicate class definition")) {
							Class<?> prev = Class.forName(className);
							System.err.println(e.getMessage());
							System.err.printf("Already loaded: %s%n", prev);
							String resourcePath = className.replace('.', '/').concat(".class");
							URL classResource = Launch.classLoader.findResource(resourcePath);
							if (classResource != null) {
								String path = classResource.getPath();
								System.err.println("Class source: " + path);
							} else {
								System.err.println("Class source: Null");
							}
						}
					} catch (Exception e2) {
						e2.printStackTrace();
					}
					throw e;
				}
				LogisticsWrapperHandler.lookupMap.put(className, clazz);
			}
		}

		T proxy = null;
		Throwable e = null;
		if ((ModStatusHelper.areModsLoaded(modId) || ignoreModLoaded) && proxyClazz != null) {
			try {
				proxy = proxyClazz.newInstance();
			} catch (Exception e1) {
				if (e1 instanceof VersionNotSupportedException) {
					throw (VersionNotSupportedException) e1;
				}
				if (!(e1 instanceof DontLoadProxy)) {
					e1.printStackTrace();
					e = e1;
				}
			} catch (NoClassDefFoundError e1) {
				if (!ignoreModLoaded) {
					e1.printStackTrace();
					e = e1;
				}
			}
		}
		T instance = (T) clazz.getConstructor(new Class<?>[] { interfaze, interfaze }).newInstance(dummyProxy, proxy);
		if (proxy != null) {
			LogisticsPipes.log.info("Loaded " + proxyName + "Proxy");
		} else {
			LogisticsPipes.log.info("Loaded " + proxyName + " DummyProxy");
			if (e != null) {
				((AbstractWrapper) instance).setState(WrapperState.Exception);
				((AbstractWrapper) instance).setReason(e);
			} else {
				((AbstractWrapper) instance).setState(WrapperState.ModMissing);
			}
		}
		((AbstractWrapper) instance).setModId(modId);
		((AbstractWrapper) instance).setWrapperInterfaces(Collections.unmodifiableList(wrapperInterfacesList));
		LogisticsWrapperHandler.wrapperController.add((AbstractWrapper) instance);
		return instance;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getWrappedSubProxy(AbstractWrapper wrapper, Class<T> interfaze, T proxy, T dummyProxy) throws SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		if (proxy == null) {
			return null;
		}
		String proxyName = interfaze.getSimpleName().substring(1);
		String className = "logisticspipes/asm/wrapper/generated/" + proxyName + "ProxyWrapper";

		Class<?> clazz;
		synchronized (lookupMap) {
			clazz = LogisticsWrapperHandler.lookupMap.get(className);
			if (clazz == null) {
				String fieldName = interfaze.getName().replace('.', '/');
				//String classFile = interfaze.getSimpleName().substring(1) + "Wrapper.java";
				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

				cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, className, null, "logisticspipes/asm/wrapper/AbstractSubWrapper", new String[] { fieldName });

				cw.visitSource(".LP|ASM.dynamic", null);

				{
					FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, "proxy", "L" + fieldName + ";", null, null);
					fv.visitEnd();
				}
				{
					FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "dummyProxy", "L" + fieldName + ";", null, null);
					fv.visitEnd();
				}
				{
					MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Llogisticspipes/asm/wrapper/AbstractWrapper;L" + fieldName + ";L" + fieldName + ";)V", null, null);
					mv.visitCode();
					Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitLineNumber(11, l0);
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitVarInsn(Opcodes.ALOAD, 1);
					mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "logisticspipes/asm/wrapper/AbstractSubWrapper", "<init>", "(Llogisticspipes/asm/wrapper/AbstractWrapper;)V");
					Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitLineNumber(12, l1);
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitVarInsn(Opcodes.ALOAD, 2);
					mv.visitFieldInsn(Opcodes.PUTFIELD, className, "dummyProxy", "L" + fieldName + ";");
					Label l2 = new Label();
					mv.visitLabel(l2);
					mv.visitLineNumber(13, l2);
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitVarInsn(Opcodes.ALOAD, 3);
					mv.visitFieldInsn(Opcodes.PUTFIELD, className, "proxy", "L" + fieldName + ";");
					Label l3 = new Label();
					mv.visitLabel(l3);
					mv.visitLineNumber(14, l3);
					mv.visitInsn(Opcodes.RETURN);
					Label l4 = new Label();
					mv.visitLabel(l4);
					mv.visitLocalVariable("this", "L" + className + ";", null, l0, l4, 0);
					mv.visitLocalVariable("wrapper", "Llogisticspipes/asm/wrapper/AbstractWrapper;", null, l0, l4, 1);
					mv.visitLocalVariable("dProxy", "L" + fieldName + ";", null, l0, l4, 2);
					mv.visitLocalVariable("iProxy", "L" + fieldName + ";", null, l0, l4, 3);
					mv.visitMaxs(2, 3);
					mv.visitEnd();
				}
				int lineAddition = 100;
				for (Method method : interfaze.getMethods()) {
					LogisticsWrapperHandler.addProxyMethod(cw, method, fieldName, className, lineAddition, !wrapper.getWrapperInterfaces().contains(method.getReturnType()));
					lineAddition += 10;
				}
				cw.visitEnd();

				String lookfor = className.replace('/', '.');

				byte[] bytes = cw.toByteArray();

				if (LogisticsPipes.isDEBUG()) {
					if (LogisticsWrapperHandler.DUMP) {
						LogisticsWrapperHandler.saveGeneratedClass(bytes, lookfor, "LP_WRAPPER_CLASSES");
					}
					ClassReader cr = new ClassReader(bytes);
					org.objectweb.asm.util.CheckClassAdapter.verify(cr, Launch.classLoader, false, new PrintWriter(System.err));
				}

				try {
					clazz = LogisticsWrapperHandler.loadClass(bytes, lookfor);
				} catch (LinkageError e) {
					try {
						if (e.getMessage().contains("attempted") && e.getMessage().contains("duplicate class definition")) {
							Class<?> prev = Class.forName(className);
							System.err.println(e.getMessage());
							System.err.printf("Already loaded: %s%n", prev);
							String resourcePath = className.replace('.', '/').concat(".class");
							URL classResource = Launch.classLoader.findResource(resourcePath);
							if (classResource != null) {
								String path = classResource.getPath();
								System.err.println("Class source: " + path);
							} else {
								System.err.println("Class source: Null");
							}
						}
					} catch (Exception e2) {
						e2.printStackTrace();
					}
					throw e;
				}
				LogisticsWrapperHandler.lookupMap.put(className, clazz);
			}
		}

		T instance = (T) clazz.getConstructor(new Class<?>[] { AbstractWrapper.class, interfaze, interfaze }).newInstance(wrapper, dummyProxy, proxy);
		((AbstractWrapper) instance).setWrapperInterfaces(wrapper.getWrapperInterfaces());
		return instance;
	}

	private static Class<?> loadClass(byte[] data, String lookfor) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (LogisticsWrapperHandler.m_defineClass == null) {
			LogisticsWrapperHandler.m_defineClass = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
			LogisticsWrapperHandler.m_defineClass.setAccessible(true);
		}
		return (Class<?>) LogisticsWrapperHandler.m_defineClass.invoke(Launch.classLoader, data, 0, data.length);
	}

	private static void addGetTypeName(ClassWriter cw, String className, String type) {
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getTypeName", "()Ljava/lang/String;", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(31, l0);
		mv.visitLdcInsn(type);
		mv.visitInsn(Opcodes.ARETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", "L" + className + ";", null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private static void addGetName(ClassWriter cw, String className, String proxyName) {
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getName", "()Ljava/lang/String;", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(41, l0);
		mv.visitLdcInsn(proxyName);
		mv.visitInsn(Opcodes.ARETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", "L" + className + ";", null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private static void addProxyMethod(ClassWriter cw, Method method, String fieldName, String className, int lineAddition, boolean normalResult) {
		Class<?> retclazz = method.getReturnType();
		int eIndex = 1;
		StringBuilder desc = new StringBuilder("(");
		for (Class<?> clazz : method.getParameterTypes()) {
			desc.append(LogisticsWrapperHandler.getClassSignature(clazz));
			if (clazz == long.class || clazz == double.class) {
				eIndex += 2;
			} else {
				eIndex += 1;
			}
		}
		eIndex++;
		desc.append(")");
		String resultClassL = null;
		String resultClass = null;
		int returnType;
		if (retclazz == null || retclazz == void.class) {
			desc.append("V");
			returnType = Opcodes.RETURN;
		} else if (retclazz.isPrimitive()) {
			desc.append(LogisticsWrapperHandler.getPrimitiveMapping(retclazz));
			returnType = LogisticsWrapperHandler.getPrimitiveReturnMapping(retclazz);
		} else if (retclazz.isArray()) {
			// Arrays in bytecode are always typeless, like primitives
			// e.g. Object[] = [Ljava/lang/Object; (not L[Ljava/lang/Object;;)
			resultClassL = retclazz.getName().replace('.', '/');
			resultClass = resultClassL;
			desc.append(resultClassL);
			returnType = Opcodes.ARETURN;
		} else {
			resultClassL = "L" + retclazz.getName().replace('.', '/') + ";";
			resultClass = retclazz.getName().replace('.', '/');
			desc.append(resultClassL);
			returnType = Opcodes.ARETURN;
		}
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, method.getName(), desc.toString(), null, null);
		mv.visitCode();
		Label l0 = new Label();
		Label l1 = new Label();
		Label l2 = new Label();
		mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");
		Label l3 = new Label();
		mv.visitTryCatchBlock(l0, l1, l3, "java/lang/NoClassDefFoundError");
		Label l4 = new Label();
		mv.visitLabel(l4);
		mv.visitLineNumber(lineAddition + 1, l4);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		if (method.isAnnotationPresent(IgnoreDisabledProxy.class) || !normalResult) {
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, "canTryAnyway", "()Z");
		} else {
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, "isEnabled", "()Z");
		}
		Label l5 = new Label();
		mv.visitJumpInsn(Opcodes.IFEQ, l5);
		mv.visitLabel(l0);
		mv.visitLineNumber(lineAddition + 2, l0);
		if (normalResult) {
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, className, "proxy", "L" + fieldName + ";");
			LogisticsWrapperHandler.addMethodParameterLoad(mv, method);
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, fieldName, method.getName(), desc.toString());
			mv.visitLabel(l1);
			mv.visitInsn(returnType);
		} else {
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitLdcInsn(Type.getType(resultClassL));
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, className, "proxy", "L" + fieldName + ";");
			LogisticsWrapperHandler.addMethodParameterLoad(mv, method);
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, fieldName, method.getName(), desc.toString());
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, className, "dummyProxy", "L" + fieldName + ";");
			LogisticsWrapperHandler.addMethodParameterLoad(mv, method);
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, fieldName, method.getName(), desc.toString());
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/wrapper/LogisticsWrapperHandler", "getWrappedSubProxy", "(Llogisticspipes/asm/wrapper/AbstractWrapper;Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
			mv.visitTypeInsn(Opcodes.CHECKCAST, resultClass);
			mv.visitLabel(l1);
			mv.visitInsn(Opcodes.ARETURN);
		}
		mv.visitLabel(l2);
		mv.visitLineNumber(lineAddition + 3, l2);
		mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/Exception" });
		mv.visitVarInsn(Opcodes.ASTORE, eIndex);
		Label l6 = new Label();
		mv.visitLabel(l6);
		mv.visitLineNumber(lineAddition + 4, l6);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, eIndex);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, "handleException", "(Ljava/lang/Throwable;)V");
		Label l7 = new Label();
		mv.visitLabel(l7);
		mv.visitJumpInsn(Opcodes.GOTO, l5);
		mv.visitLabel(l3);
		mv.visitLineNumber(lineAddition + 5, l3);
		mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/NoClassDefFoundError" });
		mv.visitVarInsn(Opcodes.ASTORE, eIndex);
		Label l8 = new Label();
		mv.visitLabel(l8);
		mv.visitLineNumber(lineAddition + 6, l8);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, eIndex);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, "handleException", "(Ljava/lang/Throwable;)V");
		mv.visitLabel(l5);
		mv.visitLineNumber(lineAddition + 7, l5);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitFieldInsn(Opcodes.GETFIELD, className, "dummyProxy", "L" + fieldName + ";");
		LogisticsWrapperHandler.addMethodParameterLoad(mv, method);
		mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, fieldName, method.getName(), desc.toString());
		mv.visitInsn(returnType);
		Label l9 = new Label();
		mv.visitLabel(l9);
		mv.visitLocalVariable("this", "L" + className + ";", null, l4, l9, 0);
		LogisticsWrapperHandler.addParameterVars(mv, method, l4, l9);
		mv.visitLocalVariable("e", "Ljava/lang/Exception;", null, l6, l7, eIndex);
		mv.visitLocalVariable("e", "Ljava/lang/NoClassDefFoundError;", null, l8, l5, eIndex);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private static void addMethodParameterLoad(MethodVisitor mv, Method method) {
		int i = 1;
		for (Class<?> clazz : method.getParameterTypes()) {
			if (clazz.isPrimitive()) {
				if (clazz == int.class || clazz == boolean.class || clazz == short.class || clazz == byte.class) {
					mv.visitVarInsn(Opcodes.ILOAD, i);
					i++;
				} else if (clazz == long.class) {
					mv.visitVarInsn(Opcodes.LLOAD, i);
					i += 2;
				} else if (clazz == float.class) {
					mv.visitVarInsn(Opcodes.FLOAD, i);
					i++;
				} else if (clazz == double.class) {
					mv.visitVarInsn(Opcodes.DLOAD, i);
					i += 2;
				} else {
					throw new UnsupportedOperationException("Unmapped clazz: " + clazz.getName());
				}
			} else {
				mv.visitVarInsn(Opcodes.ALOAD, i);
				i++;
			}
		}
	}

	private static void addParameterVars(MethodVisitor mv, Method method, Label l3, Label l8) {
		int i = 1;
		for (Class<?> clazz : method.getParameterTypes()) {
			mv.visitLocalVariable("par" + i, LogisticsWrapperHandler.getClassSignature(clazz), null, l3, l8, i);
			if (clazz == long.class || clazz == double.class) {
				i++;
			}
			i++;
		}
	}

	private static String getPrimitiveMapping(Class<?> clazz) {
		if (clazz == int.class) {
			return "I";
		} else if (clazz == long.class) {
			return "J";
		} else if (clazz == float.class) {
			return "F";
		} else if (clazz == double.class) {
			return "D";
		} else if (clazz == boolean.class) {
			return "Z";
		} else if (clazz == short.class) {
			return "S";
		} else if (clazz == byte.class) {
			return "B";
		} else {
			throw new UnsupportedOperationException("Unmapped clazz: " + clazz.getName());
		}
	}

	private static int getPrimitiveReturnMapping(Class<?> clazz) {
		if (clazz == int.class || clazz == boolean.class || clazz == short.class || clazz == byte.class) {
			return Opcodes.IRETURN;
		} else if (clazz == long.class) {
			return Opcodes.LRETURN;
		} else if (clazz == float.class) {
			return Opcodes.FRETURN;
		} else if (clazz == double.class) {
			return Opcodes.DRETURN;
		} else {
			throw new UnsupportedOperationException("Unmapped clazz: " + clazz.getName());
		}
	}

	private static String getClassSignature(Class<?> clazz) {
		if (clazz.isPrimitive()) {
			return LogisticsWrapperHandler.getPrimitiveMapping(clazz);
		} else {
			if (clazz.isArray()) {
				return clazz.getName().replace('.', '/');
			} else {
				return "L" + clazz.getName().replace('.', '/') + ";";
			}
		}
	}

	private static File tempFolder = null;

	public static void saveGeneratedClass(final byte[] data, final String transformedName, final String folder) {
		if (LogisticsWrapperHandler.tempFolder == null) {
			LogisticsWrapperHandler.tempFolder = new File(Launch.minecraftHome, folder);
		}

		final File outFile = new File(LogisticsWrapperHandler.tempFolder, transformedName.replace('.', File.separatorChar) + ".class");
		final File outDir = outFile.getParentFile();

		if (!outDir.exists()) {
			outDir.mkdirs();
		}

		if (outFile.exists()) {
			outFile.delete();
		}

		try {
			LogWrapper.fine("Saving transformed class \"%s\" to \"%s\"", transformedName, outFile.getAbsolutePath().replace('\\', '/'));

			final OutputStream output = new FileOutputStream(outFile);
			output.write(data);
			output.close();
		} catch (IOException ex) {
			LogWrapper.log(Level.WARN, ex, "Could not save transformed class \"%s\"", transformedName);
		}
	}
}
