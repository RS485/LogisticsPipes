package logisticspipes.utils;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class FinalNBTTagCompound extends NBTTagCompound {
	private boolean constructing;
	public FinalNBTTagCompound(NBTTagCompound base) {
		super();
		constructing = true;
		super.merge(base);
		constructing = false;
	}

	@Nonnull
	@Override
	public Set<String> getKeySet() {
		return Collections.unmodifiableSet(super.getKeySet());
	}

	@Override
	public void setBoolean(@Nonnull String key, boolean value) {
		if(constructing)super.setBoolean(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void setByte(@Nonnull String key, byte value) {
		if(constructing)super.setByte(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void setByteArray(@Nonnull String key, @Nonnull byte[] value) {
		if(constructing)super.setByteArray(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void setDouble(@Nonnull String key, double value) {
		if(constructing)super.setDouble(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void setFloat(@Nonnull String key, float value) {
		if(constructing)super.setFloat(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void setIntArray(@Nonnull String key, @Nonnull int[] value) {
		if(constructing)super.setIntArray(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void setInteger(@Nonnull String key, int value) {
		if(constructing)super.setInteger(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void setLong(@Nonnull String key, long value) {
		if(constructing)super.setLong(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void setShort(@Nonnull String key, short value) {
		if(constructing)super.setShort(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void setString(@Nonnull String key, String value) {
		if(constructing)super.setString(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void setTag(@Nonnull String key, @Nonnull NBTBase value) {
		if(constructing)super.setTag(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void setUniqueId(String key, UUID value) {
		if(constructing)super.setUniqueId(key, value);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void merge(NBTTagCompound other) {
		if(constructing)super.merge(other);
		else throw new UnsupportedOperationException();
	}

	@Override
	public void removeTag(@Nonnull String key) {
		if(constructing)super.removeTag(key);
		else throw new UnsupportedOperationException();
	}
}
