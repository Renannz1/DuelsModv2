package dev.hytalemodding.listeners;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.hytalemodding.duel.Duel;
import dev.hytalemodding.duel.DuelManager;
import dev.hytalemodding.duel.DuelRespawnHandler;

import javax.annotation.Nonnull;
import java.util.UUID;

public class DuelListener {
    
    // Sistema ECS para controlar PvP em duelos
    public static class DuelPvPSystem extends DamageEventSystem {
        private static final Query QUERY = Player.getComponentType();
        
        @Nonnull
        @Override
        public Query getQuery() {
            return QUERY;
        }
        
        @Override
        public SystemGroup getGroup() {
            return com.hypixel.hytale.server.core.modules.entity.damage.DamageModule.get().getFilterDamageGroup();
        }
        
        @Override
        public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Damage damage) {
            // Pegar a vítima (sempre é um jogador porque a Query filtra)
            Ref<EntityStore> victimRef = archetypeChunk.getReferenceTo(index);
            ComponentAccessor accessor = store;
            Player victim = (Player) accessor.getComponent(victimRef, Player.getComponentType());
            
            if (victim == null) {
                return;
            }
            
            UUID victimUuid = victim.getUuid();
            
            // Verificar se é dano de outro jogador
            Damage.Source source = damage.getSource();
            
            // Tentar pegar o atacante
            UUID attackerUuid = null;
            
            if (source instanceof Damage.EntitySource) {
                Damage.EntitySource entitySource = (Damage.EntitySource) source;
                Ref<EntityStore> attackerRef = entitySource.getRef();
                if (attackerRef != null) {
                    Player attacker = (Player) accessor.getComponent(attackerRef, Player.getComponentType());
                    if (attacker != null) {
                        attackerUuid = attacker.getUuid();
                    }
                }
            }
            
            // Se não conseguiu pegar o atacante, não é PvP
            if (attackerUuid == null) {
                return;
            }
            
            DuelManager manager = DuelManager.getInstance();
            
            // Verificar estado do duelo
            boolean inSameDuel = manager.areInSameDuel(attackerUuid, victimUuid);
            Duel duel = manager.getDuel(attackerUuid);
            boolean isActive = duel != null && duel.isActive();
            
            // Se estão no mesmo duelo e o duelo está ativo, permitir dano
            if (inSameDuel && isActive) {
                return;
            }
            
            // Caso contrário, cancelar dano (PvP desabilitado)
            damage.setCancelled(true);
        }
    }
    
    // Sistema ECS para detectar morte de jogadores em duelo
    public static class DuelDeathSystem extends DeathSystems.OnDeathSystem {
        private static final Query QUERY = Player.getComponentType();
        
        @Nonnull
        @Override
        public Query getQuery() {
            return QUERY;
        }
        
        @Override
        public void onComponentAdded(@Nonnull Ref<EntityStore> ref, @Nonnull DeathComponent component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
            // Quando um jogador morre, verificar se está em duelo
            ComponentAccessor accessor = store;
            Player player = (Player) accessor.getComponent(ref, Player.getComponentType());
            
            if (player == null) return;
            
            PlayerRef playerRef = player.getPlayerRef();
            UUID playerUuid = playerRef.getUuid();
            
            DuelManager manager = DuelManager.getInstance();
            
            if (manager.isInDuel(playerUuid)) {
                Duel duel = manager.getDuel(playerUuid);
                if (duel != null) {
                    // O oponente venceu
                    PlayerRef opponent = duel.getOpponent(playerRef);
                    manager.endDuel(playerUuid, opponent);
                }
            }
        }
    }
    
    public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
        // Quando um jogador desconecta, verificar se está em duelo
        PlayerRef playerRef = event.getPlayerRef();
        UUID playerUuid = playerRef.getUuid();
        
        DuelManager manager = DuelManager.getInstance();
        
        if (manager.isInDuel(playerUuid)) {
            Duel duel = manager.getDuel(playerUuid);
            if (duel != null) {
                // O oponente venceu por desconexão
                PlayerRef opponent = duel.getOpponent(playerRef);
                manager.endDuel(playerUuid, opponent);
            }
        }
    }
    
    public static void onPlayerRespawn(AddPlayerToWorldEvent event) {
        // Quando um jogador respawna, restaurar sua posição se estiver marcado
        com.hypixel.hytale.component.Holder holder = event.getHolder();
        PlayerRef playerRef = (PlayerRef) holder.getComponent(PlayerRef.getComponentType());
        if (playerRef != null) {
            DuelRespawnHandler.restorePositionIfPending(playerRef);
        }
    }
}
