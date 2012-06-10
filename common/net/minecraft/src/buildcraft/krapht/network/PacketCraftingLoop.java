package net.minecraft.src.buildcraft.krapht.network;

import java.util.LinkedList;

import net.minecraft.src.buildcraft.krapht.ErrorMessage;

public class PacketCraftingLoop extends PacketMissingItems {

	public PacketCraftingLoop() {
		super();
	}

	public PacketCraftingLoop(LinkedList<ErrorMessage> error) {
		super(error);
	}

	@Override
	public int getID() {
		return NetworkConstants.CRAFTING_LOOP;
	}
}
