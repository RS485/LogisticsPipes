package logisticspipes.request.resources;

import javax.annotation.Nonnull;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.util.LPFinalSerializable;

/**
 * With Destination and amount
 */
public interface IResource extends ILPCCTypeHolder, LPFinalSerializable {

	ItemIdentifier getAsItem();

	int getRequestedAmount();

	@Nonnull
	IRouter getRouter();

	boolean matches(ItemIdentifier itemType, MatchSettings settings);

	IResource clone(int multiplier);

	void writeData(LPDataOutput output);

	boolean mergeForDisplay(IResource resource, int withAmount); //Amount overrides existing amount inside the resource

	IResource copyForDisplayWith(int amount);

	@SideOnly(Side.CLIENT)
	String getDisplayText(ColorCode missing);

	ItemIdentifierStack getDisplayItem();

	@Override
	default void write(LPDataOutput output) {
		ResourceNetwork.writeResource(output, this);
	}

	/**
	 * Settings only apply for the normal Item Implementation.
	 */
	enum MatchSettings {
		NORMAL,
		WITHOUT_NBT
	}

	enum ColorCode {
		NONE,
		MISSING,
		SUCCESS
	}
}
