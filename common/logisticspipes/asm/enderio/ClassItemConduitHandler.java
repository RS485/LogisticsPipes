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

public class ClassItemConduitHandler {
	public static byte[] handleItemConduitClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);

		boolean noChecksumMatch = false;
		final String sumHandleEvent = ASMHelper.getCheckSumForMethod(reader, "getExternalInventory", "(Lnet/minecraftforge/common/util/EnumFacing;)Lnet/minecraft/inventory/IInventory;");
		if (!"D37FEE8DB16279D8D85139FEED862D542C5E4E52".equals(sumHandleEvent) && !"5E0C7EC3AAF256A5689F65D79092C1599804C9ED".equals(sumHandleEvent)) {
			noChecksumMatch = true;
		}
		if (noChecksumMatch) {
			System.out.println("getExternalInventory: " + sumHandleEvent);
			new UnsupportedOperationException("This LP version isn't compatible with the installed EnderIO version.").printStackTrace();
			if(LPConstants.DEBUG) {
				FMLCommonHandler.instance().exitJava(1, true);
			} else {
				EnderIOHooks.disableHooks();
				return bytes;
			}
		}

		node.methods.stream()
			.filter(m -> m.name.equals("getExternalInventory") && m.desc.equals("(Lnet/minecraftforge/common/util/EnumFacing;)Lnet/minecraft/inventory/IInventory;"))
			.forEach(m -> {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					int nullcount = 0;
					@Override
					public void visitInsn(int opcode) {
						if(opcode == Opcodes.ACONST_NULL) {
							nullcount++;
							if(nullcount == 2) {
								this.visitVarInsn(Opcodes.ALOAD, 0);
								this.visitVarInsn(Opcodes.ALOAD, 4);
								this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/enderio/EnderIOHooks", "handleGetExternalInventory", "(Lcrazypants/enderio/conduit/item/ItemConduit;Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/inventory/IInventory;", false);
								return;
							}
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
