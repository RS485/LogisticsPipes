package logisticspipes.network.guis.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import logisticspipes.LPItems;
import logisticspipes.blocks.LogisticsProgramCompilerTileEntity;
import logisticspipes.gui.GuiProgramCompiler;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.DummyContainer;

@StaticResolve
public class ProgramCompilerGui extends CoordinatesGuiProvider {

	public ProgramCompilerGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsProgramCompilerTileEntity tile = this.getTile(player.getEntityWorld(), LogisticsProgramCompilerTileEntity.class);
		if (tile == null) {
			return null;
		}
		return new GuiProgramCompiler(player, tile);
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		LogisticsProgramCompilerTileEntity tile = this.getTile(player.getEntityWorld(), LogisticsProgramCompilerTileEntity.class);
		if (tile == null) {
			return null;
		}
		DummyContainer dummy = new DummyContainer(player, null, tile);

		dummy.addRestrictedSlot(0, tile.getInventory(), 10, 10, LPItems.disk);
		dummy.addRestrictedSlot(1, tile.getInventory(), 154, 10, LPItems.logisticsProgrammer);

		dummy.addNormalSlotsForPlayerInventory(10, 45);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new ProgramCompilerGui(getId());
	}
}
