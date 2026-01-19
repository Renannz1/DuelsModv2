package dev.hytalemodding.duel;

import com.hypixel.hytale.server.core.universe.PlayerRef;

public class DuelRequest {
    private final PlayerRef challenger;
    private final PlayerRef target;
    private final long createdAt;
    private static final long EXPIRATION_TIME = 30000; // 30 segundos
    
    public DuelRequest(PlayerRef challenger, PlayerRef target) {
        this.challenger = challenger;
        this.target = target;
        this.createdAt = System.currentTimeMillis();
    }
    
    public PlayerRef getChallenger() {
        return challenger;
    }
    
    public PlayerRef getTarget() {
        return target;
    }
    
    public boolean isExpired() {
        return System.currentTimeMillis() - createdAt > EXPIRATION_TIME;
    }
    
    public long getRemainingTime() {
        long elapsed = System.currentTimeMillis() - createdAt;
        return Math.max(0, EXPIRATION_TIME - elapsed);
    }
}
