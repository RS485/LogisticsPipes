package logisticspipes.network.packets.pipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.gui.popup.GuiRecipeImport;
import logisticspipes.network.IWriteListObject;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.world.CoordinateUtils;

public class FindMostLikelyRecipeComponents extends CoordinatesPacket {

	@Getter
	@Setter
	private List<GuiRecipeImport.Canidates> content;

	public FindMostLikelyRecipeComponents(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		TileEntity tile = this.getTile(player.getEntityWorld(), TileEntity.class);
		CoreRoutedPipe pipe = null;
		if (tile instanceof LogisticsCraftingTableTileEntity) {
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity conn = CoordinateUtils.add(((LogisticsCraftingTableTileEntity) tile).getLPPosition(), dir).getTileEntity(player.getEntityWorld());
				if (conn instanceof LogisticsTileGenericPipe) {
					if (((LogisticsTileGenericPipe) conn).pipe instanceof PipeItemsCraftingLogistics) {
						pipe = (CoreRoutedPipe) ((LogisticsTileGenericPipe) conn).pipe;
						break;
					}
				}
			}
		} else if (tile instanceof LogisticsTileGenericPipe) {
			if (((LogisticsTileGenericPipe) tile).pipe instanceof PipeBlockRequestTable) {
				pipe = (CoreRoutedPipe) ((LogisticsTileGenericPipe) tile).pipe;
			}
		}
		List<Integer> list = new ArrayList<Integer>(content.size());
		while (list.size() < content.size()) {
			list.add(-1);
		}
		if (pipe == null) return;
		LinkedList<ItemIdentifier> craftable = null;
		for (int j = 0; j < content.size(); j++) {
			GuiRecipeImport.Canidates canidates = content.get(j);
			int maxItemPos = -1;
			int max = 0;
			for (int i = 0; i < canidates.order.size(); i++) {
				ItemIdentifier ident = canidates.order.get(i).getItem();
				int newAmount = SimpleServiceLocator.logisticsManager.getAmountFor(ident, pipe.getRouter().getIRoutersByCost());
				if (newAmount > max) {
					max = newAmount;
					maxItemPos = i;
				}
			}
			if (max < 64) {
				if (craftable == null) {
					craftable = SimpleServiceLocator.logisticsManager.getCraftableItems(pipe.getRouter().getIRoutersByCost());
				}
				for (ItemIdentifier craft : craftable) {
					for (int i = 0; i < canidates.order.size(); i++) {
						ItemIdentifier ident = canidates.order.get(i).getItem();
						if (craft == ident) {
							maxItemPos = i;
							break;
						}
					}
				}
			}
			list.set(j, maxItemPos);
		}
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MostLikelyRecipeComponentsResponse.class).setResponse(list), player);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		super.readData(input);
		content = input.readArrayList(input1 -> {
			GuiRecipeImport.Canidates can = new GuiRecipeImport.Canidates(new TreeSet<>());
			can.order = input1.readArrayList(LPDataInput::readItemIdentifierStack);
			return can;
		});
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		super.writeData(output);
		output.writeCollection(content, (data, object) -> data.writeCollection(object.order,
				(IWriteListObject<ItemIdentifierStack>) LPDataOutput::writeItemIdentifierStack));
	}

	@Override
	public ModernPacket template() {
		return new FindMostLikelyRecipeComponents(getId());
	}
}
