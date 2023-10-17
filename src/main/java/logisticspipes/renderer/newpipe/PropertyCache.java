package logisticspipes.renderer.newpipe;

import net.minecraftforge.common.property.IUnlistedProperty;

import com.google.common.cache.Cache;

import logisticspipes.renderer.state.PipeRenderState;

public class PropertyCache implements IUnlistedProperty<Cache<PipeRenderState.LocalCacheType, Object>> {

	@Override
	public String getName() {
		return "lpcache";
	}

	@Override
	public boolean isValid(Cache<PipeRenderState.LocalCacheType, Object> value) {
		return value != null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class<Cache<PipeRenderState.LocalCacheType, Object>> getType() {
		return (Class) Cache.class;
	}

	@Override
	public String valueToString(Cache<PipeRenderState.LocalCacheType, Object> value) {
		return value.toString();
	}

}
