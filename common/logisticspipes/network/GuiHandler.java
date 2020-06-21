package logisticspipes.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.IGuiHandler;

import logisticspipes.LPItems;
import logisticspipes.gui.GuiFirewall;
import logisticspipes.gui.GuiFluidBasic;
import logisticspipes.gui.GuiFluidSupplierMk2Pipe;
import logisticspipes.gui.GuiFluidSupplierPipe;
import logisticspipes.gui.GuiFreqCardContent;
import logisticspipes.gui.GuiProviderPipe;
import logisticspipes.gui.GuiSatellitePipe;
import logisticspipes.gui.hud.GuiHUDSettings;
import logisticspipes.gui.orderer.FluidGuiOrderer;
import logisticspipes.gui.orderer.GuiRequestTable;
import logisticspipes.gui.orderer.NormalGuiOrderer;
import logisticspipes.gui.orderer.NormalMk2GuiOrderer;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.items.ItemGuideBook;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.network.packets.pipe.FluidSupplierMinMode;
import logisticspipes.network.packets.pipe.FluidSupplierMode;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.PipeFluidBasic;
import logisticspipes.pipes.PipeFluidRequestLogistics;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeFluidSupplierMk2;
import logisticspipes.pipes.PipeItemsFirewall;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.PipeItemsSystemDestinationLogistics;
import logisticspipes.pipes.PipeItemsSystemEntranceLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, final int x, final int y, final int z) {

		TileEntity tile = null;
		if (y != -1) {
			tile = world.getTileEntity(new BlockPos(x, y, z));
		}
		LogisticsTileGenericPipe pipe = null;
		if (tile instanceof LogisticsTileGenericPipe) {
			pipe = (LogisticsTileGenericPipe) tile;
		}
		final LogisticsTileGenericPipe fpipe = pipe;

		DummyContainer dummy;
		int xOffset;
		int yOffset;

		if (ID > 10000) {
			ID -= 10000;
		}

		//Handle Module Configuration
		if (ID == -1) {
			return getServerGuiElement(100 * -20 + x, player, world, 0, -1, z);
		}

		if (ID < 110 && ID > 0) {
			switch (ID) {

				case GuiIDs.GUI_FluidSupplier_ID:
					if (pipe == null || !(pipe.pipe instanceof PipeItemsFluidSupplier)) {
						return null;
					}
					dummy = new DummyContainer(player.inventory, ((PipeItemsFluidSupplier) pipe.pipe).getDummyInventory());
					dummy.addNormalSlotsForPlayerInventory(18, 97);

					xOffset = 72;
					yOffset = 18;

					for (int row = 0; row < 3; row++) {
						for (int column = 0; column < 3; column++) {
							dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);
						}
					}

					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidSupplierMode.class).setInteger((((PipeItemsFluidSupplier) pipe.pipe).isRequestingPartials() ? 1 : 0)).setBlockPos(pipe.getPos()), player);
					return dummy;

				case GuiIDs.GUI_FluidSupplier_MK2_ID:
					if (pipe == null || !(pipe.pipe instanceof PipeFluidSupplierMk2)) {
						return null;
					}
					dummy = new DummyContainer(player.inventory, ((PipeFluidSupplierMk2) pipe.pipe).getDummyInventory());
					dummy.addNormalSlotsForPlayerInventory(18, 97);
					dummy.addFluidSlot(0, ((PipeFluidSupplierMk2) pipe.pipe).getDummyInventory(), 0, 0);

					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidSupplierMode.class).setInteger((((PipeFluidSupplierMk2) pipe.pipe).isRequestingPartials() ? 1 : 0)).setBlockPos(pipe.getPos()), player);
					MainProxy.sendPacketToPlayer(PacketHandler.getPacket(FluidSupplierMinMode.class).setInteger(((PipeFluidSupplierMk2) pipe.pipe).getMinMode().ordinal()).setBlockPos(pipe.getPos()), player);
					return dummy;

				case GuiIDs.GUI_ProviderPipe_ID:
					if (pipe == null || !(pipe.pipe instanceof PipeItemsProviderLogistics)) {
						return null;
					}
					dummy = new DummyContainer(player.inventory, ((PipeItemsProviderLogistics) pipe.pipe).getprovidingInventory());
					dummy.addNormalSlotsForPlayerInventory(18, 97);

					xOffset = 72;
					yOffset = 18;

					for (int row = 0; row < 3; row++) {
						for (int column = 0; column < 3; column++) {
							dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);
						}
					}
					return dummy;

				case GuiIDs.GUI_SatellitePipe_ID:
					if (pipe != null && pipe.pipe instanceof PipeItemsSatelliteLogistics) {
						return new DummyContainer(player.inventory, null);
					}
					if (pipe != null && pipe.pipe instanceof PipeFluidSatellite) {
						return new DummyContainer(player.inventory, null);
					}

				case GuiIDs.GUI_Normal_Orderer_ID:
					if (pipe == null || !(pipe.pipe instanceof CoreRoutedPipe)) {
						return null;
					}
					return new DummyContainer(player.inventory, null);

				case GuiIDs.GUI_Normal_Mk2_Orderer_ID:
					if (pipe == null || !(pipe.pipe instanceof PipeItemsRequestLogisticsMk2)) {
						return null;
					}
					return new DummyContainer(player.inventory, null);

				case GuiIDs.GUI_Fluid_Orderer_ID:
					if (pipe == null || !(pipe.pipe instanceof PipeFluidRequestLogistics)) {
						return null;
					}
					return new DummyContainer(player.inventory, null);

				case GuiIDs.GUI_Freq_Card_ID:
					if (pipe == null || !((pipe.pipe instanceof PipeItemsSystemEntranceLogistics) || (pipe.pipe instanceof PipeItemsSystemDestinationLogistics))) {
						return null;
					}
					IInventory inv = null;
					if (pipe.pipe instanceof PipeItemsSystemEntranceLogistics) {
						inv = ((PipeItemsSystemEntranceLogistics) pipe.pipe).inv;
					} else if (pipe.pipe instanceof PipeItemsSystemDestinationLogistics) {
						inv = ((PipeItemsSystemDestinationLogistics) pipe.pipe).inv;
					}

					dummy = new DummyContainer(player.inventory, inv);

					dummy.addRestrictedSlot(0, inv, 40, 40, itemStack -> {
						if (itemStack.isEmpty()) {
							return false;
						}
						if (itemStack.getItem() != LPItems.itemCard) {
							return false;
						}
						return itemStack.getItemDamage() == LogisticsItemCard.FREQ_CARD;
					});
					dummy.addNormalSlotsForPlayerInventory(0, 0);

					return dummy;

				case GuiIDs.GUI_HUD_Settings:
					dummy = new DummyContainer(player.inventory, null);
					dummy.addRestrictedHotbarForPlayerInventory(10, 160);
					dummy.addRestrictedArmorForPlayerInventory(10, 60);
					return dummy;

				case GuiIDs.GUI_Fluid_Basic_ID:
					if (pipe == null || !((pipe.pipe instanceof PipeFluidBasic))) {
						return null;
					}
					dummy = new DummyContainer(player, ((PipeFluidBasic) pipe.pipe).filterInv, new IGuiOpenControler() {

						@Override
						public void guiOpenedByPlayer(EntityPlayer player) {
							((PipeFluidBasic) fpipe.pipe).guiOpenedByPlayer(player);
						}

						@Override
						public void guiClosedByPlayer(EntityPlayer player) {
							((PipeFluidBasic) fpipe.pipe).guiClosedByPlayer(player);
						}
					});
					dummy.addFluidSlot(0, ((PipeFluidBasic) pipe.pipe).filterInv, 28, 15);
					dummy.addNormalSlotsForPlayerInventory(10, 45);
					return dummy;

				case GuiIDs.GUI_FIREWALL:
					if (pipe == null || !((pipe.pipe instanceof PipeItemsFirewall))) {
						return null;
					}
					dummy = new DummyContainer(player.inventory, ((PipeItemsFirewall) pipe.pipe).inv);
					dummy.addNormalSlotsForPlayerInventory(33, 147);
					for (int i = 0; i < 6; i++) {
						for (int j = 0; j < 6; j++) {
							dummy.addDummySlot(i * 6 + j, 0, 0);
						}
					}
					return dummy;

				case GuiIDs.GUI_Request_Table_ID:
					if (pipe == null || !(pipe.pipe instanceof PipeBlockRequestTable)) {
						return null;
					}
					dummy = new DummyContainer(player, ((PipeBlockRequestTable) pipe.pipe).matrix, (PipeBlockRequestTable) pipe.pipe);
					int i = 0;
					for (int Y = 0; Y < 3; Y++) {
						for (int X = 0; X < 9; X++) {
							dummy.addNormalSlot(i++, ((PipeBlockRequestTable) pipe.pipe).inv, 0, 0);
						}
					}
					i = 0;
					for (int Y = 0; Y < 3; Y++) {
						for (int X = 0; X < 3; X++) {
							dummy.addDummySlot(i++, 0, 0);
						}
					}
					dummy.addCallableSlotHandler(0, ((PipeBlockRequestTable) pipe.pipe).resultInv, 0, 0, () -> ((PipeBlockRequestTable) fpipe.pipe).getResultForClick());
					dummy.addNormalSlot(0, ((PipeBlockRequestTable) pipe.pipe).toSortInv, 0, 0);
					dummy.addNormalSlot(0, ((PipeBlockRequestTable) pipe.pipe).diskInv, 0, 0);
					dummy.addNormalSlotsForPlayerInventory(0, 0);
					return dummy;

				default:
					break;
			}
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, final World world, int x, int y, int z) {
		if (ID == -1) {
			return getClientGuiElement(-100 * 20 + x, player, world, 0, -1, z);
		}

		if (ID == GuiIDs.GUI_Guide_Book_ID) {
			ItemStack itemstack = player.getHeldItem(player.getActiveHand());
			if (itemstack.getItem() == LPItems.itemGuideBook) {
				return ItemGuideBook.openGuideBook(player.getActiveHand());
			} else {
				return null;
			}
		}

		TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
		LogisticsTileGenericPipe pipe = null;
		if (tile instanceof LogisticsTileGenericPipe) {
			pipe = (LogisticsTileGenericPipe) tile;
		}

		if (ID < 110 && ID > 0) {
			switch (ID) {

				case GuiIDs.GUI_FluidSupplier_ID:
					if (pipe == null || !(pipe.pipe instanceof PipeItemsFluidSupplier)) {
						return null;
					}
					return new GuiFluidSupplierPipe(player.inventory, ((PipeItemsFluidSupplier) pipe.pipe).getDummyInventory(), (PipeItemsFluidSupplier) pipe.pipe);

				case GuiIDs.GUI_FluidSupplier_MK2_ID:
					if (pipe == null || !(pipe.pipe instanceof PipeFluidSupplierMk2)) {
						return null;
					}
					return new GuiFluidSupplierMk2Pipe(player.inventory, ((PipeFluidSupplierMk2) pipe.pipe).getDummyInventory(), (PipeFluidSupplierMk2) pipe.pipe);

				case GuiIDs.GUI_ProviderPipe_ID:
					if (pipe == null || !(pipe.pipe instanceof PipeItemsProviderLogistics)) {
						return null;
					}
					return new GuiProviderPipe(player.inventory, ((PipeItemsProviderLogistics) pipe.pipe).getprovidingInventory(), (PipeItemsProviderLogistics) pipe.pipe);

				case GuiIDs.GUI_SatellitePipe_ID:
					if (pipe != null && pipe.pipe instanceof PipeItemsSatelliteLogistics) {
						return new GuiSatellitePipe((PipeItemsSatelliteLogistics) pipe.pipe, player);
					}
					if (pipe != null && pipe.pipe instanceof PipeFluidSatellite) {
						return new GuiSatellitePipe((PipeFluidSatellite) pipe.pipe, player);
					}
					return null;

				case GuiIDs.GUI_Normal_Orderer_ID:
					return new NormalGuiOrderer(x, y, z, world.provider.getDimension(), player);

				case GuiIDs.GUI_Normal_Mk2_Orderer_ID:
					if (pipe == null || !(pipe.pipe instanceof PipeItemsRequestLogisticsMk2)) {
						return null;
					}
					return new NormalMk2GuiOrderer(((PipeItemsRequestLogisticsMk2) pipe.pipe), player);

				case GuiIDs.GUI_Fluid_Orderer_ID:
					if (pipe == null || !(pipe.pipe instanceof PipeFluidRequestLogistics)) {
						return null;
					}
					return new FluidGuiOrderer(((PipeFluidRequestLogistics) pipe.pipe), player);

				case GuiIDs.GUI_Freq_Card_ID:
					if (pipe == null || !((pipe.pipe instanceof PipeItemsSystemEntranceLogistics) || (pipe.pipe instanceof PipeItemsSystemDestinationLogistics))) {
						return null;
					}
					IInventory inv = null;
					if (pipe.pipe instanceof PipeItemsSystemEntranceLogistics) {
						inv = ((PipeItemsSystemEntranceLogistics) pipe.pipe).inv;
					} else if (pipe.pipe instanceof PipeItemsSystemDestinationLogistics) {
						inv = ((PipeItemsSystemDestinationLogistics) pipe.pipe).inv;
					}
					return new GuiFreqCardContent(player, inv);

				case GuiIDs.GUI_HUD_Settings:
					return new GuiHUDSettings(player, x);

				case GuiIDs.GUI_Fluid_Basic_ID:
					if (pipe == null || !((pipe.pipe instanceof PipeFluidBasic))) {
						return null;
					}
					return new GuiFluidBasic(player, ((PipeFluidBasic) pipe.pipe).filterInv);

				case GuiIDs.GUI_FIREWALL:
					if (pipe == null || !((pipe.pipe instanceof PipeItemsFirewall))) {
						return null;
					}
					return new GuiFirewall((PipeItemsFirewall) pipe.pipe, player);

				case GuiIDs.GUI_Request_Table_ID:
					if (pipe == null || !(pipe.pipe instanceof PipeBlockRequestTable)) {
						return null;
					}
					return new GuiRequestTable(player, ((PipeBlockRequestTable) pipe.pipe));

				default:
					break;
			}
		}
		return null;
	}

}
