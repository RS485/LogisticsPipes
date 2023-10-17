package logisticspipes.asm;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.launchwrapper.IClassTransformer;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

import com.google.common.collect.Multimap;

public class ModAccessTransformerRemapper {

	private final Map<String, Map<String, String>> fieldMappings;
	private final Map<String, Map<String, String>> methodMappings;
	private final Field modifiersField;

	private Field modifierDesc;
	private Field modifierName;

	@SuppressWarnings("unchecked")
	public ModAccessTransformerRemapper() {
		try {
			final Field rawFieldMaps = FMLDeobfuscatingRemapper.class.getDeclaredField("rawFieldMaps");
			final boolean wasAccessible = rawFieldMaps.isAccessible();
			if (!wasAccessible) rawFieldMaps.setAccessible(true);
			fieldMappings = (Map<String, Map<String, String>>) rawFieldMaps.get(FMLDeobfuscatingRemapper.INSTANCE);
			if (!wasAccessible) rawFieldMaps.setAccessible(false);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new IllegalStateException("Could not access rawFieldMaps of FMLDeobfuscatingRemapper", e);
		}

		try {
			final Field rawMethodMaps = FMLDeobfuscatingRemapper.class.getDeclaredField("rawMethodMaps");
			final boolean wasAccessible = rawMethodMaps.isAccessible();
			if (!wasAccessible) rawMethodMaps.setAccessible(true);
			methodMappings = (Map<String, Map<String, String>>) rawMethodMaps.get(FMLDeobfuscatingRemapper.INSTANCE);
			if (!wasAccessible) rawMethodMaps.setAccessible(false);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new IllegalStateException("Could not access rawMethodMaps of FMLDeobfuscatingRemapper", e);
		}

		try {
			modifiersField = AccessTransformer.class.getDeclaredField("modifiers");
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException("Could not access modifiers field of AccessTransformer", e);
		}
	}

	public void apply(IClassTransformer modAT) {
		final Multimap<String, ?> modifiersMap = getModifiers(modAT);
		if (modifiersMap.isEmpty()) return;

		modifiersMap.forEach(this::applySingleModifier);

		if (modifierDesc != null) {
			modifierDesc.setAccessible(false);
			modifierDesc = null;
		}
		if (modifierName != null) {
			modifierName.setAccessible(false);
			modifierName = null;
		}
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	private Multimap<String, ?> getModifiers(IClassTransformer modAT) {
		final boolean wasAccessible = modifiersField.isAccessible();
		if (!wasAccessible) modifiersField.setAccessible(true);
		final Multimap<String, ?> modifiersMap; // String to AccessTransformer.Modifier
		try {
			modifiersMap = (Multimap<String, ?>) modifiersField.get(modAT);
		} catch (IllegalAccessException | ClassCastException e) {
			throw new IllegalStateException("Could not access modifiers field of " + modAT, e);
		} finally {
			if (!wasAccessible) modifiersField.setAccessible(false);
		}
		return modifiersMap;
	}

	private void applySingleModifier(String className, Object modifier) {
		final String classKey = className.replaceAll("\\.", "/");
		final String desc, name;
		try {
			name = getName(modifier);
			if (name.equals("*")) return; // doesn't need mapping
			desc = getDesc(modifier);
		} catch (RuntimeException e) {
			e.printStackTrace();
			return;
		}

		if (desc.isEmpty() && fieldMappings.containsKey(classKey)) {
			final Map<String, String> classFieldMappings = fieldMappings.get(classKey);
			classFieldMappings.keySet().stream()
					.filter(obfNameWithType -> obfNameWithType.startsWith(name + ":"))
					.findAny()
					.ifPresent(obfNameWithType -> {
						final String newName = classFieldMappings.get(obfNameWithType);
						System.out.printf("Remapping class %s field %s -> %s%n", className, name, newName);
						setName(modifier, newName);
					});
		} else if (methodMappings.containsKey(classKey)) {
			final Map<String, String> classMethodMappings = methodMappings.get(classKey);
			classMethodMappings.keySet().stream()
					.filter(obfNameWithType -> obfNameWithType.startsWith(name + "(") && obfNameWithType.endsWith(desc))
					.findAny()
					.ifPresent(obfNameWithType -> {
						final String newName = classMethodMappings.get(obfNameWithType);
						System.out.printf("Remapping class %s method %s %s -> %s%n", className, name, desc, newName);
						setName(modifier, newName);
					});
		}
	}

	private String getDesc(Object modifier) {
		Objects.requireNonNull(modifier, "Modifier may not be null");
		ensureModifierDesc(modifier);
		try {
			return (String) modifierDesc.get(modifier);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Could not access desc field", e);
		}
	}

	private void ensureModifierDesc(@Nonnull Object modifier) {
		if (modifierDesc == null) {
			try {
				modifierDesc = modifier.getClass().getDeclaredField("desc");
			} catch (NoSuchFieldException e) {
				throw new IllegalStateException("Could not find desc field", e);
			}
		}
		if (!modifierDesc.isAccessible()) modifierDesc.setAccessible(true);
	}

	private String getName(Object modifier) {
		Objects.requireNonNull(modifier, "Modifier may not be null");
		ensureModifierName(modifier);
		try {
			return (String) modifierName.get(modifier);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Could not access name field", e);
		}
	}

	private void setName(Object modifier, String newName) {
		Objects.requireNonNull(modifier, "Modifier may not be null");
		ensureModifierName(modifier);
		try {
			modifierName.set(modifier, newName);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Could not access name field", e);
		}
	}

	private void ensureModifierName(@Nonnull Object modifier) {
		if (modifierName == null) {
			try {
				modifierName = modifier.getClass().getDeclaredField("name");

			} catch (NoSuchFieldException e) {
				throw new IllegalStateException("Could not find name field", e);
			}
		}
		if (!modifierName.isAccessible()) modifierName.setAccessible(true);
	}
}
