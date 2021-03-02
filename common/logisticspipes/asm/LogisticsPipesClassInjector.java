package logisticspipes.asm;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import com.google.common.io.BaseEncoding;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.computers.wrapper.CCObjectWrapper;
import logisticspipes.proxy.opencomputers.asm.ClassCreator;
import logisticspipes.utils.ModStatusHelper;

public class LogisticsPipesClassInjector implements IClassTransformer {

	private Field fResourceCache;
	private Boolean isObfEnv = null;

	public LogisticsPipesClassInjector() throws NoSuchFieldException, SecurityException {
		fResourceCache = LaunchClassLoader.class.getDeclaredField("resourceCache");
		fResourceCache.setAccessible(true);
	}

	@Override
	@SuppressWarnings("unchecked")
	public byte[] transform(String name, String transformedName, byte[] bytes) {
		if (bytes != null) {
			if (name.startsWith("logisticspipes.")) {
				final ClassReader reader = new ClassReader(bytes);
				final ClassNode node = new ClassNode();
				reader.accept(node, 0);
				if (node.visibleAnnotations != null) {
					for (AnnotationNode a : node.visibleAnnotations) {
						if (a.desc.equals("Llogisticspipes/asm/ModVersionedClass;")) {
							if (a.values.size() == 8 && a.values.get(0).equals("modId") && a.values.get(2).equals("version") && a.values.get(4).equals("classData") && a.values.get(6).equals("classDataDev")) {
								String modId = a.values.get(1).toString();
								String version = a.values.get(3).toString();
								String classData = a.values.get(5).toString();
								String classDataDev = a.values.get(7).toString();
								if (ModStatusHelper.isModLoaded(modId) && !ModStatusHelper.isModVersionEqualsOrHigher(modId, version)) {
									if (isObfEnv == null) {
										try {
											isObfEnv = (Class.forName("net.minecraft.world.World").getDeclaredField("chunkProvider") == null);
										} catch (Throwable e) {
											isObfEnv = true;
										}
									}
									bytes = transform(name, transformedName, BaseEncoding.base64().decode(isObfEnv ? classData : classDataDev));
								}
							} else {
								throw new UnsupportedOperationException("Can't parse the annotations correctly");
							}
						}
					}
				}
			}
			return bytes;
		}
		try {
			if (name.startsWith("logisticspipes.proxy.opencomputers.asm.BaseWrapperClass$") && name.endsWith("$OpenComputersWrapper")) {
				String correctName = name.substring(56, name.length() - 21);
				Class<?> clazz = Launch.classLoader.findClass(correctName);
				bytes = ClassCreator.getWrappedClassAsBytes(CCObjectWrapper.getWrapperInformation(clazz), clazz.getName());
				Set<String> set = new TreeSet<>();
				set.add(name);
				Launch.classLoader.clearNegativeEntries(set);
				Map<String, byte[]> map = (Map<String, byte[]>) fResourceCache.get(Launch.classLoader);
				map.put(name, bytes);
				return bytes;
			}
		} catch (Exception e) {
			if (LogisticsPipes.isDEBUG()) { // For better Debugging
				e.printStackTrace();
				return bytes;
			}
			throw new RuntimeException(e);
		}
		return bytes;
	}
}
