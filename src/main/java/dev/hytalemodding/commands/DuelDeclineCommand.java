package dev.hytalemodding.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.hytalemodding.duel.DuelManager;
import dev.hytalemodding.duel.DuelRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class DuelDeclineCommand extends AbstractCommand {
    
    public DuelDeclineCommand() {
        super("decline", "Recusar um convite de duelo");
        this.addAliases("recusar");
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
        PlayerRef decliner = player.getPlayerRef();
        
        DuelManager manager = DuelManager.getInstance();
        
        DuelRequest request = manager.getPendingInvite(decliner.getUuid());
        if (request == null) {
            context.sendMessage(Message.raw("Você não tem nenhum convite de duelo pendente!").color("#FF5555"));
            return CompletableFuture.completedFuture(null);
        }
        
        manager.removePendingInvite(decliner.getUuid());
        
        PlayerRef challenger = request.getChallenger();
        
        Message declineMsg = Message.raw("[DUELO] ").color("#FF5555")
            .insert(Message.raw("Você recusou o convite de duelo.").color("#AAAAAA"));
        context.sendMessage(declineMsg);
        
        Message challengerMsg = Message.raw("[DUELO] ").color("#FF5555")
            .insert(Message.raw(decliner.getUsername()).color("#FFFF55"))
            .insert(Message.raw(" recusou seu convite de duelo.").color("#AAAAAA"));
        challenger.sendMessage(challengerMsg);
        
        return CompletableFuture.completedFuture(null);
    }
}
