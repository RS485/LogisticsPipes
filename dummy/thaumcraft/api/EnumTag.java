package thaumcraft.api;

//Dummy Class
public enum EnumTag {
	UNKNOWN(63,"Obscurus","Unknown, Obscured",1,false,0x282828),
	;
	
	public String name;
	private EnumTag(int id, String name, String meaning, int element,boolean aggro, int color ) {
	}

	public static EnumTag get(Integer integer) {
		return EnumTag.UNKNOWN;
	}
}
