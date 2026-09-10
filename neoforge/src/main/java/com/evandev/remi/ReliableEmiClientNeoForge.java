package com.evandev.remi;

import com.evandev.ReliableEmi;
import com.evandev.remi.config.ReliableEmiConfig;
import com.evandev.remi.config.ReliableEmiConfigScreen;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

public class ReliableEmiClientNeoForge {
    public static void register(ModContainer container) {
        ReliableEmiConfig.load();

        container.registerExtensionPoint(IConfigScreenFactory.class,
                (minecraft, parent) -> ReliableEmiConfigScreen.createScreen(parent));

        NeoForge.EVENT_BUS.addListener(ReliableEmiClientNeoForge::onRegisterClientCommands);
    }

    private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("remi")
                .then(Commands.literal("reload")
                        .executes(ReliableEmiClientNeoForge::executeReload)
                )
                .executes(ReliableEmiClientNeoForge::executeReload)
        );
    }

    private static int executeReload(CommandContext<CommandSourceStack> context) {
        try {
            ReliableEmiConfig.reload();
            context.getSource().sendSuccess(() -> ReliableEmi.text("command.reload.success"), false);
        } catch (Exception e) {
            ReliableEmi.LOGGER.error("Failed to reload Reliable EMI configs", e);
            context.getSource().sendFailure(ReliableEmi.text("command.reload.failure", e.getMessage()));
        }
        return Command.SINGLE_SUCCESS;
    }
}