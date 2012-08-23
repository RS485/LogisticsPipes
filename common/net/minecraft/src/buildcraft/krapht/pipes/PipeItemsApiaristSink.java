package net.minecraft.src.buildcraft.krapht.pipes;

import java.util.UUID;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.krapht.logic.BaseRoutingLogic;
import net.minecraft.src.buildcraft.krapht.logic.TemporaryLogic;
import net.minecraft.src.buildcraft.logisticspipes.ChassiTransportLayer;
import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;
import net.minecraft.src.buildcraft.logisticspipes.IRoutedItem;
import net.minecraft.src.buildcraft.logisticspipes.SidedInventoryAdapter;
import net.minecraft.src.buildcraft.logisticspipes.TransportLayer;
import net.minecraft.src.buildcraft.logisticspipes.IRoutedItem.TransportMode;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.buildcraft.logisticspipes.modules.ISendRoutedItem;
import net.minecraft.src.buildcraft.logisticspipes.modules.IWorldProvider;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleApiaristSink;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleItemSink;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.forge.ISidedInventory;

public class PipeItemsApiaristSink extends RoutedPipe {
	
	private ModuleApiaristSink sinkModule;

	public PipeItemsApiaristSink(int itemID) {
		super(new TemporaryLogic(), itemID);
		sinkModule = new ModuleApiaristSink();
		sinkModule.registerHandler(null, null, this);
	}

	@Override
	public int getCenterTexture() {
		return mod_LogisticsPipes.LOGISTICSPIPE_APIARIST_SINK_TEXTURE;
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return sinkModule;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
}
