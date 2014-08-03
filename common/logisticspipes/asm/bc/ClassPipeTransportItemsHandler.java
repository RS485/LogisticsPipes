package logisticspipes.asm.bc;

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
		MethodVisitor mv = node.visitMethod(Opcodes.ACC_PUBLIC, "injectItem", "(Lbuildcraft/transport/TravelingItem;Lnet/minecraftforge/common/util/ForgeDirection;)V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(157, l0);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitVarInsn(Opcodes.ALOAD, 2);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/bc/InjectItemHook", "handleInjectItem", "(Lbuildcraft/transport/PipeTransportItems;Lbuildcraft/transport/TravelingItem;Lnet/minecraftforge/common/util/ForgeDirection;)V");
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLineNumber(158, l1);
		mv.visitInsn(Opcodes.RETURN);
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
			if(m.name.equals("injectItem")) {
				iter.remove();
			}
			if(m.name.equals("canPipeConnect")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					public void visitCode() {
						super.visitCode();
						Label l0 = new Label();
						this.visitLabel(l0);
						this.visitVarInsn(Opcodes.ALOAD, 1);
						this.visitTypeInsn(Opcodes.INSTANCEOF, "logisticspipes/pipes/basic/LogisticsTileGenericPipe");
						Label l1 = new Label();
						this.visitJumpInsn(Opcodes.IFEQ, l1);
						Label l2 = new Label();
						this.visitLabel(l2);
						this.visitInsn(Opcodes.ICONST_1);
						this.visitInsn(Opcodes.IRETURN);
						this.visitLabel(l1);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("passToNextPipe")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					public void visitCode() {
						super.visitCode();
						Label l0 = new Label();
						this.visitLabel(l0);
						this.visitVarInsn(Opcodes.ALOAD, 2);
						this.visitTypeInsn(Opcodes.INSTANCEOF, "logisticspipes/pipes/basic/LogisticsTileGenericPipe");
						Label l1 = new Label();
						this.visitJumpInsn(Opcodes.IFEQ, l1);
						Label l2 = new Label();
						this.visitLabel(l2);
						this.visitVarInsn(Opcodes.ALOAD, 2);
						this.visitTypeInsn(Opcodes.CHECKCAST, "logisticspipes/pipes/basic/LogisticsTileGenericPipe");
						this.visitVarInsn(Opcodes.ALOAD, 1);
						this.visitVarInsn(Opcodes.ALOAD, 1);
						this.visitFieldInsn(Opcodes.GETFIELD, "buildcraft/transport/TravelingItem", "output", "Lnet/minecraftforge/common/ForgeDirection;");
						this.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "logisticspipes/pipes/basic/LogisticsTileGenericPipe", "acceptBCTravelingItem", "(Lbuildcraft/transport/TravelingItem;Lnet/minecraftforge/common/ForgeDirection;)V");
						Label l3 = new Label();
						this.visitLabel(l3);
						this.visitInsn(Opcodes.ICONST_1);
						this.visitInsn(Opcodes.IRETURN);
						this.visitLabel(l1);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("canReceivePipeObjects")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					public void visitCode() {
						super.visitCode();
						Label l0 = new Label();
						super.visitLabel(l0);
						super.visitLineNumber(247, l0);
						super.visitVarInsn(Opcodes.ALOAD, 0);
						super.visitFieldInsn(Opcodes.GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
						super.visitVarInsn(Opcodes.ALOAD, 1);
						super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "buildcraft/transport/TileGenericPipe", "getTile", "(Lnet/minecraftforge/common/ForgeDirection;)Lnet/minecraft/tileentity/TileEntity;");
						super.visitTypeInsn(Opcodes.INSTANCEOF, "logisticspipes/pipes/basic/LogisticsTileGenericPipe");
						Label l1 = new Label();
						super.visitJumpInsn(Opcodes.IFEQ, l1);
						super.visitVarInsn(Opcodes.ALOAD, 0);
						super.visitFieldInsn(Opcodes.GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
						super.visitVarInsn(Opcodes.ALOAD, 1);
						super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "buildcraft/transport/TileGenericPipe", "getTile", "(Lnet/minecraftforge/common/ForgeDirection;)Lnet/minecraft/tileentity/TileEntity;");
						super.visitTypeInsn(Opcodes.CHECKCAST, "logisticspipes/pipes/basic/LogisticsTileGenericPipe");
						super.visitVarInsn(Opcodes.ALOAD, 0);
						super.visitFieldInsn(Opcodes.GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
						super.visitVarInsn(Opcodes.ALOAD, 1);
						super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "logisticspipes/pipes/basic/LogisticsTileGenericPipe", "isBCPipeConnected", "(Lbuildcraft/transport/TileGenericPipe;Lnet/minecraftforge/common/ForgeDirection;)Z");
						super.visitInsn(Opcodes.IRETURN);
						super.visitLabel(l1);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
		}
		insertNewInjectItemMethod(node);
		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}
}
