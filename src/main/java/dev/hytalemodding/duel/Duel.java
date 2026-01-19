package dev.hytalemodding.duel;

import com.hypixel.hytale.server.core.universe.PlayerRef;

public class Duel {
    private final PlayerRef player1;
    private final PlayerRef player2;
    private final long startTime;
    private boolean active;
    private PlayerRef winner;
    
    // Inventários salvos dos jogadores
    private DuelPlayerInventory player1Inventory;
    private DuelPlayerInventory player2Inventory;
    
    public Duel(PlayerRef player1, PlayerRef player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.startTime = System.currentTimeMillis();
        this.active = false;
    }
    
    public PlayerRef getPlayer1() {
        return player1;
    }
    
    public PlayerRef getPlayer2() {
        return player2;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public PlayerRef getOpponent(PlayerRef player) {
        if (player.getUuid().equals(player1.getUuid())) {
            return player2;
        } else if (player.getUuid().equals(player2.getUuid())) {
            return player1;
        }
        return null;
    }
    
    public boolean involves(PlayerRef player) {
        return player.getUuid().equals(player1.getUuid()) || 
               player.getUuid().equals(player2.getUuid());
    }
    
    public void end(PlayerRef winner) {
        this.active = false;
        this.winner = winner;
    }
    
    public PlayerRef getWinner() {
        return winner;
    }
    
    public void setPlayer1Inventory(DuelPlayerInventory inventory) {
        this.player1Inventory = inventory;
    }
    
    public void setPlayer2Inventory(DuelPlayerInventory inventory) {
        this.player2Inventory = inventory;
    }
    
    public DuelPlayerInventory getPlayer1Inventory() {
        return player1Inventory;
    }
    
    public DuelPlayerInventory getPlayer2Inventory() {
        return player2Inventory;
    }
    
    public DuelPlayerInventory getInventoryFor(PlayerRef player) {
        if (player.getUuid().equals(player1.getUuid())) {
            return player1Inventory;
        } else if (player.getUuid().equals(player2.getUuid())) {
            return player2Inventory;
        }
        return null;
    }
}
