package logisticspipes.request.resources;

import java.io.IOException;

import logisticspipes.network.LPDataOutputStream;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * With Destination and amount
 */
public interface IResource extends ILPCCTypeHolder {

	ItemIdentifier getAsItem();

	/**
	 * Settings only apply for the normal Item Implementation.
	 */
	enum MatchSettings {
		NORMAL,
		WITHOUT_NBT;
	}

	int getRequestedAmount();

	IRouter getRouter();

	boolean matches(ItemIdentifier itemType, MatchSettings settings);

	IResource clone(int multiplier);

	void writeData(LPDataOutputStream data) throws IOException;

	boolean mergeForDisplay(IResource resource, int withAmount); //Amount overrides existing amount inside the resource

	IResource copyForDisplayWith(int amount);

	@SideOnly(Side.CLIENT)
	String getDisplayText(ColorCode missing);

	ItemIdentifierStack getDisplayItem();

	enum ColorCode {
		NONE,
		MISSING,
		SUCCESS;
	}
}
