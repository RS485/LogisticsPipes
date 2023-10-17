package org.luaj.vm2;

public class LuaNil extends LuaValue {

	static final LuaNil _NIL = new LuaNil();

	@Override
	public int type() {
		return 0;
	}

}
