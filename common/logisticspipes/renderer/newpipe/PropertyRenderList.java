package logisticspipes.renderer.newpipe;

import java.util.List;

import net.minecraftforge.common.property.IUnlistedProperty;

public class PropertyRenderList implements IUnlistedProperty<List<RenderEntry>> {

	@Override
	public String getName() {
		return "lprenderentrylist";
	}

	@Override
	public boolean isValid(List<RenderEntry> value) {
		return value != null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class<List<RenderEntry>> getType() {
		return (Class) List.class;
	}

	@Override
	public String valueToString(List<RenderEntry> value) {
		return value.toString();
	}

}
