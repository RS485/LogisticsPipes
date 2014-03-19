package codechicken.multipart;

import net.minecraft.tileentity.TileEntity;
import scala.collection.Seq;

public class TileMultipart extends TileEntity {
	public Seq partList() {return null;}
	public boolean occlusionTest(Seq partList, TMultiPart partOcclusionTest) {return false;}
}
