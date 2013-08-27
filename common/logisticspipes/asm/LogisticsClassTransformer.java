package logisticspipes.asm;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import net.minecraft.launchwrapper.IClassTransformer;

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
import cpw.mods.fml.relauncher.Side;

public class LogisticsClassTransformer implements IClassTransformer {

	private abstract class LocalMethodVisitor extends MethodNode {

		public LocalMethodVisitor(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
			super(access, name, desc, signature, exceptions);
		}

		@Override
		public void visitCode() {
			super.visitCode();
			addCode(this);
		}
		
		protected abstract void addCode(MethodNode node);
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		try {
			if(name.equals("buildcraft.transport.PipeTransportItems")) {
				//return handlePipeTransportItems(bytes);
				return bytes;
			}
			if(!name.startsWith("logisticspipes.")) {
				return bytes;
			}
			if(name.equals("logisticspipes.asm.LogisticsASMHelperClass")) { //Don't check the helper class
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
	
	private byte[] handlePipeTransportItems(byte[] bytes) {
		ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(node, 0);
		boolean handled = false;
		for(MethodNode m:node.methods) {
			if(m.name.equals("canReceivePipeObjects")) {
				MethodNode newM = new LocalMethodVisitor(m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {
					@Override
					protected void addCode(MethodNode node) {
						LogisticsASMHelperClass.visitCanRecivePipeObject(node);
					}
				};
				m.accept(newM);
				node.methods.set(node.methods.indexOf(m), newM);
				handled = true;
				break;
			}
		}
		if(!handled) {
			throw new RuntimeException("Method 'canReceivePipeObjects' from 'buildcraft.transport.PipeTransportItems' could not be found.");
		}
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		node.accept(writer);
		return writer.toByteArray();
	}

	@SuppressWarnings("unchecked")
	private byte[] handleLPTransformation(byte[] bytes) {
		ClassNode node = new ClassNode();
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
							m.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/LogisitcsASMHookClass", "callingClearedMethod", "()V");
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
}
