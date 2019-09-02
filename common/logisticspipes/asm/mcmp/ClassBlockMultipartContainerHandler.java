package logisticspipes.asm.mcmp;

import net.minecraftforge.fml.common.FMLCommonHandler;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import logisticspipes.asm.util.ASMHelper;

public class ClassBlockMultipartContainerHandler {

	public static byte[] handleClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);

		boolean noChecksumMatch = false;
		final String sumHandleEvent = ASMHelper.getCheckSumForMethod(reader, "getTile", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Ljava/util/Optional;");
		if (!"C755146A0F8F81CCE6637174E17B6D48EC967D59".equals(sumHandleEvent) && !"570C0380BCA4C6B6BBB32CE0D400B8DE13E6D800".equals(sumHandleEvent)) {
			noChecksumMatch = true;
		}
		if (noChecksumMatch) {
			System.out.println("getTile: " + sumHandleEvent);
			new UnsupportedOperationException("This LP version isn't compatible with the installed MCMultipart version.").printStackTrace();
			FMLCommonHandler.instance().exitJava(1, true);
		}

		node.methods.stream()
				.filter(m -> m.name.equals("getTile") && m.desc.equals("(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Ljava/util/Optional;"))
				.forEach(m -> {
					MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

						boolean inserted = false;

						@Override
						public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
							if (!inserted && opcode == Opcodes.INVOKEINTERFACE && owner.equals("net/minecraft/world/IBlockAccess") && (name.equals("getTileEntity") || name.equals("func_175625_s")) && desc.equals("(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;") && itf) {
								super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/mcmp/MCMPHooks", "getTileEntityForBlockClass", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;", false);
								inserted = true;
							} else {
								super.visitMethodInsn(opcode, owner, name, desc, itf);
							}
						}
					};
					m.accept(mv);
					node.methods.set(node.methods.indexOf(m), mv);
				});

		ClassWriter writer = new ClassWriter(0/* ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES */);
		node.accept(writer);
		return writer.toByteArray();
	}
}
