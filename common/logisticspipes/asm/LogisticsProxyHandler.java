package logisticspipes.asm;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.VersionNotSupportedException;
import logisticspipes.proxy.interfaces.IProxyController;
import logisticspipes.utils.ModStatusHelper;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class LogisticsProxyHandler {
	public static List<IProxyController> proxyController = new ArrayList<IProxyController>();
	
	public static <T> T getWrapped(String modId, Class<T> interfaze, Class<? extends T> proxyClazz, T dummyProxy) {
		try {
			return getWrappedException(modId, interfaze, proxyClazz, dummyProxy);
		} catch(Exception e) {
			if(e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getWrappedException(String modId, Class<T> interfaze, Class<? extends T> proxyClazz, T dummyProxy) throws NoSuchFieldException, SecurityException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		String fieldName = interfaze.getName().replace('.', '/');
		String proxyName = interfaze.getSimpleName().substring(1);
		if(!proxyName.endsWith("Proxy")) {
			throw new RuntimeException("UnuportedProxyName: " + proxyName);
		}
		proxyName = proxyName.substring(0, proxyName.length() - 5);
		String className = "logisticspipes/asm/proxywrapper/" + proxyName + "ProxyWrapper";
		String classFile = interfaze.getSimpleName().substring(1) + "Wrapper.java";
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		
		cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", new String[] { fieldName, "logisticspipes/proxy/interfaces/IProxyController" });
		
		cw.visitSource(classFile, null);
		
		{
			FieldVisitor fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "proxy", "L" + fieldName + ";", null, null);
			fv.visitEnd();
		}
		{
			FieldVisitor fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "dummyProxy", "L" + fieldName + ";", null, null);
			fv.visitEnd();
		}
		{
			FieldVisitor fv = cw.visitField(ACC_PRIVATE, "enabled", "Z", null, null);
			fv.visitEnd();
		}
		{
			FieldVisitor fv = cw.visitField(ACC_PRIVATE, "reason", "Ljava/lang/String;", null, null);
			fv.visitEnd();
		}
		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(L" + fieldName + ";L" + fieldName + ";)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ICONST_1);
			mv.visitFieldInsn(PUTFIELD, className, "enabled", "Z");
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitFieldInsn(PUTFIELD, className, "dummyProxy", "L" + fieldName + ";");
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitFieldInsn(PUTFIELD, className, "proxy", "L" + fieldName + ";");
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitInsn(RETURN);
			Label l5 = new Label();
			mv.visitLabel(l5);
			mv.visitLocalVariable("this", "L" + className + ";", null, l0, l5, 0);
			mv.visitLocalVariable("dProxy", "L" + fieldName + ";", null, l0, l5, 1);
			mv.visitLocalVariable("iProxy", "L" + fieldName + ";", null, l0, l5, 2);
			mv.visitMaxs(2, 3);
			mv.visitEnd();
		}
		for(Method method: interfaze.getMethods()) {
			addMethod(cw, method, fieldName, className);
		}
		{
			MethodVisitor mv = cw.visitMethod(ACC_PRIVATE, "error", "()V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, "logisticspipes/LogisticsPipes", "log", "Ljava/util/logging/Logger;");
			mv.visitLdcInsn("Disabled " +  proxyName + "Proxy for Mod: " + modId);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/logging/Logger", "severe", "(Ljava/lang/String;)V");
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(PUTFIELD, className, "enabled", "Z");
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitInsn(RETURN);
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitLocalVariable("this", "L" + className + ";", null, l0, l3, 0);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "setEnabled", "(Z)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ILOAD, 1);
			mv.visitFieldInsn(PUTFIELD, className, "enabled", "Z");
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitInsn(RETURN);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLocalVariable("this", "L" + className + ";", null, l0, l2, 0);
			mv.visitLocalVariable("flag", "Z", null, l0, l2, 1);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "isEnabled", "()Z", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "enabled", "Z");
			mv.visitInsn(IRETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "L" + className + ";", null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getReason", "()Ljava/lang/String;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, "reason", "Ljava/lang/String;");
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "L" + className + ";", null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getProxyName", "()Ljava/lang/String;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLdcInsn(proxyName);
			mv.visitInsn(ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "L" + className + ";", null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		cw.visitEnd();
		
		String lookfor = className.replace('/', '.');
		
		byte[] data = cw.toByteArray();
		
		Field f = LaunchClassLoader.class.getDeclaredField("transformers");
		f.setAccessible(true);
		List<IClassTransformer> tmp = (List<IClassTransformer>) f.get(Launch.classLoader);
	    List<IClassTransformer> list = new ArrayList<IClassTransformer>(1);
	    list.add(LogisticsClassTransformer.instance);
		f.set(Launch.classLoader, list); // Fix ClassTransformer who don't ignore a null byte array

		LogisticsClassTransformer.instance.cachedClasses.put(lookfor, data);
		LogisticsClassTransformer.instance.interfacesToClearA.add(lookfor);
		LogisticsClassTransformer.instance.clearNegativeInterfaceCache();
		
		Class<?> clazz = Launch.classLoader.findClass(lookfor);

		f.set(Launch.classLoader, tmp);
		T proxy = null;
		boolean isDummy = false;
		if(ModStatusHelper.isModLoaded(modId)) {
			try {
				proxy = proxyClazz.newInstance();
			} catch(Throwable e) {
				if(e instanceof VersionNotSupportedException) {
					throw (VersionNotSupportedException) e;
				}
				e.printStackTrace();
			}
		} else {
			isDummy = true;
		}
		T instance = (T) clazz.getConstructor(new Class<?>[]{interfaze, interfaze}).newInstance(dummyProxy, proxy);
		if(isDummy) {
			Field reason = clazz.getDeclaredField("reason");
			reason.setAccessible(true);
			reason.set(instance, "dummy");
		}
		if(proxy != null) {
			LogisticsPipes.log.info("Loaded " + proxyName + "Proxy");
		} else {
			((IProxyController)instance).setEnabled(false);
			LogisticsPipes.log.info("Loaded " + proxyName + " DummyProxy");
		}
		proxyController.add((IProxyController) instance);
		return instance;
	}
	
	private static void addMethod(ClassWriter cw, Method method, String fieldName, String className) {
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
		mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "enabled", "Z");
		Label l4 = new Label();
		mv.visitJumpInsn(IFEQ, l4);
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "proxy", "L" + fieldName + ";");
		addMethodParameterLoad(mv, method);
		mv.visitMethodInsn(INVOKEINTERFACE, fieldName, method.getName(), desc.toString());
		mv.visitLabel(l1);
		mv.visitInsn(returnType);
		mv.visitLabel(l2);
		mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/Throwable" });
		mv.visitVarInsn(ASTORE, eIndex);
		Label l5 = new Label();
		mv.visitLabel(l5);
		mv.visitVarInsn(ALOAD, eIndex);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Throwable", "printStackTrace", "()V");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "error", "()V");
		mv.visitLabel(l4);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, className, "dummyProxy", "L" + fieldName + ";");
		addMethodParameterLoad(mv, method);
		mv.visitMethodInsn(INVOKEINTERFACE, fieldName, method.getName(), desc.toString());
		Label l7 = new Label();
		mv.visitLabel(l7);
		mv.visitInsn(returnType);
		Label l8 = new Label();
		mv.visitLabel(l8);
		mv.visitLocalVariable("this", "L" + className + ";", null, l3, l8, 0);
		addParameterVars(mv, method, l3, l8);
		mv.visitLocalVariable("e", "Ljava/lang/Throwable;", null, l5, l4, eIndex);
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
