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
		if(!"093EBF1B662D30236BFAA7ACB99A28F8892129D8".equals(sumHandleEvent1) && !"A08A65438C985EC67BFF98B2799825CCB68A2DD4".equals(sumHandleEvent1)) noChecksumMatch = true;
		if(!"A445D208BF7DAC96EE833588CB2DE73A641689EF".equals(sumHandleEvent2) && !"D75E0B54783F85E1794826B2B26F700254CF90C6".equals(sumHandleEvent2)) noChecksumMatch = true;
		if(noChecksumMatch) {
			System.out.println("toNBT: " + sumHandleEvent1);
			System.out.println("<init>: " + sumHandleEvent2);
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
			
		}
		
		ClassWriter writer = new ClassWriter(0/*ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES*/);
		node.accept(writer);
		return writer.toByteArray();
	}
	
}
