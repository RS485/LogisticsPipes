package logisticspipes.proxy.cc;

import dan200.computercraft.api.lua.ILuaObject;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import logisticspipes.proxy.cc.wrapper.CCCommandWrapper;
import logisticspipes.proxy.computers.interfaces.ICCTypeWrapped;

public class LPASMHookCC {

	public static boolean handleCCWrappedILuaObject(ILuaObject object) {
		if (object instanceof CCCommandWrapper) {
			return ((CCCommandWrapper) object).table != null;
		}
		return false;
	}

	public static LuaTable returnCCWrappedILuaObject(ILuaObject object) {
		return ((CCCommandWrapper) object).table;
	}

	public static LuaTable onCCWrappedILuaObject(final LuaTable table, final ILuaObject object) {
		if (object instanceof CCCommandWrapper) {
			LuaTable lpTable = new LPLuaTable((CCCommandWrapper) object);
			LuaValue k = LuaValue.NIL;
			while (true) {
				Varargs n = table.next(k);
				if ((k = n.arg1()).isnil()) {
					break;
				}
				LuaValue v = n.arg(2);
				lpTable.set(k, v);
			}
			((CCCommandWrapper) object).table = lpTable;
			return lpTable;
		}
		return table;
	}

	public static boolean handleCCToObject(LuaValue value) {
		if (value.type() != LuaValue.TTABLE) {
			return false;
		}
		if (!(value instanceof LPLuaTable)) {
			return false;
		}

		LPLuaTable table = (LPLuaTable) value;
		return table.wrapper.getObject() != null;
	}

	public static Object returnCCToObject(LuaValue value) {
		Object object = ((LPLuaTable) value).wrapper.getObject();
		if (object instanceof ICCTypeWrapped) {
			object = ((ICCTypeWrapped) object).getObject();
		}
		return object;
	}

	private static class LPLuaTable extends LuaTable {

		final CCCommandWrapper wrapper;

		public LPLuaTable(CCCommandWrapper wrapper) {
			this.wrapper = wrapper;
		}

		@Override
		public String tojstring() {
			return wrapper.getType() + " [" + super.tojstring() + "]";
		}
	}
}
