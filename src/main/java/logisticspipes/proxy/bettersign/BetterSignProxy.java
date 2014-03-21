package logisticspipes.proxy.bettersign;

import java.lang.reflect.Field;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSign;
import logisticspipes.proxy.interfaces.IBetterSignProxy;

public class BetterSignProxy implements IBetterSignProxy {

	private Field signStickVertical;
	private Field signStickHorizontal;
	
	public BetterSignProxy() {
		try {
			signStickVertical = ModelSign.class.getDeclaredField("signStickVertical");
			signStickHorizontal = ModelSign.class.getDeclaredField("signStickHorizontal");
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void hideSignSticks(ModelSign model) {
		try {
			resetModel((ModelRenderer) signStickVertical.get(model));
			resetModel((ModelRenderer) signStickHorizontal.get(model));
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void resetModel(ModelRenderer m) {
		m.showModel = false;
	}
}
