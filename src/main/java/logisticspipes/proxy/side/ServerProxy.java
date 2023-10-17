package logisticspipes.proxy.side;

import java.io.File;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.server.FMLServerHandler;

import logisticspipes.LogisticsPipes;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.UpdateName;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.IProxy;
import logisticspipes.utils.item.ItemIdentifier;

public class ServerProxy implements IProxy {

	private Configuration langDatabase;
	private long saveThreadTime = 0;

	public ServerProxy() {
		langDatabase = new Configuration(new File("config/LogisticsPipes-LangDatabase.cfg"));
	}

	@Override
	public String getSide() {
		return "Server";
	}

	@Override
	public World getWorld() {
		return null;
	}

	@Override
	public void registerTileEntities() {
	}

	@Override
	public EntityPlayer getClientPlayer() {
		return null;
	}

	@Override
	public void registerParticles() {
		//Only Client Side
	}

	private String getNameForCategory(String category, ItemIdentifier item) {
		String name = langDatabase.get(category, "name", "").getString();
		if (name.equals("")) {
			saveLangDatabase();
			if (item.isDamageable()) {
				return item.getFriendlyName();
			} else {
				return "LP|UNDEFINED";
			}
		}
		return name;
	}

	private void setNameForCategory(String category, ItemIdentifier item, String newName) {
		langDatabase.get(category, "name", newName).set(newName);
		saveLangDatabase();
	}

	private void saveLangDatabase() {
		saveThreadTime = System.currentTimeMillis() + 30 * 1000;
	}

	@Override
	public String getName(ItemIdentifier item) {
		String category;
		if (item.isDamageable()) {
			category = String.format("itemNames.%d", Item.getIdFromItem(item.item));
		} else {
			if (item.itemDamage == 0) {
				category = String.format("itemNames.%d", Item.getIdFromItem(item.item));
			} else {
				category = String.format("itemNames.%d.%d", Item.getIdFromItem(item.item), item.itemDamage);
			}
		}
		String name = getNameForCategory(category, item);
		if (name.equals("LP|UNDEFINED")) {
			if (item.itemDamage == 0) {
				return item.getFriendlyName();
			} else {
				category = String.format("itemNames.%d", Item.getIdFromItem(item.item));
				name = getNameForCategory(category, item);
				if (name.equals("LP|UNDEFINED")) {
					return item.getFriendlyName();
				}
			}
		}
		return name;
	}

	@Override
	public void updateNames(ItemIdentifier item, String name) {
		String category;
		if (item.isDamageable()) {
			category = String.format("itemNames.%d", Item.getIdFromItem(item.item));
		} else {
			if (item.itemDamage == 0) {
				category = String.format("itemNames.%d", Item.getIdFromItem(item.item));
			} else {
				category = String.format("itemNames.%d.%d", Item.getIdFromItem(item.item), item.itemDamage);
			}
		}
		setNameForCategory(category, item, name);
	}

	@Override
	public void tick() {
		//Save Language Database
		if (saveThreadTime != 0) {
			if (saveThreadTime < System.currentTimeMillis()) {
				saveThreadTime = 0;
				langDatabase.save();
				LogisticsPipes.log.info("LangDatabase saved");
			}
		}
	}

	@Override
	public void sendNameUpdateRequest(EntityPlayer player) {
		for (String category : langDatabase.getCategoryNames()) {
			if (!category.startsWith("itemNames.")) {
				continue;
			}
			String name = langDatabase.get(category, "name", "").getString();
			if (name.equals("")) {
				String itemPart = category.substring(10);
				String metaPart = "0";
				if (itemPart.contains(".")) {
					String[] itemPartSplit = itemPart.split("\\.");
					itemPart = itemPartSplit[0];
					metaPart = itemPartSplit[1];
				}
				int id = Integer.valueOf(itemPart);
				int meta = Integer.valueOf(metaPart);
				SimpleServiceLocator.serverBufferHandler.addPacketToCompressor(PacketHandler.getPacket(UpdateName.class).setIdent(ItemIdentifier.get(Item.getItemById(id), meta, null)).setName("-"), player);
			}
		}
	}

	@Override
	public LogisticsTileGenericPipe getPipeInDimensionAt(int dimension, int x, int y, int z, EntityPlayer player) {
		return ServerProxy.getPipe(DimensionManager.getWorld(dimension), x, y, z);
	}

	// BuildCraft method

	/**
	 * Retrieves pipe at specified coordinates if any.
	 *
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	protected static LogisticsTileGenericPipe getPipe(World world, int x, int y, int z) {
		if (world == null) {
			return null;
		}
		if (world.isAirBlock(new BlockPos(x, y, z))) {
			return null;
		}

		final TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
		if (!(tile instanceof LogisticsTileGenericPipe)) {
			return null;
		}

		return (LogisticsTileGenericPipe) tile;
	}

	// BuildCraft method end
	@Override
	public void addLogisticsPipesOverride(Object par1IIconRegister, int index, String override1, String override2, boolean flag) {
		// TODO Auto-generated method stub

	}

	@Override
	@SuppressWarnings("rawtypes")
	public void sendBroadCast(String message) {
		MinecraftServer server = FMLServerHandler.instance().getServer();
		if (server != null && server.getPlayerList() != null) {
			List<EntityPlayerMP> list = server.getPlayerList().getPlayers();
			if (list != null && !list.isEmpty()) {
				list.forEach(obj -> obj.sendMessage(new TextComponentString("[LP] Server: " + message)));
			}
		}
	}

	@Override
	public void tickServer() {
		MainProxy.addTick();
	}

	@Override
	public void tickClient() {}

	@Override
	public EntityPlayer getEntityPlayerFromNetHandler(INetHandler handler) {
		if (handler instanceof NetHandlerPlayServer) {
			return ((NetHandlerPlayServer) handler).player;
		}
		return null;
	}

	@Override
	public void setIconProviderFromPipe(ItemLogisticsPipe item, CoreUnroutedPipe dummyPipe) {}

	@Override
	public LogisticsModule getModuleFromGui() {
		return null;
	}

	@Override
	public boolean checkSinglePlayerOwner(String commandSenderName) {
		return false;
	}

	@Override
	public void openFluidSelectGui(int slotId) {}

	@Override
	public void registerTextures() {}

	@Override
	public void initModelLoader() {}

	@Override
	public int getRenderIndex() {
		return 0;
	}

}
