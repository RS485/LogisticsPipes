package logisticspipes.proxy.side;

import java.io.File;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.blocks.LogisticsSignTileEntity;
import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.config.Configs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.UpdateName;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.IProxy;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.DimensionManager;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.server.FMLServerHandler;

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
		GameRegistry.registerTileEntity(LogisticsSignTileEntity.class, "net.minecraft.src.buildcraft.logisticspipes.blocks.LogisticsTileEntiy");
		GameRegistry.registerTileEntity(LogisticsSignTileEntity.class, "logisticspipes.blocks.LogisticsSignTileEntity");
		GameRegistry.registerTileEntity(LogisticsSolderingTileEntity.class, "logisticspipes.blocks.LogisticsSolderingTileEntity");
		GameRegistry.registerTileEntity(LogisticsPowerJunctionTileEntity.class, "logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity");
		GameRegistry.registerTileEntity(LogisticsSecurityTileEntity.class, "logisticspipes.blocks.LogisticsSecurityTileEntity");
		GameRegistry.registerTileEntity(LogisticsCraftingTableTileEntity.class, "logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity");
		if(!Configs.LOGISTICS_TILE_GENERIC_PIPE_REPLACEMENT_DISABLED) {
			GameRegistry.registerTileEntity(LogisticsTileGenericPipe.class, LogisticsPipes.logisticsTileGenericPipeMapping);
		}
	}

	@Override
	public EntityPlayer getClientPlayer() {
		return null;
	}

	@Override
	public boolean isMainThreadRunning() {
		return FMLServerHandler.instance().getServer().isServerRunning();
	}
	

	@Override
	public void registerParticles() {
		//Only Client Side
	}
	
	private String tryGetName(ItemIdentifier item) {
		String name = "???";
		try {
			name = Item.itemsList[item.itemID].getItemDisplayName(item.unsafeMakeNormalStack(1));
			if(name == null) {
				throw new Exception();
			}
		} catch(Exception e) {
			try {
				name = Item.itemsList[item.itemID].getUnlocalizedName(item.unsafeMakeNormalStack(1));
				if(name == null) {
					throw new Exception();
				}
			} catch(Exception e1) {
				try {
					name = Item.itemsList[item.itemID].getUnlocalizedName();
					if(name == null) {
						throw new Exception();
					}
				} catch(Exception e2) {
					name = "???"; 
				}
			}
		}
		return name;
	}
	
	private String getNameForCategory(String category, ItemIdentifier item) {
		String name = langDatabase.get(category, "name", "").getString();
		if(name.equals("")) {
			saveLangDatabase();
			if(item.unsafeMakeNormalStack(1).isItemStackDamageable()) {
				return tryGetName(item);
			} else {
				return  "LP|UNDEFINED";
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
		String category = "";
		if(item.unsafeMakeNormalStack(1).isItemStackDamageable()) {
			category = "itemNames." + Integer.toString(item.itemID);
		} else {
			if(item.itemDamage == 0) {
				category = "itemNames." + Integer.toString(item.itemID);
			} else {
				category = "itemNames." + Integer.toString(item.itemID) + "." + Integer.toString(item.itemDamage);
			}
		}
		String name = getNameForCategory(category, item);
		if(name.equals("LP|UNDEFINED")) {
			if(item.itemDamage == 0) {
				return tryGetName(item);
			} else {
				category = "itemNames." + Integer.toString(item.itemID);
				name = getNameForCategory(category, item);
				if(name.equals("LP|UNDEFINED")) {
					return tryGetName(item);
				}
			}
		}
		return name;
	}

	@Override
	public void updateNames(ItemIdentifier item, String name) {
		String category = "";
		if(item.unsafeMakeNormalStack(1).isItemStackDamageable()) {
			category = "itemNames." + Integer.toString(item.itemID);
		} else {
			if(item.itemDamage == 0) {
				category = "itemNames." + Integer.toString(item.itemID);
			} else {
				category = "itemNames." + Integer.toString(item.itemID) + "." + Integer.toString(item.itemDamage);
			}
		}
		setNameForCategory(category, item, name);
	}

	@Override
	public void tick() {
		//Save Language Database
		if(saveThreadTime != 0) {
			if(saveThreadTime < System.currentTimeMillis()) {
				saveThreadTime = 0;
				langDatabase.save();
				LogisticsPipes.log.info("LangDatabase saved");
			}
		}
	}

	@Override
	public void sendNameUpdateRequest(Player player) {
		for(String category:langDatabase.getCategoryNames()) {
			if(!category.startsWith("itemNames.")) continue;
			String name = langDatabase.get(category, "name", "").getString();
			if(name.equals("")) {
				String itemPart = category.substring(10);
				String metaPart = "0";
				if(itemPart.contains(".")) {
					String[] itemPartSplit = itemPart.split(".");
					itemPart = itemPartSplit[0];
					metaPart = itemPartSplit[1];
				}
				int id = Integer.valueOf(itemPart);
				int meta = Integer.valueOf(metaPart);
				//SimpleServiceLocator.serverBufferHandler.addPacketToCompressor((Packet250CustomPayload) new PacketNameUpdatePacket(ItemIdentifier.get(id, meta, null), "-").getPacket(), player);
				SimpleServiceLocator.serverBufferHandler.addPacketToCompressor(PacketHandler.getPacket(UpdateName.class).setIdent(ItemIdentifier.get(id, meta, null)).setName("-").getPacket(), player);
			}
		}
	}
	
	@Override
	public int getDimensionForWorld(World world) {
		if(world instanceof WorldServer) {
			return ((WorldServer)world).provider.dimensionId;
		}
		if(world instanceof WorldClient) {
			return ((WorldClient)world).provider.dimensionId;
		}
		return world.getWorldInfo().getVanillaDimension();
	}

	@Override
	public TileGenericPipe getPipeInDimensionAt(int dimension, int x, int y, int z, EntityPlayer player) {
		return getPipe(DimensionManager.getWorld(dimension), x, y, z);
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
	protected static TileGenericPipe getPipe(World world, int x, int y, int z) {
		if(world == null) {
			return null;
		}
		if (!world.blockExists(x, y, z)) {
			return null;
		}

		final TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileGenericPipe)) {
			return null;
		}

		return (TileGenericPipe) tile;
	}
	// BuildCraft method end
	@Override
	public void addLogisticsPipesOverride(IconRegister par1IconRegister, int index, String override1,
			String override2, boolean flag) {
		// TODO Auto-generated method stub
		
	}
	
	
}
