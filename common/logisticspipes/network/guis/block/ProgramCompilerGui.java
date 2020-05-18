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
		return new GuiProgramCompiler(player, getTileAs(player.world, LogisticsProgramCompilerTileEntity.class));
	}

	@Override
	public Container getContainer(EntityPlayer player) {
		LogisticsProgramCompilerTileEntity compilerBlock = getTileAs(player.world, LogisticsProgramCompilerTileEntity.class);
		DummyContainer dummy = new DummyContainer(player, null, compilerBlock);

		dummy.addRestrictedSlot(0, compilerBlock.getInventory(), 10, 10, LPItems.disk);
		dummy.addRestrictedSlot(1, compilerBlock.getInventory(), 154, 10, LPItems.logisticsProgrammer);

		dummy.addNormalSlotsForPlayerInventory(10, 45);
		return dummy;
	}

	@Override
	public GuiProvider template() {
		return new ProgramCompilerGui(getId());
	}
}
