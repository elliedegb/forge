# Forge Commander edition

This fork of [Card-Forge/forge](https://github.com/Card-Forge/forge) opens on Commander.

## What changes

With **Settings › Commander Edition** on (the default in this fork):

- **New Game** opens the Commander lobby. Its menu lists Commander games and puzzles only.
- The lobby's **Format** list offers only Commander, and a lobby that an earlier session saved with another format opens on Commander.
- The **Deck Manager** opens on Commander decks, and the deck editor's format list offers only Commander.

Turn the setting off and restart Forge to get standard Forge back. The code is `forge-gui/src/main/java/forge/gui/CommanderEdition.java` and a few checks that call it, so upstream changes merge easily.

## New cards after each release

Forge's contributors script the cards of each new set upstream. `.github/workflows/sync-upstream.yml` runs every day: it merges Card-Forge/forge into this fork, and when the merge brings new or changed card scripts or set files it rebuilds the Android APK. Card images download from Scryfall while you play. If an upstream change conflicts with this fork, the workflow stops and asks for a manual merge.

For the daily run, this branch must be the fork's default branch, and **Settings › Actions › General › Workflow permissions** must be **Read and write**.

## Android APK

`.github/workflows/commander-edition-apk.yml` builds the APK (from the Actions tab, or after the sync) and publishes it as the **commander-edition-latest** release. It uses the same package name as the official Forge app: uninstall that first, since a build signed with another key cannot install over it.

## With Scry Magic

Scry Magic's deck page has **Play in Forge**: a Commander check, the list for Forge's importer (Deck Manager › Commander › New deck › Import) and a `.dck` file for desktop Forge. It also imports Forge `.dck` files.

Forge is licensed under GPL-3.0; this fork keeps that license.
