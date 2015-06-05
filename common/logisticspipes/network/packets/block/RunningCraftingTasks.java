package logisticspipes.network.packets.block;

import java.io.IOException;
import java.util.List;

import logisticspipes.gui.GuiStatistics;
import logisticspipes.network.IReadListObject;
import logisticspipes.network.IWriteListObject;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.item.ItemIdentifierStack;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.client.FMLClientHandler;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class RunningCraftingTasks extends ModernPacket {

	@Getter
	@Setter
	private List<ItemIdentifierStack> identList;

	public RunningCraftingTasks(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiStatistics) {
			((GuiStatistics) FMLClientHandler.instance().getClient().currentScreen).handlePacket_2(getIdentList());
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeList(identList, new IWriteListObject<ItemIdentifierStack>() {

			@Override
			public void writeObject(LPDataOutputStream data, ItemIdentifierStack object) throws IOException {
				data.writeItemIdentifierStack(object);
			}
		});
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		identList = data.readList(new IReadListObject<ItemIdentifierStack>() {

			@Override
			public ItemIdentifierStack readObject(LPDataInputStream data) throws IOException {
				return data.readItemIdentifierStack();
			}
		});
	}

	@Override
	public ModernPacket template() {
		return new RunningCraftingTasks(getId());
	}
}
