package com.evandev.remi;

import com.evandev.ReliableEmi;
import com.evandev.remi.config.ReliableEmiConfig;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class ReliableEmiClientFabric implements ClientModInitializer {
    private static int executeReload(CommandContext<FabricClientCommandSource> context) {
        try {
            ReliableEmiConfig.reload();
            context.getSource().sendFeedback(ReliableEmi.text("command.reload.success"));
        } catch (Exception e) {
            ReliableEmi.LOGGER.error("Failed to reload Reliable EMI configs", e);
            context.getSource().sendError(ReliableEmi.text("command.reload.failure", e.getMessage()));
        }
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public void onInitializeClient() {
        ReliableEmiConfig.load();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("remi")
                    .then(ClientCommandManager.literal("reload")
                            .executes(ReliableEmiClientFabric::executeReload)
                    )
                    .executes(ReliableEmiClientFabric::executeReload)
            );
        });
    }
}

