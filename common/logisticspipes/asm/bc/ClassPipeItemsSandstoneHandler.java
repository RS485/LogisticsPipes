package logisticspipes.asm.bc;

import logisticspipes.LPConstants;
import logisticspipes.asm.util.ASMHelper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import cpw.mods.fml.common.FMLCommonHandler;

public class ClassPipeItemsSandstoneHandler {

	public static byte[] handleClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);
		
		if(LPConstants.DEBUG) {
			boolean noChecksumMatch = false;
		
			if(!"E14DAC659737F7B1C4AE0426D63537710F407B91".equals(ASMHelper.getCheckSumForMethod(reader, "ignoreConnectionOverrides"))) noChecksumMatch = true;
			
			System.out.println(ASMHelper.getCheckSumForMethod(reader, "ignoreConnectionOverrides"));
			
			if(noChecksumMatch) {
				new UnsupportedOperationException("This LP version isn't compatible with the installed BC version.").printStackTrace();
				FMLCommonHandler.instance().exitJava(1, true);
			}
		}
		
		for(MethodNode m:node.methods) {
			if(m.name.equals("ignoreConnectionOverrides")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0]));
				mv.visitCode();
				Label l0 = new Label();
				mv.visitLabel(l0);
				mv.visitLineNumber(51, l0);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitFieldInsn(Opcodes.GETFIELD, "buildcraft/transport/pipes/PipeItemsSandstone", "container", "Lbuildcraft/transport/TileGenericPipe;");
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "buildcraft/transport/TileGenericPipe", "getAdjacentTile", "(Lnet/minecraftforge/common/util/ForgeDirection;)Lnet/minecraft/tileentity/TileEntity;", false);
				mv.visitTypeInsn(Opcodes.INSTANCEOF, "logisticspipes/pipes/basic/LogisticsTileGenericPipe");
				Label l1 = new Label();
				mv.visitJumpInsn(Opcodes.IFEQ, l1);
				mv.visitInsn(Opcodes.ICONST_0);
				Label l2 = new Label();
				mv.visitJumpInsn(Opcodes.GOTO, l2);
				mv.visitLabel(l1);
				mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
				mv.visitInsn(Opcodes.ICONST_1);
				mv.visitLabel(l2);
				mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
				mv.visitInsn(Opcodes.IRETURN);
				Label l3 = new Label();
				mv.visitLabel(l3);
				mv.visitLocalVariable("this", "Lbuildcraft/transport/pipes/PipeItemsSandstone;", null, l0, l3, 0);
				mv.visitLocalVariable("with", "Lnet/minecraftforge/common/util/ForgeDirection;", null, l0, l3, 1);
				mv.visitMaxs(2, 2);
				mv.visitEnd();
				node.methods.set(node.methods.indexOf(m), mv);
			}
		}
		
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		node.accept(writer);
		return writer.toByteArray();
	}
}
