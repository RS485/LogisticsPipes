package logisticspipes.asm.bc;

import java.util.Iterator;

import logisticspipes.asm.util.ASMHelper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import cpw.mods.fml.common.FMLCommonHandler;

public class ClassPipeTransportItemsHandler {
	
	private static void insertNewInjectItemMethod(ClassNode node) {
		MethodVisitor mv = node.visitMethod(Opcodes.ACC_PUBLIC, "injectItem", "(Lbuildcraft/transport/TravelingItem;Lnet/minecraftforge/common/util/ForgeDirection;)V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(157, l0);
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitVarInsn(Opcodes.ALOAD, 1);
		mv.visitVarInsn(Opcodes.ALOAD, 2);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/bc/InjectItemHook", "handleInjectItem", "(Lbuildcraft/transport/PipeTransportItems;Lbuildcraft/transport/TravelingItem;Lnet/minecraftforge/common/util/ForgeDirection;)V");
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitLineNumber(158, l1);
		mv.visitInsn(Opcodes.RETURN);
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitLocalVariable("this", "Lbuildcraft/transport/PipeTransportItems;", null, l0, l2, 0);
		mv.visitLocalVariable("item", "Lbuildcraft/transport/TravelingItem;", null, l0, l2, 1);
		mv.visitLocalVariable("inputOrientation", "Lnet/minecraftforge/common/util/ForgeDirection;", null, l0, l2, 2);
		mv.visitMaxs(3, 3);
		mv.visitEnd();
	}

	public static byte[] handlePipeTransportItems(byte[] bytes) {
		final ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(node, 0);

		boolean noChecksumMatch = false;

		String sumInjectItem = ASMHelper.getCheckSumForMethod(reader, "injectItem");

		// if(!"956E67FF1103A53C970F22669CF70624DE3D4CF8".equals(sumInjectItem)
		// && !"E7C1D1F202E00935B89B35E7F2A46B97E1FDC6F7".equals(sumInjectItem))
		if (!"E7C1D1F202E00935B89B35E7F2A46B97E1FDC6F7".equals(ASMHelper.getCheckSumForMethod(reader, "injectItem"))) noChecksumMatch = true;

		if (noChecksumMatch) {
			System.out.println("injectItem:" + ASMHelper.getCheckSumForMethod(reader, "injectItem"));
			new UnsupportedOperationException("This LP version isn't compatible with the installed BC version.").printStackTrace();
			FMLCommonHandler.instance().exitJava(1, true);
		}

		Iterator<MethodNode> iter = node.methods.iterator();
		while (iter.hasNext()) {
			MethodNode m = iter.next();
			if (m.name.equals("injectItem")) {
				iter.remove();
			}
		}
		insertNewInjectItemMethod(node);
		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}
}
