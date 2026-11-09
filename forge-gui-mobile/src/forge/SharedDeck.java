package forge;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;

import forge.deck.Deck;
import forge.deck.DeckSection;
import forge.deck.io.DeckSerializer;
import forge.model.FModel;
import forge.toolbox.FOptionPane;
import forge.util.FileSection;
import forge.util.storage.IStorage;

/**
 * A deck shared to Forge from another app, as Scry Magic does with "Send to Forge": Forge .dck text is saved straight
 * into the Deck Manager (Commander decks with the Commander decks, the rest with the constructed decks).
 * Other shared text keeps going to the clipboard, for the deck importer.
 */
public final class SharedDeck {
    private static String pending;

    private SharedDeck() {
    }

    public static boolean looksLikeDeck(String text) {
        if (text == null) { return false; }
        final String lower = text.toLowerCase();
        return lower.contains("[metadata]") && (lower.contains("[main]") || lower.contains("[commander]"));
    }

    /** Called by the Android launcher; the deck is saved once Forge has loaded its card database. */
    public static void receive(String text) {
        pending = text;
        if (Forge.afterDBloaded && Forge.getCurrentScreen() != null && Gdx.app != null) { //otherwise openHomeDefault picks it up
            Gdx.app.postRunnable(SharedDeck::importPending);
        }
    }

    /** Saves a deck waiting from a share, if any. Runs on the render thread. */
    public static void importPending() {
        final String text = pending;
        pending = null;
        if (text == null) { return; }
        try {
            final Deck deck = DeckSerializer.fromSections(FileSection.parseSections(Arrays.asList(text.split("\\r?\\n"))));
            if (deck == null || deck.getAllCardsInASinglePool().isEmpty()) {
                throw new IllegalArgumentException("no cards were recognized");
            }
            final IStorage<Deck> storage = deck.has(DeckSection.Commander) ? FModel.getDecks().getCommander() : FModel.getDecks().getConstructed();
            final String base = deck.getName() == null || deck.getName().trim().isEmpty() ? "Scry Magic deck" : deck.getName().trim();
            String name = base;
            for (int n = 2; storage.contains(name); n++) {
                name = base + " (" + n + ")";
            }
            deck.setName(name);
            storage.add(deck);
            FOptionPane.showMessageDialog(String.format(Forge.getLocalizer().getMessageorUseDefault("lblSharedDeckSaved", "Deck \"%s\" was saved in the Deck Manager."), name));
        } catch (Exception e) {
            FOptionPane.showMessageDialog(String.format(Forge.getLocalizer().getMessageorUseDefault("lblSharedDeckFailed", "The shared deck could not be imported: %s"), e.getMessage()));
        }
    }
}
