package dev.hytalemodding.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.hytalemodding.duel.Duel;
import dev.hytalemodding.duel.DuelCountdown;
import dev.hytalemodding.duel.DuelManager;
import dev.hytalemodding.duel.DuelRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class DuelAcceptCommand extends AbstractCommand {
    
    public DuelAcceptCommand() {
        super("accept", "Aceitar um convite de duelo");
        this.addAliases("aceitar");
    }
    
    @Override
    protected boolean canGeneratePermission() {
        return false;
    }
    
    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("Apenas jogadores podem usar este comando!").color("#FF5555"));
            return CompletableFuture.completedFuture(null);
        }
        
        Player player = (Player) context.senderAs(Player.class);
        PlayerRef accepter = player.getPlayerRef();
        
        DuelManager manager = DuelManager.getInstance();
        
        DuelRequest request = manager.getPendingInvite(accepter.getUuid());
        if (request == null) {
            context.sendMessage(Message.raw("Você não tem nenhum convite de duelo pendente!").color("#FF5555"));
            return CompletableFuture.completedFuture(null);
        }
        
        if (request.isExpired()) {
            manager.removePendingInvite(accepter.getUuid());
            context.sendMessage(Message.raw("O convite de duelo expirou!").color("#FF5555"));
            return CompletableFuture.completedFuture(null);
        }
        
        PlayerRef challenger = request.getChallenger();
        
        // Verificar se o desafiante ainda está online e disponível
        if (manager.isInDuel(challenger.getUuid())) {
            manager.removePendingInvite(accepter.getUuid());
            context.sendMessage(Message.raw("O desafiante já está em outro duelo!").color("#FF5555"));
            return CompletableFuture.completedFuture(null);
        }
        
        // Aceitar duelo
        boolean accepted = manager.acceptDuel(accepter.getUuid());
        if (!accepted) {
            context.sendMessage(Message.raw("Não foi possível aceitar o duelo!").color("#FF5555"));
            return CompletableFuture.completedFuture(null);
        }
        
        Duel duel = manager.getDuel(accepter.getUuid());
        
        System.out.println("[DuelMod] Duelo aceito, iniciando countdown...");
        System.out.println("  Duel object: " + duel);
        System.out.println("  Challenger: " + challenger.getUsername());
        System.out.println("  Accepter: " + accepter.getUsername());
        
        // Mensagens
        Message acceptMsg = Message.raw("[DUELO] ").color("#55FF55")
            .insert(Message.raw(accepter.getUsername()).color("#FFFF55"))
            .insert(Message.raw(" aceitou o duelo!").color("#55FF55"));
        challenger.sendMessage(acceptMsg);
        accepter.sendMessage(acceptMsg);
        
        // Iniciar countdown
        try {
            DuelCountdown.startCountdown(challenger, accepter, duel);
            System.out.println("[DuelMod] Countdown iniciado com sucesso");
        } catch (Exception e) {
            System.err.println("[DuelMod] ERRO ao iniciar countdown:");
            e.printStackTrace();
        }
        
        return CompletableFuture.completedFuture(null);
    }
}
