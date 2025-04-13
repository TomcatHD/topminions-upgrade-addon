# 🛠️ TopMinions Upgrade Addon – ItemsAdder Compatible

A powerful upgrade and crafting system for [TopMinions](https://www.spigotmc.org/resources/topminions-%E2%9C%85-hypixel-minions-clone-%E2%9A%94%EF%B8%8F-%E2%9A%A1-very-optimized-%E2%9A%99%EF%B8%8Ffully-customizable-1-18-1-21-4.111627/), fully compatible with [ItemsAdder](https://www.spigotmc.org/resources/itemsadder.73355/) and vanilla Minecraft items.

This addon **replaces the default upgrade UI** with a fully configurable system that supports custom items, dynamic recipes, and full GUI translations.

## ✨ Features

- 🧱 **Minion Crafting GUI** with ghost items and slot locking
- 🌍 **Global Crafting Recipes** – general recipes for any item (not just minions)
- 📈 **Custom Upgrade Menus** using ItemsAdder or vanilla items
- 🌐 **100% Translatable** via `messages.yml`
- 🎨 **Supports ItemsAdder CustomStacks** (e.g. `myplugin:gold_plate`)
- 💡 **Requirement Checker** with ✔/✘ feedback in GUI
- 🧠 **Minion Identity via Persistent Data** – reliable recognition of crafted heads
- 🔁 **Navigation Buttons** between crafting ↔ upgrades

## 📦 Installation

1. Drop the JAR into your `plugins/` folder
2. Switch `useUpgradeMenu` to "false" inside Topminions config.yml
3. Go to languages.yml file inside Topminions and change `no_materials: ''` string to be empty
4.  Restart the server
5. Enjoy your new minion experience!

## 📂 Files

- `upgrade.yml` – Upgrade GUI layout, slots, and level logic
- `crafting.yml` – Pattern-based minion & global crafting recipes
- `messages.yml` – GUI names, button texts, lore, feedback messages
- `items.yml` – Optional ItemsAdder item definitions
- to `reload` all files use the command `/iatopminions reload`

## 📋 Usage

- Players open a **GUI to craft minions or global items**
- Upgrade menus dynamically show what's needed to level up
- Fully translatable and intuitive interface
- Works with both **ItemsAdder** and **vanilla** items
- Recipes can include **other minions as ingredients**

## 🖥️ Example Config Snippet

```yaml
levels:
  1:
    recipes:
      slayer_zombie_minion_1:
        pattern:
          - "XAX"
          - "ABA"
          - "XCX"
        ingredients:
          A:
            item: "iasurvival:arkaner_bruchstein"
            amount: 64
          B:
            item: "minion#ZOMBIE#SLAYER#1"
            amount: 1
          C:
            item: "OAK_LOG"
            amount: 1
        result:
          item: "minion#ZOMBIE#SLAYER#1"
          amount: 1
  2:
    recipes:
      slayer_zombie_minion_2:
        pattern:
          - "CAC"
          - "ABA"
          - "CAC"
        ingredients:
          A:
            item: "iasurvival:arkaner_bruchstein"
            amount: 64
          B:
            item: "minion#ZOMBIE#SLAYER#2"
            amount: 1
          C:
            item: "OAK_LOG"
            amount: 1
        result:
          item: "minion#ZOMBIE#SLAYER#2"
          amount: 1
