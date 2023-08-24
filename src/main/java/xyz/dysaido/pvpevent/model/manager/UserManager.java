package xyz.dysaido.pvpevent.model.manager;

import xyz.dysaido.pvpevent.PvPEventPlugin;
import xyz.dysaido.pvpevent.model.User;
import xyz.dysaido.pvpevent.serializer.UserSerializer;
import xyz.dysaido.pvpevent.util.YamlStorage;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserManager extends AbstractManager<UUID, User> {

    private final Map<UUID, User> objects = new ConcurrentHashMap<>();
    private final PvPEventPlugin pvpEvent;
    private final UserSerializer serializer;

    public UserManager(PvPEventPlugin pvpEvent) {
        this.pvpEvent = pvpEvent;
        this.serializer = new UserSerializer(this, YamlStorage.of(pvpEvent.getPlugin(), "users"));
    }

    @Override
    public User getOrMake(UUID id) {
        User user = super.getOrMake(id);
        serializer.append(user);
        return user;
    }

    @Override
    public User remove(UUID id) {
        User user = getIfPresent(id);
        objects.remove(id);
        serializer.remove(id);
        return user;
    }

    @Override
    public void load() {
        CompletableFuture.supplyAsync(serializer::read)
                .whenComplete((users, throwable) -> {
                    Map<UUID, User> usersByUuid = users.stream()
                            .collect(Collectors.toMap(User::getIdentifier, Function.identity()));
                    this.objects.putAll(usersByUuid);
                });
    }

    public List<User> getToplist(int limit) {
        List<User> topList = new ArrayList<>(objects.values());
        topList.sort(Comparator.comparingInt(User::getWins).reversed());

        Iterator<User> iterator = topList.iterator();

        for (int i = 0; i < Math.max(10, limit) && iterator.hasNext(); i++) {
            topList.add(iterator.next());
        }

        return Collections.unmodifiableList(topList);
    }

    @Override
    public void unload() {
        serializer.write();
        super.unload();
    }

    @Override
    public User apply(UUID uuid) {
        return new User(uuid);
    }

    public UserSerializer getSerializer() {
        return serializer;
    }

    @Override
    protected Map<UUID, User> objects() {
        return objects;
    }
}
