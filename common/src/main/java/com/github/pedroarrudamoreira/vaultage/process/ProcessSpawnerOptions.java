package com.github.pedroarrudamoreira.vaultage.process;

import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import lombok.*;

import java.util.function.Consumer;
import java.util.function.IntFunction;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProcessSpawnerOptions {

    @Getter
    private final String[] command;

    private final IntFunction<Boolean> failureCodeHandler;

    @Getter
    private final Consumer<String> logConsumer;

    @Getter
    private final EventLoop loop;

    public IntFunction<Boolean> getFailureCodeHandler() {
        if (failureCodeHandler == null) {
            return (int ret) -> ret == 0;
        }
        return failureCodeHandler;
    }
}
