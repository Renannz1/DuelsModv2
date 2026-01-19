# Arquitetura do DuelMod

## Visão Geral do Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                         HYTALE SERVER                            │
│                     (PvP Global: DISABLED)                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         ExamplePlugin                            │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  Registra:                                                 │  │
│  │  • DuelCommand                                             │  │
│  │  • DuelListener (Damage, Death, Disconnect)               │  │
│  │  • Task de limpeza de convites expirados                  │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
        ┌─────────────────────┴─────────────────────┐
        │                                             │
        ▼                                             ▼
┌──────────────────┐                      ┌──────────────────┐
│  DuelCommand     │                      │  DuelListener    │
│                  │                      │                  │
│  /duel challenge │                      │  onDamage()      │
│  /duel accept    │                      │  onDeath()       │
│  /duel decline   │                      │  onDisconnect()  │
└──────────────────┘                      └──────────────────┘
        │                                             │
        └─────────────────────┬─────────────────────┘
                              ▼
                    ┌──────────────────┐
                    │  DuelManager     │
                    │  (Singleton)     │
                    │                  │
                    │  • Convites      │
                    │  • Duelos Ativos │
                    └──────────────────┘
                              │
                ┌─────────────┴─────────────┐
                ▼                           ▼
        ┌──────────────┐            ┌──────────────┐
        │ DuelRequest  │            │    Duel      │
        │              │            │              │
        │ • Desafiante │            │ • Jogador 1  │
        │ • Alvo       │            │ • Jogador 2  │
        │ • Timestamp  │            │ • Ativo?     │
        │ • Expirado?  │            │ • Vencedor   │
        └──────────────┘            └──────────────┘
                                            │
                                            ▼
                                    ┌──────────────┐
                                    │DuelCountdown │
                                    │              │
                                    │ 3... 2... 1..│
                                    │    LUTEM!    │
                                    └──────────────┘
```

## Fluxo de Dados

### 1. Desafio de Duelo

```
Jogador A                DuelCommand              DuelManager
    │                         │                         │
    │  /duel challenge B      │                         │
    ├────────────────────────>│                         │
    │                         │  sendDuelRequest()      │
    │                         ├────────────────────────>│
    │                         │                         │
    │                         │  Cria DuelRequest       │
    │                         │  Armazena em Map        │
    │                         │<────────────────────────┤
    │  "Convite enviado"      │                         │
    │<────────────────────────┤                         │
    │                         │                         │
    │                         │  Envia mensagem         │
    │                         │  para Jogador B         │
    │                         └────────────────────────>│
                                                   Jogador B
```

### 2. Aceitação de Duelo

```
Jogador B                DuelCommand              DuelManager           DuelCountdown
    │                         │                         │                      │
    │  /duel accept           │                         │                      │
    ├────────────────────────>│                         │                      │
    │                         │  acceptDuel()           │                      │
    │                         ├────────────────────────>│                      │
    │                         │                         │                      │
    │                         │  Remove DuelRequest     │                      │
    │                         │  Cria Duel              │                      │
    │                         │  Armazena em Map        │                      │
    │                         │<────────────────────────┤                      │
    │                         │                         │                      │
    │                         │  startCountdown()       │                      │
    │                         ├─────────────────────────┴─────────────────────>│
    │                         │                                                 │
    │  "3... 2... 1... LUTEM!"│                         Agenda tasks            │
    │<────────────────────────┤                         com SCHEDULED_EXECUTOR │
    │                         │                                                 │
    │                         │                         Após 3s: duel.setActive(true)
    │                         │<────────────────────────────────────────────────┤
```

### 3. Combate (Interceptação de Dano)

```
Jogador A ataca B        DuelListener             DuelManager              Damage Event
    │                         │                         │                      │
    │  Ataque                 │                         │                      │
    ├─────────────────────────┴─────────────────────────┴─────────────────────>│
    │                         │                         │                      │
    │                         │  onDamage()             │                      │
    │                         │<────────────────────────┴──────────────────────┤
    │                         │                         │                      │
    │                         │  areInSameDuel(A, B)?   │                      │
    │                         ├────────────────────────>│                      │
    │                         │  true                   │                      │
    │                         │<────────────────────────┤                      │
    │                         │                         │                      │
    │                         │  getDuel(A).isActive()? │                      │
    │                         ├────────────────────────>│                      │
    │                         │  true                   │                      │
    │                         │<────────────────────────┤                      │
    │                         │                         │                      │
    │                         │  PERMITIR DANO          │                      │
    │                         │  (não cancela evento)   │                      │
    │                         ├─────────────────────────┴─────────────────────>│
```

### 4. Finalização (Morte)

```
Jogador B morre          DuelListener             DuelManager
    │                         │                         │
    │  Health = 0             │                         │
    │  DeathComponent added   │                         │
    ├────────────────────────>│                         │
    │                         │  onDeath()              │
    │                         │                         │
    │                         │  isInDuel(B)?           │
    │                         ├────────────────────────>│
    │                         │  true                   │
    │                         │<────────────────────────┤
    │                         │                         │
    │                         │  endDuel(B, winner=A)   │
    │                         ├────────────────────────>│
    │                         │                         │
    │                         │  Remove do Map          │
    │                         │  Envia mensagens        │
    │                         │<────────────────────────┤
    │                         │                         │
    │  "A venceu o duelo!"    │                         │
    │<────────────────────────┤                         │
```

## Estrutura de Dados

### DuelManager (Singleton)

```java
Map<UUID, DuelRequest> pendingInvites
    Key: UUID do jogador ALVO (quem recebeu o convite)
    Value: DuelRequest (desafiante, alvo, timestamp)

Map<UUID, Duel> activeDuels
    Key: UUID de QUALQUER jogador no duelo
    Value: Duel (mesma instância para ambos os jogadores)
```

### Exemplo de Estado

```
Estado Inicial:
pendingInvites: {}
activeDuels: {}

Após /duel challenge Steve (por Alex):
pendingInvites: {
    UUID(Steve) -> DuelRequest(Alex, Steve, timestamp)
}
activeDuels: {}

Após /duel accept (por Steve):
pendingInvites: {}
activeDuels: {
    UUID(Alex) -> Duel(Alex, Steve, active=false),
    UUID(Steve) -> Duel(Alex, Steve, active=false)  // mesma instância
}

Após countdown (3 segundos):
activeDuels: {
    UUID(Alex) -> Duel(Alex, Steve, active=true),
    UUID(Steve) -> Duel(Alex, Steve, active=true)
}

Após Steve morrer:
pendingInvites: {}
activeDuels: {}
```

## Prioridades de Eventos

### Damage Event
- **Prioridade**: HIGH (EventPriority.HIGH)
- **Motivo**: Precisa interceptar ANTES de outros plugins processarem o dano
- **Ação**: Cancela dano se não estiverem em duelo ativo

### Death Event
- **Prioridade**: NORMAL (padrão)
- **Motivo**: Processa após a morte ser confirmada
- **Ação**: Finaliza duelo e anuncia vencedor

### Disconnect Event
- **Prioridade**: NORMAL (padrão)
- **Motivo**: Processa quando jogador desconecta
- **Ação**: Finaliza duelo e anuncia vencedor

## Threads e Concorrência

### Thread Safety
- `DuelManager` usa `ConcurrentHashMap` para thread-safety
- Todos os métodos são thread-safe
- Eventos são processados na thread do servidor

### Scheduled Tasks
- Countdown: `HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate()`
- Limpeza de convites: Task agendada a cada 10 segundos
- Todas as tasks são registradas no `TaskRegistry` para cleanup automático

## Integração com Hytale API

### Component System
```
Player (Component)
    └─> PlayerRef (Component)
        └─> UUID, Username, PacketHandler

EntityStore (Store)
    └─> Contém todos os componentes de entidades
    └─> Acesso via ComponentAccessor
```

### Event System
```
EventRegistry (Plugin)
    └─> EventBus (Global)
        └─> Dispatcha eventos para listeners registrados
```

### Command System
```
CommandRegistry (Plugin)
    └─> CommandManager (Global)
        └─> Processa comandos e chama execute()
```

## Segurança e Validações

### Validações Implementadas

1. **Desafio**
   - ✅ Jogador não pode desafiar a si mesmo
   - ✅ Jogador não pode desafiar se já está em duelo
   - ✅ Alvo não pode estar em duelo
   - ✅ Alvo não pode ter convite pendente

2. **Aceitação**
   - ✅ Convite deve existir
   - ✅ Convite não pode estar expirado
   - ✅ Desafiante deve estar disponível

3. **Combate**
   - ✅ Dano só é permitido entre duelistas
   - ✅ Dano só é permitido se duelo está ativo
   - ✅ Outros jogadores não podem interferir

4. **Finalização**
   - ✅ Duelo termina automaticamente na morte
   - ✅ Duelo termina automaticamente na desconexão
   - ✅ Ambos os jogadores são removidos do Map

## Performance

### Otimizações
- Uso de `ConcurrentHashMap` para acesso O(1)
- Limpeza periódica de convites expirados (evita memory leak)
- Eventos com prioridade adequada (evita processamento desnecessário)
- Singleton pattern para DuelManager (única instância)

### Complexidade
- `sendDuelRequest()`: O(1)
- `acceptDuel()`: O(1)
- `isInDuel()`: O(1)
- `areInSameDuel()`: O(1)
- `endDuel()`: O(1)
- `cleanupExpiredInvites()`: O(n) onde n = número de convites pendentes
