package logisticspipes.asm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import com.google.common.io.Resources;
import logisticspipes.LPConstants;
import logisticspipes.asm.DevEnvHelper.MappingLoader_MCP.CantLoadMCPMappingException;
import logisticspipes.asm.DevEnvHelper.MinecraftNameSet.Side;

import lombok.SneakyThrows;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;
import cpw.mods.fml.common.asm.transformers.ModAccessTransformer;
import cpw.mods.fml.relauncher.CoreModManager;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.FileListHelper;

import com.google.common.base.Strings;
import com.google.common.collect.ObjectArrays;
import com.google.common.primitives.Ints;
import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class DevEnvHelper {

	private static final Attributes.Name COREMODCONTAINSFMLMOD = new Attributes.Name("FMLCorePluginContainsFMLMod");

	public static boolean isDevelopmentEnvironment() {
		if (!LPConstants.DEBUG) {
			return false;
		} else {
			boolean eclipseCheck = (new File(".classpath")).exists();
			boolean ideaCheck = System.getProperty("java.class.path").contains("idea_rt.jar");

			return eclipseCheck || ideaCheck;
		}
	}

	@SuppressWarnings("unchecked")
	public static void detectCoreModInDevEnv() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException, IOException {
		if (!isDevelopmentEnvironment()) {
			return;
		}

		Method handleCascadingTweak = CoreModManager.class.getDeclaredMethod("handleCascadingTweak", File.class, JarFile.class, String.class, LaunchClassLoader.class, Integer.class);
		handleCascadingTweak.setAccessible(true);
		Method loadCoreMod = CoreModManager.class.getDeclaredMethod("loadCoreMod", LaunchClassLoader.class, String.class, File.class);
		loadCoreMod.setAccessible(true);
		Method setupCoreModDir = CoreModManager.class.getDeclaredMethod("setupCoreModDir", File.class);
		setupCoreModDir.setAccessible(true);
		Field loadedCoremods = CoreModManager.class.getDeclaredField("loadedCoremods");
		loadedCoremods.setAccessible(true);
		Field reparsedCoremods = CoreModManager.class.getDeclaredField("reparsedCoremods");
		reparsedCoremods.setAccessible(true);
		Field mcDir = CoreModManager.class.getDeclaredField("mcDir");
		mcDir.setAccessible(true);
		Field transformers = LaunchClassLoader.class.getDeclaredField("transformers");
		transformers.setAccessible(true);

		LaunchClassLoader classLoader = Launch.classLoader;

		FMLRelaunchLog.fine("Discovering coremods");
		File coreMods = (File) setupCoreModDir.invoke(null, mcDir.get(null));
		FilenameFilter ff = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		};
		File[] FMLcoreModListArray = coreMods.listFiles(ff);
		File versionedModDir = new File(coreMods, "1.7.10");
		if (versionedModDir.isDirectory()) {
			File[] versionedCoreMods = versionedModDir.listFiles(ff);
			FMLcoreModListArray = ObjectArrays.concat(FMLcoreModListArray, versionedCoreMods, File.class);
		}

		List<String> FMLcoreModList = new ArrayList<String>();

		for (File f : FMLcoreModListArray) {
			FMLcoreModList.add(f.getName());
		}

		List<File> coreModList = new ArrayList<File>();

		for (URL path : classLoader.getURLs()) {
			File file = new File(URLDecoder.decode(path.getFile()));
			if (!FMLcoreModList.contains(file.getName()) && file.getName().endsWith(".jar")) {
				coreModList.add(file);
			}
		}

		coreModList = Arrays.asList(FileListHelper.sortFileList(coreModList.toArray(new File[coreModList.size()])));

		for (File coreMod : coreModList) {
			FMLRelaunchLog.fine("Examining for coremod candidacy %s", coreMod.getName());
			JarFile jar = null;
			Attributes mfAttributes;
			try {
				jar = new JarFile(coreMod);
				if (jar.getManifest() == null) {
					// Not a coremod and no access transformer list
					continue;
				}
				ModAccessTransformer.addJar(jar);
				mfAttributes = jar.getManifest().getMainAttributes();
			} catch (IOException ioe) {
				FMLRelaunchLog.log(Level.ERROR, ioe, "Unable to read the jar file %s - ignoring", coreMod.getName());
				continue;
			} finally {
				if (jar != null) {
					try {
						jar.close();
					} catch (IOException e) {
						// Noise
					}
				}
			}
			//AccessTransformer //For NEI
			if (mfAttributes.getValue("AccessTransformer") != null) {
				String cfg = mfAttributes.getValue("AccessTransformer");
				((List<IClassTransformer>) transformers.get(classLoader)).add(new AccessTransformer(cfg) {});
			}
			//FMLAT //For newer NEI
			if (mfAttributes.getValue("FMLAT") != null) {
				String cfg = "META-INF/" + mfAttributes.getValue("FMLAT");
				((List<IClassTransformer>) transformers.get(classLoader)).add(new AccessTransformer(cfg) {});
			}
			String cascadedTweaker = mfAttributes.getValue("TweakClass");
			if (cascadedTweaker != null) {
				FMLRelaunchLog.info("Loading tweaker %s from %s", cascadedTweaker, coreMod.getName());
				Integer sortOrder = Ints.tryParse(Strings.nullToEmpty(mfAttributes.getValue("TweakOrder")));
				sortOrder = (sortOrder == null ? Integer.valueOf(0) : sortOrder);
				handleCascadingTweak.invoke(null, coreMod, jar, cascadedTweaker, classLoader, sortOrder);
				((List<String>) loadedCoremods.get(null)).add(coreMod.getName());
				continue;
			}

			String fmlCorePlugin = mfAttributes.getValue("FMLCorePlugin");
			if (fmlCorePlugin == null) {
				// Not a coremod
				FMLRelaunchLog.fine("Not found coremod data in %s", coreMod.getName());
				continue;
			}

			//try {
			//classLoader.addURL(coreMod.toURI().toURL());
			if (!mfAttributes.containsKey(DevEnvHelper.COREMODCONTAINSFMLMOD)) {
				FMLRelaunchLog.finer("Adding %s to the list of known coremods, it will not be examined again", coreMod.getName());
				((List<String>) loadedCoremods.get(null)).add(coreMod.getName());
			} else {
				FMLRelaunchLog.finer("Found FMLCorePluginContainsFMLMod marker in %s, it will be examined later for regular @Mod instances", coreMod.getName());
				//((List<String>)reparsedCoremods.get(null)).add(coreMod.getName());
			}
			//} catch(MalformedURLException e) {
			//	FMLRelaunchLog.log(Level.ERROR, e, "Unable to convert file into a URL. weird");
			//	continue;
			//}
			loadCoreMod.invoke(null, classLoader, fmlCorePlugin, coreMod);
		}

		try {
			URL resource = Resources.getResource("CoFH_at.cfg");
			if (resource != null) {
				AccessTransformer acc = new AccessTransformer("CoFH_at.cfg") {
				};
				insertTransformer(acc);
			}
		} catch(Throwable t) {}
	}

	@SneakyThrows
	private static void insertTransformer(IClassTransformer transformer) {
		Field fTransformers = LaunchClassLoader.class.getDeclaredField("transformers");
		fTransformers.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<IClassTransformer> transformerList = (List<IClassTransformer>) fTransformers.get(Launch.classLoader);
		transformerList.add(transformer);
	}

	public static void handleSpecialClassTransformer() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException, CantLoadMCPMappingException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
		if (!isDevelopmentEnvironment() || !new File(".mcpMappings").exists()) {
			return;
		}
		DevEnvHelper.m = new MappingLoader_MCP("1.7.10", Side.UNIVERSAL, new File(".mcpMappings")).getForwardCSV();

		final Field ucpF = URLClassLoader.class.getDeclaredField("ucp");
		ucpF.setAccessible(true);
		Object ucp = ucpF.get(Launch.classLoader);
		final Field pathF = ucp.getClass().getDeclaredField("path");
		final Field loadersF = ucp.getClass().getDeclaredField("loaders");
		final Method addURL = ucp.getClass().getMethod("addURL", URL.class);
		pathF.setAccessible(true);
		loadersF.setAccessible(true);
		addURL.setAccessible(true);

		ArrayList<File> modFileList = new ArrayList<File>();
		File modsFolder = new File("mods");
		if (modsFolder.exists()) {
			File[] modses = modsFolder.listFiles();
			if (modses != null) {
				Collections.addAll(modFileList, modses);
			}
		}

		for (File f : modFileList) {
			if (!f.isFile()) {
				continue;
			}
			String path = f.getAbsolutePath();
			if (path.endsWith("LP_DEOBF.jar")) {
				URL toMove = f.toURI().toURL();
				addURL.invoke(ucp, toMove);
				Enumeration<URL> tmp = Launch.classLoader.findResources("notToBeFound");
				while (tmp.hasMoreElements()) {
					tmp.nextElement();
				}
				//ucp = ucpF.get(Launch.classLoader);

				@SuppressWarnings("unchecked")
				List<Object> pathes = (List<Object>) pathF.get(ucp);
				@SuppressWarnings("unchecked")
				List<Object> loaders = (List<Object>) loadersF.get(ucp);

				Object toMoveLoaders = null;
				Class<?> loaderClass = Class.forName("sun.misc.URLClassPath$Loader");
				for (Object loader : loaders) {
					Field baseF = loaderClass.getDeclaredField("base");
					baseF.setAccessible(true);
					URL base = (URL) baseF.get(loader);
					if (base.toExternalForm().contains(toMove.toExternalForm())) {
						toMoveLoaders = loader;
						break;
					}
				}

				Object toMovepathes = pathes.get(loaders.indexOf(toMoveLoaders));

				for (int i = loaders.indexOf(toMoveLoaders); i > 0; i--) {
					loaders.set(i, loaders.get(i - 1));
					pathes.set(i, pathes.get(i - 1));
				}
				loaders.set(0, toMoveLoaders);
				pathes.set(0, toMovepathes);
			}
		}

		Field fTransformers = LaunchClassLoader.class.getDeclaredField("transformers");
		fTransformers.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<IClassTransformer> transformers = (List<IClassTransformer>) fTransformers.get(Launch.classLoader);
		transformers.add(DevEnvHelper.transformer);
		for (int i = transformers.size() - 1; i > 0; i--) { // Move everything one up
			transformers.set(i, transformers.get(i - 1));
		}
		transformers.set(0, DevEnvHelper.transformer); // So that our injector can be first
	}

	/*
	 * Everything Below this point is based in immibis BON. Thus it is licensed under his license:
	 * LICENSE:
	 * Copyright (C) 2013 Alex "immibis" Campbell
	 * 
	 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
	 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
	 * 
	 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
	 * 
	 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
	 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
	 */

	private static Mapping m;
	private static IClassTransformer transformer = new IClassTransformer() {

		@Override
		public byte[] transform(String name, String transformedName, byte[] basicClass) {
			try {
				return transform_Sub(name, transformedName, basicClass);
			} catch (Exception e) {
				e.printStackTrace();
				return basicClass;
			}
		}

		public byte[] transform_Sub(String name, String transformedName, byte[] basicClass) throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
			if (basicClass == null) {
				return basicClass;
			}
			final String resourcePath = name.replace('.', '/').concat(".class");
			URL classResource = Launch.classLoader.findResource(resourcePath);
			String path = classResource.getPath().toString();
			if (path.contains("LP_DEOBF.jar!/")) {
				final ClassNode cn = new ClassNode();
				ClassReader reader = new ClassReader(basicClass);
				reader.accept(cn, 0);

				for (MethodNode mn : cn.methods) {

					String[] resolvedMN = DevEnvHelper.resolveMethod(cn.name, mn.name, mn.desc, DevEnvHelper.m);

					if (resolvedMN != null) {
						mn.name = DevEnvHelper.m.getMethod(resolvedMN[0], mn.name, resolvedMN[1]);
						mn.desc = DevEnvHelper.m.mapMethodDescriptor(resolvedMN[1]);

					} else {
						mn.name = DevEnvHelper.m.getMethod(cn.name, mn.name, mn.desc);
						mn.desc = DevEnvHelper.m.mapMethodDescriptor(mn.desc);
					}

					if (mn.instructions != null) {
						for (AbstractInsnNode ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext()) {

							if (ain instanceof FieldInsnNode) {
								FieldInsnNode fin = (FieldInsnNode) ain;

								String realOwner = DevEnvHelper.resolveField(fin.owner, fin.name, fin.desc, DevEnvHelper.m);

								if (realOwner == null) {
									realOwner = fin.owner;
								}

								fin.name = DevEnvHelper.m.getField(realOwner, fin.name);
								fin.desc = DevEnvHelper.m.mapTypeDescriptor(fin.desc);
								fin.owner = DevEnvHelper.m.getClass(realOwner);

							} else if (ain instanceof FrameNode) {
								FrameNode fn = (FrameNode) ain;

								if (fn.local != null) {
									for (int k = 0; k < fn.local.size(); k++) {
										if (fn.local.get(k) instanceof String) {
											fn.local.set(k, DevEnvHelper.m.getClass((String) fn.local.get(k)));
										}
									}
								}

								if (fn.stack != null) {
									for (int k = 0; k < fn.stack.size(); k++) {
										if (fn.stack.get(k) instanceof String) {
											fn.stack.set(k, DevEnvHelper.m.getClass((String) fn.stack.get(k)));
										}
									}
								}

							} else if (ain instanceof MethodInsnNode) {
								MethodInsnNode min = (MethodInsnNode) ain;

								String[] realOwnerAndDesc = DevEnvHelper.resolveMethod(min.owner, min.name, min.desc, DevEnvHelper.m);

								String realOwner = realOwnerAndDesc == null ? min.owner : realOwnerAndDesc[0];
								String realDesc = realOwnerAndDesc == null ? min.desc : realOwnerAndDesc[1];

								min.name = DevEnvHelper.m.getMethod(realOwner, min.name, realDesc);
								min.owner = DevEnvHelper.m.getClass(min.owner); // note: not realOwner which could be an interface
								min.desc = DevEnvHelper.m.mapMethodDescriptor(realDesc);

							} else if (ain instanceof LdcInsnNode) {
								LdcInsnNode lin = (LdcInsnNode) ain;

								if (lin.cst instanceof Type) {
									lin.cst = Type.getType(DevEnvHelper.m.mapTypeDescriptor(((Type) lin.cst).getDescriptor()));
								}

							} else if (ain instanceof TypeInsnNode) {
								TypeInsnNode tin = (TypeInsnNode) ain;

								tin.desc = DevEnvHelper.m.getClass(tin.desc);

							} else if (ain instanceof MultiANewArrayInsnNode) {
								MultiANewArrayInsnNode min = (MultiANewArrayInsnNode) ain;

								min.desc = DevEnvHelper.m.getClass(min.desc);
							}
						}
					}

					DevEnvHelper.processAnnotationList(DevEnvHelper.m, mn.visibleAnnotations);
					DevEnvHelper.processAnnotationList(DevEnvHelper.m, mn.visibleParameterAnnotations);
					DevEnvHelper.processAnnotationList(DevEnvHelper.m, mn.invisibleAnnotations);
					DevEnvHelper.processAnnotationList(DevEnvHelper.m, mn.invisibleParameterAnnotations);

					for (TryCatchBlockNode tcb : mn.tryCatchBlocks) {
						if (tcb.type != null) {
							tcb.type = DevEnvHelper.m.getClass(tcb.type);
						}
					}

					{
						Set<String> exceptions = new HashSet<String>(mn.exceptions);
						exceptions.addAll(DevEnvHelper.m.getExceptions(cn.name, mn.name, mn.desc));
						mn.exceptions.clear();
						for (String s : exceptions) {
							mn.exceptions.add(DevEnvHelper.m.getClass(s));
						}
					}

					if (mn.localVariables != null) {
						for (LocalVariableNode lvn : mn.localVariables) {
							lvn.desc = DevEnvHelper.m.mapTypeDescriptor(lvn.desc);
						}
					}

				}

				for (FieldNode fn : cn.fields) {
					fn.name = DevEnvHelper.m.getField(cn.name, fn.name);
					fn.desc = DevEnvHelper.m.mapTypeDescriptor(fn.desc);

					DevEnvHelper.processAnnotationList(DevEnvHelper.m, fn.invisibleAnnotations);
					DevEnvHelper.processAnnotationList(DevEnvHelper.m, fn.visibleAnnotations);

					// TO DO: support signatures (for generics, even though Minecraft doesn't use them after obfuscation)
				}

				cn.name = DevEnvHelper.m.getClass(cn.name);
				cn.superName = DevEnvHelper.m.getClass(cn.superName);

				for (int k = 0; k < cn.interfaces.size(); k++) {
					cn.interfaces.set(k, DevEnvHelper.m.getClass(cn.interfaces.get(k)));
				}

				DevEnvHelper.processAnnotationList(DevEnvHelper.m, cn.invisibleAnnotations);
				DevEnvHelper.processAnnotationList(DevEnvHelper.m, cn.visibleAnnotations);

				// TO DO: support signatures (for generics, even though Minecraft doesn't use them after obfuscation)

				for (InnerClassNode icn : cn.innerClasses) {
					icn.name = DevEnvHelper.m.getClass(icn.name);
					if (icn.outerName != null) {
						icn.outerName = DevEnvHelper.m.getClass(icn.outerName);
					}
				}

				if (cn.outerMethod != null) {
					String[] resolved = DevEnvHelper.resolveMethod(cn.outerClass, cn.outerMethod, cn.outerMethodDesc, DevEnvHelper.m);
					if (resolved != null) {
						cn.outerMethod = DevEnvHelper.m.getMethod(resolved[0], cn.outerMethod, resolved[1]);
						cn.outerMethodDesc = DevEnvHelper.m.mapMethodDescriptor(resolved[1]);
					} else {
						cn.outerMethod = DevEnvHelper.m.getMethod(cn.outerClass, cn.outerMethod, cn.outerMethodDesc);
						cn.outerMethodDesc = DevEnvHelper.m.mapMethodDescriptor(cn.outerMethodDesc);
					}
				}
				if (cn.outerClass != null) {
					cn.outerClass = DevEnvHelper.m.getClass(cn.outerClass);
				}

				ClassWriter writer = new ClassWriter(0);
				cn.accept(writer);
				return writer.toByteArray();
			}
			return basicClass;
		}
	};

	private static void processAnnotationList(Mapping m, List<AnnotationNode>[] array) {
		if (array != null) {
			for (List<AnnotationNode> list : array) {
				DevEnvHelper.processAnnotationList(m, list);
			}
		}
	}

	private static void processAnnotationList(Mapping m, List<AnnotationNode> list) {
		if (list != null) {
			for (AnnotationNode an : list) {
				DevEnvHelper.processAnnotation(m, an);
			}
		}
	}

	private static void processAnnotation(Mapping m, AnnotationNode an) {
		an.desc = m.getClass(an.desc);
		if (an.values != null) {
			for (int k = 1; k < an.values.size(); k += 2) {
				an.values.set(k, DevEnvHelper.processAnnotationValue(m, an.values.get(k)));
			}
		}
	}

	private static Object processAnnotationValue(Mapping m, Object value) {
		if (value instanceof Type) {
			return Type.getType(m.getClass(((Type) value).getDescriptor()));
		}

		if (value instanceof String[]) {
			// enum value; need to remap both the enum, and the value
			String[] array = (String[]) value;
			String desc = array[0], enumvalue = array[1];
			if (!desc.startsWith("L") || !desc.endsWith(";")) {
				throw new AssertionError("Not a class type descriptor: " + desc);
			}
			return new String[] { m.getClass(desc), m.getField(desc.substring(1, desc.length() - 1), enumvalue) };
		}

		if (value instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) value;
			for (int k = 0; k < list.size(); k++) {
				list.set(k, DevEnvHelper.processAnnotationValue(m, list.get(k)));
			}
			return value;
		}

		if (value instanceof AnnotationNode) {
			DevEnvHelper.processAnnotation(m, (AnnotationNode) value);
			return value;
		}

		return value;
	}

	// returns actual owner of field
	// or null if the field could not be resolved
	private static String resolveField(String owner, String name, String desc, Mapping m) throws IOException {
		if (owner == null) {
			return null;
		}
		byte[] bytes = Launch.classLoader.getClassBytes(owner);
		if (bytes == null) {
			return null;
		}
		final ClassNode cn = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(cn, 0);

		// http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-5.html#jvms-5.4.3.2

		for (FieldNode fn : cn.fields) {
			String newName = m.getField(owner, name);
			String newDesc = m.mapTypeDescriptor(desc);
			if (fn.name.equals(newName) && fn.desc.equals(newDesc)) {
				return owner;
			}
		}

		for (String i : cn.interfaces) {
			String result = DevEnvHelper.resolveField(i, name, desc, m);
			if (result != null) {
				return result;
			}
		}

		return DevEnvHelper.resolveField(cn.superName, name, desc, m);

	}

	// returns [realOwner, realDesc]
	// or null if the method could not be resolved
	private static String[] resolveMethod(String owner, String name, String desc, Mapping m) throws IOException {
		if (owner == null) {
			return null;
		}
		byte[] bytes = Launch.classLoader.getClassBytes(owner);
		if (bytes == null) {
			return null;
		}
		ClassNode cn = new ClassNode();
		ClassReader reader = new ClassReader(bytes);
		reader.accept(cn, 0);

		if ((cn.access & Opcodes.ACC_INTERFACE) != 0) {

			// interface method resolution; http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-5.html#jvms-5.4.3.4

			for (MethodNode mn : cn.methods) {
				if (mn.name.equals(name) && mn.desc.equals(desc) && !m.getMethod(owner, name, desc).equals(name)) {
					return new String[] { owner, desc };
				}
			}

			for (String i : cn.interfaces) {
				String[] result = DevEnvHelper.resolveMethod(i, name, desc, m);
				if (result != null) {
					return result;
				}
			}

			return null;

		} else {

			// normal method resolution; http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-5.html#jvms-5.4.3.3

			String originalOwner = owner;

			while (true) {

				if(owner == null) {
					break;
				}

				bytes = Launch.classLoader.getClassBytes(owner);
				if (bytes == null) {
					break;
				}

				cn = new ClassNode();
				reader = new ClassReader(bytes);
				reader.accept(cn, 0);

				for (MethodNode mn : cn.methods) {
					if (mn.name.equals(name) && mn.desc.equals(desc) && !m.getMethod(owner, name, desc).equals(name)) {
						return new String[] { owner, desc };
					}
				}

				owner = cn.superName;
			}

			owner = originalOwner;

			while (true) {
				if (owner == null) {
					break;
				}
				bytes = Launch.classLoader.getClassBytes(owner);
				if (bytes == null) {
					break;
				}
				cn = new ClassNode();
				reader = new ClassReader(bytes);
				reader.accept(cn, 0);

				for (String i : cn.interfaces) {
					String[] result = DevEnvHelper.resolveMethod(i, name, desc, m);
					if (result != null) {
						return result;
					}
				}

				owner = cn.superName;
			}

			return null;
		}
	}

	public static class Mapping {

		private Map<String, String> classes = new HashMap<String, String>();
		private Map<String, String> methods = new HashMap<String, String>();
		private Map<String, String> fields = new HashMap<String, String>();
		private Map<String, List<String>> exceptions = new HashMap<String, List<String>>();
		private Map<String, String> classPrefixes = new HashMap<String, String>();
		private String defaultPackage = "";

		public final NameSet fromNS, toNS;

		public Mapping(NameSet fromNS, NameSet toNS) {
			this.fromNS = fromNS;
			this.toNS = toNS;
		}

		public void setClass(String in, String out) {
			classes.put(in, out);
		}

		public void setMethod(String clazz, String name, String desc, String out) {
			methods.put(clazz + "/" + name + desc, out);
		}

		public void setField(String clazz, String name, String out) {
			fields.put(clazz + "/" + name, out);
		}

		public void setExceptions(String clazz, String method, String desc, List<String> exc) {
			exceptions.put(clazz + "/" + method + desc, exc);
		}

		public String getClass(String in) {
			if (in == null) {
				return null;
			}
			if (in.startsWith("[L") && in.endsWith(";")) {
				return "[L" + getClass(in.substring(2, in.length() - 1)) + ";";
			}
			if (in.startsWith("[")) {
				return "[" + getClass(in.substring(1));
			}

			if (in.equals("B") || in.equals("C") || in.equals("D") || in.equals("F") || in.equals("I") || in.equals("J") || in.equals("S") || in.equals("Z")) {
				return in;
			}

			String ret = classes.get(in);
			if (ret != null) {
				return ret;
			}
			for (Map.Entry<String, String> e : classPrefixes.entrySet()) {
				if (in.startsWith(e.getKey())) {
					return e.getValue() + in.substring(e.getKey().length());
				}
			}
			if (!in.contains("/")) {
				return defaultPackage + in;
			}
			return in;
		}

		public String getMethod(String clazz, String name, String desc) {
			String ret = methods.get(clazz + "/" + name + desc);
			return ret == null ? name : ret;
		}

		public String getField(String clazz, String name) {
			String ret = fields.get(clazz + "/" + name);
			return ret == null ? name : ret;
		}

		public List<String> getExceptions(String clazz, String method, String desc) {
			List<String> ret = exceptions.get(clazz + "/" + method + desc);
			return ret == null ? Collections.<String> emptyList() : ret;
		}

		public void addPrefix(String old, String new_) {
			classPrefixes.put(old, new_);
		}

		// p must include trailing slash
		public void setDefaultPackage(String p) {
			defaultPackage = p;
		}

		public String mapMethodDescriptor(String desc) {
			// some basic sanity checks, doesn't ensure it's completely valid though
			if (desc.length() == 0 || desc.charAt(0) != '(' || desc.indexOf(")") < 1) {
				throw new IllegalArgumentException("Not a valid method descriptor: " + desc);
			}

			int pos = 0;
			String out = "";
			while (pos < desc.length()) {
				switch (desc.charAt(pos)) {
					case 'V':
					case 'Z':
					case 'B':
					case 'C':
					case 'S':
					case 'I':
					case 'J':
					case 'F':
					case 'D':
					case '[':
					case '(':
					case ')':
						out += desc.charAt(pos);
						pos++;
						break;
					case 'L': {
						int end = desc.indexOf(';', pos);
						String obf = desc.substring(pos + 1, end);
						pos = end + 1;
						out += "L" + getClass(obf) + ";";
					}
						break;
					default:
						throw new RuntimeException("Unknown method descriptor character: " + desc.charAt(pos) + " (in " + desc + ")");
				}
			}
			return out;
		}

		public String mapTypeDescriptor(String in) {
			if (in.startsWith("[")) {
				return "[" + mapTypeDescriptor(in.substring(1));
			}
			if (in.startsWith("L") && in.endsWith(";")) {
				return "L" + getClass(in.substring(1, in.length() - 1)) + ";";
			}
			return in;
		}
	}

	public static abstract class NameSet {

		@Override
		public abstract boolean equals(Object o);

		@Override
		public abstract int hashCode();

		@Override
		public abstract String toString();
	}

	public static class MinecraftNameSet extends NameSet {

		public static enum Type {
			OBF,
			SRG,
			MCP
		}

		public static enum Side {
			UNIVERSAL,
			CLIENT,
			SERVER
		}

		public final Type type;
		public final String mcVersion;
		public final Side side;

		public MinecraftNameSet(Type type, Side side, String mcVersion) {
			this.type = type;
			this.side = side;
			this.mcVersion = mcVersion;
		}

		@Override
		public boolean equals(Object obj) {
			try {
				MinecraftNameSet ns = (MinecraftNameSet) obj;
				return ns.type == type && ns.side == side && ns.mcVersion.equals(mcVersion);

			} catch (ClassCastException e) {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return (side.ordinal() << 8) + type.ordinal() + mcVersion.hashCode();
		}

		@Override
		public String toString() {
			return mcVersion + " " + type + " " + side;
		}
	}

	public static class MappingLoader_MCP {

		public static class CantLoadMCPMappingException extends Exception {

			private static final long serialVersionUID = 1;

			public CantLoadMCPMappingException(String reason) {
				super(reason);
			}
		}

		// forward: obf -> searge -> mcp
		// reverse: mcp -> searge -> obf
		private Mapping forwardSRG, reverseSRG, forwardCSV, reverseCSV;

		private Map<String, Set<String>> srgMethodOwnersAndDescs = new HashMap<String, Set<String>>(); // SRG name -> SRG owners
		private Map<String, Set<String>> srgFieldOwners = new HashMap<String, Set<String>>(); // SRG name -> SRG owners

		private ExcFile excFileData;

		public MappingLoader_MCP() {}

		@Deprecated
		public MappingLoader_MCP(String mcVer, MinecraftNameSet.Side side, File mcpDir) throws IOException, CantLoadMCPMappingException {
			File srgFile, excFile;
			int[] sideNumbers;

			switch (side) {
				case UNIVERSAL:
					sideNumbers = new int[] { 2, 1, 0 };
					if (new File(mcpDir, "packaged.srg").exists()) {
						srgFile = new File(mcpDir, "packaged.srg");
						excFile = new File(mcpDir, "packaged.exc");
					} else {
						srgFile = new File(mcpDir, "joined.srg");
						excFile = new File(mcpDir, "joined.exc");
					}
					break;

				case CLIENT:
					sideNumbers = new int[] { 0 };
					srgFile = new File(mcpDir, "client.srg");

					if (new File(mcpDir, "joined.exc").exists()) {
						excFile = new File(mcpDir, "joined.exc");
					} else {
						excFile = new File(mcpDir, "client.exc");
					}

					break;

				case SERVER:
					sideNumbers = new int[] { 1 };
					srgFile = new File(mcpDir, "server.srg");

					if (new File(mcpDir, "joined.exc").exists()) {
						excFile = new File(mcpDir, "joined.exc");
					} else {
						excFile = new File(mcpDir, "server.exc");
					}

					break;

				default:
					throw new AssertionError("side is " + side);
			}

			load(side, mcVer, new ExcFile(excFile), new SrgFile(srgFile, false), CsvFile.read(new File(mcpDir, "fields.csv"), sideNumbers), CsvFile.read(new File(mcpDir, "methods.csv"), sideNumbers));
		}

		public void load(MinecraftNameSet.Side side, String mcVer, ExcFile excFile, SrgFile srgFile, Map<String, String> fieldNames, Map<String, String> methodNames) throws CantLoadMCPMappingException {

			NameSet obfNS = new MinecraftNameSet(MinecraftNameSet.Type.OBF, side, mcVer);
			NameSet srgNS = new MinecraftNameSet(MinecraftNameSet.Type.SRG, side, mcVer);
			NameSet mcpNS = new MinecraftNameSet(MinecraftNameSet.Type.MCP, side, mcVer);

			forwardSRG = new Mapping(obfNS, srgNS);
			reverseSRG = new Mapping(srgNS, obfNS);

			forwardCSV = new Mapping(srgNS, mcpNS);
			reverseCSV = new Mapping(mcpNS, srgNS);

			excFileData = excFile;
			loadSRGMapping(srgFile);
			loadCSVMapping(fieldNames, methodNames);
		}

		private void loadSRGMapping(SrgFile srg) throws CantLoadMCPMappingException {
			forwardSRG.setDefaultPackage("net/minecraft/src/");
			reverseSRG.addPrefix("net/minecraft/src/", "");

			for (Map.Entry<String, String> entry : srg.classes.entrySet()) {
				String obfClass = entry.getKey();
				String srgClass = entry.getValue();

				forwardSRG.setClass(obfClass, srgClass);
				reverseSRG.setClass(srgClass, obfClass);
			}

			for (Map.Entry<String, String> entry : srg.fields.entrySet()) {
				String obfOwnerAndName = entry.getKey();
				String srgName = entry.getValue();

				String obfOwner = obfOwnerAndName.substring(0, obfOwnerAndName.lastIndexOf('/'));
				String obfName = obfOwnerAndName.substring(obfOwnerAndName.lastIndexOf('/') + 1);

				String srgOwner = srg.classes.get(obfOwner);

				// Enum values don't use the CSV and don't start with field_
				if (srgName.startsWith("field_")) {
					if (srgFieldOwners.containsKey(srgName)) {
						System.out.println("SRG field " + srgName + " appears in multiple classes (at least " + srgFieldOwners.get(srgName) + " and " + srgOwner + ")");
					}

					Set<String> owners = srgFieldOwners.get(srgName);
					if (owners == null) {
						srgFieldOwners.put(srgName, owners = new HashSet<String>());
					}
					owners.add(srgOwner);
				}

				forwardSRG.setField(obfOwner, obfName, srgName);
				reverseSRG.setField(srgOwner, srgName, obfName);
			}

			for (Map.Entry<String, String> entry : srg.methods.entrySet()) {
				String obfOwnerNameAndDesc = entry.getKey();
				String srgName = entry.getValue();

				String obfOwnerAndName = obfOwnerNameAndDesc.substring(0, obfOwnerNameAndDesc.indexOf('('));
				String obfOwner = obfOwnerAndName.substring(0, obfOwnerAndName.lastIndexOf('/'));
				String obfName = obfOwnerAndName.substring(obfOwnerAndName.lastIndexOf('/') + 1);
				String obfDesc = obfOwnerNameAndDesc.substring(obfOwnerNameAndDesc.indexOf('('));

				String srgDesc = forwardSRG.mapMethodDescriptor(obfDesc);
				String srgOwner = srg.classes.get(obfOwner);

				Set<String> srgMethodOwnersThis = srgMethodOwnersAndDescs.get(srgName);
				if (srgMethodOwnersThis == null) {
					srgMethodOwnersAndDescs.put(srgName, srgMethodOwnersThis = new HashSet<String>());
				}
				srgMethodOwnersThis.add(srgOwner + srgDesc);

				forwardSRG.setMethod(obfOwner, obfName, obfDesc, srgName);
				reverseSRG.setMethod(srgOwner, srgName, srgDesc, obfName);

				String[] srgExceptions = excFileData.getExceptionClasses(srgOwner, srgName, srgDesc);
				if (srgExceptions.length > 0) {
					List<String> obfExceptions = new ArrayList<String>();
					for (String s : srgExceptions) {
						obfExceptions.add(reverseSRG.getClass(s));
					}
					forwardSRG.setExceptions(obfOwner, obfName, obfDesc, obfExceptions);
				}
			}
		}

		private void loadCSVMapping(Map<String, String> fieldNames, Map<String, String> methodNames) throws CantLoadMCPMappingException {
			for (Map.Entry<String, String> entry : fieldNames.entrySet()) {
				String srgName = entry.getKey();
				String mcpName = entry.getValue();

				if (srgFieldOwners.get(srgName) == null) {
					System.out.println("Field exists in CSV but not in SRG: " + srgName + " (CSV name: " + mcpName + ")");
				} else {
					for (String srgOwner : srgFieldOwners.get(srgName)) {
						String mcpOwner = srgOwner;

						forwardCSV.setField(srgOwner, srgName, mcpName);
						reverseCSV.setField(mcpOwner, mcpName, srgName);
					}
				}
			}

			for (Map.Entry<String, String> entry : methodNames.entrySet()) {
				String srgName = entry.getKey();
				String mcpName = entry.getValue();

				if (srgMethodOwnersAndDescs.get(srgName) == null) {
					System.out.println("Method exists in CSV but not in SRG: " + srgName + " (CSV name: " + mcpName + ")");
				} else {
					for (String srgOwnerAndDesc : srgMethodOwnersAndDescs.get(srgName)) {
						String srgDesc = srgOwnerAndDesc.substring(srgOwnerAndDesc.indexOf('('));
						String srgOwner = srgOwnerAndDesc.substring(0, srgOwnerAndDesc.indexOf('('));
						String mcpOwner = srgOwner;
						String mcpDesc = srgDesc;

						forwardCSV.setMethod(srgOwner, srgName, srgDesc, mcpName);
						reverseCSV.setMethod(mcpOwner, mcpName, mcpDesc, srgName);
					}
				}
			}
		}

		public Mapping getReverseSRG() {
			return reverseSRG;
		}

		public Mapping getReverseCSV() {
			return reverseCSV;
		}

		public Mapping getForwardSRG() {
			return forwardSRG;
		}

		public Mapping getForwardCSV() {
			return forwardCSV;
		}

		public static String getMCVer(File mcpDir) throws IOException {
			Scanner in = new Scanner(new File(mcpDir, "version.cfg"));
			try {
				while (in.hasNextLine()) {
					String line = in.nextLine();
					if (line.startsWith("ClientVersion")) {
						return line.split("=")[1].trim();
					}
				}
				return "unknown";
			} finally {
				if (in != null) {
					in.close();
				}
			}
		}
	}

	public static abstract class CsvFile {

		/** Does not close <var>r</var>. */
		public static Map<String, String> read(Reader r, int[] n_sides) throws IOException {
			Map<String, String> data = new HashMap<String, String>();

			@SuppressWarnings("resource")
			Scanner in = new Scanner(r);

			in.useDelimiter(",");
			while (in.hasNextLine()) {
				String searge = in.next();
				String name = in.next();
				String side = in.next();
				/*String desc =*/in.nextLine();
				try {
					if (CsvFile.sideIn(Integer.parseInt(side), n_sides)) {
						data.put(searge, name);
					}
				} catch (NumberFormatException e) {}
			}
			return data;
		}

		@Deprecated
		public static Map<String, String> read(File f, int[] n_sides) throws IOException {
			Reader r = new BufferedReader(new FileReader(f));
			try {
				return CsvFile.read(r, n_sides);
			} finally {
				if (r != null) {
					r.close();
				}
			}
		}

		private static boolean sideIn(int i, int[] ar) {
			for (int n : ar) {
				if (n == i) {
					return true;
				}
			}
			return false;
		}
	}

	public static class ExcFile {

		public Map<String, String[]> exceptions = new HashMap<String, String[]>();

		private static String[] EMPTY_STRING_ARRAY = new String[0];

		// returns internal names, can return null
		// input uses SRG names
		public String[] getExceptionClasses(String clazz, String func, String desc) {
			String[] r = exceptions.get(clazz + "/" + func + desc);
			if (r == null) {
				return ExcFile.EMPTY_STRING_ARRAY;
			}
			return r;
		}

		private ExcFile() {}

		public static ExcFile read(InputStream in) throws IOException {
			return ExcFile.read(new InputStreamReader(in, StandardCharsets.UTF_8));
		}

		/** Does not close <var>r</var>. */
		public static ExcFile read(Reader r) throws IOException {
			//example line:
			//net/minecraft/src/NetClientHandler.<init>(Lnet/minecraft/client/Minecraft;Ljava/lang/String;I)V=java/net/UnknownHostException,java/io/IOException|p_i42_1_,p_i42_2_,p_i42_3_

			ExcFile rv = new ExcFile();

			@SuppressWarnings("resource")
			Scanner in = new Scanner(r);
			while (in.hasNextLine()) {
				String line = in.nextLine();

				if (line.startsWith("#")) {
					continue;
				}

				if (line.contains("-Access=")) {
					continue;
				}

				if (line.contains("=CL_")) {
					continue;
				}

				int i = line.indexOf('.');
				if (i < 0) {
					continue;
				}
				String clazz = line.substring(0, i);
				line = line.substring(i + 1);

				i = line.indexOf('(');
				String func = line.substring(0, i);
				line = line.substring(i + 1);

				i = line.indexOf('=');
				String desc = line.substring(0, i);
				line = line.substring(i + 1);

				i = line.indexOf('|');
				String excs = line.substring(0, i);
				line = line.substring(i + 1);

				if (excs.contains("CL_")) {
					throw new RuntimeException(excs);
				}

				rv.exceptions.put(clazz + "/" + func + desc, excs.split(","));
			}
			return rv;
		}

		@Deprecated
		public ExcFile(File f) throws IOException {
			FileReader fr = new FileReader(f);
			try {
				exceptions = ExcFile.read(fr).exceptions;
			} finally {
				if (fr != null) {
					fr.close();
				}
			}
		}
	}

	public static class SrgFile {

		public Map<String, String> classes = new HashMap<String, String>(); // name -> name
		public Map<String, String> fields = new HashMap<String, String>(); // owner/name -> name
		public Map<String, String> methods = new HashMap<String, String>(); // owner/namedesc -> name

		public static String getLastComponent(String s) {
			String[] parts = s.split("/");
			return parts[parts.length - 1];
		}

		private SrgFile() {}

		/** Does not close <var>r</var>. */
		public static SrgFile read(Reader r, boolean reverse) throws IOException {
			@SuppressWarnings("resource")
			Scanner in = new Scanner(r);
			SrgFile rv = new SrgFile();
			while (in.hasNextLine()) {
				if (in.hasNext("CL:")) {
					in.next();
					String obf = in.next();
					String deobf = in.next();
					if (reverse) {
						rv.classes.put(deobf, obf);
					} else {
						rv.classes.put(obf, deobf);
					}
				} else if (in.hasNext("FD:")) {
					in.next();
					String obf = in.next();
					String deobf = in.next();
					if (reverse) {
						rv.fields.put(deobf, SrgFile.getLastComponent(obf));
					} else {
						rv.fields.put(obf, SrgFile.getLastComponent(deobf));
					}
				} else if (in.hasNext("MD:")) {
					in.next();
					String obf = in.next();
					String obfdesc = in.next();
					String deobf = in.next();
					String deobfdesc = in.next();
					if (reverse) {
						rv.methods.put(deobf + deobfdesc, SrgFile.getLastComponent(obf));
					} else {
						rv.methods.put(obf + obfdesc, SrgFile.getLastComponent(deobf));
					}
				} else {
					in.nextLine();
				}
			}
			return rv;
		}

		@Deprecated
		public SrgFile(File f, boolean reverse) throws IOException {
			FileReader fr = new FileReader(f);
			try {
				SrgFile sf = SrgFile.read(new BufferedReader(fr), reverse);
				classes = sf.classes;
				fields = sf.fields;
				methods = sf.methods;
			} finally {
				if (fr != null) {
					fr.close();
				}
			}
		}

		public Mapping toMapping(NameSet fromNS, NameSet toNS) {
			Mapping m = new Mapping(fromNS, toNS);

			for (Map.Entry<String, String> entry : classes.entrySet()) {
				m.setClass(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, String> entry : fields.entrySet()) {
				int i = entry.getKey().lastIndexOf('/');
				m.setField(entry.getKey().substring(0, i), entry.getKey().substring(i + 1), entry.getValue());
			}

			for (Map.Entry<String, String> entry : methods.entrySet()) {
				int i = entry.getKey().lastIndexOf('(');
				String desc = entry.getKey().substring(i);
				String classandname = entry.getKey().substring(0, i);
				i = classandname.lastIndexOf('/');
				m.setMethod(classandname.substring(0, i), classandname.substring(i + 1), desc, entry.getValue());
			}

			return m;
		}
	}
}
