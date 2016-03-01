package logisticspipes.asm.td;

import logisticspipes.asm.util.ASMHelper;

import cpw.mods.fml.common.FMLCommonHandler;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassTileMultiBlockHandler {

	public static byte[] handleTileMultiBlockClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);

		boolean noChecksumMatch = false;
		String sumHandleEvent1 = ASMHelper.getCheckSumForMethod(reader, "getAdjTileEntitySafe", "(I)Lnet/minecraft/tileentity/TileEntity;");
		String sumHandleEvent2 = ASMHelper.getCheckSumForMethod(reader, "getConnectedSide", "(B)Lcofh/thermaldynamics/multiblock/IMultiBlock;");
		if (!"A77B2F423A03F5546E6B9C8CE5090F830BAE6295".equals(sumHandleEvent1) && !"8CCBC71E3BA6E6392E4579531996A627D3B018E9".equals(sumHandleEvent1) && !"35A53BFC01EED7CC33BBD5F8DB1F715A515C62CE".equals(sumHandleEvent1) && !"201FC2B21D5B2ED1F560EB23FE9AE4F4F86BE8E4".equals(sumHandleEvent1)) {
			noChecksumMatch = true;
		}
		if (!"D33508DD271ECD3ABD6A144DA28A1012F2A45CA4".equals(sumHandleEvent2) && !"35A53BFC01EED7CC33BBD5F8DB1F715A515C62CE".equals(sumHandleEvent2)) {
			noChecksumMatch = true;
		}
		if (noChecksumMatch) {
			System.out.println("getAdjTileEntitySafe: " + sumHandleEvent1);
			System.out.println("getConnectedSide: " + sumHandleEvent2);
			new UnsupportedOperationException("This LP version isn't compatible with the installed TD version.").printStackTrace();
			FMLCommonHandler.instance().exitJava(1, true);
		}

		for (FieldNode f : node.fields) {
			if (f.name.equals("duct")) {
				f.access |= Opcodes.ACC_PUBLIC;
			}
		}

		for (MethodNode m : node.methods) {
			if (m.name.equals("getAdjTileEntitySafe") && m.desc.equals("(I)Lnet/minecraft/tileentity/TileEntity;")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					@Override
					public void visitInsn(int opcode) {
						if (opcode == Opcodes.ARETURN) {
							visitVarInsn(Opcodes.ILOAD, 1);
							visitVarInsn(Opcodes.ALOAD, 0);
							this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/td/ThermalDynamicsHooks", "checkGetTileEntity", "(Lnet/minecraft/tileentity/TileEntity;ILcofh/thermaldynamics/block/TileTDBase;)Lnet/minecraft/tileentity/TileEntity;", false);
						}
						super.visitInsn(opcode);
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
