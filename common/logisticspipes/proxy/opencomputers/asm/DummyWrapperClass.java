package logisticspipes.proxy.opencomputers.asm;

import li.cil.oc.api.network.Arguments;
import li.cil.oc.api.network.Callback;
import li.cil.oc.api.network.Context;
import logisticspipes.proxy.computers.wrapper.CCWrapperInformation;

public class DummyWrapperClass extends BaseWrapperClass {
	public DummyWrapperClass() throws ClassNotFoundException {
		super("dummy.class.replace.automaticly");
	}
	
	public DummyWrapperClass(CCWrapperInformation info, Object object) {
		super(info, object);
	}
	
	@Callback(direct = true, doc="Dummy documentation")
	public Object[] dummyCall(Context context, Arguments args) throws Exception {
		return this.invokeMethod("dummyCall", context, args);
	}
}
