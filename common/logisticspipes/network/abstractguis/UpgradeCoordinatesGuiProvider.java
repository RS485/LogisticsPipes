package logisticspipes.network.abstractguis;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.exception.TargetNotFoundException;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.gui.DummyContainer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.io.IOException;

@Accessors(chain = true)
public abstract class UpgradeCoordinatesGuiProvider extends CoordinatesPopupGuiProvider {

	public UpgradeCoordinatesGuiProvider(int id) {
		super(id);
	}

	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PRIVATE)
	private int positionInt;

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeInt(positionInt);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		positionInt = data.readInt();
	}

	public UpgradeCoordinatesGuiProvider setSlot(Slot slot) {
		this.setPositionInt(slot.slotNumber);
		return this;
	}

	public <T extends Slot> T getSlot(EntityPlayer player, Class<T> clazz) {
		if(player.openContainer instanceof DummyContainer) {
			if (positionInt >= player.openContainer.inventorySlots.size()) {
				targetNotFound("The requested Slot was out of range");
			} else {
				Slot slot = player.openContainer.getSlot(positionInt);
				if (slot == null) {
					targetNotFound("The requested Slot was null");
				} else if(!clazz.isAssignableFrom(slot.getClass())) {
					targetNotFound("Couldn't find " + clazz.getName() + ", found slot with " + slot.getClass());
				} else {
					return (T) slot;
				}
			}
		}
		return null;
	}

	protected void targetNotFound(String message) {
		throw new TargetNotFoundException(message, this);
	}
}
