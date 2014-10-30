package logisticspipes.asm;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import logisticspipes.LPConstants;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import org.apache.logging.log4j.Level;

import com.google.common.base.Strings;
import com.google.common.collect.ObjectArrays;
import com.google.common.primitives.Ints;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;
import cpw.mods.fml.common.asm.transformers.ModAccessTransformer;
import cpw.mods.fml.relauncher.CoreModManager;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.FileListHelper;

public class DevEnvHelper {
	private static final Attributes.Name	COREMODCONTAINSFMLMOD	= new Attributes.Name("FMLCorePluginContainsFMLMod");
	
	@SuppressWarnings("unchecked")
	public static void detectCoreModInEclipseSettings() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException, IOException {
		if(!LPConstants.DEBUG || !new File(".classpath").exists()) return;
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
		File coreMods = (File)setupCoreModDir.invoke(null, mcDir.get(null));
		FilenameFilter ff = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		};
		File[] FMLcoreModListArray = coreMods.listFiles(ff);
		File versionedModDir = new File(coreMods, "1.7.10");
		if(versionedModDir.isDirectory()) {
			File[] versionedCoreMods = versionedModDir.listFiles(ff);
			FMLcoreModListArray = ObjectArrays.concat(FMLcoreModListArray, versionedCoreMods, File.class);
		}
		
		List<String> FMLcoreModList = new ArrayList<String>();
		
		for(File f:FMLcoreModListArray) {
			FMLcoreModList.add(f.getName());
		}

		List<File> coreModList = new ArrayList<File>();
		
		for(URL path:classLoader.getURLs()) {
			File file = new File(URLDecoder.decode(path.getFile()));
			if(!FMLcoreModList.contains(file.getName()) && file.getName().endsWith(".jar")) {
				coreModList.add(file);
			}
		}

		coreModList = Arrays.asList(FileListHelper.sortFileList(coreModList.toArray(new File[coreModList.size()])));
		
		for(File coreMod: coreModList) {
			FMLRelaunchLog.fine("Examining for coremod candidacy %s", coreMod.getName());
			JarFile jar = null;
			Attributes mfAttributes;
			try {
				jar = new JarFile(coreMod);
				if(jar.getManifest() == null) {
					// Not a coremod and no access transformer list
					continue;
				}
				ModAccessTransformer.addJar(jar);
				mfAttributes = jar.getManifest().getMainAttributes();
			} catch(IOException ioe) {
				FMLRelaunchLog.log(Level.ERROR, ioe, "Unable to read the jar file %s - ignoring", coreMod.getName());
				continue;
			} finally {
				if(jar != null) {
					try {
						jar.close();
					} catch(IOException e) {
						// Noise
					}
				}
			}
			//AccessTransformer //For NEI
			if(mfAttributes.getValue("AccessTransformer") != null) {
				String cfg = mfAttributes.getValue("AccessTransformer");
				((List<IClassTransformer>)transformers.get(classLoader)).add(new AccessTransformer(cfg){});
			}
			String cascadedTweaker = mfAttributes.getValue("TweakClass");
			if(cascadedTweaker != null) {
				FMLRelaunchLog.info("Loading tweaker %s from %s", cascadedTweaker, coreMod.getName());
				Integer sortOrder = Ints.tryParse(Strings.nullToEmpty(mfAttributes.getValue("TweakOrder")));
				sortOrder = (sortOrder == null ? Integer.valueOf(0) : sortOrder);
				handleCascadingTweak.invoke(null, coreMod, jar, cascadedTweaker, classLoader, sortOrder);
				((List<String>)loadedCoremods.get(null)).add(coreMod.getName());
				continue;
			}
			
			String fmlCorePlugin = mfAttributes.getValue("FMLCorePlugin");
			if(fmlCorePlugin == null) {
				// Not a coremod
				FMLRelaunchLog.fine("Not found coremod data in %s", coreMod.getName());
				continue;
			}
			
			//try {
				//classLoader.addURL(coreMod.toURI().toURL());
				if(!mfAttributes.containsKey(COREMODCONTAINSFMLMOD)) {
					FMLRelaunchLog.finer("Adding %s to the list of known coremods, it will not be examined again", coreMod.getName());
					((List<String>)loadedCoremods.get(null)).add(coreMod.getName());
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
	}
}
