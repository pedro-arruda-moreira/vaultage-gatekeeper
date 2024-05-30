package com.github.pedroarrudamoreira.vaultage.accesscontrol.suppliers;

import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class GlobalTokenSupplier implements ITokenSupplier {

    private final EventLoop eventLoop;

    private static final Set<String> TOKENS = ObjectFactory.invokeStatic(Collections.class,
            "synchronizedSet", new HashSet<>());


    @Override
    public String generateNewToken() {
        final String uuid = UUID.randomUUID().toString();
        TOKENS.add(uuid);
        eventLoop.schedule(() -> TOKENS.remove(uuid), 5, TimeUnit.MINUTES);
        return uuid;
    }

    @Override
    public boolean isTokenValid(String token) {
        return TOKENS.contains(token);
    }

    @Override
    public boolean removeToken(String uuid) {
        return TOKENS.remove(uuid);
    }

}
