# DuelsMod - Documentação Técnica

Documentação técnica para desenvolvedores que desejam entender ou modificar o código do DuelsMod.

## 🏗️ Arquitetura

O plugin é estruturado em 4 pacotes principais:

```
dev.hytalemodding/
├── DuelsPlugin.java          # Classe principal do plugin
├── commands/
│   └── DuelCommand.java      # Comando /duel
├── duel/
│   ├── Duel.java            # Representa um duelo ativo
│   ├── DuelCountdown.java   # Countdown antes do início
│   ├── DuelManager.java     # Gerencia todos os duelos
│   └── DuelRequest.java     # Representa um convite pendente
└── listeners/
    └── DuelListener.java    # Sistemas ECS para PvP e morte
```

## 🔌 Classe Principal: DuelsPlugin

```java
public class DuelsPlugin extends JavaPlugin
```

### Responsabilidades

1. **Registro de Comandos**: Registra o comando `/duel`
2. **Registro de Sistemas ECS**: 
   - `DuelPvPSystem` - Controla dano entre jogadores
   - `DuelDeathSystem` - Detecta morte de jogadores em duelo
3. **Registro de Eventos**: Listener para desconexão de jogadores
4. **Task Agendada**: Limpeza de convites expirados a cada 10 segundos

### Métodos Importantes

- `setup()` - Inicialização do plugin
- `shutdown()` - Desligamento do plugin

## 🎮 Sistema de Comandos

### DuelCommand

Herda de `AbstractCommand` e implementa todos os subcomandos do sistema de duelos.

#### Métodos Principais

```java
// Desabilita verificação de permissão
protected boolean canGeneratePermission()

// Execução principal do comando
protected CompletableFuture<Void> execute(CommandContext context)

// Handlers para cada subcomando
private void handleChallenge(CommandContext, PlayerRef, String)
private void handleAccept(CommandContext, PlayerRef)
private void handleDecline(CommandContext, PlayerRef)
private void sendHelp(CommandContext)
```

#### Formatação de Mensagens

O Hytale usa o método `.color()` para colorir mensagens:

```java
Message msg = Message.raw("[DUELO] ").color("#FFFF55")
    .insert(Message.raw("Texto").color("#AAAAAA"));
```

**Cores Padrão:**
- `#FF5555` - Vermelho (erros)
- `#55FF55` - Verde (sucesso)
- `#FFFF55` - Amarelo (destaque)
- `#AAAAAA` - Cinza (texto normal)

## 🎯 Sistema de Duelos

### DuelManager (Singleton)

Gerencia todos os duelos ativos e convites pendentes.

```java
public class DuelManager {
    private static final DuelManager INSTANCE = new DuelManager();
    private final Map<UUID, Duel> activeDuels;
    private final Map<UUID, DuelRequest> pendingInvites;
}
```

#### Métodos Principais

```java
// Enviar convite de duelo
boolean sendDuelRequest(PlayerRef challenger, PlayerRef target)

// Aceitar convite
boolean acceptDuel(UUID accepterUuid)

// Verificar se jogador está em duelo
boolean isInDuel(UUID playerUuid)

// Verificar se dois jogadores estão no mesmo duelo
boolean areInSameDuel(UUID uuid1, UUID uuid2)

// Finalizar duelo
void endDuel(UUID playerUuid, PlayerRef winner)

// Limpar convites expirados
void cleanupExpiredInvites()
```

### Duel

Representa um duelo ativo entre dois jogadores.

```java
public class Duel {
    private final PlayerRef player1;
    private final PlayerRef player2;
    private boolean active;
}
```

### DuelRequest

Representa um convite de duelo pendente.

```java
public class DuelRequest {
    private final PlayerRef challenger;
    private final long timestamp;
    private static final long EXPIRATION_TIME = 30000; // 30 segundos
}
```

### DuelCountdown

Gerencia a contagem regressiva antes do início do duelo.

```java
public static void startCountdown(PlayerRef p1, PlayerRef p2, Duel duel)
```

Usa `ScheduledExecutor` para:
1. Mostrar mensagem de preparação
2. Contar de 3 até 1
3. Ativar o duelo
4. Cancelar a task automaticamente

## 🛡️ Sistemas ECS

### DuelPvPSystem

Sistema que controla o dano entre jogadores.

```java
public class DuelPvPSystem extends DamageEventSystem
```

**Lógica:**
1. Verifica se o dano é entre dois jogadores
2. Se estão no mesmo duelo E o duelo está ativo → permite dano
3. Caso contrário → cancela dano (PvP desabilitado)

**Método Principal:**
```java
public void handle(int index, ArchetypeChunk chunk, 
                   Store store, CommandBuffer buffer, Damage damage)
```

### DuelDeathSystem

Sistema que detecta morte de jogadores em duelo.

```java
public class DuelDeathSystem extends DeathSystems.OnDeathSystem
```

**Lógica:**
1. Quando `DeathComponent` é adicionado a um jogador
2. Verifica se o jogador está em duelo
3. Se sim → finaliza o duelo com o oponente como vencedor

**Método Principal:**
```java
public void onComponentAdded(Ref ref, DeathComponent component,
                             Store store, CommandBuffer buffer)
```

## 🔄 Fluxo de um Duelo

```
1. Jogador A: /duel challenge JogadorB
   ↓
2. DuelManager cria DuelRequest
   ↓
3. Jogador B recebe convite
   ↓
4. Jogador B: /duel accept
   ↓
5. DuelManager cria Duel (inativo)
   ↓
6. DuelCountdown inicia (3, 2, 1...)
   ↓
7. Duel.setActive(true)
   ↓
8. DuelPvPSystem permite dano entre A e B
   ↓
9. Um jogador morre OU desconecta
   ↓
10. DuelDeathSystem OU PlayerDisconnectEvent
    ↓
11. DuelManager.endDuel(vencedor)
    ↓
12. Duelo removido, mensagens enviadas
```

## 🔧 Compilação

```bash
# Compilar
mvn clean compile

# Compilar e empacotar
mvn clean package

# Arquivo gerado
target/DuelMod-1.0.0.jar
```

## 📦 Dependências

Definidas em `pom.xml`:

```xml
<dependency>
    <groupId>com.hypixel.hytale</groupId>
    <artifactId>HytaleServer-parent</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>C:/Users/Manoela/Documents/renan-workspace/dev/hytale/server-hytale/Server/HytaleServer.jar</systemPath>
</dependency>
```

## 🧪 Testando

### Teste Manual

1. Inicie o servidor Hytale
2. Entre com dois jogadores
3. Execute: `/duel challenge JogadorB`
4. Com JogadorB: `/duel accept`
5. Aguarde countdown
6. Teste o combate PvP
7. Verifique detecção de vitória

### Logs

O plugin registra logs importantes:

```
[INFO] DuelMod carregado com sucesso!
[INFO] DuelMod descarregado!
```

## 🐛 Debug

Para debug, adicione logs:

```java
import java.util.logging.Level;

this.getLogger().at(Level.INFO).log("Mensagem de debug");
```

## 📚 Referências da API Hytale

### Classes Importantes

- `JavaPlugin` - Classe base para plugins
- `AbstractCommand` - Classe base para comandos
- `Message` - Sistema de mensagens
- `PlayerRef` - Referência a um jogador
- `DamageEventSystem` - Sistema para eventos de dano
- `DeathSystems.OnDeathSystem` - Sistema para eventos de morte
- `ComponentAccessor` - Acesso a componentes ECS
- `Store` - Armazenamento de entidades
- `CommandBuffer` - Buffer de comandos ECS

### Servidor Descompilado

Para referência da API, consulte:
```
C:\Users\Manoela\Documents\renan-workspace\dev\hytale\decompile-hytale-server
```

## 🔮 Possíveis Melhorias Futuras

- [ ] Sistema de ranking/estatísticas
- [ ] Arenas específicas para duelos
- [ ] Apostas/recompensas
- [ ] Duelos em equipe (2v2, 3v3)
- [ ] Configuração via arquivo
- [ ] Cooldown entre duelos
- [ ] Sistema de ELO/matchmaking
- [ ] Replay de duelos
- [ ] Espectadores

## 💡 Dicas de Desenvolvimento

1. **Sempre use sistemas ECS** para eventos de entidades (dano, morte)
2. **Não use códigos §** para cores, use `.color("#hex")`
3. **PlayerRef vs Player**: PlayerRef é a referência, Player é o componente
4. **UUID é a chave**: Use UUID para identificar jogadores
5. **Singleton para managers**: DuelManager usa padrão Singleton
6. **CompletableFuture**: Comandos retornam CompletableFuture<Void>
7. **ScheduledExecutor**: Use para tasks agendadas

## 📞 Suporte

Para dúvidas ou problemas, consulte:
- Código descompilado do servidor Hytale
- Documentação oficial da API (quando disponível)
- Exemplos no código descompilado
