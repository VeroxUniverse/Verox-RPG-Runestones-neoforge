package net.veroxuniverse.verox_rpg_runestones.friends;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.veroxuniverse.verox_rpg_runestones.RPGRunestones;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class FriendsData extends SavedData {

    private static final String DATA_NAME = RPGRunestones.MOD_ID + "_friends";

    private final Map<UUID, Set<UUID>> friends = new HashMap<>();
    private final Map<UUID, Set<UUID>> requests = new HashMap<>();
    private final Map<UUID, String> names = new HashMap<>();

    public static FriendsData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(FriendsData::new, FriendsData::load, null), DATA_NAME);
    }

    private static FriendsData load(CompoundTag tag, HolderLookup.Provider provider) {
        FriendsData data = new FriendsData();
        for (Tag element : tag.getList("friends", Tag.TAG_COMPOUND)) {
            CompoundTag pair = (CompoundTag) element;
            data.link(pair.getUUID("a"), pair.getUUID("b"));
        }
        for (Tag element : tag.getList("requests", Tag.TAG_COMPOUND)) {
            CompoundTag request = (CompoundTag) element;
            data.requests.computeIfAbsent(request.getUUID("to"), key -> new HashSet<>()).add(request.getUUID("from"));
        }
        for (Tag element : tag.getList("names", Tag.TAG_COMPOUND)) {
            CompoundTag name = (CompoundTag) element;
            data.names.put(name.getUUID("id"), name.getString("name"));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag pairs = new ListTag();
        for (Map.Entry<UUID, Set<UUID>> entry : this.friends.entrySet()) {
            for (UUID other : entry.getValue()) {
                if (entry.getKey().compareTo(other) >= 0) continue;
                CompoundTag pair = new CompoundTag();
                pair.putUUID("a", entry.getKey());
                pair.putUUID("b", other);
                pairs.add(pair);
            }
        }
        tag.put("friends", pairs);

        ListTag requestList = new ListTag();
        for (Map.Entry<UUID, Set<UUID>> entry : this.requests.entrySet()) {
            for (UUID from : entry.getValue()) {
                CompoundTag request = new CompoundTag();
                request.putUUID("from", from);
                request.putUUID("to", entry.getKey());
                requestList.add(request);
            }
        }
        tag.put("requests", requestList);

        ListTag nameList = new ListTag();
        for (Map.Entry<UUID, String> entry : this.names.entrySet()) {
            CompoundTag name = new CompoundTag();
            name.putUUID("id", entry.getKey());
            name.putString("name", entry.getValue());
            nameList.add(name);
        }
        tag.put("names", nameList);
        return tag;
    }

    public boolean areFriends(UUID a, UUID b) {
        return this.friends.getOrDefault(a, Set.of()).contains(b);
    }

    public Set<UUID> friendsOf(UUID player) {
        return Set.copyOf(this.friends.getOrDefault(player, Set.of()));
    }

    public Set<UUID> incoming(UUID player) {
        return Set.copyOf(this.requests.getOrDefault(player, Set.of()));
    }

    public Set<UUID> outgoing(UUID player) {
        Set<UUID> result = new HashSet<>();
        for (Map.Entry<UUID, Set<UUID>> entry : this.requests.entrySet()) {
            if (entry.getValue().contains(player)) result.add(entry.getKey());
        }
        return result;
    }

    public boolean hasRequest(UUID from, UUID to) {
        return this.requests.getOrDefault(to, Set.of()).contains(from);
    }

    public void addRequest(UUID from, UUID to) {
        this.requests.computeIfAbsent(to, key -> new HashSet<>()).add(from);
        this.setDirty();
    }

    public void removeRequest(UUID from, UUID to) {
        Set<UUID> set = this.requests.get(to);
        if (set != null && set.remove(from)) {
            if (set.isEmpty()) this.requests.remove(to);
            this.setDirty();
        }
    }

    public void addFriendship(UUID a, UUID b) {
        this.link(a, b);
        this.removeRequest(a, b);
        this.removeRequest(b, a);
        this.setDirty();
    }

    public void removeFriendship(UUID a, UUID b) {
        this.friends.getOrDefault(a, new HashSet<>()).remove(b);
        this.friends.getOrDefault(b, new HashSet<>()).remove(a);
        this.setDirty();
    }

    public void rememberName(UUID id, String name) {
        if (!name.equals(this.names.get(id))) {
            this.names.put(id, name);
            this.setDirty();
        }
    }

    public String nameOf(UUID id) {
        return this.names.getOrDefault(id, id.toString().substring(0, 8));
    }

    public Optional<UUID> findByName(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        for (Map.Entry<UUID, String> entry : this.names.entrySet()) {
            if (entry.getValue().toLowerCase(Locale.ROOT).equals(lower)) return Optional.of(entry.getKey());
        }
        return Optional.empty();
    }

    private void link(UUID a, UUID b) {
        this.friends.computeIfAbsent(a, key -> new HashSet<>()).add(b);
        this.friends.computeIfAbsent(b, key -> new HashSet<>()).add(a);
    }
}
