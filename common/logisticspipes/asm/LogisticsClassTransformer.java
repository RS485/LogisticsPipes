package logisticspipes.asm;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;
import net.minecraftforge.fml.common.asm.transformers.ModAccessTransformer;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;
import net.minecraftforge.fml.relauncher.Side;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import logisticspipes.LogisticsPipes;
import logisticspipes.asm.mcmp.ClassBlockMultipartContainerHandler;
import logisticspipes.asm.td.ClassRenderDuctItemsHandler;
import logisticspipes.asm.td.ClassTravelingItemHandler;
import logisticspipes.utils.ModStatusHelper;

public class LogisticsClassTransformer implements IClassTransformer {

	public List<String> interfacesToClearA = new ArrayList<>();
	public List<String> interfacesToClearB = new ArrayList<>();
	private LaunchClassLoader cl = (LaunchClassLoader) LogisticsClassTransformer.class.getClassLoader();
	private Field negativeResourceCache;
	private Field invalidClasses;

	public static LogisticsClassTransformer instance;

	public LogisticsClassTransformer() {
		LogisticsClassTransformer.instance = this;
		try {
			negativeResourceCache = LaunchClassLoader.class.getDeclaredField("negativeResourceCache");
			negativeResourceCache.setAccessible(true);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		try {
			invalidClasses = LaunchClassLoader.class.getDeclaredField("invalidClasses");
			invalidClasses.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		// if dev environment and transform on first class is launched, boot the AT remapper
		if (LogisticsPipesCoreLoader.isDevelopmentEnvironment() && name.equals("net.minecraftforge.fml.common.Loader")) bootATRemapper();

		Thread thread = Thread.currentThread();
		if (thread.getName().equals("Minecraft main thread") || thread.getName().equals("main") || thread.getName().equals("Server thread")) { //Only clear when called from the main thread to avoid ConcurrentModificationException on start
			clearNegativeInterfaceCache();
		}
		if (bytes == null) {
			return null;
		}
		if (transformedName.startsWith("logisticspipes.") || transformedName.startsWith("net.minecraft")) {
			return ParamProfiler.handleClass(applyLPTransforms(transformedName, bytes));
		}
		byte[] tmp = bytes.clone();
		bytes = applyLPTransforms(transformedName, bytes);
		if (!Arrays.equals(bytes, tmp)) {
			final ClassReader reader = new ClassReader(bytes);
			final ClassNode node = new ClassNode();
			reader.accept(node, 0);
			node.sourceFile = "[LP|ASM] " + node.sourceFile;
			ClassWriter writer = new ClassWriter(0);
			node.accept(writer);
			bytes = writer.toByteArray();
		}
		return ParamProfiler.handleClass(bytes);
	}

	private void bootATRemapper() {
		System.err.println("Fetching ModAccessTransformers");
		final List<IClassTransformer> modATs = Launch.classLoader.getTransformers().stream().filter(transformer -> transformer instanceof ModAccessTransformer).collect(Collectors.toList());
		System.err.println("Found " + modATs);
		if (modATs.size() == 0) return;

		System.err.println("Inserting missing ATs from classpath");
		try {
			readClasspathATs(modATs);
		} catch (IllegalStateException e) {
			System.err.println("Could not inject classpath FMLATs");
			e.printStackTrace();
		}

		System.err.println("Booting Logistics Pipes ModAccessTransformerRemapper");
		final ModAccessTransformerRemapper remapper;
		try {
			remapper = new ModAccessTransformerRemapper();
		} catch (IllegalStateException e) {
			System.err.println("Could not initialize ModAccessTransformerRemapper:");
			e.printStackTrace();
			return;
		}

		modATs.forEach(remapper::apply);
	}

	private void readClasspathATs(List<IClassTransformer> modATs) {
		final Method readMapFile;
		try {
			readMapFile = AccessTransformer.class.getDeclaredMethod("readMapFile", String.class);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("Could not find method readMapFile on AccessTransformer class", e);
		}
		final boolean wasAccessible = readMapFile.isAccessible();
		if (!wasAccessible) readMapFile.setAccessible(true);
		try {
			readClasspathATsInner(path -> {
				final IClassTransformer classTransformer = modATs.get(0);
				try {
					readMapFile.invoke(classTransformer, path);
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new IllegalStateException("Could not access readMapFile method of " + classTransformer);
				}
			});
		} catch (IOException e) {
			throw new IllegalArgumentException("IO Error when fetching FMLATs", e);
		} finally {
			if (!wasAccessible) readMapFile.setAccessible(false);
		}
	}

	private void readClasspathATsInner(Consumer<String> readMapFile) throws IOException {
		final Enumeration<URL> manifestEntries = Launch.classLoader.findResources("META-INF/MANIFEST.MF");
		while (manifestEntries.hasMoreElements()) {
			final String accessTransformer;
			try (InputStream manifestInputStream = manifestEntries.nextElement().openStream()) {
				final Manifest manifest = new Manifest(manifestInputStream);
				accessTransformer = manifest.getMainAttributes().getValue(ModAccessTransformer.FMLAT);
			}
			final Enumeration<URL> atEntries = Launch.classLoader.findResources("META-INF/" + accessTransformer);
			while (atEntries.hasMoreElements()) {
				readMapFile.accept(atEntries.nextElement().toString());
			}
		}
	}

	private byte[] applyLPTransforms(String name, byte[] bytes) {
		try {
			if (name.equals("net.minecraft.tileentity.TileEntity")) {
				return handleTileEntityClass(bytes);
			}
			if (name.equals("net.minecraft.item.ItemStack")) {
				return handleItemStackClass(bytes);
			}
			if (name.equals("net.minecraftforge.fluids.FluidStack")) {
				return handleFluidStackClass(bytes);
			}
			if (name.equals("net.minecraftforge.fluids.Fluid")) {
				return handleFluidClass(bytes);
			}
			if (name.equals("mcmultipart.block.BlockMultipartContainer")) {
				return ClassBlockMultipartContainerHandler.handleClass(bytes);
			}
			if (name.equals("dan200.computercraft.core.lua.LuaJLuaMachine")) {
				return handleCCLuaJLuaMachine(bytes);
			}
			if (name.equals("dan200.computercraft.core.lua.CobaltLuaMachine")) {
				return handleCCLuaJLuaMachine(bytes);
			}
			if (name.equals("cofh.thermaldynamics.duct.item.TravelingItem")) {
				return ClassTravelingItemHandler.handleTravelingItemClass(bytes);
			}
			if (name.equals("cofh.thermaldynamics.render.RenderDuctItems")) {
				return ClassRenderDuctItemsHandler.handleRenderDuctItemsClass(bytes);
			}
			if (!name.startsWith("logisticspipes.")) {
				return bytes;
			}
			return handleLPTransformation(bytes);
		} catch (Exception e) {
			if (LogisticsPipes.isDEBUG()) { //For better Debugging
				e.printStackTrace();
				return bytes;
			}
			throw new RuntimeException(e);
		}
	}

	public void clearNegativeInterfaceCache() {
		//Remove previously not found Classes to Fix ClassNotFound Exceptions for Interfaces.
		//TODO remove in future version when everybody starts using a ClassTransformer system for Interfaces.
		if (negativeResourceCache != null) {
			if (!interfacesToClearA.isEmpty()) {
				handleField(negativeResourceCache, interfacesToClearA);
			}
		}
		if (invalidClasses != null) {
			if (!interfacesToClearB.isEmpty()) {
				handleField(invalidClasses, interfacesToClearB);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void handleField(Field field, List<String> toClear) {
		try {
			Set<String> set = (Set<String>) field.get(cl);
			Iterator<String> it = toClear.iterator();
			while (it.hasNext()) {
				String content = it.next();
				if (set.contains(content)) {
					set.remove(content);
					it.remove();
				}
			}
		} catch (Exception e) {
			if (LogisticsPipes.isDEBUG()) { //For better Debugging
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
		if (node.visibleAnnotations != null) {
			for (AnnotationNode a : node.visibleAnnotations) {
				if (a.desc.equals("Llogisticspipes/asm/ModDependentInterface;")) {
					if (a.values.size() == 4 && a.values.get(0).equals("modId") && a.values.get(2).equals("interfacePath")) {
						List<String> modId = (List<String>) a.values.get(1);
						List<String> interfacePath = (List<String>) a.values.get(3);
						if (modId.size() != interfacePath.size()) {
							throw new RuntimeException("The Arrays have to be of the same size.");
						}
						for (int i = 0; i < modId.size(); i++) {
							if (!ModStatusHelper.isModLoaded(modId.get(i))) {
								interfacesToClearA.add(interfacePath.get(i));
								interfacesToClearB.add(interfacePath.get(i));
								for (String inter : node.interfaces) {
									if (inter.replace("/", ".").equals(interfacePath.get(i))) {
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
		List<MethodNode> methodsToRemove = new ArrayList<>();
		for (MethodNode m : node.methods) {
			if (m.visibleAnnotations != null) {
				for (AnnotationNode a : m.visibleAnnotations) {
					if (a.desc.equals("Llogisticspipes/asm/ModDependentMethod;")) {
						if (a.values.size() == 2 && a.values.get(0).equals("modId")) {
							String modId = a.values.get(1).toString();
							if (!ModStatusHelper.isModLoaded(modId)) {
								methodsToRemove.add(m);
								break;
							}
						} else {
							throw new UnsupportedOperationException("Can't parse the annotation correctly");
						}
					} else if (a.desc.equals("Llogisticspipes/asm/ClientSideOnlyMethodContent;")) {
						if (FMLCommonHandler.instance().getSide().equals(Side.SERVER)) {
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
					} else if (a.desc.equals("Llogisticspipes/asm/ModDependentMethodName;")) {
						if (a.values.size() == 6 && a.values.get(0).equals("modId") && a.values.get(2).equals("newName") && a.values.get(4).equals("version")) {
							String modId = a.values.get(1).toString();
							final String newName = a.values.get(3).toString();
							final String version = a.values.get(5).toString();
							boolean loaded = ModStatusHelper.isModLoaded(modId);
							if (loaded && !version.equals("")) {
								ModContainer mod = Loader.instance().getIndexedModList().get(modId);
								if (mod != null) {
									VersionRange range = VersionParser.parseRange(version);
									ArtifactVersion artifactVersion = new DefaultArtifactVersion("Version", mod.getVersion());
									loaded = range.containsVersion(artifactVersion);
								} else {
									loaded = false;
								}
							}
							if (loaded) {
								final String oldName = m.name;
								m.name = newName;
								MethodNode newM = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

									@Override
									public void visitMethodInsn(int opcode, String owner, String name, String desc) {
										if (name.equals(oldName) && owner.equals(node.superName)) {
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
		for (MethodNode m : methodsToRemove) {
			node.methods.remove(m);
		}
		List<FieldNode> fieldsToRemove = new ArrayList<>();
		for (FieldNode f : node.fields) {
			if (f.visibleAnnotations != null) {
				for (AnnotationNode a : f.visibleAnnotations) {
					if (a.desc.equals("Llogisticspipes/asm/ModDependentField;")) {
						if (a.values.size() == 2 && a.values.get(0).equals("modId")) {
							String modId = a.values.get(1).toString();
							if (!ModStatusHelper.isModLoaded(modId)) {
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
		for (FieldNode f : fieldsToRemove) {
			node.fields.remove(f);
		}
		if (!changed && methodsToRemove.isEmpty() && fieldsToRemove.isEmpty()) {
			return bytes;
		}
		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		return writer.toByteArray();
	}

	private byte[] handleCCLuaJLuaMachine(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);
		for (MethodNode m : node.methods) {
			if (m.name.equals("wrapLuaObject") && m.desc.equals("(Ldan200/computercraft/api/lua/ILuaObject;)Lorg/luaj/vm2/LuaTable;")) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					@Override
					public void visitInsn(int opcode) {
						if (opcode == Opcodes.ARETURN) {
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
			if (m.name.equals("toObject") && (m.desc.equals("(Lorg/luaj/vm2/LuaValue;)Ljava/lang/Object;") || m.desc.equals("(Lorg/luaj/vm2/LuaValue;Ljava/util/Map;)Ljava/lang/Object;"))) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					boolean added = false;

					@Override
					public void visitLineNumber(int line, Label start) {
						if (!added) {
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
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {

			protected String getCommonSuperClass(final String type1, final String type2) {
				Class<?> c, d;
				ClassLoader classLoader = Launch.classLoader; // Change Classloader to the MC one
				try {
					c = Class.forName(type1.replace('/', '.'), false, classLoader);
					d = Class.forName(type2.replace('/', '.'), false, classLoader);
				} catch (Exception e) {
					throw new RuntimeException(e.toString());
				}
				if (c.isAssignableFrom(d)) {
					return type1;
				}
				if (d.isAssignableFrom(c)) {
					return type2;
				}
				if (c.isInterface() || d.isInterface()) {
					return "java/lang/Object";
				} else {
					do {
						c = c.getSuperclass();
					} while (!c.isAssignableFrom(d));
					return c.getName().replace('.', '/');
				}
			}

		};
		node.accept(writer);
		return writer.toByteArray();
	}

	private byte[] handleTileEntityClass(byte[] bytes) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);
		node.interfaces.add("logisticspipes/asm/te/ILPTEInformation");
		node.visitField(Opcodes.ACC_PRIVATE, "informationObjectLogisticsPipes", "Llogisticspipes/asm/te/LPTileEntityObject;", null, null);
		for (MethodNode m : node.methods) {
			if (m.name.equals("validate") || m.name.equals("func_145829_t") || (m.name.equals("A") && m.desc.equals("()V"))) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					@Override
					public void visitCode() {
						super.visitCode();
						Label l0 = new Label();
						visitLabel(l0);
						visitVarInsn(Opcodes.ALOAD, 0);
						this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/LogisticsASMHookClass", "validate", "(Lnet/minecraft/tileentity/TileEntity;)V");
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
			if (m.name.equals("invalidate") || m.name.equals("func_145843_s") || (m.name.equals("z") && m.desc.equals("()V"))) {
				MethodNode mv = new MethodNode(Opcodes.ASM4, m.access, m.name, m.desc, m.signature, m.exceptions.toArray(new String[0])) {

					@Override
					public void visitCode() {
						super.visitCode();
						Label l0 = new Label();
						visitLabel(l0);
						visitVarInsn(Opcodes.ALOAD, 0);
						this.visitMethodInsn(Opcodes.INVOKESTATIC, "logisticspipes/asm/LogisticsASMHookClass", "invalidate", "(Lnet/minecraft/tileentity/TileEntity;)V");
					}
				};
				m.accept(mv);
				node.methods.set(node.methods.indexOf(m), mv);
			}
		}
		MethodVisitor mv;
		{
			mv = node.visitMethod(Opcodes.ACC_PUBLIC, "getObject", "()Llogisticspipes/asm/te/LPTileEntityObject;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/tileentity/TileEntity", "informationObjectLogisticsPipes", "Llogisticspipes/asm/te/LPTileEntityObject;");
			mv.visitInsn(Opcodes.ARETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = node.visitMethod(Opcodes.ACC_PUBLIC, "setObject", "(Llogisticspipes/asm/te/LPTileEntityObject;)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitFieldInsn(Opcodes.PUTFIELD, "net/minecraft/tileentity/TileEntity", "informationObjectLogisticsPipes", "Llogisticspipes/asm/te/LPTileEntityObject;");
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitInsn(Opcodes.RETURN);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		node.accept(writer);
		return writer.toByteArray();
	}

	private byte[] handleItemStackClass(byte[] bytes) {
		return addAddInfoPart(bytes, "net/minecraft/item/ItemStack");
	}

	private byte[] handleFluidStackClass(byte[] bytes) {
		return addAddInfoPart(bytes, "net/minecraftforge/fluids/FluidStack");
	}

	private byte[] handleFluidClass(byte[] bytes) {
		return addAddInfoPart(bytes, "net/minecraftforge/fluids/Fluid");
	}

	private byte[] addAddInfoPart(byte[] bytes, String className) {
		final ClassReader reader = new ClassReader(bytes);
		final ClassNode node = new ClassNode();
		reader.accept(node, 0);

		node.interfaces.add("logisticspipes/asm/addinfo/IAddInfoProvider");

		{
			FieldVisitor fv = node.visitField(Opcodes.ACC_PRIVATE, "logisticsPipesAdditionalInformation", "Ljava/util/ArrayList;", "Ljava/util/ArrayList<Llogisticspipes/asm/addinfo/IAddInfo;>;", null);
			fv.visitEnd();
		}

		MethodVisitor mv;
		{
			mv = node.visitMethod(Opcodes.ACC_PUBLIC, "getLogisticsPipesAddInfo", "(Ljava/lang/Class;)Llogisticspipes/asm/addinfo/IAddInfo;", "<T::Llogisticspipes/asm/addinfo/IAddInfo;>(Ljava/lang/Class<TT;>;)TT;", null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(11, l0);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, className, "logisticsPipesAdditionalInformation", "Ljava/util/ArrayList;");
			Label l1 = new Label();
			mv.visitJumpInsn(Opcodes.IFNONNULL, l1);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLineNumber(12, l2);
			mv.visitInsn(Opcodes.ACONST_NULL);
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitLabel(l1);
			mv.visitLineNumber(14, l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, className, "logisticsPipesAdditionalInformation", "Ljava/util/ArrayList;");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "iterator", "()Ljava/util/Iterator;", false);
			mv.visitVarInsn(Opcodes.ASTORE, 2);
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] { "java/util/Iterator" }, 0, null);
			mv.visitVarInsn(Opcodes.ALOAD, 2);
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
			Label l4 = new Label();
			mv.visitJumpInsn(Opcodes.IFEQ, l4);
			mv.visitVarInsn(Opcodes.ALOAD, 2);
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
			mv.visitTypeInsn(Opcodes.CHECKCAST, "logisticspipes/asm/addinfo/IAddInfo");
			mv.visitVarInsn(Opcodes.ASTORE, 3);
			Label l5 = new Label();
			mv.visitLabel(l5);
			mv.visitLineNumber(15, l5);
			mv.visitVarInsn(Opcodes.ALOAD, 3);
			Label l6 = new Label();
			mv.visitJumpInsn(Opcodes.IFNONNULL, l6);
			mv.visitJumpInsn(Opcodes.GOTO, l3);
			mv.visitLabel(l6);
			mv.visitLineNumber(16, l6);
			mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] { "logisticspipes/asm/addinfo/IAddInfo" }, 0, null);
			mv.visitVarInsn(Opcodes.ALOAD, 3);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			Label l7 = new Label();
			mv.visitJumpInsn(Opcodes.IF_ACMPNE, l7);
			Label l8 = new Label();
			mv.visitLabel(l8);
			mv.visitLineNumber(17, l8);
			mv.visitVarInsn(Opcodes.ALOAD, 3);
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitLabel(l7);
			mv.visitLineNumber(19, l7);
			mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
			mv.visitJumpInsn(Opcodes.GOTO, l3);
			mv.visitLabel(l4);
			mv.visitLineNumber(20, l4);
			mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
			mv.visitInsn(Opcodes.ACONST_NULL);
			mv.visitInsn(Opcodes.ARETURN);
			Label l9 = new Label();
			mv.visitLabel(l9);
			mv.visitLocalVariable("info", "Llogisticspipes/asm/addinfo/IAddInfo;", null, l5, l7, 3);
			mv.visitLocalVariable("this", "L" + className + ";", null, l0, l9, 0);
			mv.visitLocalVariable("clazz", "Ljava/lang/Class;", "Ljava/lang/Class<TT;>;", l0, l9, 1);
			mv.visitMaxs(2, 4);
			mv.visitEnd();
		}
		{
			mv = node.visitMethod(Opcodes.ACC_PUBLIC, "setLogisticsPipesAddInfo", "(Llogisticspipes/asm/addinfo/IAddInfo;)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(25, l0);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, className, "logisticsPipesAdditionalInformation", "Ljava/util/ArrayList;");
			Label l1 = new Label();
			mv.visitJumpInsn(Opcodes.IFNONNULL, l1);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLineNumber(26, l2);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList");
			mv.visitInsn(Opcodes.DUP);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
			mv.visitFieldInsn(Opcodes.PUTFIELD, className, "logisticsPipesAdditionalInformation", "Ljava/util/ArrayList;");
			mv.visitLabel(l1);
			mv.visitLineNumber(28, l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitVarInsn(Opcodes.ISTORE, 2);
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] { Opcodes.INTEGER }, 0, null);
			mv.visitVarInsn(Opcodes.ILOAD, 2);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, className, "logisticsPipesAdditionalInformation", "Ljava/util/ArrayList;");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "size", "()I", false);
			Label l4 = new Label();
			mv.visitJumpInsn(Opcodes.IF_ICMPGE, l4);
			Label l5 = new Label();
			mv.visitLabel(l5);
			mv.visitLineNumber(29, l5);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, className, "logisticsPipesAdditionalInformation", "Ljava/util/ArrayList;");
			mv.visitVarInsn(Opcodes.ILOAD, 2);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "get", "(I)Ljava/lang/Object;", false);
			Label l6 = new Label();
			mv.visitJumpInsn(Opcodes.IFNONNULL, l6);
			Label l7 = new Label();
			mv.visitJumpInsn(Opcodes.GOTO, l7);
			mv.visitLabel(l6);
			mv.visitLineNumber(30, l6);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, className, "logisticsPipesAdditionalInformation", "Ljava/util/ArrayList;");
			mv.visitVarInsn(Opcodes.ILOAD, 2);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "get", "(I)Ljava/lang/Object;", false);
			mv.visitTypeInsn(Opcodes.CHECKCAST, "logisticspipes/asm/addinfo/IAddInfo");
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
			mv.visitJumpInsn(Opcodes.IF_ACMPNE, l7);
			Label l8 = new Label();
			mv.visitLabel(l8);
			mv.visitLineNumber(31, l8);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, className, "logisticsPipesAdditionalInformation", "Ljava/util/ArrayList;");
			mv.visitVarInsn(Opcodes.ILOAD, 2);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "set", "(ILjava/lang/Object;)Ljava/lang/Object;", false);
			mv.visitInsn(Opcodes.POP);
			Label l9 = new Label();
			mv.visitLabel(l9);
			mv.visitLineNumber(32, l9);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitLabel(l7);
			mv.visitLineNumber(28, l7);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitIincInsn(2, 1);
			mv.visitJumpInsn(Opcodes.GOTO, l3);
			mv.visitLabel(l4);
			mv.visitLineNumber(35, l4);
			mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, className, "logisticsPipesAdditionalInformation", "Ljava/util/ArrayList;");
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
			mv.visitInsn(Opcodes.POP);
			Label l10 = new Label();
			mv.visitLabel(l10);
			mv.visitLineNumber(36, l10);
			mv.visitInsn(Opcodes.RETURN);
			Label l11 = new Label();
			mv.visitLabel(l11);
			mv.visitLocalVariable("i", "I", null, l3, l4, 2);
			mv.visitLocalVariable("this", "L" + className + ";", null, l0, l11, 0);
			mv.visitLocalVariable("info", "Llogisticspipes/asm/addinfo/IAddInfo;", null, l0, l11, 1);
			mv.visitMaxs(3, 3);
			mv.visitEnd();
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		node.accept(writer);
		return writer.toByteArray();
	}
}
