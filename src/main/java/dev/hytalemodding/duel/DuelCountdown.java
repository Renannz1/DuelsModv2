package dev.hytalemodding.duel;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public class DuelCountdown {
    
    public static void startCountdown(PlayerRef player1, PlayerRef player2, Duel duel) {
        // Mensagem inicial
        Message prepareMsg = Message.raw("[DUELO] ").color("#FFFF55")
            .insert(Message.raw("Preparando para o combate...").color("#AAAAAA"));
        player1.sendMessage(prepareMsg);
        player2.sendMessage(prepareMsg);
        
        // Criar thread para countdown
        new Thread(() -> {
            try {
                // 3
                Thread.sleep(1000);
                Message msg3 = Message.raw("3").color("#FF5555")
                    .insert(Message.raw("...").color("#AAAAAA"));
                player1.sendMessage(msg3);
                player2.sendMessage(msg3);
                
                // 2
                Thread.sleep(1000);
                Message msg2 = Message.raw("2").color("#FF5555")
                    .insert(Message.raw("...").color("#AAAAAA"));
                player1.sendMessage(msg2);
                player2.sendMessage(msg2);
                
                // 1
                Thread.sleep(1000);
                Message msg1 = Message.raw("1").color("#FF5555")
                    .insert(Message.raw("...").color("#AAAAAA"));
                player1.sendMessage(msg1);
                player2.sendMessage(msg1);
                
                // LUTEM!
                Thread.sleep(1000);
                duel.setActive(true);
                
                Message startMsg = Message.raw("[DUELO] ").color("#55FF55")
                    .insert(Message.raw("LUTEM!").color("#FF5555"));
                player1.sendMessage(startMsg);
                player2.sendMessage(startMsg);
            } catch (InterruptedException e) {
                // Countdown interrompido
            }
        }).start();
    }
}
