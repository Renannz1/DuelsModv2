package dev.hytalemodding.duel;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

import java.util.List;

/**
 * Gerencia o fim do duelo e restauração de inventários e posições
 */
public class DuelEndHandler {
    
    public static void startEndSequence(PlayerRef player1, PlayerRef player2, 
                                       PlayerRef winner, Duel duel) {
        // Mensagem de fim
        Message endMsg = Message.raw("[DUELO] ").color("#FF5555")
            .insert(Message.raw("O duelo terminou!").color("#AAAAAA"));
        player1.sendMessage(endMsg);
        player2.sendMessage(endMsg);
        
        if (winner != null) {
            Message winMsg = Message.raw("[DUELO] ").color("#55FF55")
                .insert(Message.raw(winner.getUsername()).color("#FFFF55"))
                .insert(Message.raw(" venceu o duelo!").color("#55FF55"));
            player1.sendMessage(winMsg);
            player2.sendMessage(winMsg);
            
            // Identificar o perdedor
            PlayerRef loser = winner.equals(player1) ? player2 : player1;
            
            // Marcar o perdedor para restauração de posição no respawn
            DuelPlayerInventory loserInventory = winner.equals(player1) ? 
                duel.getPlayer2Inventory() : duel.getPlayer1Inventory();
            if (loserInventory != null) {
                DuelRespawnHandler.markForPositionRestoration(loser.getUuid(), loserInventory.getPosition());
            }
        }
        
        // Criar thread para restauração de inventários e vida do vencedor
        new Thread(() -> {
            try {
                // Aguardar 5 segundos (tempo para respawn acontecer)
                Thread.sleep(5000);
                
                // Mensagem de retorno (enviada uma vez para cada jogador)
                Message returnMsg = Message.raw("[DUELO] ").color("#FFFF55")
                    .insert(Message.raw("Restaurando posição e inventário...").color("#AAAAAA"));
                player1.sendMessage(returnMsg);
                player2.sendMessage(returnMsg);
                
                // Restaurar inventário do vencedor
                if (winner != null) {
                    DuelPlayerInventory winnerInventory = winner.equals(player1) ? 
                        duel.getPlayer1Inventory() : duel.getPlayer2Inventory();
                    restorePlayerPosition(winner, winnerInventory);
                    restorePlayerInventory(winner, winnerInventory);
                    restoreWinnerHealth(winner);
                }
                
                // Restaurar inventário do perdedor (posição será restaurada no respawn)
                PlayerRef loser = winner != null && winner.equals(player1) ? player2 : player1;
                DuelPlayerInventory loserInventory = winner != null && winner.equals(player1) ? 
                    duel.getPlayer2Inventory() : duel.getPlayer1Inventory();
                restorePlayerInventory(loser, loserInventory);
                
                // Mensagem de conclusão (enviada uma vez para cada jogador)
                Message doneMsg = Message.raw("[DUELO] ").color("#55FF55")
                    .insert(Message.raw("Posição e inventário restaurados!").color("#AAAAAA"));
                player1.sendMessage(doneMsg);
                player2.sendMessage(doneMsg);
                
            } catch (InterruptedException e) {
                // Sequência interrompida
            }
        }).start();
    }
    
    private static void restorePlayerPosition(PlayerRef playerRef, DuelPlayerInventory savedInventory) {
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
            addTeleportComponent(reference, store, savedInventory);
        }, world);
    }
    
    private static void addTeleportComponent(Ref reference, com.hypixel.hytale.component.Store store, DuelPlayerInventory savedInventory) {
        // Criar componente Teleport apenas com a posição
        // A rotação será resetada naturalmente pelo servidor
        Teleport teleport = new Teleport(
            Universe.get().getDefaultWorld(),
            savedInventory.getPosition(),
            new com.hypixel.hytale.math.vector.Vector3f(0, 0, 0)  // Rotação neutra
        );
        
        // Usar putComponent para adicionar/atualizar o componente
        store.putComponent(reference, Teleport.getComponentType(), teleport);
    }
    
    private static void restorePlayerInventory(PlayerRef playerRef, DuelPlayerInventory savedInventory) {
        // Obter o Player
        Player player = (Player) playerRef.getComponent(Player.getComponentType());
        if (player == null) return;
        
        // Obter inventário
        Inventory inventory = player.getInventory();
        
        // Restaurar cada container
        restoreContainer(inventory.getStorage(), savedInventory.getStorageItems());
        restoreContainer(inventory.getHotbar(), savedInventory.getHotbarItems());
        restoreContainer(inventory.getArmor(), savedInventory.getArmorItems());
        restoreContainer(inventory.getUtility(), savedInventory.getUtilityItems());
        
        // Restaurar slot ativo
        inventory.setActiveHotbarSlot(savedInventory.getActiveHotbarSlot());
        
        // Marcar inventário como alterado para sincronizar com cliente
        inventory.markChanged();
    }
    
    private static void restoreWinnerHealth(PlayerRef playerRef) {
        // Obter a referência do jogador
        Ref reference = playerRef.getReference();
        if (reference == null) return;
        
        // Obter a Store
        com.hypixel.hytale.component.Store store = reference.getStore();
        
        // Executar na thread da Store
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            // Obter o EntityStatMap
            com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap statMap = 
                (com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap) store.getComponent(
                    reference, 
                    com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap.getComponentType());
            
            if (statMap != null) {
                // Restaurar saúde do vencedor para o máximo
                int healthStatIndex = com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getHealth();
                statMap.setStatValue(healthStatIndex, 20f);
            }
        }, ((com.hypixel.hytale.server.core.universe.world.storage.EntityStore) store.getExternalData()).getWorld());
    }
    
    private static void restoreContainer(ItemContainer container, List<ItemStack> items) {
        // Limpar container
        container.clear();
        
        // Restaurar itens
        for (short i = 0; i < items.size() && i < container.getCapacity(); i++) {
            ItemStack item = items.get(i);
            if (item != null) {
                container.addItemStack(item, false, false, false);
            }
        }
    }
}
