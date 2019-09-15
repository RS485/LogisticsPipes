package logisticspipes.utils;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class FinalNBTTagCompound extends CompoundTag {

	private boolean constructing;

	public FinalNBTTagCompound(CompoundTag base) {
		super();
		constructing = true;
		super.copyFrom(base);
		constructing = false;
	}

	@Nonnull
	@Override
	public Set<String> getKeys() {
		return Collections.unmodifiableSet(super.getKeys());
	}

	@Override
	public void putBoolean(@Nonnull String key, boolean value) {
		if (constructing) super.putBoolean(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void putByte(@Nonnull String key, byte value) {
		if (constructing) super.putByte(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void putByteArray(@Nonnull String key, @Nonnull byte[] value) {
		if (constructing) super.putByteArray(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void putDouble(@Nonnull String key, double value) {
		if (constructing) super.putDouble(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void putFloat(@Nonnull String key, float value) {
		if (constructing) super.putFloat(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void putIntArray(@Nonnull String key, @Nonnull int[] value) {
		if (constructing) super.putIntArray(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void putInt(@Nonnull String key, int value) {
		if (constructing) super.putInt(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void putLong(@Nonnull String key, long value) {
		if (constructing) super.putLong(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void putShort(@Nonnull String key, short value) {
		if (constructing) super.putShort(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void putString(@Nonnull String key, String value) {
		if (constructing) super.putString(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public Tag put(@Nonnull String key, @Nonnull Tag value) {
		if (constructing) return super.put(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void putUuid(String key, UUID value) {
		if (constructing) super.putUuid(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public CompoundTag copyFrom(CompoundTag other) {
		if (constructing) return super.copyFrom(other);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void remove(@Nonnull String key) {
		if (constructing) super.remove(key);
		else throw new UnsupportedOperationException();
	}

}
