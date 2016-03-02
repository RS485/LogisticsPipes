package logisticspipes.asm.td;

import logisticspipes.asm.util.ASMHelper;

import cpw.mods.fml.common.FMLCommonHandler;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassTravelingItemHandler {

	public static byte[] handleTravelingItemClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);

		boolean noChecksumMatch = false;
		String sumHandleEvent1 = ASMHelper.getCheckSumForMethod(reader, "toNBT", "(Lnet/minecraft/nbt/NBTTagCompound;)V");
		String sumHandleEvent2 = ASMHelper.getCheckSumForMethod(reader, "<init>", "(Lnet/minecraft/nbt/NBTTagCompound;)V");
		String sumHandleEvent3 = ASMHelper.getCheckSumForMethod(reader, "writePacket", "(Lcofh/core/network/PacketCoFHBase;)V");
		if (!"512A30E22A9C24032AAE7CE51271339A4F68D344".equals(sumHandleEvent1) && !"2166AB8FF647A90701787CCCFB4CD1C065BE640D".equals(sumHandleEvent1)) {
			noChecksumMatch = true;
		}
		if (!"77E58B3B01AF559869F2FCDBD478567E649906B5".equals(sumHandleEvent2) && !"29BF9FE63C7627E88BD6025F2FCED59B8156046C".equals(sumHandleEvent2) && !"780999E7DC3A3FCC579C1ED4802878440693ADB3".equals(sumHandleEvent2) && !"585608749C198ED7ED74E000587EDB734D107795".equals(sumHandleEvent2)) {
			noChecksumMatch = true;
		}
		if (!"65D7F6A4716118725E3D23F71667B18A8C2ED13D".equals(sumHandleEvent3)) {
			noChecksumMatch = true;
		}
		if (noChecksumMatch) {
			System.out.println("toNBT: " + sumHandleEvent1);
			System.out.println("<init>: " + sumHandleEvent2);
			System.out.println("writePacket: " + sumHandleEvent3);
			new UnsupportedOperationException("This LP version isn't compatible with the installed TD version.").printStackTrace();
			FMLCommonHandler.instance().exitJava(1, true);
		}

		{
			FieldVisitor fv = node.visitField(Opcodes.ACC_PUBLIC, "lpRoutingInformation", "Ljava/lang/Object;", null, null);
			fv.visitEnd();
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
			if (m.name.equals("writePacket") && m.desc.equals("(Lcofh/core/network/PacketCoFHBase;)V")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					@Override
					public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
						if (opcode == Opcodes.INVOKEVIRTUAL && "cofh/core/network/PacketCoFHBase".equals(owner) && "addItemStack".equals(name) && "(Lnet/minecraft/item/ItemStack;)Lcofh/core/network/PacketCoFHBase;".equals(desc)) {
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
