package logisticspipes.asm;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.FCONST_0;
import static org.objectweb.asm.Opcodes.FCONST_1;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_M1;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;

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
import org.objectweb.asm.MethodVisitor;
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
			e.printStackTrace();
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
			if(transformedName.equals("net.minecraft.block.Block")) {
				return addBlockOverwriteContructor(bytes);
			}
			if(transformedName.equals("net.minecraft.block.BlockContainer")) {
				return addBlockContainerOverwriteContructor(bytes);
			}
			if(transformedName.equals("buildcraft.transport.BlockGenericPipe")) {
				return addBlockGenericPipeOverwriteContructor(bytes);
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
	
	public byte[] addBlockOverwriteContructor(byte[] bytes) {
		ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(node, 0);
		for(MethodNode method:node.methods) {
			if(method.name.equals("<init>") && method.desc.equals("(Llogisticspipes/LogisticsPipes;I)V")) return bytes;
		}
		MethodVisitor mv = node.visitMethod(ACC_PUBLIC, "<init>", "(Llogisticspipes/LogisticsPipes;I)V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(ICONST_1);
		mv.visitFieldInsn(PUTFIELD, "net/minecraft/block/Block", "field_72030_cd", "Z"); //blockConstructorCalled
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(ICONST_1);
		mv.visitFieldInsn(PUTFIELD, "net/minecraft/block/Block", "field_72027_ce", "Z"); //enableStats
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(ICONST_M1);
		mv.visitFieldInsn(PUTFIELD, "net/minecraft/block/Block", "silk_check_meta", "I"); //silk_check_meta
		Label l4 = new Label();
		mv.visitLabel(l4);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitTypeInsn(INSTANCEOF, "net/minecraft/block/ITileEntityProvider");
		mv.visitFieldInsn(PUTFIELD, "net/minecraft/block/Block", "isTileProvider", "Z"); //isTileProvider
		Label l5 = new Label();
		mv.visitLabel(l5);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETSTATIC, "net/minecraft/block/Block", "field_71966_d", "Lnet/minecraft/block/StepSound;"); //soundPowderFootstep
		mv.visitFieldInsn(PUTFIELD, "net/minecraft/block/Block", "field_72020_cn", "Lnet/minecraft/block/StepSound;"); //stepSound
		Label l6 = new Label();
		mv.visitLabel(l6);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(FCONST_1);
		mv.visitFieldInsn(PUTFIELD, "net/minecraft/block/Block", "field_72017_co", "F"); //blockParticleGravity
		Label l7 = new Label();
		mv.visitLabel(l7);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitLdcInsn(new Float("0.6"));
		mv.visitFieldInsn(PUTFIELD, "net/minecraft/block/Block", "field_72016_cq", "F"); //slipperiness
		Label l8 = new Label();
		mv.visitLabel(l8);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETSTATIC, "net/minecraft/block/material/Material", "field_76264_q", "Lnet/minecraft/block/material/Material;"); //glass
		mv.visitFieldInsn(PUTFIELD, "net/minecraft/block/Block", "field_72018_cp", "Lnet/minecraft/block/material/Material;"); //blockMaterial
		Label l9 = new Label();
		mv.visitLabel(l9);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(FCONST_0);
		mv.visitInsn(FCONST_0);
		mv.visitInsn(FCONST_0);
		mv.visitInsn(FCONST_1);
		mv.visitInsn(FCONST_1);
		mv.visitInsn(FCONST_1);
		mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/block/Block", "func_71905_a", "(FFFFFF)V"); //setBlockBounds
		Label l10 = new Label();
		mv.visitLabel(l10);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ILOAD, 2);
		mv.visitFieldInsn(PUTFIELD, "net/minecraft/block/Block", "field_71990_ca", "I"); //blockID
		Label l11 = new Label();
		mv.visitLabel(l11);
		mv.visitFieldInsn(GETSTATIC, "net/minecraft/block/Block", "field_71973_m", "[Lnet/minecraft/block/Block;"); //blocksList
		mv.visitVarInsn(ILOAD, 2);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(AASTORE);
		Label l12 = new Label();
		mv.visitLabel(l12);
		mv.visitInsn(RETURN);
		Label l13 = new Label();
		mv.visitLabel(l13);
		mv.visitLocalVariable("this", "Lnet/minecraft/block/Block;", null, l0, l13, 0);
		mv.visitLocalVariable("mod", "Llogisticspipes/LogisticsPipes;", null, l0, l13, 1);
		mv.visitLocalVariable("idToOverwrite", "I", null, l0, l13, 2);
		mv.visitMaxs(7, 3);
		mv.visitEnd();
		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}
	
	public byte[] addBlockContainerOverwriteContructor(byte[] bytes) {
		ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(node, 0);
		for(MethodNode method:node.methods) {
			if(method.name.equals("<init>") && method.desc.equals("(Llogisticspipes/LogisticsPipes;I)V")) return bytes;
		}
		MethodVisitor mv = node.visitMethod(ACC_PUBLIC, "<init>", "(Llogisticspipes/LogisticsPipes;I)V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ILOAD, 2);
		mv.visitMethodInsn(INVOKESPECIAL, "net/minecraft/block/Block", "<init>", "(Llogisticspipes/LogisticsPipes;I)V");
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitInsn(ICONST_1);
		mv.visitFieldInsn(PUTFIELD, "net/minecraft/block/BlockContainer", "field_72025_cg", "Z"); //isBlockContainer
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitInsn(RETURN);
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitLocalVariable("this", "Lnet/minecraft/block/BlockContainer;", null, l0, l3, 0);
		mv.visitLocalVariable("instance", "Llogisticspipes/LogisticsPipes;", null, l0, l3, 1);
		mv.visitLocalVariable("i", "I", null, l0, l3, 2);
		mv.visitMaxs(3, 3);
		mv.visitEnd();
		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}
	
	public byte[] addBlockGenericPipeOverwriteContructor(byte[] bytes) {
		ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(node, 0);
		for(MethodNode method:node.methods) {
			if(method.name.equals("<init>") && method.desc.equals("(Llogisticspipes/LogisticsPipes;I)V")) return bytes;
		}
		MethodVisitor mv = node.visitMethod(ACC_PUBLIC, "<init>", "(Llogisticspipes/LogisticsPipes;I)V", null, null);
		mv.visitCode();
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ILOAD, 2);
		mv.visitMethodInsn(INVOKESPECIAL, "net/minecraft/block/BlockContainer", "<init>", "(Llogisticspipes/LogisticsPipes;I)V");
		Label l1 = new Label();
		mv.visitLabel(l1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitIntInsn(BIPUSH, 97);
		mv.visitFieldInsn(PUTFIELD, "buildcraft/transport/BlockGenericPipe", "renderAxis", "C");
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitInsn(RETURN);
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitLocalVariable("this", "Lbuildcraft/transport/BlockGenericPipe;", null, l0, l3, 0);
		mv.visitLocalVariable("instance", "Llogisticspipes/LogisticsPipes;", null, l0, l3, 1);
		mv.visitLocalVariable("i", "I", null, l0, l3, 2);
		mv.visitMaxs(3, 3);
		mv.visitEnd();
		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}
}
