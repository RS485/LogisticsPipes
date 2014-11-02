package logisticspipes.asm.bc;

import logisticspipes.LPConstants;
import logisticspipes.asm.util.ASMHelper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import cpw.mods.fml.common.FMLCommonHandler;

public class ClassPipeHandler {

	public static byte[] handleBCPipeClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);
		
		if(LPConstants.DEBUG) {
			boolean noChecksumMatch = false;
		
			if(!"1186D85A51274E12CF04FA5042472CCBFC3D5FAD".equals(ASMHelper.getCheckSumForMethod(reader, "handlePipeEvent"))) noChecksumMatch = true;
			if(!"4754C914043A5A98D74E7E3AAF7E5D582F88C7AE".equals(ASMHelper.getCheckSumForMethod(reader, "isWireConnectedTo"))) noChecksumMatch = true;
			if(!"51DAD91C172630D4FFBF23B490A344B5F02E1B5C".equals(ASMHelper.getCheckSumForMethod(reader, "updateSignalStateForColor"))) noChecksumMatch = true;
			if(!"9E1892DBBFCF2F45E93C83CB568D975B39FB176A".equals(ASMHelper.getCheckSumForMethod(reader, "readNearbyPipesSignal"))) noChecksumMatch = true;
			
			if(noChecksumMatch) {
				new UnsupportedOperationException("This LP version isn't compatible with the installed BC version.").printStackTrace();
				FMLCommonHandler.instance().exitJava(1, true);
			}
		}
		
		node.signature += "Llogisticspipes/proxy/buildcraft/BCPipeWireHooks$PipeClassReceiveSignal;";
		node.interfaces.add("logisticspipes/proxy/buildcraft/BCPipeWireHooks$PipeClassReceiveSignal");
		
		for(MethodNode m:node.methods) {
			if(m.name.equals("handlePipeEvent")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
						super.visitTryCatchBlock(start, end, handler, type);
						Label l3 = new Label();
						super.visitLabel(l3);
						super.visitLineNumber(89, l3);
						super.visitVarInsn(Opcodes.ALOAD, 1);
						super.visitVarInsn(Opcodes.ALOAD, 0);
						super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/buildcraft/BCEventHandler", "handle", "(Lbuildcraft/transport/pipes/events/PipeEvent;Lbuildcraft/transport/Pipe;)V");
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("isWireConnectedTo")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					public void visitCode() {
						super.visitCode();
						Label l0 = new Label();
						super.visitLabel(l0);
						super.visitVarInsn(Opcodes.ALOAD, 0);
						super.visitVarInsn(Opcodes.ALOAD, 1);
						super.visitVarInsn(Opcodes.ALOAD, 2);
						super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/buildcraft/BCPipeWireHooks", "isWireConnectedTo", "(Lbuildcraft/transport/Pipe;Lnet/minecraft/tileentity/TileEntity;Lbuildcraft/api/transport/PipeWire;)Z");
						Label l1 = new Label();
						super.visitJumpInsn(Opcodes.IFEQ, l1);
						super.visitInsn(Opcodes.ICONST_1);
						super.visitInsn(Opcodes.IRETURN);
						super.visitLabel(l1);
					}
					
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("updateSignalStateForColor")) {
				InsnList list = m.instructions;
				AbstractInsnNode current = list.getLast();
				while(!(current instanceof InsnNode && ((InsnNode)current).getOpcode() == Opcodes.RETURN)) {
					current = current.getPrevious();
				}
				current = current.getPrevious();
				InsnList toAdd = new InsnList();
				toAdd.add(new VarInsnNode(Opcodes.ALOAD, 0));
				toAdd.add(new VarInsnNode(Opcodes.ALOAD, 1));
				toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "logisticspipes/proxy/buildcraft/BCPipeWireHooks", "updateSignalStateForColor", "(Lbuildcraft/transport/Pipe;Lbuildcraft/api/transport/PipeWire;)V"));
				toAdd.add(getLabelNode(new Label()));
				m.instructions.insert(current, toAdd);
			}
			if(m.name.equals("readNearbyPipesSignal")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					boolean handled = false;
					@Override
					public void visitInsn(int opcode) {
						if(!handled) {
							handled = true;
							super.visitVarInsn(Opcodes.ALOAD, 0);
							super.visitVarInsn(Opcodes.ALOAD, 1);
							super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/buildcraft/BCPipeWireHooks", "readNearbyPipesSignal_Pre", "(Lbuildcraft/transport/Pipe;Lbuildcraft/api/transport/PipeWire;)Z");
						} else {
							super.visitInsn(opcode);
						}
					}
				};

				m.accept(mv);
				
				InsnList list = mv.instructions;
				AbstractInsnNode current = list.getLast();
				while(!(current instanceof InsnNode && ((InsnNode)current).getOpcode() == Opcodes.RETURN)) {
					current = current.getPrevious();
				}
				current = current.getPrevious();
				InsnList toAdd = new InsnList();
				toAdd.add(new VarInsnNode(Opcodes.ALOAD, 0));
				toAdd.add(new VarInsnNode(Opcodes.ALOAD, 1));
				toAdd.add(new VarInsnNode(Opcodes.ILOAD, 2));
				toAdd.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "logisticspipes/proxy/buildcraft/BCPipeWireHooks", "readNearbyPipesSignal_Post", "(Lbuildcraft/transport/Pipe;Lbuildcraft/api/transport/PipeWire;Z)V"));
				toAdd.add(getLabelNode(new Label()));
				mv.instructions.insert(current, toAdd);
				
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("receiveSignal")) {
				m.access ^= Opcodes.ACC_PRIVATE;
				m.access |= Opcodes.ACC_PUBLIC;
			}
		}
		
		{
			MethodVisitor mv = node.visitMethod(Opcodes.ACC_PUBLIC, "triggerInternalUpdateScheduled", "()V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(604, l0);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitInsn(Opcodes.ICONST_1);
			mv.visitFieldInsn(Opcodes.PUTFIELD, "buildcraft/transport/Pipe", "internalUpdateScheduled", "Z");
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLineNumber(605, l1);
			mv.visitInsn(Opcodes.RETURN);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLocalVariable("this", "Lbuildcraft/transport/Pipe;", "Lbuildcraft/transport/Pipe<TT;>;", l0, l2, 0);
			mv.visitMaxs(2, 1);
			mv.visitEnd();
		}
		
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		node.accept(writer);
		return writer.toByteArray();
	}
	
    protected static LabelNode getLabelNode(final Label l) {
        if (!(l.info instanceof LabelNode)) {
            l.info = new LabelNode();
        }
        return (LabelNode) l.info;
    }
}
