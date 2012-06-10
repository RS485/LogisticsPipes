package net.minecraft.src.buildcraft.krapht.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import net.minecraft.src.buildcraft.krapht.ErrorMessage;

public class PacketMissingItems extends LogisticsPipesPacket {
	
	LinkedList<ErrorMessage> errors = new LinkedList<ErrorMessage>();
	
	public PacketMissingItems() {
		super();
	}
	
	public PacketMissingItems(LinkedList<ErrorMessage> errors) {
		super();
		this.errors = errors;
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		for(ErrorMessage error:errors) {
			data.write(1);
			data.writeInt(error.id);
			data.writeInt(error.data);
			data.writeInt(error.amount);
		}
		data.write(0);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		while(data.read() != 0) {
			ErrorMessage error = new ErrorMessage();
			error.id = data.readInt();
			error.data = data.readInt();
			error.amount = data.readInt();
			errors.add(error);
		}
	}

	@Override
	public int getID() {
		return NetworkConstants.MISSING_ITEMS;
	}
}
