package fr.astratime.lucky.animations;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import fr.astratime.lucky.assets.Palette;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Bingo du Ticket : un ticket à gratter géant tourbillonne jusqu'au centre de
 * la table ; une pièce le gratte en zigzag de gauche à droite
 * ({@link #SCRATCH_START} à {@link #SCRATCH_END}), la couche argentée part en
 * copeaux et découvre trois Tickets dorés, qui s'illuminent à
 * {@link #REVEAL_TIME} (les copeaux, les ondes et les Tickets projetés sont
 * lancés par {@link JackpotCelebration}, qui suit {@link #coin}).
 */
public class ScratchTicketScene extends BingoScene {

    /** La pièce commence à gratter. */
    public static final float SCRATCH_START = 0.45f;
    /** Tout est gratté. */
    public static final float SCRATCH_END   = 1.3f;
    /** Les trois Tickets s'illuminent. */
    public static final float REVEAL_TIME   = 1.4f;

    private static final int TICKET_W = 70, TICKET_H = 34;
    private static final float TICKET_SCALE = 9f;
    private static final int STRIPS = 14;
    private static final float ICON_SIZE = 150f;

    private final Image ticket, coinImage, glow;
    private final Image[] icons = new Image[3];
    private final Image[] strips = new Image[STRIPS];
    private final Vector2 center = new Vector2(), coin = new Vector2();

    /** @param ticketIcon image du symbole Ticket */
    public ScratchTicketScene(TextureRegion ticketIcon) {
        glow = image(texture(halo(56, hex("ffe680"))));
        glow.setOrigin(glow.getWidth() / 2f, glow.getHeight() / 2f);
        addActor(glow);
        ticket = image(new TextureRegion(texture(ticket())), TICKET_SCALE);
        ticket.setOrigin(ticket.getWidth() / 2f, ticket.getHeight() / 2f);
        addActor(ticket);
        for (int i = 0; i < 3; i++) {
            icons[i] = image(ticketIcon, ICON_SIZE / ticketIcon.getRegionWidth());
            icons[i].setOrigin(icons[i].getWidth() / 2f, icons[i].getHeight() / 2f);
            addActor(icons[i]);
        }
        TextureRegion foil = new TextureRegion(texture(foil()));
        for (int i = 0; i < STRIPS; i++) {
            strips[i] = image(foil, 1f);
            addActor(strips[i]);
        }
        coinImage = image(texture(coinPixmap()));
        coinImage.setOrigin(coinImage.getWidth() / 2f, coinImage.getHeight() / 2f);
        addActor(coinImage);
    }

    /** @return la position (Stage) de la pièce qui gratte. */
    public Vector2 coin() { return coin; }

    /** @return la position (Stage) du Ticket {@code index} (0 à 2) révélé. */
    public Vector2 iconPosition(int index) { return new Vector2(center.x + (index - 1) * 190f, center.y); }

    @Override
    protected void start(float x, float y) {
        center.set(x, y);
        coinImage.setVisible(false);
        for (Image icon : icons) icon.setScale(1f);
        update(0f);
    }

    @Override
    protected void update(float delta) {
        float arrive = Interpolation.pow2Out.apply(Math.min(1f, time / 0.4f));
        ticket.setRotation((1f - arrive) * 540f);
        ticket.setScale(arrive);
        placeOrigin(ticket, center.x, center.y);

        // Zone à gratter : la couche argentée, en bandes verticales.
        float areaW = 56 * TICKET_SCALE, areaH = 20 * TICKET_SCALE;
        float left = center.x - areaW / 2f, bottom = center.y - areaH / 2f - 3f * TICKET_SCALE;
        float stripW = areaW / STRIPS;
        float progress = Math.max(0f, Math.min(1f, (time - SCRATCH_START) / (SCRATCH_END - SCRATCH_START)));
        float scraped = progress * STRIPS;
        for (int i = 0; i < STRIPS; i++) {
            Image strip = strips[i];
            strip.setSize(stripW + 1f, areaH);
            strip.setPosition(left + i * stripW, bottom);
            strip.setVisible(arrive >= 1f && i >= scraped);
        }
        for (int i = 0; i < 3; i++) {
            Image icon = icons[i];
            icon.setVisible(arrive >= 1f);
            float pulse = time >= REVEAL_TIME ? 1f + 0.2f * Interpolation.swingOut.apply(Math.min(1f, (time - REVEAL_TIME - i * 0.08f) / 0.3f)) : 1f;
            icon.setScale(Math.max(1f, pulse));
            placeOrigin(icon, left + areaW * (i + 0.5f) / 3f, bottom + areaH / 2f);
        }

        // La pièce gratte en zigzag.
        coinImage.setVisible(time >= SCRATCH_START - 0.15f && time < SCRATCH_END + 0.2f);
        float zig = MathUtils.sin(progress * MathUtils.PI * 9f);
        coin.set(left + areaW * progress, bottom + areaH * (0.5f + 0.42f * zig));
        if (time < SCRATCH_START) coin.set(left - 60f, bottom + areaH + 60f);
        if (time > SCRATCH_END) coin.add(0f, (time - SCRATCH_END) * 900f);
        coinImage.setRotation(-30f + zig * 15f);
        placeOrigin(coinImage, coin.x, coin.y);

        glow.setScale(time >= REVEAL_TIME ? 7f + 0.5f * MathUtils.sin(time * 9f) : 0f, 3.5f);
        glow.getColor().a = 0.75f;
        placeOrigin(glow, center.x, center.y);
    }

    /** @return le ticket : carton crème dentelé, bandeau rouge en haut, zone à gratter dorée en dessous. */
    private static Pixmap ticket() {
        Pixmap pixmap = pixmap(TICKET_W, TICKET_H);
        Color cream = hex("f6ecd0"), red = hex("d8343c"), gold = hex("e8c050"), stripe = hex("f09840");
        Shape shape = (x, y) -> {
            boolean notch = (x == 0 || x == TICKET_W - 1) && y % 4 == 1;   // bords dentelés
            boolean hole = (x <= 2 || x >= TICKET_W - 3) && Math.abs(y + 0.5f - TICKET_H / 2f) < 3f; // encoches
            return !notch && !hole;
        };
        fillShape(pixmap, shape, (x, y) -> y < 7 ? (x / 3 % 2 == 0 ? red : stripe) : cream);
        fillOutlined(pixmap, 6, 9, 58, 22, gold);
        return pixmap;
    }

    /** @return la couche à gratter : argent mat, quelques reflets. */
    private static Pixmap foil() {
        Pixmap pixmap = pixmap(8, 8);
        pixmap.setColor(hex("aab2bc"));
        pixmap.fill();
        pixmap.setColor(hex("ccd4dc"));
        pixmap.drawLine(0, 7, 7, 0);
        pixmap.setColor(hex("8c949e"));
        pixmap.drawPixel(2, 2);
        pixmap.drawPixel(6, 5);
        return pixmap;
    }

    /** @return la pièce qui gratte, vue de biais. */
    private static Pixmap coinPixmap() {
        Pixmap pixmap = pixmap(13, 13);
        fillOutlinedCircle(pixmap, 6, 6, 6, Palette.GOLD);
        pixmap.setColor(Palette.GOLD_PALE);
        pixmap.fillRectangle(3, 3, 3, 2);
        pixmap.setColor(hex("b07a10"));
        pixmap.drawCircle(6, 6, 3);
        return pixmap;
    }
}
