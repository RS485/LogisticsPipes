package logisticspipes.asm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import logisticspipes.Configs;
import logisticspipes.LogisticsPipes;
import logisticspipes.asm.bc.ClassPipeTransportItemsHandler;
import logisticspipes.utils.ModStatusHelper;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import cpw.mods.fml.common.versioning.VersionParser;
import cpw.mods.fml.common.versioning.VersionRange;
import cpw.mods.fml.relauncher.Side;

public class LogisticsClassTransformer implements IClassTransformer {

	public List<String> interfacesToClearA = new ArrayList<String>();
	public List<String> interfacesToClearB = new ArrayList<String>();
	private LaunchClassLoader cl = (LaunchClassLoader)LogisticsClassTransformer.class.getClassLoader();
	private Field negativeResourceCache;
	private Field invalidClasses;

	public static LogisticsClassTransformer instance;
	
	public LogisticsClassTransformer() {
		instance = this;
		try {
			negativeResourceCache = LaunchClassLoader.class.getDeclaredField("negativeResourceCache");
			negativeResourceCache.setAccessible(true);
		} catch(Exception e) {
			//e.printStackTrace();
		}
		try {
			invalidClasses = LaunchClassLoader.class.getDeclaredField("invalidClasses");
			invalidClasses.setAccessible(true);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		try {
			Thread thread = Thread.currentThread();
			if(thread.getName().equals("Minecraft main thread") || thread.getName().equals("main") || thread.getName().equals("Server thread")) { //Only clear when called from the main thread to avoid ConcurrentModificationException on start
				clearNegativeInterfaceCache();
			}
			if(bytes == null) return null;
			if(name.equals("buildcraft.transport.PipeTransportItems")) {
				return ClassPipeTransportItemsHandler.handlePipeTransportItems(bytes);
			}
			if(name.equals("buildcraft.transport.Pipe")) {
				return handleBCPipeClass(bytes);
			}
			if(name.equals("thermalexpansion.part.conduit.ConduitBase")) {
				Configs.load();
				if(Configs.TE_PIPE_SUPPORT) {
					return handleTEConduitBase(bytes);
				}
			}
			if(name.equals("thermalexpansion.part.conduit.item.ConduitItem")) {
				Configs.load();
				if(Configs.TE_PIPE_SUPPORT) {
					return handleTEConduitItem(bytes);
				}
			}
			if(name.equals("thermalexpansion.part.conduit.item.ItemRoute")) {
				Configs.load();
				if(Configs.TE_PIPE_SUPPORT) {
					return handleTEItemRoute(bytes);
				}
			}
			if(name.equals("thermalexpansion.part.conduit.item.TravelingItem")) {
				Configs.load();
				if(Configs.TE_PIPE_SUPPORT) {
					return handleTETravelingItem(bytes);
				}
			}
			if(name.equals("net.minecraft.crash.CrashReport")) {
				return handleCrashReportClass(bytes);
			}
			if(name.equals("dan200.computercraft.core.lua.LuaJLuaMachine")) {
				return handleCCLuaJLuaMachine(bytes);
			}
			if(!name.startsWith("logisticspipes.")) {
				return bytes;
			}
			return handleLPTransformation(bytes);
		} catch(Exception e) {
			if(LogisticsPipes.DEBUG) { //For better Debugging
				e.printStackTrace();
				return bytes;
			}
			throw new RuntimeException(e);
		}
	}

	public void clearNegativeInterfaceCache() {
		//Remove previously not found Classes to Fix ClassNotFound Exceptions for Interfaces.
		//TODO remove in future version when everybody starts using a ClassTransformer system for Interfaces.
		if(negativeResourceCache != null) {
			if(!interfacesToClearA.isEmpty()) {
				handleField(negativeResourceCache, interfacesToClearA);
			}
		}
		if(invalidClasses != null) {
			if(!interfacesToClearB.isEmpty()) {
				handleField(invalidClasses, interfacesToClearB);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void handleField(Field field, List<String> toClear) {
		try {
			Set<String> set = (Set<String>) field.get(cl);
			Iterator<String> it = toClear.iterator();
			while(it.hasNext()) {
				String content = it.next();
				if(set.contains(content)) {
					set.remove(content);
					it.remove();
				}
			}
		} catch(Exception e) {
			if(LogisticsPipes.DEBUG) { //For better Debugging
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private byte[] handleLPTransformation(byte[] bytes) {
		final ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(node, 0);
		boolean changed = false;
		if(node.visibleAnnotations != null) {
			for(AnnotationNode a:node.visibleAnnotations) {
				if(a.desc.equals("Llogisticspipes/asm/ModDependentInterface;")) {
					if(a.values.size() == 4 && a.values.get(0).equals("modId") && a.values.get(2).equals("interfacePath")) {
						List<String> modId = (List<String>) a.values.get(1);
						List<String> interfacePath = (List<String>) a.values.get(3);
						if(modId.size() != interfacePath.size()) {
							throw new RuntimeException("The Arrays have to be of the same size.");
						}
						for(int i=0;i<modId.size();i++) {
							if(!ModStatusHelper.isModLoaded(modId.get(i))) {
								interfacesToClearA.add(interfacePath.get(i));
								interfacesToClearB.add(interfacePath.get(i));
								for(String inter:node.interfaces) {
									if(inter.replace("/", ".").equals(interfacePath.get(i))) {
										node.interfaces.remove(inter);
										changed = true;
										break;
									}
								}
							}
						}
					} else {
						throw new UnsupportedOperationException("Can't parse the annotations correctly");
					}
				}
			}
		}
		List<MethodNode> methodsToRemove = new ArrayList<MethodNode>();
		for(MethodNode m:node.methods) {
			if(m.visibleAnnotations != null) {
				for(AnnotationNode a:m.visibleAnnotations) {
					if(a.desc.equals("Llogisticspipes/asm/ModDependentMethod;")) {
						if(a.values.size() == 2 && a.values.get(0).equals("modId")) {
							String modId = a.values.get(1).toString();
							if(!ModStatusHelper.isModLoaded(modId)) {
								methodsToRemove.add(m);
								break;
							}
						} else {
							throw new UnsupportedOperationException("Can't parse the annotation correctly");
						}
					} else if(a.desc.equals("Llogisticspipes/asm/ClientSideOnlyMethodContent;")) {
						if(FMLCommonHandler.instance().getSide().equals(Side.SERVER)) {
							m.instructions.clear();
							m.localVariables.clear();
							m.tryCatchBlocks.clear();
							m.visitCode();
							Label l0 = new Label();
							m.visitLabel(l0);
							m.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/LogisticsASMHookClass", "callingClearedMethod", "()V");
							Label l1 = new Label();
							m.visitLabel(l1);
							m.visitInsn(Opcodes.RETURN);
							Label l2 = new Label();
							m.visitLabel(l2);
							m.visitLocalVariable("this", "Llogisticspipes/network/packets/DummyPacket;", null, l0, l2, 0);
							m.visitLocalVariable("player", "Lnet/minecraft/entity/player/EntityPlayer;", null, l0, l2, 1);
							m.visitMaxs(0, 2);
							m.visitEnd();
							changed = true;
							break;
						}
					} else if(a.desc.equals("Llogisticspipes/asm/ModDependentMethodName;")) {
						if(a.values.size() == 6 && a.values.get(0).equals("modId") && a.values.get(2).equals("newName") && a.values.get(4).equals("version")) {
							String modId = a.values.get(1).toString();
							final String newName = a.values.get(3).toString();
							final String version = a.values.get(5).toString();
							boolean loaded = ModStatusHelper.isModLoaded(modId);
							if(loaded && !version.equals("")) {
								ModContainer mod = Loader.instance().getIndexedModList().get(modId);
								if(mod != null) {
									VersionRange range = VersionParser.parseRange(version);
									ArtifactVersion artifactVersion = new DefaultArtifactVersion("Version", mod.getVersion());
									loaded = range.containsVersion(artifactVersion);
								} else {
									loaded = false;
								}
							}
							if(loaded) {
								final String oldName = m.name;
								m.name = newName;
								MethodNode newM = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
									@Override
									public void visitMethodInsn(int opcode, String owner, String name, String desc) {
										if(name.equals(oldName) && owner.equals(node.superName)) {
											super.visitMethodInsn(opcode, owner, newName, desc);
										} else {
											super.visitMethodInsn(opcode, owner, name, desc);
										}
									}
								};
								m.accept(newM);
								node.methods.set(node.methods.indexOf(m), newM);
								changed = true;
								break;
							}
						} else {
							throw new UnsupportedOperationException("Can't parse the annotation correctly");
						}
					}
				}
			}
		}
		for(MethodNode m:methodsToRemove) {
			node.methods.remove(m);
		}
		List<FieldNode> fieldsToRemove = new ArrayList<FieldNode>();
		for(FieldNode f:node.fields) {
			if(f.visibleAnnotations != null) {
				for(AnnotationNode a:f.visibleAnnotations) {
					if(a.desc.equals("Llogisticspipes/asm/ModDependentField;")) {
						if(a.values.size() == 2 && a.values.get(0).equals("modId")) {
							String modId = a.values.get(1).toString();
							if(!ModStatusHelper.isModLoaded(modId)) {
								fieldsToRemove.add(f);
								break;
							}
						} else {
							throw new UnsupportedOperationException("Can't parse the annotation correctly");
						}
					}
				}
			}
		}
		for(FieldNode f:fieldsToRemove) {
			node.fields.remove(f);
		}
		if(!changed && methodsToRemove.isEmpty() && fieldsToRemove.isEmpty()) {
			return bytes;
		}
		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}
	
	private enum STATE {SEARCHING, INSERTING, DONE};

	private byte[] handleTEConduitBase(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);
		for(MethodNode m:node.methods) {
			if(m.name.equals("onNeighborChanged")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					private STATE state = STATE.SEARCHING;
					
					@Override
					public void visitJumpInsn(int opcode, Label label) {
						super.visitJumpInsn(opcode, label);
						if(state == STATE.INSERTING) {
							Label l0 = new Label();
							super.visitLabel(l0);
							super.visitVarInsn(Opcodes.ALOAD, 0);
							super.visitVarInsn(Opcodes.ILOAD, 5);
							super.visitVarInsn(Opcodes.ALOAD, 1);
							super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/te/ASMHookClass", "handleOnNeighborChanged", "(Lthermalexpansion/part/conduit/ConduitBase;ILnet/minecraft/tileentity/TileEntity;)V");
							state = STATE.DONE;
						}
					}

					@Override
					public void visitMethodInsn(int opcode, String owner, String name, String desc) {
						if(state == STATE.SEARCHING && "passOcclusionTest".equals(name)) {
							state = STATE.INSERTING;
						}
						super.visitMethodInsn(opcode, owner, name, desc);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("onNeighborTileChanged")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					private STATE state = STATE.SEARCHING;
					
					@Override
					public void visitJumpInsn(int opcode, Label label) {
						super.visitJumpInsn(opcode, label);
						if(state == STATE.INSERTING) {
							Label l0 = new Label();
							super.visitLabel(l0);
							super.visitVarInsn(Opcodes.ALOAD, 0);
							super.visitVarInsn(Opcodes.ILOAD, 1);
							super.visitVarInsn(Opcodes.ALOAD, 3);
							super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/te/ASMHookClass", "handleOnNeighborTileChanged", "(Lthermalexpansion/part/conduit/ConduitBase;ILnet/minecraft/tileentity/TileEntity;)V");
							state = STATE.DONE;
						}
					}

					@Override
					public void visitMethodInsn(int opcode, String owner, String name, String desc) {
						if(state == STATE.SEARCHING && "passOcclusionTest".equals(name)) {
							state = STATE.INSERTING;
						}
						super.visitMethodInsn(opcode, owner, name, desc);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("onAdded")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					private STATE state = STATE.SEARCHING;
					
					@Override
					public void visitJumpInsn(int opcode, Label label) {
						super.visitJumpInsn(opcode, label);
						if(state == STATE.INSERTING) {
							Label l0 = new Label();
							super.visitLabel(l0);
							super.visitVarInsn(Opcodes.ALOAD, 0);
							super.visitVarInsn(Opcodes.ILOAD, 2);
							super.visitVarInsn(Opcodes.ALOAD, 1);
							super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/te/ASMHookClass", "handleOnAdded", "(Lthermalexpansion/part/conduit/ConduitBase;ILnet/minecraft/tileentity/TileEntity;)V");
							state = STATE.DONE;
						}
					}

					@Override
					public void visitMethodInsn(int opcode, String owner, String name, String desc) {
						if(state == STATE.SEARCHING && "passOcclusionTest".equals(name)) {
							state = STATE.INSERTING;
						}
						super.visitMethodInsn(opcode, owner, name, desc);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("onPartChanged")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					public void visitMethodInsn(int opcode, String owner, String name, String desc) {
						if("isConduit".equals(name)) {
							super.visitVarInsn(Opcodes.ILOAD, 7);
							super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/te/ASMHookClass", "handleOnPartChanged", "(Lthermalexpansion/part/conduit/ConduitBase;Lnet/minecraft/tileentity/TileEntity;I)V");
							Label l0 = new Label();
							super.visitLabel(l0);
							super.visitVarInsn(Opcodes.ALOAD, 0);
							super.visitVarInsn(Opcodes.ALOAD, 2);
							
						}
						super.visitMethodInsn(opcode, owner, name, desc);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("getConduit") && m.desc.equals("(I)Lthermalexpansion/part/conduit/ConduitBase;")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					private STATE state = STATE.SEARCHING;
					
					@Override
					public void visitMethodInsn(int opcode, String owner, String name, String desc) {
						super.visitMethodInsn(opcode, owner, name, desc);
						if(state == STATE.SEARCHING && name.equals("getAdjacentTileEntity")) {
							state = STATE.INSERTING;
						}
					}

					@Override
					public void visitVarInsn(int opcode, int var) {
						super.visitVarInsn(opcode, var);
						if(state == STATE.INSERTING) {
							Label l0 = new Label();
							super.visitLabel(l0);
							super.visitVarInsn(Opcodes.ALOAD, 0);
							super.visitVarInsn(Opcodes.ILOAD, 1);
							super.visitVarInsn(Opcodes.ALOAD, 2);
							super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/te/ASMHookClass", "handleGetConduit", "(Lthermalexpansion/part/conduit/ConduitBase;ILnet/minecraft/tileentity/TileEntity;)V");
							state = STATE.DONE;
						}
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("onRemoved")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					public void visitCode() {
						super.visitCode();
						Label l0 = new Label();
						super.visitLabel(l0);
						super.visitVarInsn(Opcodes.ALOAD, 0);
						super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/te/ASMHookClass", "handleOnRemoved", "(Lthermalexpansion/part/conduit/ConduitBase;)V");
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		node.accept(writer);
		return writer.toByteArray();
	}

	private byte[] handleTEConduitItem(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);
		for(MethodNode m:node.methods) {
			if(m.name.equals("getConnectionType")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					private Label l1 = new Label();
					private Label l2 = new Label();
					private boolean varAdded = false;
					
					@Override
					public void visitCode() {
						super.visitCode();
						Label l0 = new Label();
						super.visitLabel(l0);
						super.visitVarInsn(Opcodes.ALOAD, 0);
						super.visitVarInsn(Opcodes.ILOAD, 1);
						super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/te/ASMHookClass", "getTEPipeRenderMode", "(Lthermalexpansion/part/conduit/item/ConduitItem;I)I");
						super.visitVarInsn(Opcodes.ISTORE, 2);
						super.visitLabel(l1);
						super.visitLineNumber(147, l1);
						super.visitVarInsn(Opcodes.ILOAD, 2);
						super.visitInsn(Opcodes.ICONST_M1);
						super.visitJumpInsn(Opcodes.IF_ICMPEQ, l2);
						super.visitVarInsn(Opcodes.ILOAD, 2);
						super.visitInsn(Opcodes.IRETURN);
						super.visitLabel(l2);
					}

					@Override
					public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
						super.visitLocalVariable(name, desc, signature, start, end, index);
						if(!varAdded) {
							varAdded = true;
							super.visitLocalVariable("i_LP_TEMPVAR", "I", null, l1, l2, 2);
						}
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		node.accept(writer);
		return writer.toByteArray();
	}

	private byte[] handleTEItemRoute(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);
		for(FieldNode f:node.fields) {
			f.access = Opcodes.ACC_PUBLIC;
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		node.accept(writer);
		return writer.toByteArray();
	}

	private byte[] handleTETravelingItem(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);
		boolean add = true;
		for(FieldNode f:node.fields) {
			if(f.name.equals("routedLPInfo")) {
				add = false;
				break;
			}
		}
		if(add) {
			node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "routedLPInfo", "Llogisticspipes/routing/ItemRoutingInformation;", null, null));
		}
		for(MethodNode m:node.methods) {
			if(m.name.equals("toNBT") && m.desc.equals("(Lnet/minecraft/nbt/NBTTagCompound;)V")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					@Override
					public void visitCode() {
						super.visitCode();
						Label l0 = new Label();
						this.visitLabel(l0);
						this.visitVarInsn(Opcodes.ALOAD, 0);
						this.visitVarInsn(Opcodes.ALOAD, 1);
						this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/te/ASMHookClass", "handleTETravelingItemSave", "(Lthermalexpansion/part/conduit/item/TravelingItem;Lnet/minecraft/nbt/NBTTagCompound;)V");
					}
					
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("<init>") && m.desc.equals("(Lnet/minecraft/nbt/NBTTagCompound;)V")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					private STATE state = STATE.SEARCHING;

					@Override
					public void visitFieldInsn(int opcode, String owner, String name, String desc) {
						if(state == STATE.SEARCHING && "thermalexpansion/part/conduit/item/TravelingItem".equals(owner) && "startZ".equals(name) && "I".equals(desc)) {
							state = STATE.INSERTING;
						}
						super.visitFieldInsn(opcode, owner, name, desc);
					}

					@Override
					public void visitLabel(Label label) {
						if(state == STATE.INSERTING) {
							Label l23 = new Label();
							super.visitLabel(l23);
							super.visitVarInsn(Opcodes.ALOAD, 0);
							super.visitVarInsn(Opcodes.ALOAD, 1);
							super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/te/ASMHookClass", "handleTETravelingItemLoad", "(Lthermalexpansion/part/conduit/item/TravelingItem;Lnet/minecraft/nbt/NBTTagCompound;)V");
							state = STATE.DONE;
						}
						super.visitLabel(label);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		node.accept(writer);
		return writer.toByteArray();
	}

	private byte[] handleCrashReportClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);
		for(MethodNode m:node.methods) {
			if(m.name.equals("getCompleteReport") || m.name.equals("func_71502_e")) { //TODO SRG NAME
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					private STATE state = STATE.SEARCHING;
					
					@Override
					public void visitLdcInsn(Object cst) {
						super.visitLdcInsn(cst);
						if("\n\n".equals(cst) && state == STATE.SEARCHING) {
							state = STATE.INSERTING;
						}
					}
					
					@Override
					public void visitLabel(Label label) {
						if(state == STATE.INSERTING) {
							Label l0 = new Label();
							super.visitLabel(l0);
							super.visitVarInsn(Opcodes.ALOAD, 1);
							super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/LogisticsASMHookClass", "getCrashReportAddition", "()Ljava/lang/String;");
							super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
							super.visitInsn(Opcodes.POP);
							state = STATE.DONE;
						}
						super.visitLabel(label);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		node.accept(writer);
		return writer.toByteArray();
	}

	private byte[] handleBCPipeClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);
		for(MethodNode m:node.methods) {
			if(m.name.equals("handlePipeEvent")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
						super.visitTryCatchBlock(start, end, handler, type);
						Label l3 = new Label();
						super.visitLabel(l3);
						super.visitLineNumber(89, l3);
						super.visitVarInsn(Opcodes.ALOAD, 1);
						super.visitVarInsn(Opcodes.ALOAD, 0);
						super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/buildcraft/BCEventHandler", "handle", "(Lbuildcraft/transport/pipes/events/PipeEvent;Lbuildcraft/transport/Pipe;)V");
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
		}
		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}

	private byte[] handleCCLuaJLuaMachine(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);
		for(MethodNode m:node.methods) {
			if(m.name.equals("wrapLuaObject") && m.desc.equals("(Ldan200/computercraft/api/lua/ILuaObject;)Lorg/luaj/vm2/LuaTable;")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					public void visitInsn(int opcode) {
						if(opcode == Opcodes.ARETURN) {
							super.visitVarInsn(Opcodes.ALOAD, 1);
							super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/cc/LPASMHookCC", "onCCWrappedILuaObject", "(Lorg/luaj/vm2/LuaTable;Ldan200/computercraft/api/lua/ILuaObject;)Lorg/luaj/vm2/LuaTable;");
						}
						super.visitInsn(opcode);
					}

					@Override
					public void visitCode() {
						super.visitCode();
						Label l0 = new Label();
						super.visitLabel(l0);
						super.visitVarInsn(Opcodes.ALOAD, 1);
						super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/cc/LPASMHookCC", "handleCCWrappedILuaObject", "(Ldan200/computercraft/api/lua/ILuaObject;)Z");
						Label l1 = new Label();
						super.visitJumpInsn(Opcodes.IFEQ, l1);
						Label l2 = new Label();
						super.visitLabel(l2);
						super.visitVarInsn(Opcodes.ALOAD, 1);
						super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/cc/LPASMHookCC", "returnCCWrappedILuaObject", "(Ldan200/computercraft/api/lua/ILuaObject;)Lorg/luaj/vm2/LuaTable;");
						super.visitInsn(Opcodes.ARETURN);
						super.visitLabel(l1);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if(m.name.equals("toObject") && m.desc.equals("(Lorg/luaj/vm2/LuaValue;)Ljava/lang/Object;")) {
				MethodNode mv = new MethodNode(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					boolean added = false;
					@Override
					public void visitLineNumber(int line, Label start) {
						if(!added) {
							added = true;
							super.visitVarInsn(Opcodes.ALOAD, 1);
							super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/cc/LPASMHookCC", "handleCCToObject", "(Lorg/luaj/vm2/LuaValue;)Z");
							start = new Label();
							super.visitJumpInsn(Opcodes.IFEQ, start);
							Label l5 = new Label();
							super.visitLabel(l5);
							super.visitVarInsn(Opcodes.ALOAD, 1);
							super.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/proxy/cc/LPASMHookCC", "returnCCToObject", "(Lorg/luaj/vm2/LuaValue;)Ljava/lang/Object;");
							super.visitInsn(Opcodes.ARETURN);
							super.visitLabel(start);
						}
						super.visitLineNumber(line, start);
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
		}
		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}
}