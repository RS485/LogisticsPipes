package logisticspipes.ticks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import logisticspipes.network.ServerPacketHandler;
import logisticspipes.network.packets.PacketBufferTransfer;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.Pair;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ServerPacketBufferHandlerThread {

	private class ServerCompressorThread extends Thread {
		//Map of Players to lists of S->C packets to be serialized and compressed
		private HashMap<Player, LinkedList<Packet250CustomPayload>> serverList = new HashMap<Player,LinkedList<Packet250CustomPayload>>();
		//Map of Players to serialized but still uncompressed S->C data
		private HashMap<Player, byte[]> serverBuffer = new HashMap<Player, byte[]>();

		public ServerCompressorThread() {
			super("LogisticsPipes Packet Compressor Server");
			this.setDaemon(true);
			this.start();
		}

		@Override
		public void run() {
			while(true) {
				try {
					synchronized(serverList) {
						for(Entry<Player, LinkedList<Packet250CustomPayload>> player:serverList.entrySet()) {
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							DataOutputStream data = new DataOutputStream(out);
							byte[] towrite = serverBuffer.get(player.getKey());
							if(towrite != null) {
								data.write(towrite);
							}
							LinkedList<Packet250CustomPayload> packets = player.getValue();
							for(Packet250CustomPayload packet:packets) {
								data.writeInt(packet.data.length);
								data.write(packet.data);
							}
							packets.clear();
							serverBuffer.put(player.getKey(), out.toByteArray());
						}
					}
					//Send Content
					for(Entry<Player, byte[]> player:serverBuffer.entrySet()) {
						if(player.getValue().length > 0) {
							byte[] sendbuffer = Arrays.copyOf(player.getValue(), Math.min(1024 * 32, player.getValue().length));
							byte[] newbuffer = Arrays.copyOfRange(player.getValue(), Math.min(1024 * 32, player.getValue().length), player.getValue().length);
							player.setValue(newbuffer);
							byte[] compressed = compress(sendbuffer);
							MainProxy.sendPacketToPlayer(new PacketBufferTransfer(compressed).getPacket(), player.getKey());
						}
					}
				} catch (IOException e) {
						e.printStackTrace();
				}
				try {
					boolean toDo = false;
					synchronized(serverList) {
						for(LinkedList<Packet250CustomPayload> tocompress:serverList.values()) {
							if(tocompress.size() > 0) {
								toDo = true;
								break;
							}
						}
					}
					if(!toDo) {
						for(byte[] towrite:serverBuffer.values()) {
							if(towrite.length > 0) {
								toDo = true;
								break;
							}
						}
					}
					if(!toDo) {
						Thread.sleep(100);
					}
				} catch(Exception e) {}
			}
		}

		public void addPacketToCompressor(Packet250CustomPayload packet, Player player) {
			if(packet.channel.equals("BCLP")) {
				synchronized(serverList) {
					LinkedList<Packet250CustomPayload> packetList = serverList.get(player);
					if(packetList == null) {
						packetList = new LinkedList<Packet250CustomPayload>();
						serverList.put(player, packetList);
					}
					packetList.add(packet);
				}
			}
		}
	}
	private final ServerCompressorThread serverCompressorThread = new ServerCompressorThread();

	private class ServerDecompressorThread extends Thread {
		//Map of Player to received compressed C->S data
		private HashMap<Player, LinkedList<byte[]>> queue = new HashMap<Player, LinkedList<byte[]>>();
		//Map of Player to decompressed serialized C->S data
		private HashMap<Player, byte[]> ByteBuffer = new HashMap<Player, byte[]>();
		//FIFO for deserialized C->S packets, decompressor adds, tickEnd removes
		private LinkedList<Pair<Player,byte[]>> PacketBuffer = new LinkedList<Pair<Player,byte[]>>();

		public ServerDecompressorThread() {
			super("LogisticsPipes Packet Decompressor Server");
			this.setDaemon(true);
			this.start();
			TickRegistry.registerTickHandler(new ITickHandler() {
				@Override
				public EnumSet<TickType> ticks() {
					return EnumSet.of(TickType.SERVER);
				}

				@Override
				public void tickStart(EnumSet<TickType> type, Object... tickData) {}

				@Override
				public void tickEnd(EnumSet<TickType> type, Object... tickData) {
					boolean flag = false;
					do {
						flag = false;
						Pair<Player,byte[]> part = null;
						synchronized (PacketBuffer) {
							if(PacketBuffer.size() > 0) {
								flag = true;
								part = PacketBuffer.pop();
							}
						}
						if(flag) {
							ServerPacketHandler.onPacketData(new DataInputStream(new ByteArrayInputStream(part.getValue2())), part.getValue1());
						}
					} while(flag);
				}

				@Override
				public String getLabel() {
					return "LogisticsPipes Packet Compressor Tick Server";
				}
			}, Side.SERVER);
		}

		@Override
		public void run() {
			while(true) {
				boolean flag = false;
				do {
					flag = false;
					byte[] buffer = null;
					Player player = null;
					synchronized(queue) {
						if(queue.size() > 0) {
							for(Entry<Player, LinkedList<byte[]>> lPlayer:queue.entrySet()) {
								if(lPlayer.getValue() != null && lPlayer.getValue().size() > 0) {
									flag = true;
									buffer = lPlayer.getValue().getFirst();
									player = lPlayer.getKey();
									lPlayer.getValue().removeFirst();
									break;
								}
							}
						}
					}
					if(flag && buffer != null && player != null) {
						byte[] ByteBufferForPlayer = ByteBuffer.get(player);
						if(ByteBufferForPlayer==null) {
							ByteBufferForPlayer= new byte[]{};
							ByteBuffer.put(player,ByteBufferForPlayer);
						}
						byte[] packetbytes = decompress(buffer);
						byte[] newBuffer = new byte[packetbytes.length + ByteBufferForPlayer.length];
						System.arraycopy(ByteBufferForPlayer, 0, newBuffer, 0, ByteBufferForPlayer.length);
						System.arraycopy(packetbytes, 0, newBuffer, ByteBufferForPlayer.length, packetbytes.length);
						ByteBuffer.put(player, newBuffer);
					}
				}
				while(flag);
				for(Entry<Player, byte[]> player:ByteBuffer.entrySet()) {
					if(player.getValue() != null && player.getValue().length > 0) {
						/*if(!ByteBuffer.containsKey(player)) {
							ByteBuffer.put(player, new byte[]{});
						} Never true, we are iterating over the keys, and an undifiend operation; modifiying a colleciton while iterating, even if it was.*/
						byte[] ByteBufferForPlayer = player.getValue();
						int size = ((ByteBufferForPlayer[0] & 255) << 24) + ((ByteBufferForPlayer[1] & 255) << 16) + ((ByteBufferForPlayer[2] & 255) << 8) + ((ByteBufferForPlayer[3] & 255) << 0);
						while(size + 4 <= ByteBufferForPlayer.length) {
							byte[] packet = Arrays.copyOfRange(ByteBufferForPlayer, 4, size + 4);
							ByteBufferForPlayer = Arrays.copyOfRange(ByteBufferForPlayer, size + 4, ByteBufferForPlayer.length);
							player.setValue(ByteBufferForPlayer);
							synchronized (PacketBuffer) {
								PacketBuffer.add(new Pair<Player,byte[]>(player.getKey() ,packet));
							}
							if(ByteBufferForPlayer.length > 4) {
								size = ((ByteBufferForPlayer[0] & 255) << 24) + ((ByteBufferForPlayer[1] & 255) << 16) + ((ByteBufferForPlayer[2] & 255) << 8) + ((ByteBufferForPlayer[3] & 255) << 0);
							} else {
								size = 0;
							}
						}
					}
				}
				try {
					boolean toDo = false;
					for(LinkedList<byte[]> lPlayer:queue.values()) {
						if(lPlayer != null && lPlayer.size() > 0) {
							toDo = true;
							break;
						}
					}
					if(!toDo) {
						for(byte[] ByteBufferForPlayer:ByteBuffer.values()) {
							if(ByteBufferForPlayer != null && ByteBufferForPlayer.length > 0) {
								toDo = true;
								break;
							}
						}
					}
					if(!toDo) {
						Thread.sleep(100);
					}
				} catch(Exception e) {}
			}
		}

		public void handlePacket(PacketBufferTransfer packet, Player player) {
			synchronized(queue) {
				LinkedList<byte[]> list=queue.get(player);
				if(list == null) {
					list = new LinkedList<byte[]>();
					queue.put(player, list);
				}
				list.addLast(packet.content);
			}
		}
	}
	private final ServerDecompressorThread serverDecompressorThread = new ServerDecompressorThread();

	public ServerPacketBufferHandlerThread() {
	}

	public void addPacketToCompressor(Packet250CustomPayload packet, Player player) {
		serverCompressorThread.addPacketToCompressor(packet, player);
	}

	public void handlePacket(PacketBufferTransfer packet, Player player) {
		serverDecompressorThread.handlePacket(packet, player);
	}

	private static byte[] compress(byte[] content){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try{
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            gzipOutputStream.write(content);
            gzipOutputStream.close();
        } catch(IOException e){
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

	private static byte[] decompress(byte[] contentBytes){
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
        	GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(contentBytes));
        	int buffer = 0;
        	while((buffer = gzip.read()) != -1) {
        		out.write(buffer);
        	}
        } catch(IOException e){
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }
}
