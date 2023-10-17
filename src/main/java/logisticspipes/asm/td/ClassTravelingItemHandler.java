package logisticspipes.asm.td;

import net.minecraftforge.fml.common.FMLCommonHandler;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import logisticspipes.asm.util.ASMHelper;

public class ClassTravelingItemHandler {

	public static byte[] handleTravelingItemClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);

		boolean noChecksumMatch = false;
		String sumHandleEvent1 = ASMHelper.getCheckSumForMethod(reader, "toNBT", "(Lnet/minecraft/nbt/NBTTagCompound;)V");
		String sumHandleEvent2 = ASMHelper.getCheckSumForMethod(reader, "<init>", "(Lnet/minecraft/nbt/NBTTagCompound;)V");
		String sumHandleEvent3 = ASMHelper.getCheckSumForMethod(reader, "writePacket", "(Lcofh/core/network/PacketBase;)V");
		if (!"512A30E22A9C24032AAE7CE51271339A4F68D344".equals(sumHandleEvent1) &&
				!"2166AB8FF647A90701787CCCFB4CD1C065BE640D".equals(sumHandleEvent1)) {
			noChecksumMatch = true;
		}
		if (!"E8DD6BEB9676D079E6C97FF8206283E766147E45".equals(sumHandleEvent2) &&
				!"BB4178915C3A10EBAC5158B8742592388E47B181".equals(sumHandleEvent2) &&
				!"0151577E2B8CF9BF2D528A309D5AE919308ABE03".equals(sumHandleEvent2) &&
				!"768681BA4851C581B15713ED3FF9CA6530C24931".equals(sumHandleEvent2)) {
			noChecksumMatch = true;
		}
		if (!"98A853547FA1771D6D7D63E0458960405DFEE092".equals(sumHandleEvent3)) {
			noChecksumMatch = true;
		}
		if (noChecksumMatch) {
			System.out.println("toNBT: " + sumHandleEvent1);
			System.out.println("<init>: " + sumHandleEvent2);
			System.out.println("writePacket: " + sumHandleEvent3);
			new UnsupportedOperationException("This LP version isn't compatible with the installed TD version.").printStackTrace();
			FMLCommonHandler.instance().exitJava(1, true);
		}

		node.interfaces.add("logisticspipes/asm/td/ILPTravelingItemInfo");
		{
			FieldVisitor fv = node.visitField(Opcodes.ACC_PUBLIC, "lpRoutingInformation", "Ljava/lang/Object;", null, null);
			fv.visitEnd();
		}
		{
			MethodVisitor mv;
			mv = node.visitMethod(Opcodes.ACC_PUBLIC, "getLPRoutingInfoAddition", "()Ljava/lang/Object;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(23, l0);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, "cofh/thermaldynamics/duct/item/TravelingItem", "lpRoutingInformation", "Ljava/lang/Object;");
			mv.visitInsn(Opcodes.ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "Lcofh/thermaldynamics/duct/item/TravelingItem;", null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			MethodVisitor mv;
			mv = node.visitMethod(Opcodes.ACC_PUBLIC, "setLPRoutingInfoAddition", "(Ljava/lang/Object;)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(28, l0);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitFieldInsn(Opcodes.PUTFIELD, "cofh/thermaldynamics/duct/item/TravelingItem", "lpRoutingInformation", "Ljava/lang/Object;");
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLineNumber(29, l1);
			mv.visitInsn(Opcodes.RETURN);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLocalVariable("this", "Lcofh/thermaldynamics/duct/item/TravelingItem;", null, l0, l2, 0);
			mv.visitLocalVariable("info", "Ljava/lang/Object;", null, l0, l2, 1);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		for (MethodNode m : node.methods) {
			if (m.name.equals("toNBT") && m.desc.equals("(Lnet/minecraft/nbt/NBTTagCompound;)V")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					@Override
					public void visitCode() {
						super.visitCode();
						Label l = new Label();
						visitLabel(l);
						visitVarInsn(Opcodes.ALOAD, 0);
						visitVarInsn(Opcodes.ALOAD, 1);
						this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/td/ThermalDynamicsHooks", "travelingItemToNBT", "(Lcofh/thermaldynamics/duct/item/TravelingItem;Lnet/minecraft/nbt/NBTTagCompound;)V", false);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if (m.name.equals("<init>") && m.desc.equals("(Lnet/minecraft/nbt/NBTTagCompound;)V")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					@Override
					public void visitInsn(int opcode) {
						if (opcode == Opcodes.RETURN) {
							visitVarInsn(Opcodes.ALOAD, 0);
							visitVarInsn(Opcodes.ALOAD, 1);
							this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/td/ThermalDynamicsHooks", "travelingItemNBTContructor", "(Lcofh/thermaldynamics/duct/item/TravelingItem;Lnet/minecraft/nbt/NBTTagCompound;)V", false);
							Label l = new Label();
							visitLabel(l);
						}
						super.visitInsn(opcode);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if (m.name.equals("writePacket") && m.desc.equals("(Lcofh/core/network/PacketBase;)V")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					@Override
					public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
						if (opcode == Opcodes.INVOKEVIRTUAL && "cofh/core/network/PacketBase".equals(owner) && "addItemStack".equals(name) && "(Lnet/minecraft/item/ItemStack;)Lcofh/core/network/PacketBase;".equals(desc)) {
							visitVarInsn(Opcodes.ALOAD, 0);
							this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/td/ThermalDynamicsHooks", "handleItemSendPacket", "(Lnet/minecraft/item/ItemStack;Lcofh/thermaldynamics/duct/item/TravelingItem;)Lnet/minecraft/item/ItemStack;", false);
						}
						super.visitMethodInsn(opcode, owner, name, desc, itf);
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
