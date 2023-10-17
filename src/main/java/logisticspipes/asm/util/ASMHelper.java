package logisticspipes.asm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

public class ASMHelper {

	private static final String HEXES = "0123456789ABCDEF";

	public static String getContentForMethod(ClassReader classReader, String methodName, boolean newLine) {
		return ASMHelper.getContentForMethod(classReader, methodName, "", newLine);
	}

	public static String getContentForMethod(ClassReader classReader, String methodName, String desc, boolean newLine) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		TraceClassVisitor traceClassVisitor = new TraceClassVisitor(null, new MethodTextifier(), printWriter);
		FilterClassVisitor myClassVisitor = new FilterClassVisitor(traceClassVisitor, methodName, desc);
		classReader.accept(myClassVisitor, ClassReader.SKIP_DEBUG);
		BufferedReader reader = new BufferedReader(new StringReader(stringWriter.toString()));
		StringBuilder builder = new StringBuilder();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				String oldLine = line;
				line += " ";
				while (!oldLine.equals(line)) {
					oldLine = line;
					line = line.trim();
					line = line.replace("\t", " ");
					line = line.replace("  ", " ");
				}
				builder.append(" ");
				builder.append(line);
				if (newLine) {
					builder.append(System.lineSeparator());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	public static String getCheckSumForMethod(ClassReader classReader, String methodName) {
		return ASMHelper.getCheckSumForMethod(classReader, methodName, "");
	}

	public static String getCheckSumForMethod(ClassReader classReader, String methodName, String desc) {
		String result = ASMHelper.getContentForMethod(classReader, methodName, desc, false);
		if (result.isEmpty()) {
			throw new NoSuchMethodError();
		}
		return ASMHelper.toSHA1(result.getBytes());
	}

	public static String toSHA1(byte[] convertme) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return ASMHelper.getHex(md.digest(convertme));
	}

	private static String getHex(byte[] raw) {
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b : raw) {
			hex.append(ASMHelper.HEXES.charAt((b & 0xF0) >> 4)).append(ASMHelper.HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}

	private static class FilterClassVisitor extends ClassVisitor {

		private final String methodName;
		private final String methodDesc;

		public FilterClassVisitor(TraceClassVisitor traceClassVisitor, String methodName, String desc) {
			super(Opcodes.ASM4, traceClassVisitor);
			this.methodName = methodName;
			methodDesc = desc;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			if (methodName.equals(name) && (methodDesc.isEmpty() || methodDesc.equals(desc))) {
				return new FilterMaxVisitMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions));
			}
			return null;
		}
	}

	private static class FilterMaxVisitMethodVisitor extends MethodVisitor {

		public FilterMaxVisitMethodVisitor(MethodVisitor mv) {
			super(Opcodes.ASM4, mv);
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {}
	}

	private static class MethodTextifier extends Printer {

		public MethodTextifier() {
			super(Opcodes.ASM4);
		}

		@Override
		public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {}

		@Override
		public void visitSource(final String file, final String debug) {}

		@Override
		public void visitOuterClass(final String owner, final String name, final String desc) {}

		@Override
		public Textifier visitClassAnnotation(final String desc, final boolean visible) {
			return new Textifier();
		}

		@Override
		public void visitClassAttribute(final Attribute attr) {}

		@Override
		public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {}

		@Override
		public Textifier visitField(final int access, final String name, final String desc, final String signature, final Object value) {
			return new Textifier();
		}

		@Override
		public Textifier visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
			Textifier t = new Textifier();
			text.add(t.getText());
			return t;
		}

		@Override
		public void visitClassEnd() {}

		@Override
		public void visit(final String name, final Object value) {}

		@Override
		public void visitEnum(final String name, final String desc, final String value) {}

		@Override
		public Textifier visitAnnotation(final String name, final String desc) {
			return new Textifier();
		}

		@Override
		public Textifier visitArray(final String name) {
			return new Textifier();
		}

		@Override
		public void visitAnnotationEnd() {}

		@Override
		public Textifier visitFieldAnnotation(final String desc, final boolean visible) {
			return new Textifier();
		}

		@Override
		public void visitFieldAttribute(final Attribute attr) {
			visitAttribute(attr);
		}

		@Override
		public void visitFieldEnd() {}

		@Override
		public Textifier visitAnnotationDefault() {
			return new Textifier();
		}

		@Override
		public Textifier visitMethodAnnotation(final String desc, final boolean visible) {
			return new Textifier();
		}

		@Override
		public Textifier visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
			return new Textifier();
		}

		@Override
		public void visitMethodAttribute(final Attribute attr) {}

		@Override
		public void visitCode() {}

		@Override
		public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {}

		@Override
		public void visitInsn(final int opcode) {}

		@Override
		public void visitIntInsn(final int opcode, final int operand) {}

		@Override
		public void visitVarInsn(final int opcode, final int var) {}

		@Override
		public void visitTypeInsn(final int opcode, final String type) {}

		@Override
		public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {}

		@Override
		public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {}

		@Override
		public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {}

		@Override
		public void visitJumpInsn(final int opcode, final Label label) {}

		@Override
		public void visitLabel(final Label label) {}

		@Override
		public void visitLdcInsn(final Object cst) {}

		@Override
		public void visitIincInsn(final int var, final int increment) {}

		@Override
		public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {}

		@Override
		public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {}

		@Override
		public void visitMultiANewArrayInsn(final String desc, final int dims) {}

		@Override
		public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {}

		@Override
		public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {}

		@Override
		public void visitLineNumber(final int line, final Label start) {}

		@Override
		public void visitMaxs(final int maxStack, final int maxLocals) {}

		@Override
		public void visitMethodEnd() {}

		public void visitAttribute(final Attribute attr) {}
	}
}
