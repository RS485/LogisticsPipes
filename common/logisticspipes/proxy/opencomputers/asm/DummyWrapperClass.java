package logisticspipes.proxy.opencomputers.asm;

import logisticspipes.proxy.computers.wrapper.CCWrapperInformation;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

public class DummyWrapperClass extends BaseWrapperClass {

	public DummyWrapperClass() throws ClassNotFoundException {
		super("dummy.class.replace.automaticly");
	}

	public DummyWrapperClass(CCWrapperInformation info, Object object) {
		super(info, object);
	}

	@Callback(direct = true, doc = "Dummy documentation")
	public Object[] dummyCall(Context context, Arguments args) throws Exception {
		return invokeMethod("dummyCall", context, args);
	}
}
