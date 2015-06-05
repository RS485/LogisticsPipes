package logisticspipes.asm.bc;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassDockingStation {

	public static byte[] handleDockingStationClass(byte[] bytes) {
		final ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(node, 0);

		for (MethodNode m : node.methods) {
			if (m.name.equals("getPipe") && m.desc.equals("()Lbuildcraft/transport/TileGenericPipe;")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					@Override
					public void visitCode() {
						super.visitCode();
						Label l0 = new Label();
						visitLabel(l0);
						visitLineNumber(54, l0);
						visitVarInsn(Opcodes.ALOAD, 0);
						visitFieldInsn(Opcodes.GETFIELD, "buildcraft/robots/DockingStation", "pipe", "Lbuildcraft/transport/TileGenericPipe;");
						Label l1 = new Label();
						visitJumpInsn(Opcodes.IFNONNULL, l1);
						Label l2 = new Label();
						visitLabel(l2);
						visitLineNumber(55, l2);
						visitVarInsn(Opcodes.ALOAD, 0);
						visitVarInsn(Opcodes.ALOAD, 0);
						visitFieldInsn(Opcodes.GETFIELD, "buildcraft/robots/DockingStation", "world", "Lnet/minecraft/world/World;");
						visitVarInsn(Opcodes.ALOAD, 0);
						visitFieldInsn(Opcodes.GETFIELD, "buildcraft/robots/DockingStation", "index", "Lbuildcraft/api/core/BlockIndex;");
						visitFieldInsn(Opcodes.GETFIELD, "buildcraft/api/core/BlockIndex", "x", "I");
						visitVarInsn(Opcodes.ALOAD, 0);
						visitFieldInsn(Opcodes.GETFIELD, "buildcraft/robots/DockingStation", "index", "Lbuildcraft/api/core/BlockIndex;");
						visitFieldInsn(Opcodes.GETFIELD, "buildcraft/api/core/BlockIndex", "y", "I");
						visitVarInsn(Opcodes.ALOAD, 0);
						visitFieldInsn(Opcodes.GETFIELD, "buildcraft/robots/DockingStation", "index", "Lbuildcraft/api/core/BlockIndex;");
						visitFieldInsn(Opcodes.GETFIELD, "buildcraft/api/core/BlockIndex", "z", "I");
						this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/bc/DockingStationHook", "getPipeForDockingStation", "(Lnet/minecraft/world/World;III)Lnet/minecraft/tileentity/TileEntity;", false);
						visitTypeInsn(Opcodes.CHECKCAST, "buildcraft/transport/TileGenericPipe");
						visitFieldInsn(Opcodes.PUTFIELD, "buildcraft/robots/DockingStation", "pipe", "Lbuildcraft/transport/TileGenericPipe;");
						visitLabel(l1);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
		}

		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}

}
