package com.github.pedroarrudamoreira.vaultage.process;

import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import lombok.*;

import java.util.function.Consumer;
import java.util.function.IntFunction;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProcessSpawnerOptions {

    final String[] command;

    IntFunction<Boolean> failureCodeHandler = (int ret) -> ret == 0;

    final Consumer<String> logConsumer;

    final EventLoop loop;
}
