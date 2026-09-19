# LoveBond 💍

[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/technologies/downloads/)
[![Spigot](https://img.shields.io/badge/Spigot%2FPaper-1.13%2B-blue)](https://www.spigotmc.org/)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)
[![MCVersions](https://img.shields.io/badge/MC-1.13%20%E2%86%92%201.21-dbd585)]()

A lightweight **marriage plugin** for Spigot / Paper servers. Players can propose, accept (via a beautiful GUI), get married with fireworks, set up a shared home and teleport together, and send hugs/kisses to their partner. Supports full message customization and Persian/English translations out of the box.

---

## ✨ Features

- 💘 **Proposal system** — `/marry <player>` sends a proposal with an interactive GUI (Accept / Deny)
- 💍 **Marriage** — fireworks, regeneration effect, global announcement
- ❤️ **Shared home** — `/marry sethome`, `/marry home` (your partner is teleported with you!)
- 💔 **Divorce system** with configurable cooldown
- 😘 **Fun commands** — `/marry hug`, `/marry kiss` (with heart particles & sounds)
- 🔥 **Hourly affection** — `/sex` every hour gives both partners a reward (built-in Love Points OR any other economy plugin!)
- 🪙 **Love Points** — built-in point store with `/lovepoints` (send, top leaderboard + admin add/take/set)
- 🌀 **`/marry tp`** — teleport next to your partner (cooldown)
- 💝 **`/marry gift`** — gift the item in your hand to your partner
- 💬 **`/marry chat`** — private chat channel that only your partner can see
- ❤️ **Heart particles** automatically appear whenever partners stand close to each other
- 📊 **Stats & admin tools** — `/marry stats`, `/marry list`, `/marry reload`
- 🗂️ **Persistent storage** — couples, homes & points saved in `data.yml`
- 🌍 **Fully translatable** — every message editable in `messages.yml` (English by default, Persian translation included)
- 🛡️ **No external dependencies** — works on vanilla Spigot/Paper out of the box

---

## 📥 Installation

1. Grab the latest `.jar` from the [Releases](https://github.com/iAghaTraker/LoveBond/releases) page
2. Drop it into your server's `plugins/` folder
3. Restart the server (or run `/reload` + `/marry reload`)
4. Done! Run `/marry` to see the commands

> Requires **Java 17+** and **Spigot/Paper 1.13 or newer**.

---

## 🎮 Commands

| Command | Description | Permission |
|---|---|---|
| `/marry <player>` | Propose marriage to a player | `lovebond.use` |
| `/marry accept` | Accept the pending proposal | `lovebond.use` |
| `/marry deny` | Deny the pending proposal | `lovebond.use` |
| `/marry divorce` | Divorce your partner | `lovebond.use` |
| `/marry divorce <player>` | Force divorce (admin) | `lovebond.admin` |
| `/marry sethome` | Set your shared home | `lovebond.use` |
| `/marry home` | Teleport to your shared home | `lovebond.use` |
| `/marry hug` | Hug your partner ♥ | `lovebond.use` |
| `/marry kiss` | Kiss your partner 💋 (fireworks!) | `lovebond.use` |
| `/marry tp` | Teleport next to your partner | `lovebond.use` |
| `/marry gift` | Gift the item in your hand | `lovebond.use` |
| `/marry chat` | Toggle private couple chat | `lovebond.use` |
| `/marry lovepoints` | Show your love point balance | `lovebond.use` |
| `/marry send <player> <amount>` | Send love points to a player | `lovebond.use` |
| `/sex` | Hourly affection reward for the couple 💝 | `lovebond.use` |
| `/lovepoints` | Love points balance & management | `lovebond.use` |
| `/lovepoints top` | Show top 5 holders | `lovebond.admin` |
| `/lovepoints add/take/set <player> <amount>` | Manage points (admin) | `lovebond.admin` |
| `/marry top` | Show top 5 love point holders | `lovebond.admin` |
| `/marry stats` | Show your marriage info | `lovebond.use` |
| `/marry list` | List all couples (admin) | `lovebond.admin` |
| `/marry reload` | Reload config & messages | `lovebond.admin` |

**Permissions**

```yaml
lovebond.use:    # default: true (everyone)
lovebond.admin:  # default: op
```

---

## ⚙️ Configuration

### `config.yml`
```yaml
settings:
  divorce-cooldown-seconds: 300      # wait time before divorce
  announce-marriage-globally: true   # broadcast new marriages
  proposal-cooldown-seconds: 30      # anti-spam for proposals
  proposal-expire-seconds: 60        # proposal auto-expiry
  enable-fun-commands: true          # /marry hug & /marry kiss
  hug-kiss-max-distance: 25          # max distance for hug/kiss
  effects:
    marriage-fireworks: true
    marriage-regeneration-seconds: 15
```
Home teleport sync, particles and GUI item layouts are also configurable. See the full file after first run.

### Hourly affection & rewards (`config.yml`)
```yaml
affection:
  cooldown-minutes: 60            # /sex cooldown (60 = hourly)
  reward-mode: lovepoints         # 'lovepoints' (built-in) OR 'command'
  reward-amount: 10
  reward-units: 'Love Points'
  # when reward-mode is 'command' - give from ANY other economy/coins plugin:
  reward-command: 'eco give {player} {amount}'   # Vault/Essentials
  # reward-command: 'coins give {player} {amount}' # CoinsEngine
  # reward-command: 'tokens give {player} {amount}' # TokenManager
```
With `reward-mode: lovepoints` couples are rewarded with the built-in **Love Points** store. With `reward-mode: command` the console command you provide is run for each partner (so you can hook into **any** coins/tokens/economy plugin you already use).

### `messages.yml`
Every single message is editable here. Colors use the `&` code system (`&c`, `&6`, `&l`, …).
Placeholders: `{player}`, `{partner}`, `{prefix}`.

> **Persian translation** — a complete Persian translation is included in [`messages_fa.yml`](src/main/resources/messages_fa.yml). Just copy its values into `messages.yml`, or set them directly.

---

## 🛠️ Building from source

```bash
git clone https://github.com/iAghaTraker/LoveBond.git
cd LoveBond
mvn package
# the jar will be in target/LoveBond-1.1.0.jar
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE). Free to use, modify and share.

---

