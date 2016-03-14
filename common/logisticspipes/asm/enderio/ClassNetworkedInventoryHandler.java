package logisticspipes.asm.enderio;

import net.minecraftforge.fml.common.FMLCommonHandler;
import logisticspipes.LPConstants;
import logisticspipes.asm.util.ASMHelper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassNetworkedInventoryHandler {
	public static byte[] handleNetworkedInventoryClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);

		boolean noChecksumMatch = false;
		final String sumHandleEvent = ASMHelper.getCheckSumForMethod(reader, "updateInventory", "()V");
		if (!"FD19F73A342FDEE61BDF3F3D48EB0729EB9AF099".equals(sumHandleEvent) && !"B7EAC876ABADD173C105F27251D883F5C01DD78B".equals(sumHandleEvent)) {
			noChecksumMatch = true;
		}
		if (noChecksumMatch) {
			System.out.println("updateInventory: " + sumHandleEvent);
			new UnsupportedOperationException("This LP version isn't compatible with the installed EnderIO version.").printStackTrace();
			if(LPConstants.DEBUG) {
				FMLCommonHandler.instance().exitJava(1, true);
			} else {
				EnderIOHooks.disableHooks();
				return bytes;
			}
		}

		node.methods.stream()
				.filter(m -> m.name.equals("updateInventory") && m.desc.equals("()V"))
				.forEach(m -> {
					MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
						@Override
						public void visitInsn(int opcode) {
							if(opcode == Opcodes.RETURN) {
								this.visitVarInsn(Opcodes.ALOAD, 0);
								this.visitVarInsn(Opcodes.ALOAD, 0);
								this.visitFieldInsn(Opcodes.GETFIELD, "crazypants/enderio/conduit/item/NetworkedInventory", "inv", "Lnet/minecraft/inventory/ISidedInventory;");
								this.visitVarInsn(Opcodes.ALOAD, 1);
								this.visitVarInsn(Opcodes.ALOAD, 0);
								this.visitFieldInsn(Opcodes.GETFIELD, "crazypants/enderio/conduit/item/NetworkedInventory", "conDir", "Lnet/minecraftforge/common/util/EnumFacing;");
								this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/enderio/EnderIOHooks", "handleUpdateInventoryNetworkedInventory", "(Lnet/minecraft/inventory/ISidedInventory;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraftforge/common/util/EnumFacing;)Lnet/minecraft/inventory/ISidedInventory;", false);
								this.visitFieldInsn(Opcodes.PUTFIELD, "crazypants/enderio/conduit/item/NetworkedInventory", "inv", "Lnet/minecraft/inventory/ISidedInventory;");
								this.visitLabel(new Label());
							}
							super.visitInsn(opcode);
						}
					};
					m.accept(mv);
					node.methods.set(node.methods.indexOf(m), mv);
				});

		ClassWriter writer = new ClassWriter(0/*ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES*/);
		node.accept(writer);
		return writer.toByteArray();
	}
}
