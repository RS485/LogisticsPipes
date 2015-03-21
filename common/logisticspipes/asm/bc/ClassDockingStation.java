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
		
		for(MethodNode m: node.methods) {
			if(m.name.equals("getPipe") && m.desc.equals("()Lbuildcraft/transport/TileGenericPipe;")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					public void visitCode() {
						super.visitCode();
						Label l0 = new Label();
						this.visitLabel(l0);
						this.visitLineNumber(54, l0);
						this.visitVarInsn(Opcodes.ALOAD, 0);
						this.visitFieldInsn(Opcodes.GETFIELD, "buildcraft/robots/DockingStation", "pipe", "Lbuildcraft/transport/TileGenericPipe;");
						Label l1 = new Label();
						this.visitJumpInsn(Opcodes.IFNONNULL, l1);
						Label l2 = new Label();
						this.visitLabel(l2);
						this.visitLineNumber(55, l2);
						this.visitVarInsn(Opcodes.ALOAD, 0);
						this.visitVarInsn(Opcodes.ALOAD, 0);
						this.visitFieldInsn(Opcodes.GETFIELD, "buildcraft/robots/DockingStation", "world", "Lnet/minecraft/world/World;");
						this.visitVarInsn(Opcodes.ALOAD, 0);
						this.visitFieldInsn(Opcodes.GETFIELD, "buildcraft/robots/DockingStation", "index", "Lbuildcraft/api/core/BlockIndex;");
						this.visitFieldInsn(Opcodes.GETFIELD, "buildcraft/api/core/BlockIndex", "x", "I");
						this.visitVarInsn(Opcodes.ALOAD, 0);
						this.visitFieldInsn(Opcodes.GETFIELD, "buildcraft/robots/DockingStation", "index", "Lbuildcraft/api/core/BlockIndex;");
						this.visitFieldInsn(Opcodes.GETFIELD, "buildcraft/api/core/BlockIndex", "y", "I");
						this.visitVarInsn(Opcodes.ALOAD, 0);
						this.visitFieldInsn(Opcodes.GETFIELD, "buildcraft/robots/DockingStation", "index", "Lbuildcraft/api/core/BlockIndex;");
						this.visitFieldInsn(Opcodes.GETFIELD, "buildcraft/api/core/BlockIndex", "z", "I");
						this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/bc/DockingStationHook", "getPipeForDockingStation", "(Lnet/minecraft/world/World;III)Lnet/minecraft/tileentity/TileEntity;", false);
						this.visitTypeInsn(Opcodes.CHECKCAST, "buildcraft/transport/TileGenericPipe");
						this.visitFieldInsn(Opcodes.PUTFIELD, "buildcraft/robots/DockingStation", "pipe", "Lbuildcraft/transport/TileGenericPipe;");
						this.visitLabel(l1);
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
