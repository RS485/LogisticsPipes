package logisticspipes.asm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import logisticspipes.LPConstants;
import logisticspipes.asm.bc.ClassPipeHandler;
import logisticspipes.asm.bc.ClassPipeItemsSandstoneHandler;
import logisticspipes.asm.bc.ClassPipeRendererTESRHandler;
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
		Thread thread = Thread.currentThread();
		if(thread.getName().equals("Minecraft main thread") || thread.getName().equals("main") || thread.getName().equals("Server thread")) { //Only clear when called from the main thread to avoid ConcurrentModificationException on start
			clearNegativeInterfaceCache();
		}
		if(bytes == null) return null;
		if(name.startsWith("logisticspipes.") || name.startsWith("net.minecraft") || LPConstants.DEBUG) {
			return applyLPTransforms(name, bytes);
		}
		byte[] tmp = bytes.clone();
		bytes = applyLPTransforms(name, bytes);
		if(!Arrays.equals(bytes, tmp)) {
			final ClassReader reader = new ClassReader(bytes);
			final ClassNode node = new ClassNode();
			reader.accept(node, 0);
			node.sourceFile = "[LP|ASM] " + node.sourceFile;
			ClassWriter writer = new ClassWriter(0);
			node.accept(writer);
			bytes = writer.toByteArray();
		}
		return bytes;
	}
	
	private byte[] applyLPTransforms(String name, byte[] bytes) {
		try {
			if(name.equals("buildcraft.transport.PipeTransportItems")) {
				return ClassPipeTransportItemsHandler.handlePipeTransportItems(bytes);
			}
			if(name.equals("buildcraft.transport.Pipe")) {
				return ClassPipeHandler.handleBCPipeClass(bytes);
			}
			if(name.equals("buildcraft.transport.pipes.PipeItemsSandstone")) {
				return ClassPipeItemsSandstoneHandler.handleClass(bytes);
			}
			if(name.equals("buildcraft.transport.render.PipeRendererTESR")) {
				return ClassPipeRendererTESRHandler.handlePipeRendererTESRClass(bytes);
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
			if(LPConstants.DEBUG) { //For better Debugging
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
			if(LPConstants.DEBUG) { //For better Debugging
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
								MethodNode newM = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
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

	private byte[] handleCrashReportClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);
		for(MethodNode m:node.methods) {
			if(m.name.equals("getCompleteReport") || m.name.equals("func_71502_e")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
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

	private byte[] handleCCLuaJLuaMachine(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);
		for(MethodNode m:node.methods) {
			if(m.name.equals("wrapLuaObject") && m.desc.equals("(Ldan200/computercraft/api/lua/ILuaObject;)Lorg/luaj/vm2/LuaTable;")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
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
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
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