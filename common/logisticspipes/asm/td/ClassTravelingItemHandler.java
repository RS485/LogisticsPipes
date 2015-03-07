package logisticspipes.asm.td;

import logisticspipes.asm.util.ASMHelper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import cpw.mods.fml.common.FMLCommonHandler;

public class ClassTravelingItemHandler {

	public static byte[] handleTravelingItemClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);

		boolean noChecksumMatch = false;
		String sumHandleEvent1 = ASMHelper.getCheckSumForMethod(reader, "toNBT", "(Lnet/minecraft/nbt/NBTTagCompound;)V");
		String sumHandleEvent2 = ASMHelper.getCheckSumForMethod(reader, "<init>", "(Lnet/minecraft/nbt/NBTTagCompound;)V");
		String sumHandleEvent3 = ASMHelper.getCheckSumForMethod(reader, "writePacket", "(Lcofh/core/network/PacketCoFHBase;)V");
		if(!"32EE544498CBD0F3FF0B9ACB92D2B3A08BDAB8D7".equals(sumHandleEvent1) && !"0D19759D08F6CDE773BDF4B6B6A99AADD8DE765F".equals(sumHandleEvent1)) noChecksumMatch = true;
		if(!"1B8743B2E8AB2804A8605B8165E30F75AF531B3B".equals(sumHandleEvent2) && !"8FD4C845CA8BCA11DB79968D61C47BC3789F1410".equals(sumHandleEvent2)) noChecksumMatch = true;
		if(!"C7204D0FD96CE512F34AAA64466CE67CAD477514".equals(sumHandleEvent3)) noChecksumMatch = true;
		if(noChecksumMatch) {
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

		for(MethodNode m:node.methods) {
			if(m.name.equals("toNBT") && m.desc.equals("(Lnet/minecraft/nbt/NBTTagCompound;)V")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					public void visitCode() {
						super.visitCode();
						Label l = new Label();
						this.visitLabel(l);
						this.visitVarInsn(Opcodes.ALOAD, 0);
						this.visitVarInsn(Opcodes.ALOAD, 1);
						this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/td/ThermalDynamicsHooks", "travelingItemToNBT", "(Lcofh/thermaldynamics/ducts/item/TravelingItem;Lnet/minecraft/nbt/NBTTagCompound;)V", false);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("<init>") && m.desc.equals("(Lnet/minecraft/nbt/NBTTagCompound;)V")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					public void visitInsn(int opcode) {
						if(opcode == Opcodes.RETURN) {
							this.visitVarInsn(Opcodes.ALOAD, 0);
							this.visitVarInsn(Opcodes.ALOAD, 1);
							this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/td/ThermalDynamicsHooks", "travelingItemNBTContructor", "(Lcofh/thermaldynamics/ducts/item/TravelingItem;Lnet/minecraft/nbt/NBTTagCompound;)V", false);
							Label l = new Label();
							this.visitLabel(l);
						}
						super.visitInsn(opcode);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("writePacket") && m.desc.equals("(Lcofh/core/network/PacketCoFHBase;)V")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
						if(opcode == Opcodes.INVOKEVIRTUAL && "cofh/core/network/PacketCoFHBase".equals(owner) && "addItemStack".equals(name) && "(Lnet/minecraft/item/ItemStack;)Lcofh/core/network/PacketCoFHBase;".equals(desc)) {
							this.visitVarInsn(Opcodes.ALOAD, 0);
							this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/td/ThermalDynamicsHooks", "handleItemSendPacket", "(Lnet/minecraft/item/ItemStack;Lcofh/thermaldynamics/ducts/item/TravelingItem;)Lnet/minecraft/item/ItemStack;", false);
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
