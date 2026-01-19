package dev.hytalemodding.duel;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DuelManager {
    private static DuelManager instance;
    
    // Convites pendentes: UUID do desafiado -> Duel Request
    private final Map<UUID, DuelRequest> pendingInvites = new ConcurrentHashMap<>();
    
    // Duelos ativos: UUID do jogador -> Duel
    private final Map<UUID, Duel> activeDuels = new ConcurrentHashMap<>();
    
    // Estado original do PvP do mundo
    private Boolean originalPvpState = null;
    
    private DuelManager() {}
    
    public static DuelManager getInstance() {
        if (instance == null) {
            instance = new DuelManager();
        }
        return instance;
    }
    
    private void updateWorldPvP() {
        World world = Universe.get().getDefaultWorld();
        if (world == null) return;
        
        WorldConfig config = world.getWorldConfig();
        
        // Se há duelos ativos, habilitar PvP
        if (!activeDuels.isEmpty()) {
            if (originalPvpState == null) {
                // Salvar estado original
                originalPvpState = config.isPvpEnabled();
            }
            
            if (!config.isPvpEnabled()) {
                config.setPvpEnabled(true);
                config.markChanged();
            }
        } else {
            // Se não há duelos, restaurar estado original
            if (originalPvpState != null && config.isPvpEnabled() != originalPvpState) {
                config.setPvpEnabled(originalPvpState);
                config.markChanged();
                originalPvpState = null;
            }
        }
    }
    
    public boolean sendDuelRequest(PlayerRef challenger, PlayerRef target) {
        // Verificar se algum já está em duelo
        if (isInDuel(challenger.getUuid()) || isInDuel(target.getUuid())) {
            return false;
        }
        
        // Verificar se já existe convite pendente
        if (pendingInvites.containsKey(target.getUuid())) {
            return false;
        }
        
        DuelRequest request = new DuelRequest(challenger, target);
        pendingInvites.put(target.getUuid(), request);
        
        return true;
    }
    
    public DuelRequest getPendingInvite(UUID targetUuid) {
        return pendingInvites.get(targetUuid);
    }
    
    public void removePendingInvite(UUID targetUuid) {
        pendingInvites.remove(targetUuid);
    }
    
    public boolean acceptDuel(UUID targetUuid) {
        DuelRequest request = pendingInvites.remove(targetUuid);
        if (request == null || request.isExpired()) {
            return false;
        }
        
        // Criar duelo ativo
        Duel duel = new Duel(request.getChallenger(), request.getTarget());
        
        // Salvar inventários e posições dos jogadores ANTES do duelo começar
        savePlaye
        rState(request.getChallenger(), duel, true);
        savePlayerState(request.getTarget(), duel, false);
        
        activeDuels.put(request.getChallenger().getUuid(), duel);
        activeDuels.put(request.getTarget().getUuid(), duel);
        
        // Habilitar PvP no mundo
        updateWorldPvP();
        
        return true;
    }
    
    private void savePlayerState(PlayerRef playerRef, Duel duel, boolean isPlayer1) {
        // Salvar inventário e posição
        DuelPlayerInventory playerInventory = savePlayerInventory(playerRef);
        
        if (playerInventory != null) {
            if (isPlayer1) {
                duel.setPlayer1Inventory(playerInventory);
            } else {
                duel.setPlayer2Inventory(playerInventory);
            }
        }
    }
    
    private DuelPlayerInventory savePlayerInventory(PlayerRef playerRef) {
        // Obter o Player
        Player player = (Player) playerRef.getComponent(Player.getComponentType());
        if (player == null) return null;
        
        // Obter Transform para posição
        com.hypixel.hytale.server.core.modules.entity.component.TransformComponent transform = 
            (com.hypixel.hytale.server.core.modules.entity.component.TransformComponent) playerRef.getComponent(
                com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        
        if (transform == null) return null;
        
        // Obter inventário
        com.hypixel.hytale.server.core.inventory.Inventory inventory = player.getInventory();
        
        // Criar e retornar estado do inventário com apenas a posição
        return new DuelPlayerInventory(
            transform.getPosition(),
            inventory.getStorage(),
            inventory.getHotbar(),
            inventory.getArmor(),
            inventory.getUtility(),
            inventory.getActiveHotbarSlot()
        );
    }
    
    public boolean isInDuel(UUID playerUuid) {
        return activeDuels.containsKey(playerUuid);
    }
    
    public Duel getDuel(UUID playerUuid) {
        return activeDuels.get(playerUuid);
    }
    
    public boolean areInSameDuel(UUID player1, UUID player2) {
        Duel duel1 = activeDuels.get(player1);
        Duel duel2 = activeDuels.get(player2);
        return duel1 != null && duel1 == duel2;
    }
    
    public void endDuel(UUID playerUuid, PlayerRef winner) {
        Duel duel = activeDuels.get(playerUuid);
        if (duel == null) return;
        
        duel.end(winner);
        
        // Remover dos duelos ativos
        activeDuels.remove(duel.getPlayer1().getUuid());
        activeDuels.remove(duel.getPlayer2().getUuid());
        
        // Atualizar PvP do mundo
        updateWorldPvP();
        
        // Iniciar sequência de fim do duelo com restauração de inventários
        DuelEndHandler.startEndSequence(duel.getPlayer1(), duel.getPlayer2(), winner, duel);
    }
    
    public void cleanupExpiredInvites() {
        pendingInvites.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}
