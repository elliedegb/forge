package forge.toolbox;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;

import forge.Forge;
import forge.Graphics;
import forge.assets.FSkinFont;
import forge.gui.CommanderEdition;
import forge.util.Utils;

/**
 * The Commander edition's duel look, like a modern digital card game: midnight panels with gold trim,
 * a glowing ember button for the main action and dark steel buttons for the rest.
 * Only used when the Commander edition is on, never in Adventure mode.
 */
public final class Duel {
    public static final Color NIGHT = rgb(7, 9, 19, 1f);
    public static final Color PANEL_TOP = rgb(24, 29, 50, 0.94f);
    public static final Color PANEL_BOTTOM = rgb(10, 13, 26, 0.94f);
    public static final Color GOLD = rgb(226, 184, 92, 1f);
    public static final Color GOLD_LIGHT = rgb(255, 228, 156, 1f);
    public static final Color GOLD_DIM = rgb(150, 112, 48, 1f);
    public static final Color GOLD_FAINT = rgb(226, 184, 92, 0.35f);
    public static final Color TEXT = rgb(246, 240, 226, 1f);
    public static final Color TEXT_DIM = rgb(246, 240, 226, 0.55f);
    public static final Color INK = rgb(38, 20, 4, 1f);
    public static final Color ARCANE = rgb(110, 210, 255, 1f);
    private static final Color EMBER_TOP = rgb(255, 196, 92, 1f);
    private static final Color EMBER_BOTTOM = rgb(214, 104, 24, 1f);
    private static final Color STEEL_TOP = rgb(56, 66, 98, 1f);
    private static final Color STEEL_BOTTOM = rgb(22, 27, 46, 1f);
    private static final Color CLEAR = rgb(0, 0, 0, 0f);
    private static final Color scratch = new Color(), scratch2 = new Color(); //render thread only

    private static int on = -1;

    private Duel() {
    }

    private static Color rgb(int r, int g, int b, float a) {
        return new Color(r / 255f, g / 255f, b / 255f, a);
    }

    /** @return true when the duel look is in use: the Commander edition, outside Adventure mode */
    public static boolean isOn() {
        if (Forge.isMobileAdventureMode) { return false; }
        if (on < 0) {
            try {
                on = CommanderEdition.isEnabled() ? 1 : 0;
            } catch (RuntimeException e) { //preferences not loaded yet, as on the splash screen
                return false;
            }
        }
        return on > 0;
    }

    /** a colour with a different alpha, reusing a scratch object */
    private static Color alpha(Color c, float a, Color into) {
        return into.set(c.r, c.g, c.b, c.a * a);
    }

    /** A gold hairline that fades out at both ends, like the trim of a card frame. */
    public static void drawTrim(Graphics g, float x, float y, float w, float thickness) {
        final float half = w / 2;
        g.fillGradientRect(CLEAR, GOLD, false, x, y, half, thickness);
        g.fillGradientRect(GOLD, CLEAR, false, x + half, y, w - half, thickness);
    }

    /** A midnight panel with gold trim along the given edges. */
    public static void drawPlate(Graphics g, float x, float y, float w, float h, boolean trimTop, boolean trimBottom) {
        g.fillGradientRect(PANEL_TOP, PANEL_BOTTOM, true, x, y, w, h);
        final float t = Math.max(1f, Utils.scale(1));
        if (trimTop) { drawTrim(g, x, y, w, t); }
        if (trimBottom) { drawTrim(g, x, y + h - t, w, t); }
    }

    /**
     * A button face: gold rim, then an ember gradient for the main action or dark steel for the others.
     * Hovering brightens it, pressing darkens it and a disabled button loses its colour.
     */
    public static void drawButton(Graphics g, float x, float y, float w, float h, boolean hover, boolean pressed, boolean enabled, boolean primary) {
        if (w <= 0 || h <= 0) { return; }
        final float radius = Math.min(h / 2f, Utils.scale(9));
        final float rim = Math.max(1f, Utils.scale(1.5f));
        final float k = !enabled ? 0.55f : pressed ? 0.8f : hover ? 1.15f : 1f;
        if (primary && enabled && !pressed) { //soft glow around the main action
            g.fillRoundRect(alpha(EMBER_TOP, 0.35f, scratch), x - rim * 2, y - rim * 2, w + rim * 4, h + rim * 4, radius + rim * 2);
        }
        g.fillRoundRect(shade(enabled ? (primary ? GOLD_LIGHT : GOLD) : GOLD_DIM, k, 1f, scratch), x, y, w, h, radius);
        final Color top = primary ? EMBER_TOP : STEEL_TOP, bottom = primary ? EMBER_BOTTOM : STEEL_BOTTOM;
        final float ix = x + rim, iy = y + rim, iw = w - 2 * rim, ih = h - 2 * rim, ir = Math.max(0f, radius - rim);
        fillRoundedGradient(g, ix, iy, iw, ih, ir, shade(top, k, 1f, scratch), shade(bottom, k, 1f, scratch2));
        //glass sheen on the upper half, fading downwards
        final float sheen = primary ? 0.26f : 0.10f;
        fillRoundedGradient(g, ix + rim, iy + rim, iw - 2 * rim, ih * 0.45f, Math.max(0f, ir - rim),
                alpha(Color.WHITE, sheen, scratch), alpha(Color.WHITE, sheen * 0.15f, scratch2));
    }

    /**
     * A vertical gradient inside a rounded rectangle, drawn as thin bands that follow the corners,
     * because a plain gradient rectangle would leave square corners.
     */
    private static void fillRoundedGradient(Graphics g, float x, float y, float w, float h, float r, Color top, Color bottom) {
        if (w <= 0 || h <= 0) { return; }
        r = Math.min(r, Math.min(w, h) / 2f);
        final float tr = top.r, tg = top.g, tb = top.b, ta = top.a;
        final float br = bottom.r, bg = bottom.g, bb = bottom.b, ba = bottom.a;
        final float step = Math.max(1f, Utils.scale(1));
        for (float by = 0; by < h; by += step) {
            final float bh = Math.min(step, h - by);
            final float mid = by + bh / 2f;
            float inset = 0;
            if (mid < r) { //top corners
                final float d = r - mid;
                inset = r - (float) Math.sqrt(Math.max(0f, r * r - d * d));
            } else if (mid > h - r) { //bottom corners
                final float d = mid - (h - r);
                inset = r - (float) Math.sqrt(Math.max(0f, r * r - d * d));
            }
            final float t = mid / h;
            BAND.set(tr + (br - tr) * t, tg + (bg - tg) * t, tb + (bb - tb) * t, ta + (ba - ta) * t);
            g.fillRect(BAND, x + inset, y + by, w - 2 * inset, bh);
        }
    }

    private static final Color BAND = new Color();

    /** Draws a button face and its centred label. */
    public static void drawButton(Graphics g, float w, float h, boolean hover, boolean pressed, boolean enabled, boolean primary, String text, FSkinFont font) {
        drawButton(g, 0, 0, w, h, hover, pressed, enabled, primary);
        if (text != null && !text.isEmpty()) {
            g.drawText(text, font, textColor(primary, enabled), Utils.scale(6), 0, w - Utils.scale(12), h, false, Align.center, true);
        }
    }

    public static Color textColor(boolean primary, boolean enabled) {
        if (!enabled) { return TEXT_DIM; }
        return primary ? INK : TEXT;
    }

    /** A gold framed badge, as behind the life total. */
    public static void drawBadge(Graphics g, float x, float y, float w, float h, boolean highlight) {
        final float radius = Math.min(Math.min(w, h) / 2f, Utils.scale(8));
        final float rim = Math.max(1f, Utils.scale(1.5f));
        g.fillRoundRect(highlight ? GOLD_LIGHT : GOLD, x, y, w, h, radius);
        fillRoundedGradient(g, x + rim, y + rim, w - 2 * rim, h - 2 * rim, Math.max(0f, radius - rim), STEEL_TOP, STEEL_BOTTOM);
    }

    /** A small gold rimmed tile, as for the zone tabs of the player bar. */
    public static void drawTile(Graphics g, float x, float y, float w, float h, boolean selected, boolean hover) {
        final float radius = Math.min(Math.min(w, h) / 2f, Utils.scale(5));
        final float rim = Math.max(1f, Utils.scale(1.2f));
        g.fillRoundRect(selected ? GOLD_LIGHT : hover ? GOLD : GOLD_DIM, x, y, w, h, radius);
        fillRoundedGradient(g, x + rim, y + rim, w - 2 * rim, h - 2 * rim, Math.max(0f, radius - rim),
                selected ? rgb(90, 74, 40, 1f) : STEEL_TOP, STEEL_BOTTOM);
    }

    /** A card pile: two card backs peeking out under a gold rimmed card. */
    public static void drawPile(Graphics g, float x, float y, float w, float h, boolean selected, boolean hover) {
        final float off = Math.max(1f, Utils.scale(2));
        final float radius = Math.min(w, h) * 0.08f;
        g.fillRoundRect(GOLD_DIM, x + 2 * off, y - off, w - 2 * off, h, radius);
        g.fillRoundRect(STEEL_BOTTOM, x + 2 * off + 1, y - off + 1, w - 2 * off - 2, h - 2, radius);
        g.fillRoundRect(GOLD_DIM, x + off, y, w - off, h - off / 2, radius);
        g.fillRoundRect(STEEL_BOTTOM, x + off + 1, y + 1, w - off - 2, h - off / 2 - 2, radius);
        drawTile(g, x, y + off, w - 2 * off, h - off, selected, hover);
    }

    /** A square gold frame around a portrait. */
    public static void drawFrame(Graphics g, float x, float y, float w, float h) {
        final float t = Math.max(1f, Utils.scale(2));
        g.drawRect(t, GOLD, x + t / 2, y + t / 2, w - t, h - t);
        g.drawRect(1f, alpha(NIGHT, 0.8f, scratch), x + t, y + t, w - 2 * t, h - 2 * t);
    }

    /** The glowing seam between two players' halves of the battlefield. */
    public static void drawSeam(Graphics g, float x, float y, float w) {
        final float glow = Utils.scale(10);
        g.fillGradientRect(alpha(ARCANE, 0f, scratch), alpha(ARCANE, 0.16f, scratch2), true, x, y - glow, w, glow);
        g.fillGradientRect(alpha(ARCANE, 0.16f, scratch), alpha(ARCANE, 0f, scratch2), true, x, y, w, glow);
        drawTrim(g, x, y - Utils.scale(1), w, Math.max(1f, Utils.scale(2)));
        final float d = Utils.scale(7); //a small diamond in the middle
        final float cx = x + w / 2;
        g.fillTriangle(GOLD, cx - d, y, cx + d, y, cx, y - d);
        g.fillTriangle(GOLD, cx - d, y, cx + d, y, cx, y + d);
        g.fillTriangle(NIGHT, cx - d / 2, y, cx + d / 2, y, cx, y - d / 2);
        g.fillTriangle(NIGHT, cx - d / 2, y, cx + d / 2, y, cx, y + d / 2);
    }

    /** the dark glass of the match panels: midnight blue in the duel look, slate otherwise */
    public static Color glass(float a) {
        return isOn() ? new Color(12 / 255f, 16 / 255f, 31 / 255f, a) : new Color(30 / 255f, 35 / 255f, 40 / 255f, a);
    }

    private static Color shade(Color c, float k, float a, Color into) {
        return into.set(Math.min(1f, c.r * k), Math.min(1f, c.g * k), Math.min(1f, c.b * k), c.a * a);
    }
}
