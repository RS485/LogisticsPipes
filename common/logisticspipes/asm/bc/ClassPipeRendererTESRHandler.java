package logisticspipes.asm.bc;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassPipeRendererTESRHandler {

	public static byte[] handlePipeRendererTESRClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);

		for (MethodNode m : node.methods) {
			if (m.name.equals("doRenderItem")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					@Override
					public void visitCode() {
						super.visitCode();
						Label l0 = new Label();
						visitLabel(l0);
						visitVarInsn(Opcodes.ALOAD, 1);
						visitVarInsn(Opcodes.DLOAD, 2);
						visitVarInsn(Opcodes.DLOAD, 4);
						visitVarInsn(Opcodes.DLOAD, 6);
						visitVarInsn(Opcodes.FLOAD, 8);
						this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/bc/RenderItemInPipeHook", "renderItemInPipe", "(Lbuildcraft/transport/TravelingItem;DDDF)V");
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
		}

		ClassWriter writer = new ClassWriter(0/*ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES*/);
		node.accept(writer);
		return writer.toByteArray();
	}

}
