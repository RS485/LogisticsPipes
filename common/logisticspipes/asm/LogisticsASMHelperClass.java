package logisticspipes.asm;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class LogisticsASMHelperClass {
	
	//if(!(container.getTile(o) instanceof IPipeEntry || container.getTile(o) instanceof TileGenericPipe) && item.getItemStack() != null && item.getItemStack().getItem() instanceof logisticspipes.interfaces.IItemAdvancedExistance && !((logisticspipes.interfaces.IItemAdvancedExistance)item.getItemStack().getItem()).canExistInNormalInventory(item.getItemStack())) return false;
	public static void visitCanRecivePipeObject(MethodVisitor mv) {
		Label l0 = new Label();
		mv.visitLabel(l0);
		//mv.visitLineNumber(213, l0); //No line number because it isn't original code.
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "buildcraft/transport/TileGenericPipe", "getTile", "(Lnet/minecraftforge/common/ForgeDirection;)Lnet/minecraft/tileentity/TileEntity;");
		mv.visitTypeInsn(INSTANCEOF, "buildcraft/api/transport/IPipeEntry");
		Label l1 = new Label();
		mv.visitJumpInsn(IFNE, l1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "buildcraft/transport/TileGenericPipe", "getTile", "(Lnet/minecraftforge/common/ForgeDirection;)Lnet/minecraft/tileentity/TileEntity;");
		mv.visitTypeInsn(INSTANCEOF, "buildcraft/transport/TileGenericPipe");
		mv.visitJumpInsn(IFNE, l1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEINTERFACE, "buildcraft/api/transport/IPipedItem", "getItemStack", "()Lnet/minecraft/item/ItemStack;");
		mv.visitJumpInsn(IFNULL, l1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEINTERFACE, "buildcraft/api/transport/IPipedItem", "getItemStack", "()Lnet/minecraft/item/ItemStack;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/item/ItemStack", "getItem", "()Lnet/minecraft/item/Item;");
		mv.visitTypeInsn(INSTANCEOF, "logisticspipes/interfaces/IItemAdvancedExistance");
		mv.visitJumpInsn(IFEQ, l1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEINTERFACE, "buildcraft/api/transport/IPipedItem", "getItemStack", "()Lnet/minecraft/item/ItemStack;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/item/ItemStack", "getItem", "()Lnet/minecraft/item/Item;");
		mv.visitTypeInsn(CHECKCAST, "logisticspipes/interfaces/IItemAdvancedExistance");
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEINTERFACE, "buildcraft/api/transport/IPipedItem", "getItemStack", "()Lnet/minecraft/item/ItemStack;");
		mv.visitMethodInsn(INVOKEINTERFACE, "logisticspipes/interfaces/IItemAdvancedExistance", "canExistInNormalInventory", "(Lnet/minecraft/item/ItemStack;)Z");
		mv.visitJumpInsn(IFNE, l1);
		mv.visitInsn(ICONST_0);
		mv.visitInsn(IRETURN);
		mv.visitLabel(l1);
	}
}
