package forge.screens.home;

import forge.assets.FSkinTexture;
import forge.toolbox.Duel;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;

import forge.Forge;
import forge.Graphics;
import forge.assets.FImage;
import forge.assets.FSkin;
import forge.assets.FSkinColor;
import forge.assets.FSkinColor.Colors;
import forge.assets.FSkinFont;
import forge.assets.FSkinImage;
import forge.deck.FDeckChooser;
import forge.game.GameType;
import forge.gamemodes.replay.ReplayLauncher;
import forge.gui.CommanderEdition;
import forge.gui.FThreads;
import forge.screens.FScreen;
import forge.screens.achievements.AchievementsScreen;
import forge.screens.online.OnlineMenu.OnlineScreen;
import forge.screens.planarconquest.ConquestMenu;
import forge.screens.quest.QuestMenu;
import forge.screens.settings.SettingsScreen;
import forge.toolbox.FButton;
import forge.toolbox.FEvent.FEventHandler;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FScrollPane;
import forge.toolbox.Frost;
import forge.util.Utils;

public class HomeScreen extends FScreen {
    private static final float PADDING = Utils.scale(5);
    private static final FSkinColor clrTheme = FSkinColor.get(Colors.CLR_THEME);
    private static final FSkinColor l00 = clrTheme.stepColor(0);
    private static final FSkinColor d80 = clrTheme.stepColor(-80);
    public static final float MAIN_MENU_WIDTH_FACTOR = 0.35f;
    // Commander edition, portrait: Scry Magic's palette, a hero card with the logo and New Game, then tiles in two columns
    private static final Color CE_SURFACE = new Color(23 / 255f, 29 / 255f, 50 / 255f, 0.94f);
    private static final Color CE_RAISED = new Color(40 / 255f, 51 / 255f, 75 / 255f, 0.96f);
    private static final Color CE_LINE = new Color(88 / 255f, 103 / 255f, 129 / 255f, 1f);
    private static final Color CE_ACCENT = new Color(140 / 255f, 220 / 255f, 248 / 255f, 1f);
    private static final Color CE_ACCENT_DOWN = new Color(104 / 255f, 186 / 255f, 220 / 255f, 1f);
    private static final Color CE_HERO_EDGE = new Color(226 / 255f, 184 / 255f, 92 / 255f, 0.85f);
    private static final Color CE_HERO_FILL = new Color(12 / 255f, 16 / 255f, 31 / 255f, 1f);
    private static final Color CE_BG_TOP = new Color(7 / 255f, 9 / 255f, 19 / 255f, 0.55f);
    private static final Color CE_BG_BOTTOM = new Color(7 / 255f, 9 / 255f, 19 / 255f, 0.92f);
    private static final Color CE_TILE = new Color(23 / 255f, 29 / 255f, 50 / 255f, 1f);
    private static final Color CE_INK = new Color(16 / 255f, 36 / 255f, 54 / 255f, 1f);
    private static final Color CE_TEXT = new Color(246 / 255f, 243 / 255f, 1f, 1f);
    private static final Color CE_MUTED = new Color(194 / 255f, 202 / 255f, 221 / 255f, 1f);
    private static final float CE_PAD = Utils.scale(12);
    private float heroTop, heroBottom, heroTextRight;

    public static final HomeScreen instance = new HomeScreen();

    private final FLabel lblLogo = add(new FLabel.Builder().icon(
            new FImage() {
                final float size = Forge.getScreenWidth() * 0.6f;
                @Override
                public float getWidth() {
                    return size;
                }
                @Override
                public float getHeight() {
                    return size;
                }
                @Override
                public void draw(Graphics g, float x, float y, float w, float h) {
                    if (FSkin.getLogo() == null)
                        FSkinImage.LOGO.draw(g, x, y, w, h);
                    else
                        g.drawImage(FSkin.getLogo(), x, y, w, h);
                }
            }
    ).iconInBackground().iconScaleFactor(1).build());
    private final ButtonScroller buttonScroller = add(new ButtonScroller());
    private final List<MenuButton> buttons = new ArrayList<>();
    private int activeButtonIndex, baseButtonCount;
    private FDeckChooser deckManager;
    private boolean QuestCommander = false;
    private String QuestWorld = "";

    private HomeScreen() {
        super((Header)null);

        addButton(Forge.getLocalizer().getMessage("lblNewGame"), e -> {
            activeButtonIndex = 0;
            Forge.lastButtonIndex = activeButtonIndex;
            NewGameMenu.getPreferredScreen().open();
        });
        addButton(Forge.getLocalizer().getMessage("lblLoadGame"), e -> {
            activeButtonIndex = 1;
            Forge.lastButtonIndex = activeButtonIndex;
            LoadGameMenu.getPreferredScreen().open();
        });
        addButton(Forge.getLocalizer().getMessage("lblPlayOnline"), e -> {
            activeButtonIndex = 2;
            Forge.lastButtonIndex = activeButtonIndex;
            OnlineScreen.Lobby.open();
        });
        addButton(Forge.getLocalizer().getMessage("lblDeckManager"), e -> {
            activeButtonIndex = 3;
            Forge.lastButtonIndex = activeButtonIndex;
            if (deckManager == null) {
                deckManager = new FDeckChooser(GameType.DeckManager, false, null) {
                    @Override
                    protected float doLandscapeLayout(float width, float height) {
                        //don't show header in landscape mode
                        getHeader().setBounds(0, 0, 0, 0);
                        doLayout(0, width, height);
                        return 0;
                    }
                };
                deckManager.setHeaderCaption(Forge.getLocalizer().getMessage("lblDeckManager"));
            }
            Forge.openScreen(deckManager);
        });
        addButton(Forge.getLocalizer().getMessage("lblAchievements"), e -> {
            activeButtonIndex = 4;
            Forge.lastButtonIndex = activeButtonIndex;
            AchievementsScreen.show();
        });
        addButton(Forge.getLocalizer().getMessage("lblSettings"), e -> {
            activeButtonIndex = 5;
            Forge.lastButtonIndex = activeButtonIndex;
            SettingsScreen.show(true);
        });
        addButton("Replays", e -> ReplayLauncher.chooseAndPlay());
        addButton(Forge.getLocalizer().getMessage("lblHelp"), e -> FThreads.invokeInEdtLater(() -> {
            try {
                if (Forge.getDeviceAdapter().isConnectedToInternet()) {
                    FOptionPane.showOptionDialog("Join Discord option will open the invite link to join Forge Discord server. Forge Support option will open the Forge Support Channel.", "Choose option", FOptionPane.INFORMATION_ICON, List.of("Join Discord", "Forge Support"), -1, result -> {
                        switch (result) {
                            case 0:
                                Gdx.net.openURI("https://discord.gg/3v9JCVr");
                                break;
                            case 1:
                                Gdx.net.openURI("https://discord.com/channels/267367946135928833/692000787856883752");
                                break;
                            default:
                                break;
                        }
                    });
                } else {
                    FOptionPane.showErrorDialog("Internet Connection required to open Forge Discord server", "No Internet");
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }));
        baseButtonCount = buttons.size();
    }

    private void addButton(String caption, FEventHandler command) {
        buttons.add(buttonScroller.add(new MenuButton(caption, command)));
    }

    public void updateQuestCommanderMode(boolean isCommander){
        QuestCommander = isCommander;
    }

    public void updateQuestWorld(String questWorld){
        QuestWorld = questWorld;
    }

    public void openMenu(int index){
        if (index < 0)
            return; //menu on startup
        if (index == 2)
            OnlineScreen.Lobby.open();
        else if (index < 6)
            NewGameMenu.getPreferredScreen().open();
        else if (index == 6)
            QuestMenu.launchQuestMode(QuestMenu.LaunchReason.StartQuestMode, HomeScreen.instance.getQuestCommanderMode());
        else if (index == 7)
            ConquestMenu.launchPlanarConquest(ConquestMenu.LaunchReason.StartPlanarConquest);
    }

    public boolean getQuestCommanderMode() {
        return QuestCommander;
    }

    public int getActiveButtonIndex() {
        return activeButtonIndex;
    }

    public String getQuestWorld() {
        return QuestWorld;
    }

    public void addButtonForMode(String caption, final FEventHandler command) {
        //ensure we don't add the same mode button more than once
        for (int i = baseButtonCount; i < buttons.size(); i++) {
            if (buttons.get(i).getText().equals(caption)) {
                final int index = i;
                buttons.get(i).setCommand(e -> {
                    activeButtonIndex = index;
                    Forge.lastButtonIndex = activeButtonIndex;
                    command.handleEvent(e);
                });
                activeButtonIndex = i;
                return;
            }
        }
        final int index = buttons.size();
        activeButtonIndex = index;
        addButton(caption, e -> {
            activeButtonIndex = index;
            Forge.lastButtonIndex = activeButtonIndex;
            command.handleEvent(e);
        });
        revalidate();
        buttonScroller.scrollIntoView(buttons.get(index));
    }

    private static boolean commanderLayout() {
        return CommanderEdition.isEnabled() && !Forge.isLandscapeMode();
    }

    private void doCommanderLayout(float width, float height) {
        float logoSize = Math.min(width * 0.28f, height * 0.15f);
        heroTop = CE_PAD;
        lblLogo.setBounds(width - 2 * CE_PAD - logoSize, heroTop + CE_PAD, logoSize, logoSize);
        heroTextRight = lblLogo.getLeft() - CE_PAD;
        float scrollerTop = lblLogo.getBottom() + CE_PAD;
        buttonScroller.buttonHeight = Utils.AVG_FINGER_HEIGHT * 1.25f;
        buttonScroller.padding = CE_PAD;
        buttonScroller.setBounds(0, scrollerTop, width, height - scrollerTop);
        heroBottom = scrollerTop + buttonScroller.buttonHeight + CE_PAD;
    }

    @Override
    protected void doLayout(float startY, float width, float height) {
        if (commanderLayout()) {
            doCommanderLayout(width, height);
            return;
        }
        float buttonWidth = width - 2 * PADDING;
        float buttonHeight = buttons.get(0).getFont().getCapHeight() * 3.5f;
        float x = PADDING;
        float dy = buttonHeight + PADDING;
        float y = height - buttons.size() * dy;

        buttonScroller.buttonHeight = buttonHeight;
        buttonScroller.padding = PADDING;
        buttonScroller.setBounds(x, y, buttonWidth, height - y);

        float logoSize = y - 2 * PADDING;
        y = PADDING;
        if (logoSize > buttonWidth) {
            y += (logoSize - buttonWidth) / 2;
            logoSize = buttonWidth;
        }
        x = (width - logoSize) / 2;
        lblLogo.setBounds(x, y, logoSize, logoSize);
    }

    @Override
    protected float doLandscapeLayout(float width, float height) {
        float mainMenuWidth = height * MAIN_MENU_WIDTH_FACTOR;
        float logoSize = mainMenuWidth - 2 * PADDING;
        lblLogo.setBounds(PADDING, PADDING, logoSize, logoSize);

        float x = 2 * PADDING;
        float y = lblLogo.getBottom() + PADDING;
        float buttonWidth = mainMenuWidth - x;
        float buttonHeight = Utils.AVG_FINGER_HEIGHT * 0.9f;

        buttonScroller.buttonHeight = buttonHeight;
        buttonScroller.padding = 0;
        buttonScroller.setBounds(x, y, buttonWidth, height - y);

        return width - mainMenuWidth; //move hosted screens to the right of menu
    }

    @Override
    public FScreen getLandscapeBackdropScreen() {
        return null;
    }

    @Override
    protected void drawBackground(Graphics g) {
        super.drawBackground(g);

        if (commanderLayout()) { //navy backdrop as in the app, then the hero card behind the logo, the title and New Game
            //the battlefield art under a dark wash, as on a card game's title screen
            FSkinTexture art = FSkinTexture.BG_MATCH;
            float artH = getHeight(), artW = art.getHeight() > 0 ? artH * art.getWidth() / art.getHeight() : getWidth();
            if (artW < getWidth()) { artH *= getWidth() / artW; artW = getWidth(); }
            g.drawImage(art, (getWidth() - artW) / 2, 0, artW, artH);
            g.fillGradientRect(CE_BG_TOP, CE_BG_BOTTOM, true, 0, 0, getWidth(), getHeight());
            float radius = CE_PAD * 1.4f, x = CE_PAD, w = getWidth() - 2 * CE_PAD;
            //the edge is a rounded fill under the card: drawRoundRect leaves straight lines inside the corners
            float edge = Utils.scale(1.5f);
            g.fillRoundRect(CE_HERO_EDGE, x, heroTop, w, heroBottom - heroTop, radius);
            g.fillRoundRect(CE_HERO_FILL, x + edge, heroTop + edge, w - 2 * edge, heroBottom - heroTop - 2 * edge, radius - edge);
            float tx = 2 * CE_PAD, tw = heroTextRight - tx, ty = lblLogo.getTop();
            FSkinFont eyebrow = FSkinFont.get(11), title = FSkinFont.get(26), body = FSkinFont.get(13);
            g.drawText("FORGE \u00b7 COMMANDER EDITION", eyebrow, Duel.GOLD, tx, ty, tw, eyebrow.getLineHeight(), false, Align.left, false);
            ty += eyebrow.getLineHeight() * 1.3f;
            g.drawText(Forge.getLocalizer().getMessageorUseDefault("lblCommander", "Commander"), title, CE_TEXT, tx, ty, tw, title.getLineHeight(), false, Align.left, false);
            ty += title.getLineHeight() * 1.1f;
            g.drawText(Forge.getLocalizer().getMessageorUseDefault("lblCommanderEditionTagline", "Your decks against the AI, from 2 to 4 players."), body, CE_MUTED, tx, ty, tw, lblLogo.getBottom() - ty, true, Align.left, false);
            return;
        }

        //handle drawing main menu background for Landscape mode
        float w = getWidth();
        float h = getHeight();
        if (w > h) {
            w = h * MAIN_MENU_WIDTH_FACTOR;

            float y1 = 0;
            float h1 = h;
            float y2 = 0;
            float h2 = 0;
            if (activeButtonIndex != -1) {
                MenuButton activeButton = buttons.get(activeButtonIndex);
                h1 = buttonScroller.getTop() + activeButton.getTop();
                y2 = h1 + activeButton.getHeight();
                h2 = h - y2;

                //prevent showing active button height behind logo
                float scrollerTop = buttonScroller.getTop();
                if (y2 < scrollerTop) { //if entire active button behind logo, only need to draw one rectangle
                    h1 = h;
                    y2 = 0;
                    h2 = 0;
                }
                else if (h1 < scrollerTop) {
                    h1 = scrollerTop;
                }
            }

            if (Frost.isOn()) { //frosted navigation column: slate glass, the active entry washed with the accent and a bar on the left
                g.fillGradientRect(Frost.slate(0.90f), Frost.slate(0.70f), false, 0, 0, w, h);
                g.drawLine(1, Frost.EDGE, w, 0, w, h);
                if (activeButtonIndex != -1) {
                    final MenuButton active = buttons.get(activeButtonIndex);
                    final float top = Math.max(buttonScroller.getTop() + active.getTop(), buttonScroller.getTop());
                    final float bottom = Math.min(buttonScroller.getTop() + active.getBottom(), h);
                    if (bottom > top) {
                        g.fillGradientRect(Frost.accent(0.32f), Frost.accent(0.04f), false, 0, top, w, bottom - top);
                        g.fillRect(Frost.accent(1f), 0, top, Math.max(2f, Utils.scale(3)), bottom - top);
                    }
                }
                return;
            }

            float w1 = w * 0.66f;
            float w2 = w - w1;
            g.fillRect(l00, 0, y1, w1, h1);
            if (h2 > 0) {
                g.fillRect(l00, 0, y2, w1, h2);
            }
            g.fillGradientRect(l00, d80, false, w1, y1, w2, h1);
            if (h2 > 0) {
                g.fillGradientRect(l00, d80, false, w1, y2, w2, h2);
            }
        }
    }

    private class ButtonScroller extends FScrollPane {
        private float buttonHeight, padding;

        @Override
        protected ScrollBounds layoutAndGetScrollBounds(float visibleWidth, float visibleHeight) {
            if (commanderLayout()) { //New Game full width inside the hero card, the rest as two columns of tiles
                float side = padding * 2, inner = visibleWidth - 2 * side, tileWidth = (inner - padding) / 2, tileHeight = buttonHeight * 1.15f;
                buttons.get(0).setBounds(side, 0, inner, buttonHeight);
                float y = buttonHeight + padding * 2.5f;
                for (int i = 1; i < buttons.size(); i++) {
                    int col = (i - 1) % 2;
                    boolean lastAlone = col == 0 && i == buttons.size() - 1; //an odd tile out spans both columns
                    buttons.get(i).setBounds(side + col * (tileWidth + padding), y, lastAlone ? inner : tileWidth, tileHeight);
                    if (col == 1 || i == buttons.size() - 1) {
                        y += tileHeight + padding;
                    }
                }
                return new ScrollBounds(visibleWidth, y);
            }
            float y = 0;
            float dy = buttonHeight + padding;
            for (MenuButton button : buttons) {
                button.setBounds(0, y, visibleWidth, buttonHeight);
                y += dy;
            }
            return new ScrollBounds(visibleWidth, y - padding);
        }
    }

    private class MenuButton extends FButton {
        public MenuButton(String caption, FEventHandler command) {
            super(caption, command);
        }

        @Override
        public void draw(Graphics g) {
            if (commanderLayout()) { //rounded Scry Magic tiles; New Game is the accent button
                boolean primary = buttons.indexOf(this) == 0, down = isPressed();
                //duel buttons: the glowing ember button for New Game, gold rimmed steel tiles for the rest
                Duel.drawButton(g, 0, 0, getWidth(), getHeight(), isHovered(), down, true, primary);
                //long single words such as Achievements get a smaller font instead of breaking mid-word
                FSkinFont font = primary ? FSkinFont.get(20) : FSkinFont.get(getText().length() > 10 ? 10 : 11);
                g.drawText(getText(), font, Duel.textColor(primary, true), CE_PAD / 2, 0, getWidth() - CE_PAD, getHeight(), getText().contains(" "), Align.center, true);
                return;
            }
            if (Forge.isLandscapeMode()) {
                //draw text only for Landscape mode
                if (Frost.isOn()) { //frosted: white text, brighter for the active entry, indented from the accent bar
                    final boolean active = buttons.indexOf(this) == activeButtonIndex;
                    final float indent = PADDING * 3;
                    if (isHovered() && !active) {
                        g.fillRect(Frost.HOVER, -2 * PADDING, 0, getWidth() + 2 * PADDING, getHeight());
                    }
                    g.drawText(getText(), isHovered() && getFont().canIncrease() ? getFont().increase() : getFont(), active ? Frost.TEXT : Frost.TEXT_DIM, indent, 0, getWidth() - indent, getHeight(), false, Align.left, true);
                    return;
                }
                g.drawText(getText(), isHovered() && getFont().canIncrease() ? getFont().increase() : getFont(), getForeColor(), 0, 0, getWidth(), getHeight(), false, Align.left, true);
            }
            else { //draw buttons normally for portrait mode
                super.draw(g);
            }
        }
    }
}
