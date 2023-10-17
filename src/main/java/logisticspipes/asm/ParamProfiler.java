package logisticspipes.asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ParamProfiler {

	private static final boolean isActive = false;

	private static volatile long minMethodId = 0;

	public static byte[] handleClass(byte[] bytes) {
		if (!isActive) return bytes;
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);
		final String className = node.name;
		for (final MethodNode m : node.methods) {
			final String methodName = m.name;
			final String methodDesc = m.desc;
			final boolean isConst = methodName.contains("<") || methodName.contains(">");
			if (isConst) continue;
			final long methodId = minMethodId++;

			final List<String> varList = new ArrayList<>();
			if (!methodDesc.startsWith("(")) throw new UnsupportedOperationException(methodDesc);
			outer:
			for (int i = 1; i < methodDesc.length(); i++) {
				switch (methodDesc.charAt(i)) {
					case ')':
						break outer;
					case 'L':
						int startA = i;
						while (methodDesc.charAt(i) != ';') i++;
						varList.add(methodDesc.substring(startA, i + 1));
						break;
					case '[':
						int startB = i;
						while (methodDesc.charAt(i) == '[') i++;
						if (methodDesc.charAt(i) == 'L') {
							while (methodDesc.charAt(i) != ';') i++;
						}
						varList.add(methodDesc.substring(startB, i + 1));
						break;
					default:
						varList.add(String.valueOf(methodDesc.charAt(i)));
				}
			}
			final List<Label> catchStatement = new ArrayList<>();
			MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

				@Override
				public void visitCode() {
					super.visitCode();
					Label l0 = new Label();
					visitLabel(l0);

					visitLdcInsn(new Long(methodId));
					visitLdcInsn(className + "+" + methodName + "+" + methodDesc);
					if ((m.access & Opcodes.ACC_STATIC) != 0) {
						visitInsn(Opcodes.ACONST_NULL);
					} else {
						visitVarInsn(Opcodes.ALOAD, 0);
					}

					visitIntInsn(Opcodes.BIPUSH, varList.size());
					visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
					int count = 0;
					int pos = 0;
					if ((m.access & Opcodes.ACC_STATIC) == 0) {
						pos = 1;
					}
					for (String varNode : varList) {
						visitInsn(Opcodes.DUP);
						visitIntInsn(Opcodes.BIPUSH, count++);
						if (!varNode.startsWith("L") && !varNode.startsWith("[")) {
							switch (varNode.charAt(0)) {
								case 'I':
									visitVarInsn(Opcodes.ILOAD, pos);
									pos += 1;
									visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
									break;
								case 'J':
									visitVarInsn(Opcodes.LLOAD, pos);
									pos += 2;
									visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
									break;
								case 'Z':
									visitVarInsn(Opcodes.ILOAD, pos);
									pos += 1;
									visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
									break;
								case 'B':
									visitVarInsn(Opcodes.ILOAD, pos);
									pos += 1;
									visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
									break;
								case 'C':
									visitVarInsn(Opcodes.ILOAD, pos);
									pos += 1;
									visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
									break;
								case 'S':
									visitVarInsn(Opcodes.ILOAD, pos);
									pos += 1;
									visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
									break;
								case 'F':
									visitVarInsn(Opcodes.FLOAD, pos);
									pos += 1;
									visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
									break;
								case 'D':
									visitVarInsn(Opcodes.DLOAD, pos);
									pos += 2;
									visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
									break;
								default:
									throw new UnsupportedOperationException("'" + varNode + "'");
							}
						} else {
							visitVarInsn(Opcodes.ALOAD, pos);
							pos += 1;
						}
						visitInsn(Opcodes.AASTORE);
					}
					visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/ParamProfiler", "methodStart", "(JLjava/lang/String;Ljava/lang/Object;[Ljava/lang/Object;)V", false);
				}

				@Override
				public void visitInsn(int opcode) {
					if (opcode == Opcodes.RETURN || opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN || opcode == Opcodes.FRETURN || opcode == Opcodes.DRETURN || opcode == Opcodes.ARETURN) {
						visitLdcInsn(new Long(methodId));
						visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/ParamProfiler", "methodEnd", "(J)V", false);
					}
					super.visitInsn(opcode);
				}

				@Override
				public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
					catchStatement.add(handler);
					super.visitTryCatchBlock(start, end, handler, type);
				}

				boolean watchForHandling = false;

				@Override
				public void visitLabel(Label label) {
					watchForHandling = false;
					super.visitLabel(label);
					if (catchStatement.contains(label)) {
						watchForHandling = true;
					}
				}

				@Override
				public void visitVarInsn(int opcode, int var) {
					super.visitVarInsn(opcode, var);
					if (watchForHandling) {
						watchForHandling = false;
						Label l = new Label();
						visitLabel(l);
						visitVarInsn(Opcodes.ALOAD, var);
						visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/ParamProfiler", "handleException", "(Ljava/lang/Throwable;)V", false);
					}
				}
			};
			m.accept(mv);
			node.methods.set(node.methods.indexOf(m), mv);

		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		node.accept(writer);
		return writer.toByteArray();
	}

	private static Map<Thread, Stack<Entry>> stack = new HashMap<>();

	@SuppressWarnings("unused") // Used by ASM
	public static void methodStart(long id, String name, Object root, Object... params) {
		if (!isActive) return;
		Stack<Entry> access = stack.get(Thread.currentThread());
		if (access == null) {
			access = new Stack<>();
			stack.put(Thread.currentThread(), access);
		}
		access.push(new Entry(id, name, root, params));
	}

	@SuppressWarnings("unused") // Used by ASM
	public static void methodEnd(long id) {
		if (!isActive) return;
		Stack<Entry> access = stack.get(Thread.currentThread());
		if (access == null || access.isEmpty()) return;
		Entry exit = access.pop();
		if (exit.id != id) {
			while (!access.isEmpty() && access.peek().id != id) {
				access.pop();
			}
			if (!access.isEmpty()) {
				access.pop();
			}
		}
	}

	@Getter
	private static WeakHashMap<Throwable, ArrayList<Entry>> infoLink = new WeakHashMap<>();

	@SuppressWarnings("unused") // Used by ASM
	public static void handleException(Throwable t) {
		Stack<Entry> access = stack.get(Thread.currentThread());
		infoLink.put(t, new ArrayList<>(access));
	}

	@Data
	@AllArgsConstructor
	public static class Entry {

		private final long id;
		private final String name;
		private final Object root;
		private final Object[] params;
	}
}
