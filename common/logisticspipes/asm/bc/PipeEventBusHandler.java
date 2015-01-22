package logisticspipes.asm.bc;

import logisticspipes.asm.util.ASMHelper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import cpw.mods.fml.common.FMLCommonHandler;

public class PipeEventBusHandler {

	public static byte[] handleBCPipeEventBusClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);
		
		boolean noChecksumMatch = false;

		if(!"3A490EE1D1DE3A386D528D5CADBFA7F536DBC708".equals(ASMHelper.getCheckSumForMethod(reader, "handleEvent"))) noChecksumMatch = true;
		
		if(noChecksumMatch) {
			System.out.println("handleEvent:" + ASMHelper.getCheckSumForMethod(reader, "handleEvent"));
			new UnsupportedOperationException("This LP version isn't compatible with the installed BC version.").printStackTrace();
			FMLCommonHandler.instance().exitJava(1, true);
		}
		
		for(MethodNode m:node.methods) {
			if(m.name.equals("handleEvent")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
						super.visitTryCatchBlock(start, end, handler, type);
						Label l3 = new Label();
						super.visitLabel(l3);
						super.visitLineNumber(89, l3);
						super.visitVarInsn(Opcodes.ALOAD, 2);
						super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/buildcraft/BCEventHandler", "handle", "(Lbuildcraft/transport/pipes/events/PipeEvent;)V", false);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
		}
		
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		node.accept(writer);
		return writer.toByteArray();
	}
}
