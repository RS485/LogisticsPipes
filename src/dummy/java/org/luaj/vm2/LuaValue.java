package org.luaj.vm2;

public abstract class LuaValue {

	public static final LuaValue NIL = LuaNil._NIL;
	public static final int TTABLE = 5;

	public boolean isnil() {
		return false;
	}

	public String tojstring() {
		return null;
	}

	abstract public int type();
}
