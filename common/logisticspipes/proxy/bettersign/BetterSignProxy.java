package logisticspipes.proxy.bettersign;

import java.lang.reflect.Field;

import logisticspipes.proxy.interfaces.IBetterSignProxy;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSign;

public class BetterSignProxy implements IBetterSignProxy {

	private Field signStickVertical = null;
	private Field signStickHorizontal = null;

	@Override
	public void hideSignSticks(ModelSign model) {
		if(signStickVertical == null) {
			try {
				signStickVertical = ModelSign.class.getDeclaredField("signStickVertical");
				signStickHorizontal = ModelSign.class.getDeclaredField("signStickHorizontal");
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
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
