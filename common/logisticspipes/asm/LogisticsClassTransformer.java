package logisticspipes.asm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import logisticspipes.LogisticsPipes;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
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

	private List<String> interfacesToClearA = new ArrayList<String>();
	private List<String> interfacesToClearB = new ArrayList<String>();
	private LaunchClassLoader cl = (LaunchClassLoader)LogisticsClassTransformer.class.getClassLoader();
	private Field negativeResourceCache;
	private Field invalidClasses;
	
	public LogisticsClassTransformer() {
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
			clearNegativeInterfaceCache();
			if(name.equals("buildcraft.transport.PipeTransportItems")) {
				return handlePipeTransportItems(bytes);
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
							if(!Loader.isModLoaded(modId.get(i))) {
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
							if(!Loader.isModLoaded(modId)) {
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
							boolean loaded = Loader.isModLoaded(modId);
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
							if(!Loader.isModLoaded(modId)) {
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
	
	private byte[] handlePipeTransportItems(byte[] bytes) {
		final ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(node, 0);
		for(MethodNode m:node.methods) {
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
		}
		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}
}