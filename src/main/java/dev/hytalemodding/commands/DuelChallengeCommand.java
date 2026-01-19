package dev.hytalemodding.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.hytalemodding.duel.Duel;
import dev.hytalemodding.duel.DuelManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class DuelChallengeCommand extends AbstractCommand {
    
    private final RequiredArg<PlayerRef> targetArg;
    
    public DuelChallengeCommand() {
        super("challenge", "Desafiar um jogador para duelo");
        this.addAliases("desafiar");
        
        // Registrar argumento obrigatório: jogador alvo
        this.targetArg = this.withRequiredArg("jogador", "Jogador a ser desafiado", ArgTypes.PLAYER_REF);
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
        PlayerRef challenger = player.getPlayerRef();
        PlayerRef target = this.targetArg.get(context);
        
        DuelManager manager = DuelManager.getInstance();
        
        // Verificar se já está em duelo
        if (manager.isInDuel(challenger.getUuid())) {
            context.sendMessage(Message.raw("Você já está em um duelo!").color("#FF5555"));
            return CompletableFuture.completedFuture(null);
        }
        
        // Não pode desafiar a si mesmo
        if (target.getUuid().equals(challenger.getUuid())) {
            context.sendMessage(Message.raw("Você não pode desafiar a si mesmo!").color("#FF5555"));
            return CompletableFuture.completedFuture(null);
        }
        
        // Verificar se o alvo já está em duelo
        if (manager.isInDuel(target.getUuid())) {
            context.sendMessage(Message.raw("Este jogador já está em um duelo!").color("#FF5555"));
            return CompletableFuture.completedFuture(null);
        }
        
        // Enviar convite
        boolean sent = manager.sendDuelRequest(challenger, target);
        if (!sent) {
            context.sendMessage(Message.raw("Este jogador já tem um convite pendente!").color("#FF5555"));
            return CompletableFuture.completedFuture(null);
        }
        
        // Mensagens
        Message successMsg = Message.raw("[DUELO] ").color("#55FF55")
            .insert(Message.raw("Convite enviado para ").color("#AAAAAA"))
            .insert(Message.raw(target.getUsername()).color("#FFFF55"))
            .insert(Message.raw("!").color("#AAAAAA"));
        context.sendMessage(successMsg);
        
        Message inviteMsg = Message.raw("[DUELO] ").color("#FFFF55")
            .insert(Message.raw(challenger.getUsername()).color("#FFFF55"))
            .insert(Message.raw(" desafiou você para um duelo!\n").color("#AAAAAA"))
            .insert(Message.raw("Use ").color("#AAAAAA"))
            .insert(Message.raw("/duel accept").color("#55FF55"))
            .insert(Message.raw(" para aceitar ou ").color("#AAAAAA"))
            .insert(Message.raw("/duel decline").color("#FF5555"))
            .insert(Message.raw(" para recusar.\n").color("#AAAAAA"))
            .insert(Message.raw("O convite expira em ").color("#AAAAAA"))
            .insert(Message.raw("30 segundos").color("#FF5555"))
            .insert(Message.raw(".").color("#AAAAAA"));
        target.sendMessage(inviteMsg);
        
        return CompletableFuture.completedFuture(null);
    }
}
