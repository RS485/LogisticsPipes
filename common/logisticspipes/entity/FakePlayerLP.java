package logisticspipes.entity;

import java.util.Collection;
import java.util.UUID;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;

import net.minecraft.recipe.Recipe;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import com.mojang.authlib.GameProfile;

public class FakePlayerLP extends ServerPlayerEntity {

	private static final WeakHashMap<ServerWorld, FakePlayerLP> players = new WeakHashMap<>();

	public static GameProfile LPPLAYER = new GameProfile(UUID.fromString("e7d8e347-3828-4f39-b76f-ea519857c004"), "[LogisticsPipes]");

	public FakePlayerLP(ServerWorld world) {
		super(world.getServer(), world, LPPLAYER, new ServerPlayerInteractionManager(world));
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	@Nonnull
	@Override
	public Text getDisplayName() {
		return getName();
	}

	@Override
	public void tick() { }

	@Override
	public int unlockRecipes(Collection<Recipe<?>> collection_1) {
		return 0;
	}

	@Override
	public int lockRecipes(Collection<Recipe<?>> collection_1) {
		return 0;
	}

	public static FakePlayerLP getInstance(ServerWorld world) {
		return players.computeIfAbsent(world, FakePlayerLP::new);
	}

}
