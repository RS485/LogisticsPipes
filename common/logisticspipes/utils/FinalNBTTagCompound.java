package logisticspipes.utils;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class FinalNBTTagCompound extends NBTTagCompound {

	public FinalNBTTagCompound(NBTTagCompound base) {
		super.merge(base);
	}

	@Nonnull
	@Override
	public Set<String> getKeySet() {
		return Collections.unmodifiableSet(super.getKeySet());
	}

	@Override
	public void setBoolean(@Nonnull String key, boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setByte(@Nonnull String key, byte value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setByteArray(@Nonnull String key, @Nonnull byte[] value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDouble(@Nonnull String key, double value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFloat(@Nonnull String key, float value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIntArray(@Nonnull String key, @Nonnull int[] value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setInteger(@Nonnull String key, int value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLong(@Nonnull String key, long value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setShort(@Nonnull String key, short value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setString(@Nonnull String key, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setTag(@Nonnull String key, @Nonnull NBTBase value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setUniqueId(String key, UUID value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void merge(NBTTagCompound other) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeTag(@Nonnull String key) {
		throw new UnsupportedOperationException();
	}

}
