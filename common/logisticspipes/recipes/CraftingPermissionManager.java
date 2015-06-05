package logisticspipes.recipes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import logisticspipes.blocks.crafting.AutoCraftingInventory;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.CraftingPermissionPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.PlayerIdentifier;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

public class CraftingPermissionManager {

	private Map<PlayerIdentifier, Pair<Long, EnumSet<CraftingDependency>>> serverCache = new HashMap<PlayerIdentifier, Pair<Long, EnumSet<CraftingDependency>>>();
	private int tick = 0;
	public EnumSet<CraftingDependency> clientSidePermission;

	public CraftingPermissionManager() {
		clientSidePermission = EnumSet.noneOf(CraftingDependency.class);
		clientSidePermission.add(CraftingDependency.Basic);
	}

	public PlayerIdentifier getPlayerID(InventoryCrafting inv) {
		if (inv.eventHandler instanceof ContainerPlayer) {
			return PlayerIdentifier.get(((ContainerPlayer) inv.eventHandler).thePlayer);
		} else if (inv instanceof AutoCraftingInventory) {
			return ((AutoCraftingInventory) inv).placedByPlayer;
		}
		return null;
	}

	public boolean isAllowedFor(CraftingDependency dependent, PlayerIdentifier player) {
		if (MainProxy.isClient()) {
			return clientSidePermission.contains(dependent);
		} else {
			EnumSet<CraftingDependency> set = getEnumSet(player);
			return set.contains(dependent);
		}
	}

	public void tick() {
		if (tick++ % 100 != 0) {
			return;
		}
		tick = 1;
		for (PlayerIdentifier player : serverCache.keySet()) {
			if (serverCache.get(player).getValue1() + 30000 < System.currentTimeMillis()) {
				serverCache.remove(player);
				tick = 0;
				return;
			}
		}
	}

	public EnumSet<CraftingDependency> getEnumSet(PlayerIdentifier player) {
		if (!serverCache.containsKey(player)) {
			load(player);
		}
		serverCache.get(player).setValue1(System.currentTimeMillis());
		return serverCache.get(player).getValue2();
	}

	public void load(PlayerIdentifier player) {
		try {
			File lpFolder = MainProxy.getLPFolder();
			File playerFile = new File(lpFolder, player.getAsString() + "_craft.dat");
			DataInputStream din = new DataInputStream(new FileInputStream(playerFile));
			NBTTagCompound nbt = CompressedStreamTools.read(din);
			din.close();
			EnumSet<CraftingDependency> enumSet = EnumSet.noneOf(CraftingDependency.class);
			for (CraftingDependency type : CraftingDependency.values()) {
				if (nbt.getBoolean(type.name())) {
					enumSet.add(type);
				}
			}
			serverCache.put(player, new Pair<Long, EnumSet<CraftingDependency>>(System.currentTimeMillis(), enumSet));
		} catch (Exception e) {
			serverCache.put(player, new Pair<Long, EnumSet<CraftingDependency>>(System.currentTimeMillis(), EnumSet.of(CraftingDependency.Basic)));
		}
	}

	public void modify(PlayerIdentifier player, EnumSet<CraftingDependency> enumSet) {
		serverCache.get(player).setValue1(System.currentTimeMillis());
		serverCache.get(player).setValue2(enumSet);
		try {
			File lpFolder = MainProxy.getLPFolder();
			File playerFile = new File(lpFolder, player.getAsString() + "_craft.dat");
			DataOutputStream din = new DataOutputStream(new FileOutputStream(playerFile));
			NBTTagCompound nbt = new NBTTagCompound();
			for (CraftingDependency type : CraftingDependency.values()) {
				nbt.setBoolean(type.name(), enumSet.contains(type));
			}
			CompressedStreamTools.write(nbt, din);
			din.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void sendCraftingPermissionsToPlayer(EntityPlayer player) {
		EnumSet<CraftingDependency> set = getEnumSet(PlayerIdentifier.get(player));
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(CraftingPermissionPacket.class).setEnumSet(set), player);
	}
}
