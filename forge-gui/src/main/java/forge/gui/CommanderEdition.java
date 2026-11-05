package forge.gui;

import forge.localinstance.properties.ForgePreferences.FPref;
import forge.model.FModel;

/**
 * Commander edition: the lobby and deck editor offer only the Commander format, and the mobile menus open on
 * Commander games and decks. On by default in this fork; turning it off in Settings restores standard Forge.
 */
public final class CommanderEdition {
    private CommanderEdition() {
    }

    public static boolean isEnabled() {
        try {
            return FModel.getPreferences().getPrefBoolean(FPref.UI_COMMANDER_EDITION);
        } catch (final RuntimeException e) {
            // Preferences are not loaded yet while the splash screen starts
            return false;
        }
    }
}
