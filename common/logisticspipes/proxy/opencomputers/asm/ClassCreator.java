package logisticspipes.proxy.opencomputers.asm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.launchwrapper.Launch;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import logisticspipes.proxy.computers.wrapper.CCWrapperInformation;
import logisticspipes.utils.tuples.Pair;

public class ClassCreator {

	private static List<String> createdClasses = new ArrayList<>();

	public static byte[] getWrappedClassAsBytes(CCWrapperInformation info, String className) {

		String newClassName_DOT = "logisticspipes.proxy.opencomputers.asm.BaseWrapperClass$" + className + "$OpenComputersWrapper";
		String newClassName_SLASH = newClassName_DOT.replace('.', '/');
		String newClassName_TYPE = "L" + newClassName_SLASH + ";";

		ClassWriter cw = new ClassWriter(0);

		cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, newClassName_SLASH, null, "logisticspipes/proxy/opencomputers/asm/BaseWrapperClass", null);

		{
			MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, new String[] { "java/lang/ClassNotFoundException" });
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(10, l0);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitLdcInsn(className);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "logisticspipes/proxy/opencomputers/asm/BaseWrapperClass", "<init>", "(Ljava/lang/String;)V");
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLineNumber(11, l1);
			mv.visitInsn(Opcodes.RETURN);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLocalVariable("this", newClassName_TYPE, null, l0, l2, 0);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		{
			MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Llogisticspipes/proxy/computers/wrapper/CCWrapperInformation;Ljava/lang/Object;)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(14, l0);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitVarInsn(Opcodes.ALOAD, 2);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "logisticspipes/proxy/opencomputers/asm/BaseWrapperClass", "<init>", "(Llogisticspipes/proxy/computers/wrapper/CCWrapperInformation;Ljava/lang/Object;)V");
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLineNumber(15, l1);
			mv.visitInsn(Opcodes.RETURN);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLocalVariable("this", newClassName_TYPE, null, l0, l2, 0);
			mv.visitLocalVariable("info", "Llogisticspipes/proxy/computers/wrapper/CCWrapperInformation;", null, l0, l2, 1);
			mv.visitLocalVariable("object", "Ljava/lang/Object;", null, l0, l2, 2);
			mv.visitMaxs(3, 3);
			mv.visitEnd();
		}

		for (String method : info.commandTypes.keySet()) {
			Pair<Boolean, String> desc = info.commandTypes.get(method);
			ClassCreator.addMethod(cw, method, !desc.getValue1(), desc.getValue2(), newClassName_TYPE);
		}

		cw.visitEnd();

		ClassCreator.createdClasses.add(className);

		return cw.toByteArray();
	}

	public static Class<? extends BaseWrapperClass> getWrapperClass(CCWrapperInformation info, String className) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String newClassName_DOT = "logisticspipes.proxy.opencomputers.asm.BaseWrapperClass$" + className + "$OpenComputersWrapper";
		if (ClassCreator.createdClasses.contains(className)) {
			try {
				return (Class<? extends BaseWrapperClass>) Class.forName(newClassName_DOT);
			} catch (ClassNotFoundException ignored) {}
		}
		byte[] bytes = ClassCreator.getWrappedClassAsBytes(info, className);
		return (Class<? extends BaseWrapperClass>) ClassCreator.loadClass(bytes, newClassName_DOT);
	}

	private static void addMethod(ClassWriter cw, String name, boolean direct, String doc, String newClassName_TYPE) {
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, name, "(Lli/cil/oc/api/machine/Context;Lli/cil/oc/api/machine/Arguments;)[Ljava/lang/Object;", null, new String[] { "java/lang/Exception" });
		{
			AnnotationVisitor av0 = mv.visitAnnotation("Lli/cil/oc/api/machine/Callback;", true);
			if (direct) {
				av0.visit("direct", Boolean.TRUE);
			} else {
				av0.visit("direct", Boolean.FALSE);
			}
			av0.visit("doc", doc);
			av0.visitEnd();
		}
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(19, l0);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitLdcInsn(name);
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitVarInsn(Opcodes.ALOAD, 2);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "logisticspipes/proxy/opencomputers/asm/BaseWrapperClass", "invokeMethod", "(Ljava/lang/String;Lli/cil/oc/api/machine/Context;Lli/cil/oc/api/machine/Arguments;)[Ljava/lang/Object;");
		mv.visitInsn(Opcodes.ARETURN);
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLocalVariable("this", newClassName_TYPE, null, l0, l1, 0);
		mv.visitLocalVariable("context", "Lli/cil/oc/api/machine/Context;", null, l0, l1, 1);
		mv.visitLocalVariable("args", "Lli/cil/oc/api/machine/Arguments;", null, l0, l1, 2);
		mv.visitMaxs(4, 3);
		mv.visitEnd();
	}

	private static Method m_defineClass = null;

	private static Class<?> loadClass(byte[] data, String lookfor) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (ClassCreator.m_defineClass == null) {
			ClassCreator.m_defineClass = ClassLoader.class.getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
			ClassCreator.m_defineClass.setAccessible(true);
		}
		return (Class<?>) ClassCreator.m_defineClass.invoke(Launch.classLoader, data, 0, data.length);
	}
}
