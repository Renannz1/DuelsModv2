package dev.hytalemodding.duel;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia a restauração de posição do perdedor quando ele respawna
 */
public class DuelRespawnHandler {
    
    // Mapa de jogadores que precisam ter sua posição restaurada no respawn
    // UUID -> Posição salva
    private static final Map<UUID, Vector3d> playersToRestore = new ConcurrentHashMap<>();
    
    /**
     * Marca um jogador para ter sua posição restaurada quando respawnar
     */
    public static void markForPositionRestoration(UUID playerUuid, Vector3d position) {
        playersToRestore.put(playerUuid, position);
    }
    
    /**
     * Restaura a posição do jogador se ele estiver marcado para restauração
     */
    public static void restorePositionIfPending(PlayerRef playerRef) {
        UUID playerUuid = playerRef.getUuid();
        Vector3d savedPosition = playersToRestore.remove(playerUuid);
        
        if (savedPosition == null) {
            return;  // Jogador não está marcado para restauração
        }
        
        // Obter a referência do jogador
        Ref reference = playerRef.getReference();
        if (reference == null) return;
        
        // Obter a Store e a World
        com.hypixel.hytale.component.Store store = reference.getStore();
        com.hypixel.hytale.server.core.universe.world.storage.EntityStore entityStore = 
            (com.hypixel.hytale.server.core.universe.world.storage.EntityStore) store.getExternalData();
        com.hypixel.hytale.server.core.universe.world.World world = entityStore.getWorld();
        
        // Executar na thread da World
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            addTeleportComponent(reference, store, savedPosition);
        }, world);
    }
    
    private static void addTeleportComponent(Ref reference, com.hypixel.hytale.component.Store store, Vector3d position) {
        // Criar componente Teleport apenas com a posição
        // A rotação será resetada naturalmente pelo servidor
        Teleport teleport = new Teleport(
            Universe.get().getDefaultWorld(),
            position,
            new com.hypixel.hytale.math.vector.Vector3f(0, 0, 0)  // Rotação neutra
        );
        
        // Usar putComponent para adicionar/atualizar o componente
        store.putComponent(reference, Teleport.getComponentType(), teleport);
    }
}
