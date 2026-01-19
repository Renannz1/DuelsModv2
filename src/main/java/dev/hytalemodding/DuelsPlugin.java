package dev.hytalemodding;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.hytalemodding.commands.DuelCommand;
import dev.hytalemodding.duel.DuelManager;
import dev.hytalemodding.listeners.DuelListener;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DuelsPlugin extends JavaPlugin {

    public DuelsPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // Registrar comando de duelo
        this.getCommandRegistry().registerCommand(new DuelCommand());
        
        // Registrar sistema de PvP para duelos
        this.getEntityStoreRegistry().registerSystem(new DuelListener.DuelPvPSystem());
        
        // Registrar listener de desconexão
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, DuelListener::onPlayerDisconnect);
        
        // Registrar listener de respawn
        this.getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, DuelListener::onPlayerRespawn);
        
        // Registrar sistema de morte para duelos
        this.getEntityStoreRegistry().registerSystem(new DuelListener.DuelDeathSystem());
        
        // Task para limpar convites expirados a cada 10 segundos
        this.getTaskRegistry().registerTask(
            CompletableFuture.runAsync(
                () -> {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            Thread.sleep(10000); // 10 segundos
                            DuelManager.getInstance().cleanupExpiredInvites();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                },
                HytaleServer.SCHEDULED_EXECUTOR
            )
        );
        
        this.getLogger().at(Level.INFO).log("DuelMod carregado com sucesso!");
    }
    
    @Override
    protected void shutdown() {
        this.getLogger().at(Level.INFO).log("DuelMod descarregado!");
    }
}
