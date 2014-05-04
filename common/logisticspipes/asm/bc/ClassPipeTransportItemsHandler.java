package logisticspipes.asm.bc;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.RETURN;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassPipeTransportItemsHandler {
	
	private static void insertNewInjectItemMethod(ClassNode node) {
		MethodVisitor mv = node.visitMethod(ACC_PUBLIC, "injectItem", "(Lbuildcraft/transport/TravelingItem;Lnet/minecraftforge/common/util/ForgeDirection;)V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(157, l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKESTATIC, "logisticspipes/asm/bc/InjectItemHook", "handleInjectItem", "(Lbuildcraft/transport/PipeTransportItems;Lbuildcraft/transport/TravelingItem;Lnet/minecraftforge/common/util/ForgeDirection;)V");
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLineNumber(158, l1);
		mv.visitInsn(RETURN);
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitLocalVariable("this", "Lbuildcraft/transport/PipeTransportItems;", null, l0, l2, 0);
		mv.visitLocalVariable("item", "Lbuildcraft/transport/TravelingItem;", null, l0, l2, 1);
		mv.visitLocalVariable("inputOrientation", "Lnet/minecraftforge/common/util/ForgeDirection;", null, l0, l2, 2);
		mv.visitMaxs(3, 3);
		mv.visitEnd();
	}
	
	public static byte[] handlePipeTransportItems(byte[] bytes) {
		final ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(node, 0);
		Iterator<MethodNode> iter = node.methods.iterator();
		while(iter.hasNext()) {
			MethodNode m = iter.next();
			if(m.name.equals("readFromNBT")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					// logisticspipes.asm.LogisticsASMHookClass.clearInvalidFluidContainers(items);
					@Override
					public void visitInsn(int opcode) {
						if(opcode == Opcodes.RETURN) {
							AbstractInsnNode instruction_1 = null;
							AbstractInsnNode instruction_2 = null;
							instructions.remove(instruction_2 = instructions.getLast());
							instructions.remove(instruction_1 = instructions.getLast());
							Label l = new Label();
							this.visitLabel(l);
							this.visitVarInsn(Opcodes.ALOAD, 0);
							this.visitFieldInsn(Opcodes.GETFIELD, "buildcraft/transport/PipeTransportItems", "items", "Lbuildcraft/transport/TravelerSet;");
							this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/LogisticsASMHookClass", "clearInvalidFluidContainers", "(Lbuildcraft/transport/TravelerSet;)V");
							instructions.add(instruction_1);
							instructions.add(instruction_2);							
						}
						super.visitInsn(opcode);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("injectItem")) {
				iter.remove();
			}
		}
		insertNewInjectItemMethod(node);
		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}
}
