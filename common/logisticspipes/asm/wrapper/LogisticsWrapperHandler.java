package logisticspipes.asm.wrapper;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IF_ACMPNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_6;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.DontLoadProxy;
import logisticspipes.proxy.VersionNotSupportedException;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.proxy.interfaces.IGenericProgressProvider;
import logisticspipes.utils.ModStatusHelper;
import net.minecraft.launchwrapper.Launch;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class LogisticsWrapperHandler {
	public static List<AbstractWrapper> wrapperController = new ArrayList<AbstractWrapper>();
	private static Method m_defineClass = null;
	
	public static IGenericProgressProvider getWrappedProgressProvider(String modId, String name, Class<? extends IGenericProgressProvider> providerClass) {
		IGenericProgressProvider provider = null;
		Throwable e = null; 
		if(ModStatusHelper.isModLoaded(modId)) {
			try {
				provider = providerClass.newInstance();
			} catch(Exception e1) {
				if(e1 instanceof VersionNotSupportedException) {
					throw (VersionNotSupportedException) e1;
				}
				e1.printStackTrace();
				e = e1;
			} catch(NoClassDefFoundError e1) {
				e1.printStackTrace();
				e = e1;
			}
		}
		GenericProgressProviderWrapper instance = new GenericProgressProviderWrapper(provider, modId + ": " + name);
		if(provider != null) {
			LogisticsPipes.log.info("Loaded " + name + " ProgressProvider");
		} else {
			if(e != null) {
				((AbstractWrapper)instance).setState(WrapperState.Exception);
				((AbstractWrapper)instance).setReason(e);
				LogisticsPipes.log.info("Couldn't load " + name + " ProgressProvider");
			} else {
				LogisticsPipes.log.info("Didn't load " + name + " ProgressProvider");
				((AbstractWrapper)instance).setState(WrapperState.ModMissing);
			}
		}
		instance.setModId(modId);
		wrapperController.add(instance);
		return instance;
	}
	
	public static ICraftingRecipeProvider getWrappedRecipeProvider(String modId, String name, Class<? extends ICraftingRecipeProvider> providerClass) {
		ICraftingRecipeProvider provider = null;
		Throwable e = null; 
		if(ModStatusHelper.isModLoaded(modId)) {
			try {
				provider = providerClass.newInstance();
			} catch(Exception e1) {
				if(e1 instanceof VersionNotSupportedException) {
					throw (VersionNotSupportedException) e1;
				}
				e1.printStackTrace();
				e = e1;
			} catch(NoClassDefFoundError e1) {
				e1.printStackTrace();
				e = e1;
			}
		}
		CraftingRecipeProviderWrapper instance = new CraftingRecipeProviderWrapper(provider, name);
		if(provider != null) {
			LogisticsPipes.log.info("Loaded " + name + " RecipeProvider");
		} else {
			if(e != null) {
				((AbstractWrapper)instance).setState(WrapperState.Exception);
				((AbstractWrapper)instance).setReason(e);
				LogisticsPipes.log.info("Couldn't load " + name + " RecipeProvider");
			} else {
				LogisticsPipes.log.info("Didn't load " + name + " RecipeProvider");
				((AbstractWrapper)instance).setState(WrapperState.ModMissing);
			}
		}
		instance.setModId(modId);
		wrapperController.add(instance);
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getWrappedProxy(String modId, Class<T> interfaze, Class<? extends T> proxyClazz, T dummyProxy) throws NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		String fieldName = interfaze.getName().replace('.', '/');
		String proxyName = interfaze.getSimpleName().substring(1);
		if(!proxyName.endsWith("Proxy")) {
			throw new RuntimeException("UnuportedProxyName: " + proxyName);
		}
		proxyName = proxyName.substring(0, proxyName.length() - 5);
		String className = "logisticspipes/asm/wrapper/" + proxyName + "ProxyWrapper";
		String classFile = interfaze.getSimpleName().substring(1) + "Wrapper.java";
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		
		cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, "logisticspipes/asm/wrapper/AbstractWrapper", new String[] { fieldName });
		
		cw.visitSource(classFile, null);
		
		{
			FieldVisitor fv = cw.visitField(ACC_PRIVATE, "proxy", "L" + fieldName + ";", null, null);
			fv.visitEnd();
		}
		{
			FieldVisitor fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "dummyProxy", "L" + fieldName + ";", null, null);
			fv.visitEnd();
		}
		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(L" + fieldName + ";L" + fieldName + ";)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(11, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "logisticspipes/asm/wrapper/AbstractWrapper", "<init>", "()V");
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLineNumber(12, l1);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitFieldInsn(PUTFIELD, className, "dummyProxy", "L" + fieldName + ";");
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLineNumber(13, l2);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitFieldInsn(PUTFIELD, className, "proxy", "L" + fieldName + ";");
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitLineNumber(14, l3);
			mv.visitInsn(RETURN);
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitLocalVariable("this", "L" + className + ";", null, l0, l4, 0);
			mv.visitLocalVariable("dProxy", "L" + fieldName + ";", null, l0, l4, 1);
			mv.visitLocalVariable("iProxy", "L" + fieldName + ";", null, l0, l4, 2);
			mv.visitMaxs(2, 3);
			mv.visitEnd();
		}
		int lineAddition = 100;
		for(Method method: interfaze.getMethods()) {
			addProxyMethod(cw, method, fieldName, className, lineAddition);
			lineAddition += 10;
		}
		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "onDisable", "()V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(21, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ACONST_NULL);
			mv.visitFieldInsn(PUTFIELD, className, "proxy", "L" + fieldName + ";");
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLineNumber(22, l1);
			mv.visitInsn(RETURN);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLocalVariable("this", "L" + className + ";", null, l0, l2, 0);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		addGetName(cw, className, proxyName);
		addGetTypeName(cw, className, "Proxy");
		cw.visitEnd();
		
		String lookfor = className.replace('/', '.');
		
		Class<?> clazz = loadClass(cw.toByteArray(), lookfor);
		
		T proxy = null;
		Throwable e = null; 
		if(ModStatusHelper.isModLoaded(modId)) {
			try {
				proxy = proxyClazz.newInstance();
			} catch(Exception e1) {
				if(e1 instanceof VersionNotSupportedException) {
					throw (VersionNotSupportedException) e1;
				}
				if(!(e1 instanceof DontLoadProxy)) {
					e1.printStackTrace();
					e = e1;
				}
			} catch(NoClassDefFoundError e1) {
				e1.printStackTrace();
				e = e1;
			}
		}
		T instance = (T) clazz.getConstructor(new Class<?>[]{interfaze, interfaze}).newInstance(dummyProxy, proxy);
		if(proxy != null) {
			LogisticsPipes.log.info("Loaded " + proxyName + "Proxy");
		} else {
			LogisticsPipes.log.info("Loaded " + proxyName + " DummyProxy");
			if(e != null) {
				((AbstractWrapper)instance).setState(WrapperState.Exception);
				((AbstractWrapper)instance).setReason(e);
			} else {
				((AbstractWrapper)instance).setState(WrapperState.ModMissing);
			}
		}
		((AbstractWrapper)instance).setModId(modId);
		wrapperController.add((AbstractWrapper) instance);
		return instance;
	}
	
	private static Class<?> loadClass(byte[] data, String lookfor) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if(m_defineClass == null) {
			m_defineClass = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
			m_defineClass.setAccessible(true);
		}
		return (Class<?>) m_defineClass.invoke(Launch.classLoader, data, 0, data.length);
	}
	
	private static void addGetTypeName(ClassWriter cw, String className, String type) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getTypeName", "()Ljava/lang/String;", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(31, l0);
		mv.visitLdcInsn(type);
		mv.visitInsn(ARETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", "L" + className + ";", null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private static void addGetName(ClassWriter cw, String className, String proxyName) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getName", "()Ljava/lang/String;", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(41, l0);
		mv.visitLdcInsn(proxyName);
		mv.visitInsn(ARETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", "L" + className + ";", null, l0, l1, 0);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private static void addProxyMethod(ClassWriter cw, Method method, String fieldName, String className, int lineAddition) {
		Class<?> retclazz = method.getReturnType();
		int eIndex = 1;
		StringBuilder desc = new StringBuilder("(");
		for(Class<?> clazz:method.getParameterTypes()) {
			desc.append(getClassSignature(clazz));
			eIndex++;
		}
		eIndex++;
		desc.append(")");
		int returnType = 0;
		if(retclazz == null || retclazz == void.class) {
			desc.append("V");
			returnType = RETURN;
		} else if(retclazz.isPrimitive()) {
			desc.append(getPrimitiveMapping(retclazz));
			returnType = getPrimitiveReturnMapping(retclazz);
		} else {
			desc.append("L" + retclazz.getName().replace('.', '/') + ";");
			returnType = ARETURN;
		}
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, method.getName(), desc.toString(), null, null);
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
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "state", "Llogisticspipes/asm/wrapper/WrapperState;");
		mv.visitFieldInsn(GETSTATIC, "logisticspipes/asm/wrapper/WrapperState", "Enabled", "Llogisticspipes/asm/wrapper/WrapperState;");
		Label l5 = new Label();
		mv.visitJumpInsn(IF_ACMPNE, l5);
		mv.visitLabel(l0);
		mv.visitLineNumber(lineAddition + 2, l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "proxy", "L" + fieldName + ";");
		addMethodParameterLoad(mv, method);
		mv.visitMethodInsn(INVOKEINTERFACE, fieldName, method.getName(), desc.toString());
		mv.visitLabel(l1);
		mv.visitInsn(returnType);
		mv.visitLabel(l2);
		mv.visitLineNumber(lineAddition + 3, l2);
		mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/Exception" });
		mv.visitVarInsn(ASTORE, eIndex);
		Label l6 = new Label();
		mv.visitLabel(l6);
		mv.visitLineNumber(lineAddition + 4, l6);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, eIndex);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "handleException", "(Ljava/lang/Throwable;)V");
		Label l7 = new Label();
		mv.visitLabel(l7);
		mv.visitJumpInsn(GOTO, l5);
		mv.visitLabel(l3);
		mv.visitLineNumber(lineAddition + 5, l3);
		mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/NoClassDefFoundError" });
		mv.visitVarInsn(ASTORE, eIndex);
		Label l8 = new Label();
		mv.visitLabel(l8);
		mv.visitLineNumber(lineAddition + 6, l8);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, eIndex);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "handleException", "(Ljava/lang/Throwable;)V");
		mv.visitLabel(l5);
		mv.visitLineNumber(lineAddition + 7, l5);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "dummyProxy", "L" + fieldName + ";");
		addMethodParameterLoad(mv, method);
		mv.visitMethodInsn(INVOKEINTERFACE, fieldName, method.getName(), desc.toString());
		mv.visitInsn(returnType);
		Label l9 = new Label();
		mv.visitLabel(l9);
		mv.visitLocalVariable("this", "L" + className + ";", null, l4, l9, 0);
		addParameterVars(mv, method, l4, l9);
		mv.visitLocalVariable("e", "Ljava/lang/Exception;", null, l6, l7, eIndex);
		mv.visitLocalVariable("e", "Ljava/lang/NoClassDefFoundError;", null, l8, l5, eIndex);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}
	
	private static void addMethodParameterLoad(MethodVisitor mv, Method method) {
		int i=1;
		for(Class<?> clazz:method.getParameterTypes()) {
			if(clazz.isPrimitive()) {
			    if(clazz == int.class || clazz == boolean.class || clazz == short.class || clazz == byte.class) {
					mv.visitVarInsn(ILOAD, i);
				} else if(clazz == long.class) {
					mv.visitVarInsn(LLOAD, i);
				} else if(clazz == float.class) {
					mv.visitVarInsn(FLOAD, i);
				} else if(clazz == double.class) {
					mv.visitVarInsn(DLOAD, i);
				} else {
					throw new UnsupportedOperationException("Unmapped clazz: " + clazz.getName());
				}
			} else {
				mv.visitVarInsn(ALOAD, i);
			}
			i++;
		}
	}
	
	private static void addParameterVars(MethodVisitor mv, Method method, Label l3, Label l8) {
		int i=1;
		for(Class<?> clazz:method.getParameterTypes()) {
			mv.visitLocalVariable("par" + i, getClassSignature(clazz), null, l3, l8, i);
			i++;
		}
	}

	private static String getPrimitiveMapping(Class<?> clazz) {
		if(clazz == int.class) {
			return "I";
		} else if(clazz == long.class) {
			return "J";
		} else if(clazz == float.class) {
			return "F";
		} else if(clazz == double.class) {
			return "D";
		} else if(clazz == boolean.class) {
			return "Z";
		} else if(clazz == short.class) {
			return "S";
		} else if(clazz == byte.class) {
			return "B";
		} else {
			throw new UnsupportedOperationException("Unmapped clazz: " + clazz.getName());
		}
	}
	
	private static int getPrimitiveReturnMapping(Class<?> clazz) {
		if(clazz == int.class || clazz == boolean.class || clazz == short.class || clazz == byte.class) {
			return IRETURN;
		} else if(clazz == long.class) {
			return LRETURN;
		} else if(clazz == float.class) {
			return FRETURN;
		} else if(clazz == double.class) {
			return DRETURN;
		} else {
			throw new UnsupportedOperationException("Unmapped clazz: " + clazz.getName());
		}
	}
	
	private static String getClassSignature(Class<?> clazz) {
		if(clazz.isPrimitive()) {
			return getPrimitiveMapping(clazz);
		} else {
			if(clazz.isArray()) {
				return clazz.getName().replace('.', '/');
			} else {
				return "L" + clazz.getName().replace('.', '/') + ";";
			}
		}
	}
}
