package logisticspipes.asm.bc;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFLE;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SIPUSH;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassPipeTransportItemsHandler {
	/*
	public void injectItem(TravelingItem item, ForgeDirection inputOrientation) {
		if (item.isCorrupted())
			// Safe guard - if for any reason the item is corrupted at this
			// stage, avoid adding it to the pipe to avoid further exceptions.
			return;

		item.reset();
		item.input = inputOrientation;

		readjustSpeed(item);
		readjustPosition(item);


		if (!container.worldObj.isRemote) {
			item.output = resolveDestination(item);
		}

		if (container.pipe instanceof IPipeTransportItemsHook) {
			((IPipeTransportItemsHook) container.pipe).entityEntered(item, inputOrientation);
		}

		PipeEventItem.Entered event = new PipeEventItem.Entered(item);
		container.pipe.handlePipeEvent(event);
		if (event.cancelled)
			return;

		items.scheduleAdd(item);

		if (!container.worldObj.isRemote) {
			sendItemPacket(item);

			int stackCount = 0;
			int numItems = 0;
			for (TravelingItem travellingItem : items) {
				if(!(travellingItem instanceof IRoutedItem)) {
					ItemStack stack = travellingItem.getItemStack();
					if (stack != null && stack.stackSize > 0) {
						numItems += stack.stackSize;
						stackCount++;
					}
				}
			}

			if (stackCount > BuildCraftTransport.groupItemsTrigger) {
				groupEntities();
			}

			stackCount = 0;
			numItems = 0;
			for (TravelingItem travellingItem : items) {
				if(!(travellingItem instanceof IRoutedItem)) {
					ItemStack stack = travellingItem.getItemStack();
					if (stack != null && stack.stackSize > 0) {
						numItems += stack.stackSize;
						stackCount++;
					}
				}
			}

			if (stackCount > MAX_PIPE_STACKS) {
				BCLog.logger.log(Level.WARNING, String.format("Pipe exploded at %d,%d,%d because it had too many stacks: %d", container.xCoord, container.yCoord, container.zCoord, items.size()));
				destroyPipe();
				return;
			}

			if (numItems > MAX_PIPE_ITEMS) {
				BCLog.logger.log(Level.WARNING, String.format("Pipe exploded at %d,%d,%d because it had too many items: %d", container.xCoord, container.yCoord, container.zCoord, numItems));
				destroyPipe();
			}
		}
	}
	*/

	private static void insertNewInjectItemMethod(ClassNode node) {
		MethodVisitor mv = node.visitMethod(ACC_PUBLIC, "injectItem", "(Lbuildcraft/transport/TravelingItem;Lnet/minecraftforge/common/ForgeDirection;)V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitLineNumber(102, l0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "buildcraft/transport/TravelingItem", "isCorrupted", "()Z");
		Label l1 = new Label();
		mv.visitJumpInsn(IFEQ, l1);
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitLineNumber(105, l2);
		mv.visitInsn(RETURN);
		mv.visitLabel(l1);
		mv.visitLineNumber(107, l1);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "buildcraft/transport/TravelingItem", "reset", "()V");
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitLineNumber(108, l3);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitFieldInsn(PUTFIELD, "buildcraft/transport/TravelingItem", "input", "Lnet/minecraftforge/common/ForgeDirection;");
		Label l4 = new Label();
		mv.visitLabel(l4);
		mv.visitLineNumber(110, l4);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "buildcraft/transport/PipeTransportItems", "readjustSpeed", "(Lbuildcraft/transport/TravelingItem;)V");
		Label l5 = new Label();
		mv.visitLabel(l5);
		mv.visitLineNumber(111, l5);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESPECIAL, "buildcraft/transport/PipeTransportItems", "readjustPosition", "(Lbuildcraft/transport/TravelingItem;)V");
		Label l6 = new Label();
		mv.visitLabel(l6);
		mv.visitLineNumber(114, l6);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/TileGenericPipe", "worldObj", "Lnet/minecraft/world/World;");
		mv.visitFieldInsn(GETFIELD, "net/minecraft/world/World", "isRemote", "Z");
		Label l7 = new Label();
		mv.visitJumpInsn(IFNE, l7);
		Label l8 = new Label();
		mv.visitLabel(l8);
		mv.visitLineNumber(115, l8);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "buildcraft/transport/PipeTransportItems", "resolveDestination", "(Lbuildcraft/transport/TravelingItem;)Lnet/minecraftforge/common/ForgeDirection;");
		mv.visitFieldInsn(PUTFIELD, "buildcraft/transport/TravelingItem", "output", "Lnet/minecraftforge/common/ForgeDirection;");
		mv.visitLabel(l7);
		mv.visitLineNumber(118, l7);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/TileGenericPipe", "pipe", "Lbuildcraft/transport/Pipe;");
		mv.visitTypeInsn(INSTANCEOF, "buildcraft/transport/IPipeTransportItemsHook");
		Label l9 = new Label();
		mv.visitJumpInsn(IFEQ, l9);
		Label l10 = new Label();
		mv.visitLabel(l10);
		mv.visitLineNumber(119, l10);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/TileGenericPipe", "pipe", "Lbuildcraft/transport/Pipe;");
		mv.visitTypeInsn(CHECKCAST, "buildcraft/transport/IPipeTransportItemsHook");
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEINTERFACE, "buildcraft/transport/IPipeTransportItemsHook", "entityEntered", "(Lbuildcraft/transport/TravelingItem;Lnet/minecraftforge/common/ForgeDirection;)V");
		mv.visitLabel(l9);
		mv.visitLineNumber(122, l9);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitTypeInsn(NEW, "buildcraft/transport/pipes/events/PipeEventItem$Entered");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESPECIAL, "buildcraft/transport/pipes/events/PipeEventItem$Entered", "<init>", "(Lbuildcraft/transport/TravelingItem;)V");
		mv.visitVarInsn(ASTORE, 3);
		Label l11 = new Label();
		mv.visitLabel(l11);
		mv.visitLineNumber(123, l11);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/TileGenericPipe", "pipe", "Lbuildcraft/transport/Pipe;");
		mv.visitVarInsn(ALOAD, 3);
		mv.visitMethodInsn(INVOKEVIRTUAL, "buildcraft/transport/Pipe", "handlePipeEvent", "(Lbuildcraft/transport/pipes/events/PipeEvent;)V");
		Label l12 = new Label();
		mv.visitLabel(l12);
		mv.visitLineNumber(124, l12);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/pipes/events/PipeEventItem$Entered", "cancelled", "Z");
		Label l13 = new Label();
		mv.visitJumpInsn(IFEQ, l13);
		Label l14 = new Label();
		mv.visitLabel(l14);
		mv.visitLineNumber(125, l14);
		mv.visitInsn(RETURN);
		mv.visitLabel(l13);
		mv.visitLineNumber(127, l13);
		mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"buildcraft/transport/pipes/events/PipeEventItem$Entered"}, 0, null);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "items", "Lbuildcraft/transport/TravelerSet;");
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "buildcraft/transport/TravelerSet", "scheduleAdd", "(Lbuildcraft/transport/TravelingItem;)V");
		Label l15 = new Label();
		mv.visitLabel(l15);
		mv.visitLineNumber(129, l15);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/TileGenericPipe", "worldObj", "Lnet/minecraft/world/World;");
		mv.visitFieldInsn(GETFIELD, "net/minecraft/world/World", "isRemote", "Z");
		Label l16 = new Label();
		mv.visitJumpInsn(IFNE, l16);
		Label l17 = new Label();
		mv.visitLabel(l17);
		mv.visitLineNumber(130, l17);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitMethodInsn(INVOKESPECIAL, "buildcraft/transport/PipeTransportItems", "sendItemPacket", "(Lbuildcraft/transport/TravelingItem;)V");
		Label l18 = new Label();
		mv.visitLabel(l18);
		mv.visitLineNumber(132, l18);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 4);
		Label l19 = new Label();
		mv.visitLabel(l19);
		mv.visitLineNumber(133, l19);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 5);
		Label l20 = new Label();
		mv.visitLabel(l20);
		mv.visitLineNumber(134, l20);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "items", "Lbuildcraft/transport/TravelerSet;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "buildcraft/transport/TravelerSet", "iterator", "()Ljava/util/Iterator;");
		mv.visitVarInsn(ASTORE, 7);
		Label l21 = new Label();
		mv.visitJumpInsn(GOTO, l21);
		Label l22 = new Label();
		mv.visitLabel(l22);
		mv.visitFrame(Opcodes.F_FULL, 8, new Object[] {"buildcraft/transport/PipeTransportItems", "buildcraft/transport/TravelingItem", "net/minecraftforge/common/ForgeDirection", "buildcraft/transport/pipes/events/PipeEventItem$Entered", Opcodes.INTEGER, Opcodes.INTEGER, Opcodes.TOP, "java/util/Iterator"}, 0, new Object[] {});
		mv.visitVarInsn(ALOAD, 7);
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
		mv.visitTypeInsn(CHECKCAST, "buildcraft/transport/TravelingItem");
		mv.visitVarInsn(ASTORE, 6);
		Label l23 = new Label();
		mv.visitLabel(l23);
		mv.visitLineNumber(135, l23);
		mv.visitVarInsn(ALOAD, 6);
		mv.visitTypeInsn(INSTANCEOF, "logisticspipes/logisticspipes/IRoutedItem");
		mv.visitJumpInsn(IFNE, l21);
		Label l24 = new Label();
		mv.visitLabel(l24);
		mv.visitLineNumber(136, l24);
		mv.visitVarInsn(ALOAD, 6);
		mv.visitMethodInsn(INVOKEVIRTUAL, "buildcraft/transport/TravelingItem", "getItemStack", "()Lnet/minecraft/item/ItemStack;");
		mv.visitVarInsn(ASTORE, 8);
		Label l25 = new Label();
		mv.visitLabel(l25);
		mv.visitLineNumber(137, l25);
		mv.visitVarInsn(ALOAD, 8);
		mv.visitJumpInsn(IFNULL, l21);
		mv.visitVarInsn(ALOAD, 8);
		mv.visitFieldInsn(GETFIELD, "net/minecraft/item/ItemStack", "stackSize", "I");
		mv.visitJumpInsn(IFLE, l21);
		Label l26 = new Label();
		mv.visitLabel(l26);
		mv.visitLineNumber(138, l26);
		mv.visitVarInsn(ILOAD, 5);
		mv.visitVarInsn(ALOAD, 8);
		mv.visitFieldInsn(GETFIELD, "net/minecraft/item/ItemStack", "stackSize", "I");
		mv.visitInsn(IADD);
		mv.visitVarInsn(ISTORE, 5);
		Label l27 = new Label();
		mv.visitLabel(l27);
		mv.visitLineNumber(139, l27);
		mv.visitIincInsn(4, 1);
		mv.visitLabel(l21);
		mv.visitLineNumber(134, l21);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(ALOAD, 7);
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
		mv.visitJumpInsn(IFNE, l22);
		Label l28 = new Label();
		mv.visitLabel(l28);
		mv.visitLineNumber(144, l28);
		mv.visitVarInsn(ILOAD, 4);
		mv.visitFieldInsn(GETSTATIC, "buildcraft/BuildCraftTransport", "groupItemsTrigger", "I");
		Label l29 = new Label();
		mv.visitJumpInsn(IF_ICMPLE, l29);
		Label l30 = new Label();
		mv.visitLabel(l30);
		mv.visitLineNumber(145, l30);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKEVIRTUAL, "buildcraft/transport/PipeTransportItems", "groupEntities", "()V");
		mv.visitLabel(l29);
		mv.visitLineNumber(148, l29);
		mv.visitFrame(Opcodes.F_FULL, 6, new Object[] {"buildcraft/transport/PipeTransportItems", "buildcraft/transport/TravelingItem", "net/minecraftforge/common/ForgeDirection", "buildcraft/transport/pipes/events/PipeEventItem$Entered", Opcodes.INTEGER, Opcodes.INTEGER}, 0, new Object[] {});
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 4);
		Label l31 = new Label();
		mv.visitLabel(l31);
		mv.visitLineNumber(149, l31);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 5);
		Label l32 = new Label();
		mv.visitLabel(l32);
		mv.visitLineNumber(150, l32);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "items", "Lbuildcraft/transport/TravelerSet;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "buildcraft/transport/TravelerSet", "iterator", "()Ljava/util/Iterator;");
		mv.visitVarInsn(ASTORE, 7);
		Label l33 = new Label();
		mv.visitJumpInsn(GOTO, l33);
		Label l34 = new Label();
		mv.visitLabel(l34);
		mv.visitFrame(Opcodes.F_FULL, 8, new Object[] {"buildcraft/transport/PipeTransportItems", "buildcraft/transport/TravelingItem", "net/minecraftforge/common/ForgeDirection", "buildcraft/transport/pipes/events/PipeEventItem$Entered", Opcodes.INTEGER, Opcodes.INTEGER, Opcodes.TOP, "java/util/Iterator"}, 0, new Object[] {});
		mv.visitVarInsn(ALOAD, 7);
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
		mv.visitTypeInsn(CHECKCAST, "buildcraft/transport/TravelingItem");
		mv.visitVarInsn(ASTORE, 6);
		Label l35 = new Label();
		mv.visitLabel(l35);
		mv.visitLineNumber(151, l35);
		mv.visitVarInsn(ALOAD, 6);
		mv.visitTypeInsn(INSTANCEOF, "logisticspipes/logisticspipes/IRoutedItem");
		mv.visitJumpInsn(IFNE, l33);
		Label l36 = new Label();
		mv.visitLabel(l36);
		mv.visitLineNumber(152, l36);
		mv.visitVarInsn(ALOAD, 6);
		mv.visitMethodInsn(INVOKEVIRTUAL, "buildcraft/transport/TravelingItem", "getItemStack", "()Lnet/minecraft/item/ItemStack;");
		mv.visitVarInsn(ASTORE, 8);
		Label l37 = new Label();
		mv.visitLabel(l37);
		mv.visitLineNumber(153, l37);
		mv.visitVarInsn(ALOAD, 8);
		mv.visitJumpInsn(IFNULL, l33);
		mv.visitVarInsn(ALOAD, 8);
		mv.visitFieldInsn(GETFIELD, "net/minecraft/item/ItemStack", "stackSize", "I");
		mv.visitJumpInsn(IFLE, l33);
		Label l38 = new Label();
		mv.visitLabel(l38);
		mv.visitLineNumber(154, l38);
		mv.visitVarInsn(ILOAD, 5);
		mv.visitVarInsn(ALOAD, 8);
		mv.visitFieldInsn(GETFIELD, "net/minecraft/item/ItemStack", "stackSize", "I");
		mv.visitInsn(IADD);
		mv.visitVarInsn(ISTORE, 5);
		Label l39 = new Label();
		mv.visitLabel(l39);
		mv.visitLineNumber(155, l39);
		mv.visitIincInsn(4, 1);
		mv.visitLabel(l33);
		mv.visitLineNumber(150, l33);
		mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mv.visitVarInsn(ALOAD, 7);
		mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
		mv.visitJumpInsn(IFNE, l34);
		Label l40 = new Label();
		mv.visitLabel(l40);
		mv.visitLineNumber(160, l40);
		mv.visitVarInsn(ILOAD, 4);
		mv.visitIntInsn(BIPUSH, 64);
		Label l41 = new Label();
		mv.visitJumpInsn(IF_ICMPLE, l41);
		Label l42 = new Label();
		mv.visitLabel(l42);
		mv.visitLineNumber(161, l42);
		mv.visitFieldInsn(GETSTATIC, "buildcraft/core/utils/BCLog", "logger", "Ljava/util/logging/Logger;");
		mv.visitFieldInsn(GETSTATIC, "java/util/logging/Level", "WARNING", "Ljava/util/logging/Level;");
		mv.visitLdcInsn("Pipe exploded at %d,%d,%d because it had too many stacks: %d");
		mv.visitInsn(ICONST_4);
		mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/TileGenericPipe", "xCoord", "I");
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
		mv.visitInsn(AASTORE);
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/TileGenericPipe", "yCoord", "I");
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
		mv.visitInsn(AASTORE);
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_2);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/TileGenericPipe", "zCoord", "I");
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
		mv.visitInsn(AASTORE);
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_3);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "items", "Lbuildcraft/transport/TravelerSet;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "buildcraft/transport/TravelerSet", "size", "()I");
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
		mv.visitInsn(AASTORE);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "format", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/logging/Logger", "log", "(Ljava/util/logging/Level;Ljava/lang/String;)V");
		Label l43 = new Label();
		mv.visitLabel(l43);
		mv.visitLineNumber(162, l43);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "buildcraft/transport/PipeTransportItems", "destroyPipe", "()V");
		Label l44 = new Label();
		mv.visitLabel(l44);
		mv.visitLineNumber(163, l44);
		mv.visitInsn(RETURN);
		mv.visitLabel(l41);
		mv.visitLineNumber(166, l41);
		mv.visitFrame(Opcodes.F_FULL, 6, new Object[] {"buildcraft/transport/PipeTransportItems", "buildcraft/transport/TravelingItem", "net/minecraftforge/common/ForgeDirection", "buildcraft/transport/pipes/events/PipeEventItem$Entered", Opcodes.INTEGER, Opcodes.INTEGER}, 0, new Object[] {});
		mv.visitVarInsn(ILOAD, 5);
		mv.visitIntInsn(SIPUSH, 1024);
		mv.visitJumpInsn(IF_ICMPLE, l16);
		Label l45 = new Label();
		mv.visitLabel(l45);
		mv.visitLineNumber(167, l45);
		mv.visitFieldInsn(GETSTATIC, "buildcraft/core/utils/BCLog", "logger", "Ljava/util/logging/Logger;");
		mv.visitFieldInsn(GETSTATIC, "java/util/logging/Level", "WARNING", "Ljava/util/logging/Level;");
		mv.visitLdcInsn("Pipe exploded at %d,%d,%d because it had too many items: %d");
		mv.visitInsn(ICONST_4);
		mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/TileGenericPipe", "xCoord", "I");
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
		mv.visitInsn(AASTORE);
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/TileGenericPipe", "yCoord", "I");
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
		mv.visitInsn(AASTORE);
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_2);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/PipeTransportItems", "container", "Lbuildcraft/transport/TileGenericPipe;");
		mv.visitFieldInsn(GETFIELD, "buildcraft/transport/TileGenericPipe", "zCoord", "I");
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
		mv.visitInsn(AASTORE);
		mv.visitInsn(DUP);
		mv.visitInsn(ICONST_3);
		mv.visitVarInsn(ILOAD, 5);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
		mv.visitInsn(AASTORE);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "format", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/logging/Logger", "log", "(Ljava/util/logging/Level;Ljava/lang/String;)V");
		Label l46 = new Label();
		mv.visitLabel(l46);
		mv.visitLineNumber(168, l46);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "buildcraft/transport/PipeTransportItems", "destroyPipe", "()V");
		mv.visitLabel(l16);
		mv.visitLineNumber(171, l16);
		mv.visitFrame(Opcodes.F_CHOP,2, null, 0, null);
		mv.visitInsn(RETURN);
		Label l47 = new Label();
		mv.visitLabel(l47);
		mv.visitLocalVariable("this", "Lbuildcraft/transport/PipeTransportItems;", null, l0, l47, 0);
		mv.visitLocalVariable("item", "Lbuildcraft/transport/TravelingItem;", null, l0, l47, 1);
		mv.visitLocalVariable("inputOrientation", "Lnet/minecraftforge/common/ForgeDirection;", null, l0, l47, 2);
		mv.visitLocalVariable("event", "Lbuildcraft/transport/pipes/events/PipeEventItem$Entered;", null, l11, l47, 3);
		mv.visitLocalVariable("stackCount", "I", null, l19, l16, 4);
		mv.visitLocalVariable("numItems", "I", null, l20, l16, 5);
		mv.visitLocalVariable("travellingItem", "Lbuildcraft/transport/TravelingItem;", null, l23, l21, 6);
		mv.visitLocalVariable("stack", "Lnet/minecraft/item/ItemStack;", null, l25, l21, 8);
		mv.visitLocalVariable("travellingItem", "Lbuildcraft/transport/TravelingItem;", null, l35, l33, 6);
		mv.visitLocalVariable("stack", "Lnet/minecraft/item/ItemStack;", null, l37, l33, 8);
		mv.visitMaxs(7, 9);
		mv.visitEnd();
	}
	
	public static byte[] handlePipeTransportItems(byte[] bytes) {
		final ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(node, 0);
		Iterator<MethodNode> iter = node.methods.iterator();
		while(iter.hasNext()) {
			MethodNode m = iter.next();
			if(m.name.equals("readFromNBT")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					// logisticspipes.asm.LogisticsASMHookClass.clearInvalidFluidContainers(items);
					@Override
					public void visitInsn(int opcode) {
						if(opcode == Opcodes.RETURN) {
							AbstractInsnNode instruction_1 = null;
							AbstractInsnNode instruction_2 = null;
							instructions.remove(instruction_2 = instructions.getLast());
							instructions.remove(instruction_1 = instructions.getLast());
							Label l = new Label();
							this.visitLabel(l);
							this.visitVarInsn(Opcodes.ALOAD, 0);
							this.visitFieldInsn(Opcodes.GETFIELD, "buildcraft/transport/PipeTransportItems", "items", "Lbuildcraft/transport/TravelerSet;");
							this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/LogisticsASMHookClass", "clearInvalidFluidContainers", "(Lbuildcraft/transport/TravelerSet;)V");
							instructions.add(instruction_1);
							instructions.add(instruction_2);							
						}
						super.visitInsn(opcode);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("injectItem")) {
				iter.remove();
			}
		}
		insertNewInjectItemMethod(node);
		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}
}
