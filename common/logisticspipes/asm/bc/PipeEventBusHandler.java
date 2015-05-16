package logisticspipes.asm.bc;

import java.util.Arrays;
import java.util.HashSet;

import cpw.mods.fml.common.FMLCommonHandler;
import logisticspipes.asm.util.ASMHelper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class PipeEventBusHandler {

	public static byte[] handleBCPipeEventBusClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);

		String sumHandleEvent = ASMHelper.getCheckSumForMethod(reader, "handleEvent");

		// is being executed only once, so there is no need for a static variable
		HashSet<String> checkSums = new HashSet<String>(Arrays.asList(new String[] {
				"3A490EE1D1DE3A386D528D5CADBFA7F536DBC708",
				"33411F8DE31E738A237712C1E0AD2013C8CAB253", // BC 6.4.1 in dev env
		}));
		
		if (!checkSums.contains(sumHandleEvent)) {
			System.out.println("handleEvent: " + sumHandleEvent);
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
