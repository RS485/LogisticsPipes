package appeng.api.me.tiles;

/**
 * Used to signify which color a particular IGridTileEntity or IGridMachine is, you must implement both, if you wish to have color bias.
 */
public interface IColoredMETile
{
	public static String[] Colors = {
		"Blue",
		"Black",
		"White",
		"Brown",
		"Red",
		"Yellow",
		"Green"
	};
	
	boolean isColored();
	
	void setColor( int offset );

	int getColor();
}
