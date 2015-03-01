package logisticspipes.asm.td;

import logisticspipes.asm.util.ASMHelper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import cpw.mods.fml.common.FMLCommonHandler;

public class ClassRenderDuctItemsHandler {

	public static byte[] handleRenderDuctItemsClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);
		
		boolean noChecksumMatch = false;
		final String sumHandleEvent = ASMHelper.getCheckSumForMethod(reader, "renderTravelingItems", "(Ljava/util/Iterator;Lcofh/thermaldynamics/ducts/item/TileItemDuct;Lnet/minecraft/world/World;DDDF)V");
		if(!"3153BA5332E6AF00B5A7A4FA3C3CBEDB61D8C70A".equals(sumHandleEvent) && !"B29A29E5275940E837FDE72E41D315CAC4A2D5A0".equals(sumHandleEvent)) noChecksumMatch = true;
		if(noChecksumMatch) {
			System.out.println("renderTravelingItems: " + sumHandleEvent);
			new UnsupportedOperationException("This LP version isn't compatible with the installed TD version.").printStackTrace();
			FMLCommonHandler.instance().exitJava(1, true);
		}
		
		for(MethodNode m:node.methods) {
			if(m.name.equals("renderTravelingItems") && m.desc.equals("(Ljava/util/Iterator;Lcofh/thermaldynamics/ducts/item/TileItemDuct;Lnet/minecraft/world/World;DDDF)V")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					
					@Override
					public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
						super.visitMethodInsn(opcode, owner, name, desc, itf);
						if(owner.equals("org/lwjgl/opengl/GL11") && name.equals("glScalef") && desc.equals("(FFF)V")) {
							Label l = new Label();
							this.visitLabel(l);
							if("3153BA5332E6AF00B5A7A4FA3C3CBEDB61D8C70A".equals(sumHandleEvent)) {
								this.visitVarInsn(Opcodes.ALOAD, 12);
							} else if("B29A29E5275940E837FDE72E41D315CAC4A2D5A0".equals(sumHandleEvent)) {
								this.visitVarInsn(Opcodes.ALOAD, 11);
							} else {
								throw new UnsupportedOperationException();
							}
							this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/td/ThermalDynamicsHooks", "renderItemTransportBox", "(Lcofh/thermaldynamics/ducts/item/TravelingItem;)V", false);
						}
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
