package dev.hytalemodding.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class DuelCommand extends AbstractCommand {
    
    public DuelCommand() {
        super("duel", "Sistema de duelos 1v1");
        
        // Registrar subcomandos
        this.addSubCommand(new DuelChallengeCommand());
        this.addSubCommand(new DuelAcceptCommand());
        this.addSubCommand(new DuelDeclineCommand());
    }
    
    @Override
    protected boolean canGeneratePermission() {
        // Desabilitar verificação de permissão - qualquer jogador pode usar
        return false;
    }
    
    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        // Quando executado sem subcomando, mostrar ajuda
        Message helpMsg = Message.raw("[DUELO] ").color("#FFFF55")
            .insert(Message.raw("Comandos disponíveis:").color("#AAAAAA"));
        context.sendMessage(helpMsg);
        
        Message cmd1 = Message.raw("- ").color("#AAAAAA")
            .insert(Message.raw("/duel challenge <jogador>").color("#55FF55"))
            .insert(Message.raw(" - Desafiar um jogador").color("#AAAAAA"));
        context.sendMessage(cmd1);
        
        Message cmd2 = Message.raw("- ").color("#AAAAAA")
            .insert(Message.raw("/duel accept").color("#55FF55"))
            .insert(Message.raw(" - Aceitar um convite de duelo").color("#AAAAAA"));
        context.sendMessage(cmd2);
        
        Message cmd3 = Message.raw("- ").color("#AAAAAA")
            .insert(Message.raw("/duel decline").color("#55FF55"))
            .insert(Message.raw(" - Recusar um convite de duelo").color("#AAAAAA"));
        context.sendMessage(cmd3);
        
        return CompletableFuture.completedFuture(null);
    }
}
