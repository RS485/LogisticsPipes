package logisticspipes.asm.bc;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class ClassFacadeRenderHelperHandler {

	public static byte[] handleFacadeRenderHelperClass(byte[] bytes) {
		final ClassNode node = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(node, 0);
		
		for(MethodNode m:node.methods) {
			if(m.name.equals("pipeFacadeRenderer")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					@Override
					public void visitTypeInsn(int opcode, String type) {
						if(type.equals("buildcraft/transport/render/FacadeBlockAccess")) {
							type = "logisticspipes/proxy/buildcraft/LPFacadeBlockAccess";
						}
						super.visitTypeInsn(opcode, type);
					}

					@Override
					public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
						if(owner.equals("buildcraft/transport/render/FacadeBlockAccess") && name.equals("<init>")) {
							owner = "logisticspipes/proxy/buildcraft/LPFacadeBlockAccess";
						}
						super.visitMethodInsn(opcode, owner, name, desc, itf);
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
