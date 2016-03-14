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

public class ClassAbstractLiquidConduitHandler {
	public static byte[] handleAbstractLiquidConduitClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);

		boolean noChecksumMatch = false;
		final String sumHandleEvent = ASMHelper.getCheckSumForMethod(reader, "getExternalFluidHandler", "(Lnet/minecraft/world/IBlockAccess;Lcom/enderio/core/common/util/BlockCoord;)Lnet/minecraftforge/fluids/IFluidHandler;");
		final String sumHandleEvent2 = ASMHelper.getCheckSumForMethod(reader, "getTankContainer", "(Lcom/enderio/core/common/util/BlockCoord;)Lnet/minecraftforge/fluids/IFluidHandler;");
		if (!"C6ECE28DB9AE0DD1F5D930AD65F165A934095516".equals(sumHandleEvent)) {
			noChecksumMatch = true;
		}
		if (!"2CBF4212D8F2601DF99811C560E5479AB7A4B965".equals(sumHandleEvent2)) {
			noChecksumMatch = true;
		}
		if (noChecksumMatch) {
			System.out.println("getExternalFluidHandler: " + sumHandleEvent);
			System.out.println("getTankContainer: " + sumHandleEvent2);
			new UnsupportedOperationException("This LP version isn't compatible with the installed EnderIO version.").printStackTrace();
			if(LPConstants.DEBUG) {
				FMLCommonHandler.instance().exitJava(1, true);
			} else {
				EnderIOHooks.disableHooks();
				return bytes;
			}
		}

		for (MethodNode m : node.methods) {
			if (m.name.equals("getExternalFluidHandler") && m.desc.equals("(Lnet/minecraft/world/IBlockAccess;Lcom/enderio/core/common/util/BlockCoord;)Lnet/minecraftforge/fluids/IFluidHandler;")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					@Override
					public void visitCode() {
						super.visitCode();
						Label l0 = new Label();
						this.visitLabel(l0);
						this.visitVarInsn(Opcodes.ALOAD, 0);
						this.visitVarInsn(Opcodes.ALOAD, 1);
						this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/enderio/EnderIOHooks", "doesHandleGetExternalFluidHandler", "(Lnet/minecraft/world/IBlockAccess;Lcom/enderio/core/common/util/BlockCoord;)Z", false);
						Label l1 = new Label();
						this.visitJumpInsn(Opcodes.IFEQ, l1);
						Label l2 = new Label();
						this.visitLabel(l2);
						this.visitVarInsn(Opcodes.ALOAD, 0);
						this.visitVarInsn(Opcodes.ALOAD, 1);
						this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/enderio/EnderIOHooks", "handleGetExternalFluidHandler", "(Lnet/minecraft/world/IBlockAccess;Lcom/enderio/core/common/util/BlockCoord;)Lnet/minecraftforge/fluids/IFluidHandler;", false);
						this.visitInsn(Opcodes.ARETURN);
						this.visitLabel(l1);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if (m.name.equals("getTankContainer") && m.desc.equals("(Lcom/enderio/core/common/util/BlockCoord;)Lnet/minecraftforge/fluids/IFluidHandler;")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					@Override
					public void visitCode() {
						super.visitCode();
						Label l0 = new Label();
						this.visitLabel(l0);
						this.visitVarInsn(Opcodes.ALOAD, 0);
						this.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "crazypants/enderio/conduit/liquid/AbstractLiquidConduit", "getBundle", "()Lcrazypants/enderio/conduit/IConduitBundle;", false);
						this.visitMethodInsn(Opcodes.INVOKEINTERFACE, "crazypants/enderio/conduit/IConduitBundle", "getWorld", "()Lnet/minecraft/world/World;", true);
						this.visitVarInsn(Opcodes.ALOAD, 1);
						this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/enderio/EnderIOHooks", "doesHandleGetTankContainer", "(Lnet/minecraft/world/World;Lcom/enderio/core/common/util/BlockCoord;)Z", false);
						Label l1 = new Label();
						this.visitJumpInsn(Opcodes.IFEQ, l1);
						Label l2 = new Label();
						this.visitLabel(l2);
						this.visitVarInsn(Opcodes.ALOAD, 0);
						this.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "crazypants/enderio/conduit/liquid/AbstractLiquidConduit", "getBundle", "()Lcrazypants/enderio/conduit/IConduitBundle;", false);
						this.visitMethodInsn(Opcodes.INVOKEINTERFACE, "crazypants/enderio/conduit/IConduitBundle", "getWorld", "()Lnet/minecraft/world/World;", true);
						this.visitVarInsn(Opcodes.ALOAD, 1);
						this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/enderio/EnderIOHooks", "handleGetTankContainer", "(Lnet/minecraft/world/World;Lcom/enderio/core/common/util/BlockCoord;)Lnet/minecraftforge/fluids/IFluidHandler;", false);
						this.visitInsn(Opcodes.ARETURN);
						this.visitLabel(l1);
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
