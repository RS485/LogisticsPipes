package logisticspipes.ticks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import logisticspipes.network.ClientPacketHandler;
import logisticspipes.network.packets.PacketBufferTransfer;
import logisticspipes.proxy.MainProxy;
import net.minecraft.src.Packet250CustomPayload;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class PacketBufferHandlerThread extends Thread {

	private final Side side;
	private boolean running = true;
	
	//Shared
	private static HashMap<Player, LinkedList<Packet250CustomPayload>> serverList = new HashMap<Player,LinkedList<Packet250CustomPayload>>();
	private static HashMap<Player, byte[]> serverBuffer = new HashMap<Player, byte[]>();
	private static byte[] clientBuffer = new byte[]{};
	private static Object queueLock = new Object();
	private static LinkedList<byte[]> queue = new LinkedList<byte[]>();
	
	public PacketBufferHandlerThread(Side side) {
		super("LogisticsPipes Packet Compressor " + side.toString());
		this.side = side;
		this.start();
	}
	
	public synchronized static void addPacketToCompressor(Packet250CustomPayload packet, Player player) {
		if(packet.channel.equals("BCLP")) {
			synchronized(serverList) {
				if(!serverList.containsKey(player)) {
					serverList.put(player, new LinkedList<Packet250CustomPayload>());
				}
				serverList.get(player).add(packet);
			}
		}
	}
	
	public synchronized static void handlePacket(PacketBufferTransfer packet) {
		synchronized(queueLock) {
			queue.addLast(packet.content);
		}
	}
	
	@Override
	public void run() {
		while(running) {
			if(!MainProxy.proxy.isMainThreadRunning()) {
				running = false;
			}
			try {
				if(side.equals(Side.CLIENT)) {
					boolean flag = false;
					do {
						flag = false;
						byte[] buffer = null;
						synchronized(queueLock) {
							if(queue.size() > 0) {
								flag = true;
								buffer = queue.getFirst();
								queue.removeFirst();
							}
						}
						if(flag && buffer != null) {
							byte[] packetbytes = decompress(buffer);
							byte[] newBuffer = new byte[packetbytes.length + clientBuffer.length];
							System.arraycopy(clientBuffer, 0, newBuffer, 0, clientBuffer.length);
							System.arraycopy(packetbytes, 0, newBuffer, clientBuffer.length, packetbytes.length);
							clientBuffer = newBuffer;
						}
					}
					while(flag);
					if(clientBuffer.length > 0) {
						int size = ((clientBuffer[0] & 255) << 24) + ((clientBuffer[1] & 255) << 16) + ((clientBuffer[2] & 255) << 8) + ((clientBuffer[3] & 255) << 0);
						if(size + 4 <= clientBuffer.length) {
							byte[] packet = Arrays.copyOfRange(clientBuffer, 4, size + 4);
							clientBuffer = Arrays.copyOfRange(clientBuffer, size + 4, clientBuffer.length);
							ClientPacketHandler.onPacketData(new DataInputStream(new ByteArrayInputStream(packet)), (Player) MainProxy.proxy.getClientPlayer());
						}
					}
				} else if(side.equals(Side.SERVER)) {
					//Add to Buffer
					synchronized(serverList) {
						for(Player player:serverList.keySet()) {
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							DataOutputStream data = new DataOutputStream(out);
							if(serverBuffer.containsKey(player)) {
								data.write(serverBuffer.get(player));
							}
							LinkedList<Packet250CustomPayload> packets = serverList.get(player);
							for(Packet250CustomPayload packet:packets) {
								data.writeInt(packet.data.length);
								data.write(packet.data);
							}
							packets.clear();
							serverBuffer.put(player, out.toByteArray());
						}
						//Send Content
						for(Player player:serverList.keySet()) {
							if(serverBuffer.containsKey(player)) {
								if(serverBuffer.get(player).length > 0) {
									byte[] sendbuffer = Arrays.copyOf(serverBuffer.get(player), Math.min(1024 * 32, serverBuffer.get(player).length));
									byte[] newbuffer = Arrays.copyOfRange(serverBuffer.get(player), Math.min(1024 * 32, serverBuffer.get(player).length), serverBuffer.get(player).length);
									serverBuffer.put(player, newbuffer);
									byte[] compressed = compress(sendbuffer);
									PacketDispatcher.sendPacketToPlayer(new PacketBufferTransfer(compressed).getPacket(), player);
								}
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(side.equals(Side.CLIENT)) {
					if(clientBuffer.length <= 0) {
						Thread.sleep(100);
					}
				} else if(side.equals(Side.SERVER)) {
					boolean toDo = false;
					synchronized(serverList) {
						for(Player player:serverList.keySet()) {
							if(serverBuffer.containsKey(player)) {
								if(serverBuffer.get(player).length > 0) {
									toDo = true;
								}
							}
						}
					}
					if(!toDo) {
						Thread.sleep(100);					
					}
				}
			} catch(Exception e) {}
		}
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
