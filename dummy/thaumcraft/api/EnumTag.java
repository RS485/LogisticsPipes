package thaumcraft.api;

//Dummy Class
public enum EnumTag {
	UNKNOWN(63,"Obscurus","Unknown, Obscured",1,false,0x282828),
	;

	public final int id;
	public final String name;

	private EnumTag(int id, String name, String meaning, int element,boolean aggro, int color ) {
		this.id = id;
		this.name = name;
	}

	public static EnumTag get(int id) {
		return EnumTag.UNKNOWN;
	}
}
