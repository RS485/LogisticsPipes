package logisticspipes.asm;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IRETURN;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class LogisticsASMHelperClass {
	
	// if(!LogisitcsASMHookClass.continueCodeForCanReceivePipeObjects(o, item, container)) return false;
	public static void visitCanRecivePipeObject(MethodVisitor mv) {
		Label l0 = new Label();
		mv.visitLabel(l0);
		//mv.visitLineNumber(215, l0); //No line number because this in nonstandard code.
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
		mv.visitMethodInsn(INVOKESTATIC, "logisticspipes/asm/LogisitcsASMHookClass", "continueCodeForCanReceivePipeObjects", "(Lnet/minecraftforge/common/ForgeDirection;Lbuildcraft/api/transport/IPipedItem;Lbuildcraft/transport/TileGenericPipe;)Z");
		Label l1 = new Label();
		mv.visitJumpInsn(IFNE, l1);
		mv.visitInsn(ICONST_0);
		mv.visitInsn(IRETURN);
		mv.visitLabel(l1);
	}
}
