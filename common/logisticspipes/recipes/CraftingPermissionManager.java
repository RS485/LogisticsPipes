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
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.Pair;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class CraftingPermissionManager {
	
	public static EnumSet<CraftingDependency> clientSidePermission;
	static {
		clientSidePermission = EnumSet.noneOf(CraftingDependency.class);
		clientSidePermission.add(CraftingDependency.Basic);
	}
	
	private static Map<String, Pair<Long, EnumSet<CraftingDependency>>> serverCache = new HashMap<String, Pair<Long, EnumSet<CraftingDependency>>>();
	/*
	public static String getPlayerName(InventoryCrafting inv) {
		//TODO Access Transformer
		if(inv.eventHandler instanceof ContainerPlayer) {
			//TODO Access Transformer
			EntityPlayer player = ((ContainerPlayer)inv.eventHandler).thePlayer;
			return player.getEntityName();
		} else if (inv instanceof AutoCraftingInventory) {
			return ((AutoCraftingInventory)inv).placedByPlayer;
		}
		return "";
	}
	*/
	public static boolean isAllowedFor(CraftingDependency dependent, String name) {
		if(MainProxy.isClient()) {
			return CraftingPermissionManager.clientSidePermission.contains(dependent);
		} else {
			EnumSet<CraftingDependency> set = getEnumSet(name);
			return set.contains(dependent);
		}
	}
	
	private static int tick = 0;
	
	public static void tick() {
		if(tick++ % 100 != 0) return;
		tick = 1;
		for(String name: serverCache.keySet()) {
			if(serverCache.get(name).getValue1() + 30000 < System.currentTimeMillis()) {
				serverCache.remove(name);
				tick = 0;
				return;
			}
		}
	}
	
	public static EnumSet<CraftingDependency> getEnumSet(String name) {
		if(!serverCache.containsKey(name)) {
			load(name);
		}
		serverCache.get(name).setValue1(System.currentTimeMillis());
		return serverCache.get(name).getValue2();
	}
	
	public static void load(String name) {
		try {
			File lpFolder = MainProxy.getLPFolder();
			File playerFile = new File(lpFolder, name + "_craft.dat");
			DataInputStream din = new DataInputStream(new FileInputStream(playerFile));
			NBTTagCompound nbt = (NBTTagCompound) NBTBase.readNamedTag(din);
			din.close();
			EnumSet<CraftingDependency> enumSet = EnumSet.noneOf(CraftingDependency.class);
			for(CraftingDependency type: CraftingDependency.values()) {
				if(nbt.getBoolean(type.name())) {
					enumSet.add(type);
				}
			}
			serverCache.put(name, new Pair<Long, EnumSet<CraftingDependency>>(System.currentTimeMillis(), enumSet));
		} catch(Exception e) {
			serverCache.put(name, new Pair<Long, EnumSet<CraftingDependency>>(System.currentTimeMillis(), EnumSet.noneOf(CraftingDependency.class)));
		}
	}
	
	public static void modify(String name, EnumSet<CraftingDependency> enumSet) {
		serverCache.get(name).setValue1(System.currentTimeMillis());
		serverCache.get(name).setValue2(enumSet);
		try {
			File lpFolder = MainProxy.getLPFolder();
			File playerFile = new File(lpFolder, name + "_craft.dat");
			DataOutputStream din = new DataOutputStream(new FileOutputStream(playerFile));
			NBTTagCompound nbt = new NBTTagCompound("tag");
			for(CraftingDependency type: CraftingDependency.values()) {
				nbt.setBoolean(type.name(), enumSet.contains(type));
			}
			NBTBase.writeNamedTag(nbt, din);
			din.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
}
