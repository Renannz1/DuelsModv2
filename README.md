# DuelsMod - Sistema de Duelos 1v1 para Hytale

Plugin para servidores Hytale que adiciona um sistema completo de duelos PvP 1v1 em servidores Survival onde o PvP global está desativado.

## 📋 Características

- **Sistema de Convites**: Desafie outros jogadores para duelos 1v1
- **PvP Controlado**: PvP habilitado apenas entre jogadores em duelo ativo
- **Countdown**: Contagem regressiva de 3 segundos antes do início do combate
- **Convites com Expiração**: Convites expiram automaticamente após 30 segundos
- **Detecção de Vitória**: Sistema automático que detecta morte ou desconexão
- **Sem Permissões**: Qualquer jogador pode usar os comandos

## 🎮 Comandos

Todos os comandos começam com `/duel`:

| Comando | Aliases | Descrição |
|---------|---------|-----------|
| `/duel challenge <jogador>` | `/duel desafiar <jogador>` | Desafiar um jogador para duelo |
| `/duel accept` | `/duel aceitar` | Aceitar um convite de duelo |
| `/duel decline` | `/duel recusar` | Recusar um convite de duelo |
| `/duel` | - | Mostrar ajuda com todos os comandos |

## 📖 Como Usar

### Desafiando um Jogador

```
/duel challenge NomeDoJogador
```

O jogador desafiado receberá uma mensagem com instruções para aceitar ou recusar o duelo.

### Aceitando um Duelo

Quando você recebe um convite de duelo:

```
/duel accept
```

Após aceitar, haverá uma contagem regressiva de 3 segundos e então o duelo começa!

### Recusando um Duelo

Se você não quer duelar:

```
/duel decline
```

### Durante o Duelo

- ✅ Você pode atacar seu oponente
- ✅ Seu oponente pode atacar você
- ❌ Outros jogadores não podem interferir
- ❌ Você não pode atacar outros jogadores

### Fim do Duelo

O duelo termina quando:
- Um dos jogadores morre (o outro vence)
- Um dos jogadores desconecta (o outro vence)

## 🔧 Instalação

1. Compile o plugin:
   ```bash
   mvn clean package
   ```

2. Copie o arquivo `target/DuelMod-1.0.0.jar` para a pasta `plugins` do seu servidor Hytale

3. Reinicie o servidor

4. O plugin será carregado automaticamente

## ⚙️ Configuração

O plugin não requer configuração adicional. Funciona imediatamente após a instalação.

### Requisitos

- Servidor Hytale (versão compatível com a API usada)
- Java 25 ou superior
- Maven (para compilação)

## 🎨 Mensagens Coloridas

O plugin usa o sistema de cores do Hytale para mensagens visuais:

- 🟢 **Verde** - Mensagens de sucesso e confirmação
- 🔴 **Vermelho** - Erros e avisos
- 🟡 **Amarelo** - Destaques e nomes de jogadores
- ⚪ **Cinza** - Texto informativo

## 🐛 Problemas Conhecidos

Nenhum problema conhecido no momento.

## 📝 Changelog

### v1.0.0 (2026-01-15)
- ✨ Lançamento inicial
- ✅ Sistema de convites de duelo
- ✅ PvP controlado entre duelistas
- ✅ Countdown antes do início
- ✅ Detecção automática de vitória
- ✅ Mensagens coloridas
- ✅ Comandos em português e inglês

## 👥 Contribuindo

Contribuições são bem-vindas! Veja o arquivo `DEVELOPMENT.md` para detalhes técnicos sobre o código.

## 📄 Licença

Este projeto é fornecido como está, sem garantias.

## 🔗 Links

- Repositório: `C:\Users\Manoela\Documents\renan-workspace\dev\hytale\DuelsMod`
- Servidor Descompilado: `C:\Users\Manoela\Documents\renan-workspace\dev\hytale\decompile-hytale-server`
