package logisticspipes.asm;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.IClassTransformer;

public class LogisticsClassTransformer implements IClassTransformer {
	@Override
	@SuppressWarnings("unchecked")
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		if(!name.startsWith("logisticspipes.")) {
			return bytes;
		}
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
