/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.interfaces.SecurityStationManager;
import logisticspipes.interfaces.routing.ChannelConnectionManager;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.SecurityStationAuthorizedList;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.channels.ChannelConnection;

public class RouterManagerImpl implements RouterManager, ChannelConnectionManager, SecurityStationManager {

	public static final RouterManagerImpl INSTANCE = new RouterManagerImpl();

	private final Map<UUID, ServerRouter> _routersServer = new HashMap<>();

	private final WeakHashMap<LogisticsSecurityTileEntity, Void> _security = new WeakHashMap<>();
	private List<String> _authorized = new LinkedList<>();

	private final ArrayList<ChannelConnection> channelConnectedPipes = new ArrayList<>();

	private RouterManagerImpl() {}

	@Override
	public Router getRouter(@Nonnull UUID id) {
		return _routersServer.get(id);
	}

	@Override
	public boolean isRouter(@Nonnull UUID id) {
		return _routersServer.containsKey(id);
	}

	@Override
	public void removeRouter(@Nonnull UUID id) {
		_routersServer.remove(id);
	}

	@Override
	public Router getOrCreateRouter(@Nonnull UUID uuid, @Nonnull World world, @Nonnull BlockPos pos, boolean forceCreateDuplicate) {
		if (world.isClient) throw new IllegalStateException("Can't create routers on the client.");

		Router r = getRouter(uuid);

		if (r != null) return r;

		if (r == null || !r.isAt(world.dimension.getType(), pos)) {
			synchronized (_routersServer) {
				if (!forceCreateDuplicate) {
					for (Router r2 : _routersServer.values()) {
						if (r2 != null && r2.isAt(world.dimension.getType(), pos)) {
							return r2;
						}
					}
				}
				r = new ServerRouter(uuid, world, pos);

				int rId = r.getSimpleId();
				if (_routersServer.size() > rId) {
					_routersServer.set(rId, r);
				} else {
					_routersServer.ensureCapacity(rId + 1);
					while (_routersServer.size() <= rId) {
						_routersServer.add(null);
					}
					_routersServer.set(rId, r);
				}
				_uuidMap.put(r.getId(), r.getSimpleId());
			}
		}
		return r;
	}

	@Override
	public boolean isRouter(int id) {
		if (MainProxy.isClient()) {
			return true;
		} else {
			return _routersServer.get(id) != null;
		}
	}

	/**
	 * This assumes you know what you are doing. expect exceptions to be thrown
	 * if you pass the wrong side.
	 *
	 * @param id
	 * @param side false for server, true for client.
	 * @return is this a router for the side.
	 */
	@Override
	public boolean isRouterUnsafe(int id, boolean side) {
		if (side) {
			return true;
		} else {
			return _routersServer.get(id) != null;
		}
	}

	@Nonnull
	@Override
	public List<Router> getRouters() {
		if (MainProxy.isClient()) {
			return Collections.unmodifiableList(_routersClient);
		} else {
			return Collections.unmodifiableList(_routersServer);
		}
	}

	@Override
	public boolean hasChannelConnection(Router router) {
		return channelConnectedPipes.stream()
				.filter(con -> con.routers.size() > 1)
				.anyMatch(con -> con.routers.contains(router.getSimpleId()));
	}

	@Override
	public boolean addChannelConnection(UUID ident, Router router) {
		if (MainProxy.isClient()) {
			return false;
		}
		int routerSimpleID = router.getSimpleId();
		channelConnectedPipes.forEach(con -> con.routers.remove(routerSimpleID));
		Optional<ChannelConnection> channel = channelConnectedPipes.stream().filter(con -> con.identifier.equals(ident)).findFirst();
		if (channel.isPresent()) {
			channel.get().routers.add(routerSimpleID);
		} else {
			ChannelConnection newChannel = new ChannelConnection();
			channelConnectedPipes.add(newChannel);
			newChannel.identifier = ident;
			newChannel.routers.add(routerSimpleID);
		}
		return true;
	}

	@Override
	public List<CoreRoutedPipe> getConnectedPipes(Router router) {
		Optional<ChannelConnection> channel = channelConnectedPipes.stream()
				.filter(con -> con.routers.contains(router.getSimpleId()))
				.findFirst();
		return channel.
				map(channelConnection ->
						channelConnection.routers.stream()
								.filter(r -> r != router.getSimpleId())
								.map(r -> getRouter(r).getPipe())
								.filter(Objects::nonNull)
								.collect(Collectors.toList())
				)
				.orElse(Collections.emptyList());
	}

	@Override
	public void removeChannelConnection(Router router) {
		if (MainProxy.isClient()) {
			return;
		}
		Optional<ChannelConnection> channel = channelConnectedPipes.stream()
				.filter(con -> con.routers.contains(router.getSimpleId()))
				.findFirst();
		channel.ifPresent(chan -> chan.routers.remove(router.getSimpleId()));
		if (channel.filter(chan -> chan.routers.isEmpty()).isPresent()) {
			channelConnectedPipes.remove(channel.get());
		}
	}

	@Override
	public void serverStopClean() {
		channelConnectedPipes.clear();
		_routersServer.clear();
		_uuidMap.clear();
		_security.clear();
	}

	@Override
	public void clearClientRouters() {
		synchronized (_routersClient) {
			_routersClient.clear();
		}
	}

	@Override
	public void add(LogisticsSecurityTileEntity tile) {
		_security.put(tile, null);
		authorizeUUID(tile.getSecId());
	}

	@Override
	public LogisticsSecurityTileEntity getStation(UUID id) {
		if (id == null) {
			return null;
		}
		for (LogisticsSecurityTileEntity tile : _security.keySet()) {
			if (id.equals(tile.getSecId())) {
				return tile;
			}
		}
		return null;
	}

	@Override
	public void remove(LogisticsSecurityTileEntity tile) {
		_security.remove(tile);
		deauthorizeUUID(tile.getSecId());
	}

	@Override
	public void dimensionUnloaded(int dim) {
		synchronized (_routersServer) {
			_routersServer.stream().filter(r -> r != null && r.isInDim(dim)).forEach(r -> {
				r.clearPipeCache();
				r.clearInterests();
			});
		}
	}

	@Override
	public void deauthorizeUUID(UUID id) {
		_authorized.remove(id.toString());
		sendClientAuthorizationList();
	}

	@Override
	public void authorizeUUID(UUID id) {
		if (!_authorized.contains(id.toString())) {
			_authorized.add(id.toString());
		}
		sendClientAuthorizationList();
	}

	@Override
	public boolean isAuthorized(UUID id) {
		if (_authorized.isEmpty() || id == null) {
			return false;
		}
		return _authorized.contains(id.toString());
	}

	@Override
	public boolean isAuthorized(String id) {
		if (_authorized.isEmpty() || id == null) {
			return false;
		}
		return _authorized.contains(id);
	}

	@Override
	public void setClientAuthorizationList(List<String> list) {
		_authorized = list;
	}

	@Override
	public void sendClientAuthorizationList() {
		MainProxy.sendToAllPlayers(PacketHandler.getPacket(SecurityStationAuthorizedList.class).setStringList(_authorized));
	}

	@Override
	public void sendClientAuthorizationList(EntityPlayer player) {
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SecurityStationAuthorizedList.class).setStringList(_authorized), player);
	}

	@Override
	public void printAllRouters() {
		_routersServer.stream().filter(router -> router != null).forEach(router -> System.out.println(router.toString()));
	}
}
