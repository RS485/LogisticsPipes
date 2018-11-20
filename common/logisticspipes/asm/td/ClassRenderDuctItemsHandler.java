package logisticspipes.asm.td;

import net.minecraft.client.renderer.GlStateManager;

import logisticspipes.asm.util.ASMHelper;

import net.minecraftforge.fml.common.FMLCommonHandler;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassRenderDuctItemsHandler {

	public static byte[] handleRenderDuctItemsClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);

		boolean noChecksumMatch = false;
		final String sumHandleEvent = ASMHelper.getCheckSumForMethod(reader, "renderTravelingItems", "(Ljava/util/Iterator;Lcofh/thermaldynamics/duct/item/DuctUnitItem;Lnet/minecraft/world/World;DDDF)V");
		if (!"2A56C07E15F612A425E4B4D8C16DEB7545947688".equals(sumHandleEvent) && !"TODO".equals(sumHandleEvent)) {
			noChecksumMatch = true;
		}
		if (noChecksumMatch) {
			System.out.println("renderTravelingItems: " + sumHandleEvent);
			new UnsupportedOperationException("This LP version isn't compatible with the installed TD version.").printStackTrace();
			FMLCommonHandler.instance().exitJava(1, true);
		}

		node.methods.stream().filter(m -> m.name.equals("renderTravelingItems") && m.desc.equals("(Ljava/util/Iterator;Lcofh/thermaldynamics/duct/item/DuctUnitItem;Lnet/minecraft/world/World;DDDF)V"))
				.forEach(m -> {
					MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions
							.toArray(new String[0])) {

						@Override
						public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
							super.visitMethodInsn(opcode, owner, name, desc, itf);
							if (owner.equals("net/minecraft/client/renderer/GlStateManager") && (name.equals("scale") || name.equals("func_179152_a")) && desc
									.equals("(FFF)V")) {
								Label l = new Label();
								visitLabel(l);
								visitVarInsn(Opcodes.ALOAD, 11);
								this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/td/ThermalDynamicsHooks", "renderItemTransportBox", "(Lcofh/thermaldynamics/duct/item/TravelingItem;)V", false);
							}
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
