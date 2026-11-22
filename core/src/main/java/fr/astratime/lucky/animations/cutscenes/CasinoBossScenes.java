package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.assets.PlacesPortraits;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import fr.astratime.lucky.views.FrontScene;
import java.util.ArrayDeque;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les scènes de phase et de mort des quatre rois du Casino Englouti (choix d'Astra, 2026-10-24 ; refaites le
 * 2026-11-22 pour montrer ce que dit leur phrase, dans leur vrai décor : le Bar, les Machines, les Coffres, le VIP).
 */
final class CasinoBossScenes {

    private CasinoBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case SIRENE -> new SirenPhase(kit);
            case JACKPOT_VIVANT -> new JackpotPhase(kit);
            case REQUIN_BANQUIER -> new SharkPhase(kit);
            case KRAKEN -> new KrakenPhase(kit);
            default -> null;
        };
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case SIRENE -> new SirenDeath(kit);
            case JACKPOT_VIVANT -> new JackpotDeath(kit);
            case REQUIN_BANQUIER -> new SharkDeath(kit);
            case KRAKEN -> new KrakenDeath(kit);
            default -> null;
        };
    }

    // ----- Accessoires -----

    static final String[] NOTE = {"..www", "..w.w", "..w.w", "..w..", "www..", "www.."};
    static final String[] GLASS = {"owwwwwwwo", ".oppppyo.", "..oppppo.", "...oppo..", "....oo...", "....wo...", "....wo...",
        "...owwo..", "..owwwwo."};
    /** Ses crocs, posés dans sa bouche (colonnes 16 à 18, lignes 20 et 21 de son portrait). */
    static final String[] FANGS = {"w.w", "w.w"};
    static final String[] MOTH = {"ww.ww", "wwoww", ".o.o."};

    // ----- Le décor -----

    /** Un pixel du décor qu'on garde dans une découpe ({@code r}, {@code g}, {@code b} de 0 à 255). */
    interface Keep { boolean keep(int x, int y, int r, int g, int b); }

    /**
     * @return le vrai morceau ({@code left}, {@code top}, {@code w}, {@code h}) du décor de la scène, réduit aux pixels
     *         que {@code keep} garde et qui touchent ({@code seedX}, {@code seedY}), plus leur contour : un objet
     *         du décor (un requin peint, une porte de coffre) qui peut s'en détacher ({@code null} sans fichiers)
     */
    static TextureRegion decorCut(BossScene s, int left, int top, int w, int h, int seedX, int seedY, Keep keep) {
        TextureRegion[] cut = decorCut(s, left, top, w, h, seedX, seedY, keep, 0, 0);
        return cut == null ? null : cut[0];
    }

    /**
     * {@link #decorCut}, et en second la même silhouette découpée dans le décor décalé de ({@code patchX},
     * {@code patchY}) pixels : de quoi boucher le trou que l'objet laisse en partant.
     */
    static TextureRegion[] decorCut(BossScene s, int left, int top, int w, int h, int seedX, int seedY, Keep keep, int patchX, int patchY) {
        if (Gdx.files == null) return null;
        String path = FrontScene.Decor.of(s.kind).getPath();
        if (!Gdx.files.internal(path).exists()) return null;
        Pixmap full = new Pixmap(Gdx.files.internal(path));
        int[][] color = new int[h][w];
        boolean[][] in = new boolean[h][w];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) color[y][x] = full.getPixel(left + x, top + y) | 0xff;
        ArrayDeque<int[]> todo = new ArrayDeque<>();
        todo.add(new int[] {seedX - left, seedY - top});
        while (!todo.isEmpty()) {                                      // les pixels gardés qui se touchent
            int[] p = todo.poll();
            int x = p[0], y = p[1];
            if (x < 0 || y < 0 || x >= w || y >= h || in[y][x]) continue;
            int c = color[y][x];
            if (!keep.keep(left + x, top + y, c >>> 24, (c >>> 16) & 0xff, (c >>> 8) & 0xff)) continue;
            in[y][x] = true;
            todo.add(new int[] {x + 1, y}); todo.add(new int[] {x - 1, y}); todo.add(new int[] {x, y + 1}); todo.add(new int[] {x, y - 1});
        }
        for (int pass = 0; pass < 2; pass++) {                         // bouche les traits fins (ligne du ventre, oeil)
            boolean[][] closed = new boolean[h][];
            for (int y = 0; y < h; y++) closed[y] = in[y].clone();
            for (int y = 1; y < h - 1; y++) {
                for (int x = 1; x < w - 1; x++) if (in[y][x - 1] && in[y][x + 1] || in[y - 1][x] && in[y + 1][x]) closed[y][x] = true;
            }
            in = closed;
        }
        boolean[][] body = in;                                         // et son contour, un pixel tout autour
        in = new boolean[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                in[y][x] = body[y][x] || x > 0 && body[y][x - 1] || x < w - 1 && body[y][x + 1] || y > 0 && body[y - 1][x] || y < h - 1 && body[y + 1][x];
            }
        }
        Pixmap cut = new Pixmap(w, h, Pixmap.Format.RGBA8888), patch = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        cut.setBlending(Pixmap.Blending.None);
        patch.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (!in[y][x]) continue;
                cut.drawPixel(x, y, color[y][x]);
                int px = MathUtils.clamp(left + x + patchX, 0, full.getWidth() - 1), py = MathUtils.clamp(top + y + patchY, 0, full.getHeight() - 1);
                patch.drawPixel(x, y, full.getPixel(px, py) | 0xff);
            }
        }
        full.dispose();
        return new TextureRegion[] {s.region(cut, false), s.region(patch, false)};
    }

    /** @return le morceau ({@code left}, {@code top}, {@code w}, {@code h}) du décor, tel quel ({@code null} sans décor). */
    static TextureRegion decorPart(BossScene s, int left, int top, int w, int h) {
        TextureRegion decor = s.decorImage();
        return decor == null ? null : new TextureRegion(decor, left, top, w, h);
    }

    /**
     * Dessine {@code part}, un morceau du décor, éclairé comme lui et à son échelle (fois {@code size}) : son centre
     * en ({@code x}, {@code y}), retourné si {@code flip}, tourné de {@code rotation}, assombri de {@code shadow}.
     */
    static void drawPart(BossScene s, Batch batch, TextureRegion part, float x, float y, float size, boolean flip, float rotation,
                         float shadow, float alpha) {
        if (part == null) return;
        Color lit = s.litLikeDecor(Color.WHITE, s.tmp);
        float light = 1f - shadow, w = part.getRegionWidth() * s.decorScale() * size, h = part.getRegionHeight() * s.decorScale() * size;
        batch.setColor(lit.r * light, lit.g * light, lit.b * light, alpha);
        batch.draw(part, x - w / 2f, y - h / 2f, w / 2f, h / 2f, w, h, flip ? -1f : 1f, 1f, rotation);
    }

    /** @return le centre à l'écran du morceau du décor dont le coin haut-gauche est au pixel ({@code left}, {@code top}). */
    static float partX(BossScene s, float left, float w) { return s.decorX(left + w / 2f); }
    static float partY(BossScene s, float top, float h) { return s.decorY(top + h / 2f); }

    // ----- Le Bar : les bouteilles des étagères -----

    /** Une bouteille du décor, contour compris : 10 pixels de large, 23 de haut (goulot puis panse). */
    static final int BOTTLE_W = 10, BOTTLE_H = 23, BOTTLES = 32, BOTTLE_NECK = 16;

    /** @return la colonne du bord gauche de la bouteille {@code i} : 8 par étagère, à gauche puis à droite du bar. */
    static int bottleLeft(int i) { int k = i % 16; return k < 8 ? 15 + 12 * k : 255 + 12 * (k - 8); }
    /** @return la ligne du haut de la bouteille {@code i} (étagère du haut, puis du bas). */
    static int bottleTop(int i) { return i < 16 ? 17 : 53; }

    static TextureRegion[] bottles(BossScene s) {
        TextureRegion[] all = new TextureRegion[BOTTLES];
        for (int i = 0; i < BOTTLES; i++) all[i] = decorPart(s, bottleLeft(i), bottleTop(i), BOTTLE_W, BOTTLE_H);
        return all;
    }

    /**
     * Les vraies bouteilles des étagères du Bar : elles tremblent de {@code shake} pixels du décor (redessinées
     * décalées par-dessus elles-mêmes) ; celles que {@code broken} marque n'ont plus que leur fond, cassé net (le haut
     * est recouvert par le même mur à losanges, pris juste au-dessus des étagères).
     */
    static void drawBottles(BossScene s, Batch batch, TextureRegion[] bottles, float shake, boolean[] broken) {
        float ds = s.decorScale();
        Color lit = s.litLikeDecor(Color.WHITE, s.tmp);
        batch.setColor(lit);
        for (int i = 0; i < BOTTLES; i++) {
            if (bottles[i] == null) continue;
            int left = bottleLeft(i), top = bottleTop(i);
            if (broken != null && broken[i]) {
                TextureRegion wall = decorPart(s, left, 1, BOTTLE_W, BOTTLE_NECK);
                batch.draw(wall, s.decorX(left), s.decorY(top + BOTTLE_NECK), BOTTLE_W * ds, BOTTLE_NECK * ds);
                continue;
            }
            if (shake <= 0f) continue;
            float dx = MathUtils.sin(s.time * 53f + i * 1.7f) * shake * ds, dy = MathUtils.sin(s.time * 41f + i * 2.3f) * shake * 0.4f * ds;
            batch.draw(bottles[i], s.decorX(left) + dx, s.decorY(top + BOTTLE_H) + dy, BOTTLE_W * ds, BOTTLE_H * ds);
        }
    }

    // ----- Les Machines : les machines à sous du décor -----

    /** Les machines du décor, rangée du fond puis de devant : {bord gauche de leur 1re fenêtre, haut, écart, largeur, hauteur}. */
    static final int[][] MACHINES = {
        {9, 60, 9, 8, 10}, {51, 60, 9, 8, 10}, {93, 60, 9, 8, 10}, {245, 60, 9, 8, 10}, {287, 60, 9, 8, 10}, {329, 60, 9, 8, 10},
        {5, 90, 12, 11, 21}, {57, 90, 12, 11, 21}, {109, 90, 12, 11, 21}, {245, 90, 12, 11, 21}, {297, 90, 12, 11, 21}};
    /** Leur enseigne jaune : {gauche, haut, largeur, hauteur}, dans le même ordre. */
    static final int[][] SIGNS = {
        {8, 46, 29, 9}, {50, 46, 29, 9}, {92, 46, 29, 9}, {244, 46, 29, 9}, {286, 46, 29, 9}, {328, 46, 29, 9},
        {4, 76, 37, 9}, {56, 76, 37, 9}, {108, 76, 37, 9}, {244, 76, 37, 9}, {296, 76, 37, 9}};
    static final Color[] BULBS = {hex("ff3a5a"), hex("ffd23a"), hex("3aff8a"), hex("3ab0ff"), hex("ff7ad8")};
    static final Color SCREEN_OFF = hex("10141c"), SCREEN = hex("e8ecec");

    static TextureRegion[] slotSymbols(BossScene s) {
        return new TextureRegion[] {s.load("symbols/2-cherry.png"), s.load("symbols/3-seven.png"), s.load("symbols/4-bar.png"),
            s.load("symbols/6-bell.png"), s.load("symbols/7-diamond.png"), s.load("symbols/11-watermelon.png")};
    }

    /**
     * Un rouleau dans la fenêtre d'écran ({@code x}, {@code y}, {@code w}, {@code h}) : les symboles gardent leurs
     * proportions et défilent vers le bas ; {@code offset} (en symboles) donne celui du milieu.
     */
    static void slot(Batch batch, TextureRegion[] symbols, float x, float y, float w, float h, float offset) {
        float cell = w * symbols[0].getRegionHeight() / symbols[0].getRegionWidth();
        int first = MathUtils.floor(offset), reach = (int) Math.ceil(h / cell / 2f) + 1;
        float shift = offset - first, mid = y + h / 2f;
        for (int k = -reach; k <= reach; k++) {
            TextureRegion symbol = symbols[Math.floorMod(first + k, symbols.length)];
            float centre = mid + (k - shift) * cell, top = centre + cell / 2f, bottom = centre - cell / 2f;
            float clipTop = Math.min(top, y + h), clipBottom = Math.max(bottom, y);
            if (clipTop <= clipBottom) continue;
            float v0 = (top - clipTop) / cell, v1 = (top - clipBottom) / cell;
            float u = symbol.getU(), u2 = symbol.getU2(), sv = symbol.getV(), sv2 = symbol.getV2();
            batch.draw(symbol.getTexture(), x, clipBottom, w, clipTop - clipBottom, u, sv + (sv2 - sv) * v1, u2, sv + (sv2 - sv) * v0);
        }
    }

    /** @return le rouleau {@code cell} (0 à 2) de la machine {@code m} du décor : {x, y, largeur, hauteur} à l'écran. */
    static float[] machineWindow(BossScene s, int m, int cell) {
        int[] w = MACHINES[m];
        int width = cell == 2 && w[4] > 10 ? w[3] + 1 : w[3];
        float left = s.decorX(w[0] + cell * w[2]), top = s.decorY(w[1]), bottom = s.decorY(w[1] + w[4]);
        return new float[] {left, bottom, width * s.decorScale(), top - bottom};
    }

    // ----- Le VIP : les bras du Kraken -----

    static final Color KRAKEN_ARM = hex("8a1e2a"), KRAKEN_ARM_DARK = hex("5a0e18"), KRAKEN_SUCKER = hex("e8a0a0"), OUTLINE = hex("1a0f0f");

    /**
     * Un bras du Kraken en pixel art, à la taille des pixels de son portrait : une chaîne de pavés cernés de noir qui
     * part de ({@code x0}, {@code y0}) vers ({@code x1}, {@code y1}) en ondulant, avec ses ventouses.
     */
    static void arm(BossScene s, Batch batch, float x0, float y0, float x1, float y1, float thick, float wave, float phase, float light) {
        float g = s.bossScale(), dx = x1 - x0, dy = y1 - y0, len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < g) return;
        int segments = Math.max(4, (int) (len / g));
        float nx = -dy / len, ny = dx / len;
        for (int pass = 0; pass < 3; pass++) {
            for (int i = 0; i <= segments; i++) {
                float k = i / (float) segments;
                float off = MathUtils.sin(k * 7f + s.time * 4f + phase) * wave * k;
                float x = Math.round((x0 + dx * k + nx * off) / g) * g, y = Math.round((y0 + dy * k + ny * off) / g) * g;
                float size = Math.max(g, Math.round(thick * (1f - 0.7f * k) / g) * g);
                if (pass == 0) s.rect(batch, tint(OUTLINE, light), 1f, x - size / 2f - g, y - size / 2f - g, size + 2f * g, size + 2f * g);
                else if (pass == 1) {
                    s.rect(batch, tint(i % 2 == 0 ? KRAKEN_ARM : KRAKEN_ARM_DARK, light), 1f, x - size / 2f, y - size / 2f, size, size);
                } else if (i % 4 == 2 && size > g * 1.5f) {
                    s.rect(batch, tint(KRAKEN_SUCKER, light), 1f, x - g / 2f + nx * size * 0.25f, y - g / 2f + ny * size * 0.25f, g, g);
                }
            }
        }
    }

    private static final Color TINT = new Color();

    private static Color tint(Color color, float light) { return TINT.set(color.r * light, color.g * light, color.b * light, 1f); }

    // -------------------------------------------------------------------------
    // Sirène
    // -------------------------------------------------------------------------

    /**
     * « Chant mortel : Croc ! » : son chant s'enroule en spirales de notes, les bouteilles des étagères du Bar vibrent
     * puis se fêlent ; l'image ondule, ses yeux s'allument et deux crocs pointent dans sa bouche ouverte.
     */
    static final class SirenPhase extends BossScene {
        private static final Color PINK = hex("ff7ad8");
        private static final int[] CRACKED = {2, 5, 9, 12, 18, 21, 26, 29};
        private final TextureRegion note, fangs;
        private final TextureRegion[] bottles;

        SirenPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.SIRENE, 2);
            note = art("w", new Color[] {Color.WHITE}, NOTE);
            fangs = art("w", new Color[] {hex("f6f0e2")}, FANGS);
            bottles = bottles(this);
        }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_PHASE)) {
                rumble(0.5f, 8f);
                sparks(portraitX(17.5f), portraitY(21.5f), 70, PINK, 900f, 20f);
                for (int i : CRACKED) sparks(partX(this, bottleLeft(i), BOTTLE_W), partY(this, bottleTop(i), BOTTLE_H), 6, WHITE, 300f, 8f);
            }
        }

        @Override
        protected void back(Batch batch) {
            float hum = progress(0.3f, CLIMAX_PHASE);                   // les bouteilles vibrent de plus en plus
            drawBottles(this, batch, bottles, 0.15f + 0.9f * hum * (1f - 0.7f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.8f)), null);
            if (time >= CLIMAX_PHASE) {                                 // le contre-ut : les plus proches se fêlent
                for (int i : CRACKED) {
                    float x = partX(this, bottleLeft(i), BOTTLE_W), y = decorY(bottleTop(i) + 12f);
                    crack(this, batch, x, y, i);
                }
            }
            for (int i = 0; i < BOTTLES; i += 3) {                      // le verre résonne en rose
                glow(batch, partX(this, bottleLeft(i), BOTTLE_W), partY(this, bottleTop(i), BOTTLE_H), 120f, PINK,
                    0.35f * hum * pulse(14f + i, 0.3f));
            }
            // les notes s'enroulent en spirales autour d'elle
            float spin = time * MathUtils.lerp(60f, 360f, progress(0f, CLIMAX_PHASE));
            for (int arm = 0; arm < 3; arm++) {
                for (int i = 0; i < 9; i++) {
                    float k = ((time * 0.4f + i / 9f) % 1f);
                    float angle = arm * 120f + spin + k * 540f, r = worldHeight() * (0.08f + 0.45f * k);
                    float x = bossX() + MathUtils.cosDeg(angle) * r * 1.3f, y = bossY() + MathUtils.sinDeg(angle) * r;
                    float alpha = progress(0.2f, 0.8f) * (1f - k);
                    draw(batch, note, x, y, fit(46f, 6), 0f, tmp.set(WHITE).lerp(PINK, k), alpha);
                }
            }
            float waves = progress(0.8f, CLIMAX_PHASE) * (1f - progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f));
            showBoss = waves <= 0f;
            if (!showBoss) ripple(this, batch, 18f * waves, 1f, WHITE);
        }

        private void crack(BossScene s, Batch batch, float x, float y, int seed) {
            float w = s.decorScale();
            s.crack(batch, x, y, 70f + seed * 13f, 7f * w, seed * 31L, WHITE, 0.85f, 0.6f * w);
            s.crack(batch, x, y, 250f + seed * 7f, 5f * w, seed * 17L, WHITE, 0.7f, 0.5f * w);
        }

        @Override
        protected void front(Batch batch) {
            float bite = progress(CLIMAX_PHASE + 0.25f, CLIMAX_PHASE + 0.4f);   // ses crocs, une fois l'image calmée
            if (bite > 0f) {
                onPortrait(batch, fangs, 16f, 20f, 1f, bite);
                glow(batch, portraitX(17.5f), portraitY(21f), 90f, WHITE, 0.5f * bite * (1f - progress(CLIMAX_PHASE + 0.4f, 3.2f)));
            }
            eyes(batch, progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.2f), PINK, new float[] {14f, 16f}, new float[] {21f, 16f});
            wave(batch, CLIMAX_PHASE, 0.8f, portraitX(17.5f), portraitY(21.5f), PINK, worldWidth() * 1.3f);
            flash(batch, PINK, CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    /** Le ripple de la Sirène : son portrait dessiné en bandes qui ondulent comme sous l'eau. */
    static void ripple(BossScene scene, Batch batch, float amount, float alpha, Color tint) {
        int bands = scene.boss.getRegionHeight();
        float scale = scene.bossScale(), width = scene.boss.getRegionWidth() * scale;
        batch.setColor(tint.r, tint.g, tint.b, alpha);
        for (int row = 0; row < bands; row++) {
            TextureRegion band = new TextureRegion(scene.boss, 0, row, scene.boss.getRegionWidth(), 1);
            float shift = MathUtils.sin(scene.time * 9f + row * 0.35f) * amount;
            batch.draw(band, scene.bossX() - width / 2f + shift, scene.bossBottom() + (bands - row - 1) * scale, width, scale + 0.5f);
        }
    }

    /**
     * « Le chant de la Sirène se tait » : elle chante une dernière fois, ses notes filent vers les étagères et le verre
     * posé sur le comptoir ; sa note la plus haute fait éclater toutes les bouteilles du Bar ; sa voix casse, une
     * dernière note tombe, grise, et se brise au sol ; elle se défait en écume, de la queue aux cheveux.
     */
    static final class SirenDeath extends BossScene {
        private static final Color PINK = hex("ff7ad8");
        private static final float NOTE_FALL = CLIMAX_DEATH + 0.3f, NOTE_LAND = 3.5f;
        private final TextureRegion note, glass;
        private final TextureRegion[] bottles;
        private final boolean[] broken = new boolean[BOTTLES];

        SirenDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.SIRENE, 0);
            note = art("w", new Color[] {Color.WHITE}, NOTE);
            glass = art("owpy", new Color[] {hex("1a0a1a"), hex("e8f0ff"), hex("ff7ad8"), hex("ffd860")}, GLASS);
            bottles = bottles(this);
        }

        @Override protected void restart() { java.util.Arrays.fill(broken, false); }

        /** Le verre à cocktail, posé sur le comptoir du décor. */
        private float glassX() { return bossX() + bossWidth() * 0.78f; }
        private float glassY() { return decorY(125f) + fit(110f, 9) * 4.5f; }

        /** @return quand la bouteille {@code i} éclate : du centre du bar vers les murs. */
        private float burst(int i) {
            int k = i % 8, side = (i % 16) < 8 ? 7 - k : k;
            return CLIMAX_DEATH + side * 0.035f + (i < 16 ? 0f : 0.02f);
        }

        @Override
        protected void step(float delta) {
            if (at(CLIMAX_DEATH)) {                                     // sa note la plus haute brise le verre
                rumble(0.6f, 9f);
                shatter(glass, glassX(), glassY(), fit(110f, 9), 2);
            }
            for (int i = 0; i < BOTTLES; i++) {
                if (!broken[i] && time >= burst(i) && bottles[i] != null) {
                    broken[i] = true;
                    float ds = decorScale(), left = decorX(bottleLeft(i)), top = decorY(bottleTop(i));
                    TextureRegion neck = new TextureRegion(bottles[i], 0, 0, BOTTLE_W, BOTTLE_NECK);
                    for (int py = 0; py < BOTTLE_NECK; py += 4) {
                        for (int px = 1; px < BOTTLE_W - 1; px += 4) {
                            Particle shard = place(new TextureRegion(neck, px, py, 4, 4), left + (px + 2f) * ds, top - (py + 2f) * ds,
                                (random.nextFloat() - 0.5f) * 500f, 100f + 400f * random.nextFloat(), ds, 1.4f);
                            shard.gravity = 1400f;
                            shard.spin = (random.nextFloat() - 0.5f) * 720f;
                            shard.color.set(litLikeDecor(WHITE, tmp));
                        }
                    }
                    sparks(left + BOTTLE_W * ds / 2f, top - 6f * ds, 5, WHITE, 300f, 8f);
                }
            }
            if (at(NOTE_LAND)) sparks(bossX() - bossWidth() * 0.15f, groundY() + 10f, 14, hex("a0a0b0"), 260f, 8f);
            if (time > CLIMAX_DEATH + 0.2f && time < 4.6f) {            // elle se défait en écume et en bulles
                float level = progress(CLIMAX_DEATH + 0.2f, 4.4f);
                for (int i = 0; i < 4; i++) {
                    Particle bubble = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth(),
                        bossBottom() + bossHeight() * level + (random.nextFloat() - 0.5f) * 40f, 90f + (random.nextFloat() - 0.5f) * 40f,
                        120f + 200f * random.nextFloat(), 1.6f, 14f + 20f * random.nextFloat());
                    bubble.color.set(0.85f, 0.95f, 1f, 0.8f);
                    glows.add(bubble);
                }
            }
        }

        /** Fait voler l'image {@code region} en éclats de {@code size} pixels, posée en ({@code x}, {@code y}). */
        private void shatter(TextureRegion region, float x, float y, float scale, int size) {
            int w = region.getRegionWidth(), h = region.getRegionHeight();
            for (int py = 0; py < h; py += size) {
                for (int px = 0; px < w; px += size) {
                    Particle shard = place(new TextureRegion(region, px, py, Math.min(size, w - px), Math.min(size, h - py)),
                        x + (px - w / 2f) * scale, y + (h / 2f - py) * scale, (random.nextFloat() - 0.5f) * 700f,
                        200f + 500f * random.nextFloat(), scale, 1.6f);
                    shard.gravity = 1600f;
                    shard.spin = (random.nextFloat() - 0.5f) * 720f;
                }
            }
            sparks(x, y, 30, WHITE, 600f, 14f);
        }

        @Override
        protected void back(Batch batch) {
            drawBottles(this, batch, bottles, 1.1f * progress(0.6f, CLIMAX_DEATH), broken);
            // elle s'efface de bas en haut : sa queue se défait la première
            float melt = progress(CLIMAX_DEATH + 0.2f, 4.4f);
            int rows = boss.getRegionHeight(), keep = Math.round(rows * (1f - melt));
            showBoss = false;
            if (keep <= 0) return;
            float scale = bossScale(), width = boss.getRegionWidth() * scale;
            float light = 1f - 0.35f * melt;
            batch.setColor(light, light, light + 0.1f * melt, 1f - 0.3f * melt);
            TextureRegion top = new TextureRegion(boss, 0, 0, boss.getRegionWidth(), keep);
            float sing = 6f * MathUtils.sin(time * 5f) * (1f - progress(CLIMAX_DEATH - 0.4f, CLIMAX_DEATH));   // elle se balance en chantant
            poseX = sing;
            batch.draw(top, bossX() - width / 2f + sing, bossBottom() + (rows - keep) * scale, width, keep * scale);
            additive(batch);                                           // l'écume qui la ronge
            batch.setColor(0.7f, 0.9f, 1f, 0.5f * melt);
            batch.draw(soft, bossX() - width * 0.6f, bossBottom() + (rows - keep) * scale - 60f, width * 1.2f, 120f);
            normal(batch);
        }

        @Override
        protected void front(Batch batch) {
            // son dernier chant : des notes de plus en plus grandes, qui filent vers les bouteilles et le verre
            float mouthX = portraitX(17.5f), mouthY = portraitY(21.5f);
            for (int i = 0; i < 8; i++) {
                float k = ((time * 0.7f + i / 8f) % 1f);
                int target = (i * 11) % BOTTLES;
                float tx = i % 4 == 3 ? glassX() : partX(this, bottleLeft(target), BOTTLE_W);
                float ty = i % 4 == 3 ? glassY() : partY(this, bottleTop(target), BOTTLE_H);
                float x = MathUtils.lerp(mouthX, tx, k), y = MathUtils.lerp(mouthY, ty, k) + MathUtils.sin(k * MathUtils.PI) * 90f;
                float grow = 1f + 0.8f * progress(0.8f, CLIMAX_DEATH);
                draw(batch, note, x, y, fit(40f * grow, 6), 10f * MathUtils.sin(time * 6f + i), tmp.set(WHITE).lerp(PINK, k),
                    progress(0.1f, 0.5f) * (1f - k * 0.4f) * (1f - progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.15f)));
            }
            glow(batch, mouthX, mouthY, 200f, PINK, 0.6f * progress(1.6f, CLIMAX_DEATH) * (1f - progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.3f)));
            if (time < CLIMAX_DEATH) {                                  // le verre vibre, puis se brise
                float buzz = (random.nextFloat() - 0.5f) * 8f * progress(1.4f, CLIMAX_DEATH);
                draw(batch, glass, glassX() + buzz, glassY(), fit(110f, 9), 0f, litLikeDecor(WHITE, tmp).add(0.3f, 0.3f, 0.3f, 0f), 1f);
            }
            if (time >= NOTE_FALL && time < NOTE_LAND + 0.6f) {          // sa voix casse : une dernière note tombe, grise
                float k = Interpolation.pow2In.apply(progress(NOTE_FALL, NOTE_LAND));
                float x = MathUtils.lerp(mouthX, bossX() - bossWidth() * 0.15f, k), y = MathUtils.lerp(mouthY, groundY() + 20f, k);
                float fade = 1f - progress(NOTE_LAND, NOTE_LAND + 0.6f);
                draw(batch, note, x, y, fit(44f, 6), 160f * k, hex("a0a0b0"), fade);
            }
            fill(batch, hex("e0f0ff"), 0.12f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 1f));
            flash(batch, WHITE, CLIMAX_DEATH, 0.25f, 0.35f);
        }
    }

    // -------------------------------------------------------------------------
    // Jackpot Vivant
    // -------------------------------------------------------------------------

    /** Son levier, planté dans son dos : son pivot, en pixels du portrait (bords). */
    static final float LEVER_PIVOT_X = 25f, LEVER_PIVOT_Y = 14f;

    /** Dessine son vrai levier ({@code lever}) sur son portrait, tourné de {@code angle} degrés autour de son pivot. */
    static void drawLever(BossScene s, Batch batch, BossScene.Piece lever, float angle, Color color) {
        if (lever == null) return;
        float g = s.bossScale() * s.poseScale;
        float x = s.portraitX(lever.left), y = s.portraitY(lever.top + lever.height);
        float ox = (LEVER_PIVOT_X - lever.left) * g, oy = (lever.top + lever.height - LEVER_PIVOT_Y) * g;
        batch.setColor(color);
        batch.draw(lever.region, x, y, ox, oy, lever.width * g, lever.height * g, 1f, 1f, angle);
    }

    /**
     * « Mise maximale : Intérêts ! » : son vrai levier se tire tout seul, trois fois, de plus en plus fort ; toutes les
     * machines de la salle s'allument et leurs rouleaux s'affolent, puis tombent ensemble sur 7-7-7 ; elles
     * crachent des pièces avec lui : l'argent fait des petits.
     */
    static final class JackpotPhase extends BossScene {
        private static final float[] PULLS = {0.4f, 1.15f, 1.75f};
        private static final int SEVEN = 1;
        private final TextureRegion[] symbols;
        private final TextureRegion bare;
        private final Piece lever;

        JackpotPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.JACKPOT_VIVANT, 2);
            symbols = slotSymbols(this);
            int[][] rest = PlacesPortraits.without(EnemyKind.JACKPOT_VIVANT, PlacesPortraits.Prop.JACKPOT_LEVER);
            lever = lift(PlacesPortraits.pixels(EnemyKind.JACKPOT_VIVANT), rest);
            bare = pixels(rest);
        }

        @Override protected void restart() { wear(bare); }

        /** @return quand le rouleau {@code cell} de la machine {@code m} s'arrête sur le 7. */
        private float stop(int m, int cell) { return CLIMAX_PHASE - 0.3f + cell * 0.1f + (m % 3) * 0.02f; }

        @Override
        protected void step(float delta) {
            for (float pull : PULLS) if (at(pull + 0.25f)) rumble(0.15f, 6f);
            if (at(CLIMAX_PHASE)) {
                rumble(0.8f, 14f);
                for (int m = 6; m < MACHINES.length; m++) {             // les machines de devant paient
                    float x = decorX(SIGNS[m][0] + SIGNS[m][2] / 2f), y = decorY(124f);
                    spray(coin, x, y, 10, 60f, 120f, 900f, coinScale(0.035f), 1600f, 1.6f);
                    sparks(x, y, 12, GOLD, 400f, 12f);
                }
            }
            if (time > CLIMAX_PHASE && time < 3.8f && random.nextFloat() < 0.6f) {   // il crache des pièces par sa bouche-fente
                spray(coin, portraitX(3f), portraitY(26f), 3, 150f, 200f, 1100f, coinScale(0.04f), 1600f, 1.8f);
            }
        }

        /** @return le levier tiré, de 0 (debout) à 1 (tout en bas). */
        private float pulled() {
            float pull = 0f;
            for (float at : PULLS) pull = Math.max(pull, Interpolation.pow2In.apply(progress(at, at + 0.25f)) * (1f - progress(at + 0.35f, at + 0.6f)));
            return pull;
        }

        @Override
        protected void back(Batch batch) {
            float crazy = progress(0.5f, CLIMAX_PHASE), won = progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.1f);
            for (int m = 0; m < MACHINES.length; m++) {                 // les machines du décor s'allument
                int[] sign = SIGNS[m];
                int pick = (int) (time * (2f + 16f * crazy) + m) % BULBS.length;
                Color light = won > 0f ? (MathUtils.sin(time * 20f + m) > 0f ? GOLD : HOT) : BULBS[pick];
                decorRect(batch, tmp.set(light).lerp(Color.WHITE, 0.2f), sign[0], sign[1], sign[2], sign[3]);
                glow(batch, decorX(sign[0] + sign[2] / 2f), decorY(sign[1] + sign[3] / 2f), sign[2] * decorScale() * 1.4f, light,
                    0.25f + 0.25f * crazy);
                for (int cell = 0; cell < 3; cell++) {
                    float[] w = machineWindow(this, m, cell);
                    float stop = stop(m, cell), speed = 14f + 3f * cell;
                    float offset = time >= stop ? SEVEN : SEVEN - (stop - Math.max(time, 0.55f)) * speed;
                    rect(batch, litLikeDecor(SCREEN, tmp).lerp(WHITE, 0.35f), 1f, w[0], w[1], w[2], w[3]);
                    batch.setColor(litLikeDecor(WHITE, tmp).lerp(WHITE, 0.4f));
                    slot(batch, symbols, w[0], w[1], w[2], w[3], offset + m * 0.37f * (time < stop ? 1f : 0f));
                }
                if (won > 0f) glow(batch, decorX(MACHINES[m][0] + MACHINES[m][2] * 1.5f), decorY(MACHINES[m][1] + MACHINES[m][4] / 2f),
                    200f, GOLD, 0.5f * pulse(12f, 0.4f));
            }
            poseX = (random.nextFloat() - 0.5f) * 10f * crazy * (1f - 0.6f * progress(CLIMAX_PHASE + 0.4f, 3.5f));
            poseY = 6f * Math.abs(MathUtils.sin(time * 20f)) * crazy;
        }

        @Override
        protected void front(Batch batch) {
            float pull = pulled();                                      // son vrai levier, qui descend tout seul
            drawLever(this, batch, lever, -120f * pull, WHITE);
            glow(batch, portraitX(25f), portraitY(4f) - pull * bossScale() * 6f, 90f, hex("ff3a5a"), 0.5f * pull);
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.45f);
        }
    }

    /**
     * « Le Jackpot est vidé ! » : les trois rouleaux de ses flancs tournent, s'arrêtent sur trois symboles
     * différents : perdu ; ses fenêtres grillent et toutes les machines de la salle s'éteignent l'une après l'autre ;
     * son or se ternit, il se retourne le ventre en l'air et remonte, mort, comme un poisson ; sa dernière pièce
     * tombe de sa bouche-fente et roule au sol.
     */
    static final class JackpotDeath extends BossScene {
        private static final float[] STOPS = {1.2f, 1.8f, 2.4f};
        private static final int[] RESULT = {0, 3, 5};
        /** Ses trois fenêtres (portrait) : colonne de gauche de chacune ; lignes 20 à 26. */
        private static final int[] WINDOWS = {9, 15, 21};
        private static final float FLIP = CLIMAX_DEATH + 0.4f;
        private final TextureRegion[] symbols;
        private final TextureRegion dead, ashen;

        JackpotDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.JACKPOT_VIVANT, 0);
            symbols = slotSymbols(this);
            int[][] grid = PlacesPortraits.pixels(EnemyKind.JACKPOT_VIVANT);   // ses fenêtres éteintes
            int off = 0x1a1018ff;
            for (int x0 : WINDOWS) for (int y = 20; y <= 26; y++) for (int x = x0; x <= x0 + 4; x++) grid[y][x] = off;
            dead = pixels(grid);
            for (int[] row : grid) {                                    // le même, son or terni en gris
                for (int x = 0; x < row.length; x++) {
                    int c = row[x];
                    if (c == 0) continue;
                    int grey = Math.round(0.3f * (c >>> 24) + 0.55f * ((c >>> 16) & 0xff) + 0.15f * ((c >>> 8) & 0xff));
                    row[x] = (Math.round(grey * 0.8f) << 24) | (Math.round(grey * 0.82f) << 16) | (Math.round(grey * 0.9f) << 8) | (c & 0xff);
                }
            }
            ashen = pixels(grid);
        }

        /** @return quand la machine {@code m} du décor s'éteint : de la plus proche à la plus lointaine. */
        private float lightsOut(int m) { return CLIMAX_DEATH + 0.15f + Math.abs(decorX(MACHINES[m][0]) - bossX()) / worldWidth() * 1.4f; }

        @Override
        protected void step(float delta) {
            for (int r = 0; r < 3; r++) if (at(STOPS[r])) rumble(0.1f, 4f);
            if (at(CLIMAX_DEATH)) {                                     // perdu : ses fenêtres grillent
                rumble(0.5f, 9f);
                for (int x0 : WINDOWS) sparks(portraitX(x0 + 2.5f), portraitY(23.5f), 30, hex("7ad0ff"), 700f, 14f);
                wear(dead);
            }
            for (int m = 0; m < MACHINES.length; m++) {
                if (at(lightsOut(m))) sparks(decorX(MACHINES[m][0] + MACHINES[m][2] * 1.5f), decorY(MACHINES[m][1] + MACHINES[m][4] / 2f),
                    8, hex("7ad0ff"), 300f, 8f);
            }
        }

        private float flipped() { return Interpolation.pow2InInverse.apply(progress(FLIP, FLIP + 1.2f)); }

        @Override
        protected void back(Batch batch) {
            for (int m = 0; m < MACHINES.length; m++) {                 // les machines de la salle, qui s'éteignent
                boolean on = time < lightsOut(m) || (time < lightsOut(m) + 0.25f && MathUtils.sin(time * 60f) > 0f);
                for (int cell = 0; cell < 3; cell++) {
                    float[] w = machineWindow(this, m, cell);
                    if (!on) { rect(batch, litLikeDecor(SCREEN_OFF, tmp), 1f, w[0], w[1], w[2], w[3]); continue; }
                    rect(batch, litLikeDecor(SCREEN, tmp).lerp(WHITE, 0.35f), 1f, w[0], w[1], w[2], w[3]);
                    batch.setColor(litLikeDecor(WHITE, tmp).lerp(WHITE, 0.3f));
                    float stop = STOPS[cell] + (m % 3) * 0.1f;
                    slot(batch, symbols, w[0], w[1], w[2], w[3], time >= stop ? (m + cell * 2) % 6 : (m + cell * 2) % 6 - (stop - time) * 12f);
                }
                if (!on) decorRect(batch, hex("3a3420"), SIGNS[m][0], SIGNS[m][1], SIGNS[m][2], SIGNS[m][3]);
            }
            // son or se ternit, il se retourne le ventre en l'air et remonte, mort
            float flip = flipped(), rise = Interpolation.pow2In.apply(progress(FLIP + 0.6f, 5.4f));
            float grey = progress(CLIMAX_DEATH + 0.1f, FLIP + 0.8f);
            poseShadow = 0.25f * grey;
            poseX = time > CLIMAX_DEATH && time < CLIMAX_DEATH + 0.4f ? (random.nextFloat() - 0.5f) * 14f : 0f;
            if (time >= CLIMAX_DEATH) {                                 // lui-même, son or qui se ternit, puis retourné
                showBoss = false;
                float w = bossWidth(), h = bossHeight(), cx = bossX() + poseX, cy = bossY() + worldHeight() * 0.7f * rise + 14f * MathUtils.sin(time * 2f) * flip;
                float light = 1f - poseShadow;
                batch.setColor(light, light, light, 1f);
                batch.draw(dead, cx - w / 2f, cy - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, 180f * flip);
                batch.setColor(light, light, light, grey);
                batch.draw(ashen, cx - w / 2f, cy - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, 180f * flip);
            }
        }

        @Override
        protected void front(Batch batch) {
            if (time < CLIMAX_DEATH) {                                  // les rouleaux de ses flancs
                for (int r = 0; r < 3; r++) {
                    float offset = time >= STOPS[r] ? RESULT[r] : RESULT[r] - (STOPS[r] - time) * 16f;
                    float x = portraitX(WINDOWS[r]), y = portraitY(27f), w = portraitX(WINDOWS[r] + 5f) - x, h = portraitY(20f) - y;
                    rect(batch, hex("f6f0e2"), 1f, x, y, w, h);
                    batch.setColor(WHITE);
                    slot(batch, symbols, x, y, w, h, offset);
                }
            }
            // sa dernière pièce tombe de sa bouche-fente et roule au sol
            float k = progress(CLIMAX_DEATH + 0.3f, 3.4f);
            if (k > 0f) {
                float drop = Interpolation.bounceOut.apply(Math.min(1f, k * 1.6f)), roll = progress(3.0f, 4.6f);
                float x = bossX() - bossWidth() * 0.45f - worldWidth() * 0.12f * Interpolation.pow2Out.apply(roll);
                float y = MathUtils.lerp(bossBottom() + bossHeight() * 0.36f, groundY() + 12f, drop);
                float s = coinScale(0.06f);
                glow(batch, x, y, 140f, GOLD, 0.35f * (1f - roll * 0.5f));
                stretch(batch, coin, x, y, s * (0.25f + 0.75f * Math.abs(MathUtils.cos(time * 9f * (1f - roll)))), s, 0f, WHITE, 1f);
            }
            fill(batch, Color.BLACK, 0.3f * progress(3.6f, 5.0f));
            flash(batch, hex("7ad0ff"), CLIMAX_DEATH, 0.3f, 0.4f);
        }

        @Override protected float decorLight() { return 0.62f - 0.22f * progress(CLIMAX_DEATH + 0.2f, 4.0f); }
    }

    // -------------------------------------------------------------------------
    // Requin Banquier
    // -------------------------------------------------------------------------

    /** Les deux requins peints sur le mur des Coffres : {gauche, haut, largeur, hauteur, graine x, graine y, oeil x, oeil y, sens}. */
    static final float[][] WALL_SHARKS = {{95, 17, 97, 35, 150, 38, 174.5f, 38f, 1f}, {199, 47, 97, 35, 250, 70, 216f, 68.5f, -1f}};
    /**
     * Pour boucher le trou qu'ils laissent : le même mur, pris ailleurs ; le décalage garde l'appareil des briques
     * (rangées de 12 pixels, briques de 24 décalées d'une demie) et suit à peu près la pente des rais de lumière.
     */
    static final int[][] WALL_PATCH = {{-36, 108}, {12, -36}};

    /** @return le requin {@code i} du mur, puis le même morceau de briques pris ailleurs (pour boucher son trou). */
    static TextureRegion[] wallShark(BossScene s, int i) {
        float[] k = WALL_SHARKS[i];
        return decorCut(s, (int) k[0], (int) k[1], (int) k[2], (int) k[3], (int) k[4], (int) k[5],
            (x, y, r, g, b) -> b - r > 46 && b > 105, WALL_PATCH[i][0], WALL_PATCH[i][1]);
    }

    /** Bouche le trou du requin {@code i} avec {@code bricks}, des briques du même mur. */
    static void patchWall(BossScene s, Batch batch, int i, TextureRegion bricks) {
        if (bricks == null) return;
        float[] k = WALL_SHARKS[i];
        batch.setColor(s.litLikeDecor(Color.WHITE, s.tmp));
        batch.draw(bricks, s.decorX(k[0]), s.decorY(k[1] + k[3]), k[2] * s.decorScale(), k[3] * s.decorScale());
    }

    /**
     * « Frénésie : Rage ! » : l'eau des Coffres rougit ; les yeux des deux requins peints sur le mur s'allument, ils
     * s'en détachent et tournent autour de lui, de plus en plus vite, en déchiquetant la pluie de billets ; il
     * tremble de rage et claque des mâchoires.
     */
    static final class SharkPhase extends BossScene {
        private static final Color RED = hex("c8102e"), BLOOD = hex("ff2a3a");
        private static final float LEAVE = 0.55f, JOINED = 1.25f;
        private final TextureRegion bill;
        private final TextureRegion[] sharks = new TextureRegion[2], holes = new TextureRegion[2];
        private float angle;

        SharkPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.REQUIN_BANQUIER, 2);
            bill = symbol(EnemySymbol.FAKE_MONEY);
            for (int i = 0; i < 2; i++) {
                TextureRegion[] cut = wallShark(this, i);
                if (cut != null) { sharks[i] = cut[0]; holes[i] = cut[1]; }
            }
        }

        @Override protected void restart() { angle = 0f; }

        @Override
        protected void step(float delta) {
            angle += delta * MathUtils.lerp(70f, 520f, progress(JOINED - 0.4f, CLIMAX_PHASE));
            if (time > 0.5f && random.nextFloat() < 0.3f) {             // la pluie de billets
                Particle note = place(bill, worldWidth() * random.nextFloat(), worldHeight() + 40f, (random.nextFloat() - 0.5f) * 80f,
                    -170f - 120f * random.nextFloat(), iconScale(0.06f), 3.4f);
                note.spin = (random.nextFloat() - 0.5f) * 200f;
            }
            if (at(CLIMAX_PHASE)) { rumble(0.6f, 12f); sparks(portraitX(17f), portraitY(21f), 60, RED, 900f, 22f); }
            for (int i = 0; i < 2; i++) {                               // leurs mâchoires déchiquettent les billets qu'ils croisent
                float[] head = head(i);
                for (Particle p : sprites) {
                    if (p.region == bill && p.age < p.life && Math.abs(p.x - head[0]) < 70f && Math.abs(p.y - head[1]) < 60f) {
                        p.age = p.life;
                        for (int half = 0; half < 2; half++) {
                            Particle torn = place(new TextureRegion(bill, half * 8, 0, 8, 16), p.x + (half - 0.5f) * 30f, p.y,
                                (half - 0.5f) * 300f, 100f, p.size, 0.9f);
                            torn.gravity = 900f;
                            torn.spin = (half - 0.5f) * 600f;
                        }
                        sparks(p.x, p.y, 4, BLOOD, 200f, 8f);
                    }
                }
            }
        }

        /** @return le sens de nage du requin {@code i} : 1 vers la droite. */
        private float facing(int i) {
            if (time < LEAVE) return WALL_SHARKS[i][8];
            float a = angle + i * 180f;
            return -MathUtils.sinDeg(a) >= 0f ? 1f : -1f;              // il tourne dans le sens inverse des aiguilles
        }

        /** @return la position du requin {@code i} : sur le mur, puis sur son cercle autour du Requin banquier. */
        private float[] spot(int i) {
            float[] k = WALL_SHARKS[i];
            float wallX = partX(this, k[0], k[2]), wallY = partY(this, k[1], k[3]);
            float a = angle + i * 180f;
            float cx = bossX() + MathUtils.cosDeg(a) * worldWidth() * 0.34f, cy = bossBottom() + bossHeight() * 0.42f + MathUtils.sinDeg(a) * worldHeight() * 0.12f;
            float join = Interpolation.pow2InInverse.apply(progress(LEAVE, JOINED));
            return new float[] {MathUtils.lerp(wallX, cx, join), MathUtils.lerp(wallY, cy, join), MathUtils.sinDeg(a) * join, join};
        }

        /** @return sa taille (1 : celle du mur) ; plus petit quand il passe derrière. */
        private float size(int i) { float[] p = spot(i); return MathUtils.lerp(1f, 0.8f - 0.18f * p[2], p[3]); }

        /** @return l'oeil du requin {@code i} (là où il mord). */
        private float[] head(int i) {
            float[] p = spot(i), k = WALL_SHARKS[i];
            float flip = facing(i) * k[8], s = decorScale() * size(i);
            return new float[] {p[0] + (k[6] - k[0] - k[2] / 2f) * s * flip, p[1] - (k[7] - k[1] - k[3] / 2f) * s};
        }

        private void drawShark(Batch batch, int i, float shadow) {
            float[] p = spot(i), k = WALL_SHARKS[i];
            float wiggle = time >= LEAVE ? MathUtils.sin(time * 18f + i) * 4f : 0f;
            boolean flip = facing(i) != k[8];
            drawPart(this, batch, sharks[i], p[0], p[1], size(i), flip, wiggle, shadow, 1f);
            float[] eye = head(i);
            float rage = progress(0.1f, LEAVE);
            glow(batch, eye[0], eye[1], 70f, BLOOD, 0.9f * rage);
            glow(batch, eye[0], eye[1], 20f, WHITE, 0.8f * rage);
            if (time >= LEAVE) {                                        // le sillage de bulles
                additive(batch);
                batch.setColor(0.7f, 0.9f, 1f, 0.35f);
                for (int b = 1; b <= 4; b++) {
                    float bx = p[0] - facing(i) * (k[2] * decorScale() * size(i) * 0.5f + b * 26f), by = p[1] + MathUtils.sin(time * 9f + b) * 10f + b * 6f;
                    batch.draw(ring, bx - 6f - b, by - 6f - b, 12f + 2f * b, 12f + 2f * b);
                }
                normal(batch);
            }
        }

        @Override
        protected void back(Batch batch) {
            if (time >= LEAVE) for (int i = 0; i < 2; i++) patchWall(this, batch, i, holes[i]);   // ils ont quitté le mur
            float cloud = progress(0.2f, CLIMAX_PHASE);
            for (int i = 0; i < 7; i++) {                                // l'eau rougit
                glow(batch, worldWidth() * (0.1f + i * 0.13f), worldHeight() * (0.5f + 0.1f * MathUtils.sin(time + i)), 700f, RED, 0.25f * cloud);
            }
            for (int i = 0; i < 2; i++) {
                if (time < LEAVE || spot(i)[2] > 0f) drawShark(batch, i, time < LEAVE ? 0f : 0.25f * spot(i)[2]);
            }
            float rage = progress(0.4f, CLIMAX_PHASE);
            poseX = (random.nextFloat() - 0.5f) * 8f * rage;
            float snap = gesture(CLIMAX_PHASE - 0.12f, CLIMAX_PHASE + 0.12f);   // il claque des mâchoires
            poseStretch = 0.06f * snap;
            poseTintColor.set(RED);
            poseTint = 0.15f * rage + 0.2f * progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.4f);
        }

        @Override
        protected void front(Batch batch) {
            for (int i = 0; i < 2; i++) if (time >= LEAVE && spot(i)[2] <= 0f) drawShark(batch, i, 0f);
            eyes(batch, progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.2f), RED, new float[] {11.5f, 14f}, new float[] {23.5f, 14f});
            flash(batch, RED, CLIMAX_PHASE, 0.3f, 0.45f);
        }
    }

    /** La porte ronde du coffre-fort peinte sur le mur des Coffres : son centre et son rayon (pixels du décor, contour compris). */
    static final float VAULT_X = 60.5f, VAULT_Y = 80.5f, VAULT_R = 51.5f;

    /**
     * « La banque du Requin fait faillite » : la grande porte du coffre-fort du décor tourne sa roue et s'ouvre…
     * sur un coffre vide, d'où ne sortent que des mites ; son monocle lui saute de l'oeil ; sa pile de pièces
     * s'écroule pièce à pièce ; il s'affaisse et coule dans le noir.
     */
    static final class SharkDeath extends BossScene {
        private static final float OPEN = 1.2f, COINS = CLIMAX_DEATH + 0.35f;
        private final TextureRegion door, inside, moth, bare;
        private final Piece monocle, pile;
        private boolean popped;
        private float monocleX, monocleY;

        SharkDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.REQUIN_BANQUIER, 0);
            int r = (int) Math.ceil(VAULT_R);
            door = decorCut(this, (int) (VAULT_X - r), (int) (VAULT_Y - r), 2 * r, 2 * r, (int) VAULT_X, (int) VAULT_Y,
                (x, y, cr, cg, cb) -> (x + 0.5f - VAULT_X) * (x + 0.5f - VAULT_X) + (y + 0.5f - VAULT_Y) * (y + 0.5f - VAULT_Y) <= VAULT_R * VAULT_R);
            inside = vault(2 * r);
            moth = art("wo", new Color[] {hex("d8d0c0"), hex("5a5048")}, MOTH);
            int[][] full = PlacesPortraits.pixels(EnemyKind.REQUIN_BANQUIER);
            monocle = lift(full, PlacesPortraits.without(EnemyKind.REQUIN_BANQUIER, PlacesPortraits.Prop.SHARK_MONOCLE));
            pile = lift(full, PlacesPortraits.without(EnemyKind.REQUIN_BANQUIER, PlacesPortraits.Prop.SHARK_COINS));
            bare = pixels(PlacesPortraits.without(EnemyKind.REQUIN_BANQUIER, PlacesPortraits.Prop.SHARK_MONOCLE, PlacesPortraits.Prop.SHARK_COINS));
        }

        /** L'intérieur du coffre : un trou rond, sombre, aux étagères vides. */
        private TextureRegion vault(int size) {
            Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
            pixmap.setBlending(Pixmap.Blending.None);
            float c = size / 2f, r = VAULT_R - 1f;
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    float d = (float) Math.sqrt((x + 0.5f - c) * (x + 0.5f - c) + (y + 0.5f - c) * (y + 0.5f - c));
                    if (d > r) continue;
                    int color = d > r - 4f ? 0x4a5a66ff : d > r - 6f ? 0x1a2028ff : 0x0c1014ff;
                    if (d < r - 8f && (y == size / 2 - 14 || y == size / 2 + 6 || y == size / 2 + 26)) color = 0x3a2a1eff;   // les étagères, vides
                    pixmap.drawPixel(x, y, color);
                }
            }
            return region(pixmap, false);
        }

        @Override protected void restart() { popped = false; }

        private float sinkY() { return worldHeight() * 0.45f * Interpolation.pow2In.apply(progress(CLIMAX_DEATH + 0.6f, 5.0f)); }

        @Override
        protected void step(float delta) {
            if (at(OPEN + 0.5f)) rumble(0.3f, 6f);
            if (at(CLIMAX_DEATH)) {                                     // vide : son monocle saute
                rumble(0.4f, 8f);
                sparks(decorX(VAULT_X), decorY(VAULT_Y), 16, hex("c8c0a8"), 300f, 10f);
                for (int i = 0; i < 4; i++) {                           // des mites s'envolent du coffre vide
                    Particle m = place(moth, decorX(VAULT_X) + (random.nextFloat() - 0.5f) * 80f, decorY(VAULT_Y) + (random.nextFloat() - 0.5f) * 80f,
                        120f + 160f * random.nextFloat(), 60f + 120f * random.nextFloat(), decorScale() * 2.2f, 3.2f);
                    m.drag = 0.9f;
                }
            }
            if (at(COINS) && pile != null) {                            // sa pile de pièces s'écroule, pièce à pièce
                float g = bossScale();
                for (int row = 0; row < pile.height; row += 2) {
                    Particle c = place(new TextureRegion(pile.region, 0, row, pile.width, Math.min(2, pile.height - row)),
                        portraitX(pile.left + pile.width / 2f), portraitY(pile.top + row + 1f), 120f + 90f * row / 2f + random.nextFloat() * 60f,
                        150f + 40f * row, g, 2.4f);
                    c.gravity = 1500f;
                    c.spin = -200f - 300f * random.nextFloat();
                }
                rumble(0.2f, 5f);
            }
        }

        @Override
        protected void back(Batch batch) {
            // la porte du coffre-fort : sa roue tourne, puis elle pivote sur ses gonds (à gauche)
            float ds = decorScale(), size = door == null ? 0f : door.getRegionWidth() * ds;
            float cx = decorX(VAULT_X), cy = decorY(VAULT_Y), hinge = cx - size / 2f;
            float swing = Interpolation.pow2Out.apply(progress(OPEN + 0.5f, CLIMAX_DEATH));
            if (swing > 0f && inside != null) {
                batch.setColor(litLikeDecor(WHITE, tmp));
                batch.draw(inside, cx - size / 2f, cy - size / 2f, size, size);
            }
            if (door != null) {
                float open = MathUtils.cosDeg(105f * swing);
                float light = open < 0f ? 0.55f : 1f - 0.3f * swing;
                Color lit = litLikeDecor(WHITE, tmp);
                batch.setColor(lit.r * light, lit.g * light, lit.b * light, 1f);
                batch.draw(door, hinge, cy - size / 2f, 0f, size / 2f, size, size, open, 1f + 0.08f * Math.abs(MathUtils.sinDeg(105f * swing)), 0f);
                if (time < OPEN + 0.5f) {                               // la roue dorée qui tourne
                    float spin = 360f * Interpolation.pow2.apply(progress(0.3f, OPEN + 0.5f));
                    for (int spoke = 0; spoke < 3; spoke++) {
                        float a = spin + spoke * 60f, len = 22f * ds;
                        line(batch, litLikeDecor(GOLD, tmp), 1f, cx - MathUtils.cosDeg(a) * len, cy - MathUtils.sinDeg(a) * len,
                            cx + MathUtils.cosDeg(a) * len, cy + MathUtils.sinDeg(a) * len, 2f * ds);
                    }
                }
            }
            float slump = progress(CLIMAX_DEATH + 0.4f, CLIMAX_DEATH + 1.2f);
            poseY = -sinkY() - 14f * slump;
            poseTilt = -9f * slump;
            poseShadow = 0.75f * progress(CLIMAX_DEATH, 4.8f);
            if (time >= CLIMAX_DEATH && !popped) {                      // le monocle saute, la pile va tomber : on les retire du portrait
                popped = true;
                if (monocle != null) { monocleX = pieceX(monocle); monocleY = pieceY(monocle); }
                wear(bare);
            }
        }

        @Override
        protected void front(Batch batch) {
            if (!popped) {
                attached(batch, monocle, 1f);
                attached(batch, pile, 1f);
            } else if (time < COINS && pile != null) {
                draw(batch, pile.region, portraitX(pile.left + pile.width / 2f), portraitY(pile.top + pile.height / 2f), bossScale(),
                    poseTilt, tmp.set(1f - poseShadow, 1f - poseShadow, 1f - poseShadow, 1f), 1f);
            }
            if (popped && monocle != null) {                            // son monocle saute, retombe et rebondit au sol
                float k = progress(CLIMAX_DEATH, CLIMAX_DEATH + 1.3f);
                float x = monocleX + worldWidth() * 0.12f * k;
                float arc = 160f * MathUtils.sin(Math.min(1f, k * 1.6f) * MathUtils.PI) * (1f - Math.min(1f, k * 1.6f) * 0.5f);
                float fall = Interpolation.bounceOut.apply(k);
                float y = MathUtils.lerp(monocleY, groundY() + 10f, fall) + (k < 0.6f ? arc : 0f);
                draw(batch, monocle.region, x, y, bossScale(), -540f * Interpolation.pow2Out.apply(k), WHITE, 1f);
                glow(batch, x, y, 90f, WHITE, 0.4f * (1f - k));
            }
            fill(batch, Color.BLACK, 0.55f * progress(3.6f, 5.0f));     // les profondeurs
            flash(batch, WHITE, CLIMAX_DEATH, 0.2f, 0.3f);
        }
    }

    // -------------------------------------------------------------------------
    // Kraken
    // -------------------------------------------------------------------------

    /** Le lustre du salon VIP (décor) : le centre de sa couronne de bougies et le bas de ses pampilles (pixels du décor). */
    static final float CHANDELIER_X = 180f, CHANDELIER_Y = 30f, CHANDELIER_BOTTOM = 49f;
    static final Color INK = hex("12061e");

    /**
     * « Nuage d'encre : Aveuglement ! » : il crache son encre, qui jaillit de lui en grosses volutes et envahit le
     * salon VIP jusqu'à éteindre le lustre ; dans le noir, il ne reste que ses yeux qui s'allument, et ses bras
     * qui surgissent de l'encre par tous les bords.
     */
    static final class KrakenPhase extends BossScene {
        private static final Color PURPLE = hex("b05aff");
        private static final float[][] EDGES = {{-0.05f, 0.2f}, {-0.05f, 0.7f}, {1.05f, 0.25f}, {1.05f, 0.75f}, {0.2f, 1.08f},
            {0.8f, 1.08f}, {0.12f, -0.08f}, {0.88f, -0.08f}};

        KrakenPhase(CutsceneKit kit) { super(kit, Moment.PHASE, EnemyKind.KRAKEN, 2); }

        private float ink() { return progress(0.3f, 1.5f); }

        @Override
        protected void step(float delta) {
            if (at(0.3f)) rumble(0.4f, 6f);
            if (at(CLIMAX_PHASE)) { rumble(0.8f, 14f); sparks(bossX(), bossY(), 60, PURPLE, 900f, 20f); }
        }

        @Override
        protected void back(Batch batch) {
            float light = 1f - progress(0.7f, 1.5f);                    // le lustre brille, puis l'encre l'éteint
            glow(batch, decorX(CHANDELIER_X), decorY(CHANDELIER_Y), 700f, GOLD, 0.45f * light);
            for (int i = 0; i < 7; i++) {
                glow(batch, decorX(CHANDELIER_X - 21f + i * 7f), decorY(CHANDELIER_BOTTOM - 4f), 50f, HOT, 0.6f * light * pulse(9f + i, 0.5f));
            }
            poseShadow = 0.5f * progress(0.6f, 1.6f);
            poseScale = 1f + 0.04f * gesture(0.15f, 0.55f);             // il se gonfle et crache
        }

        @Override
        protected void front(Batch batch) {
            // l'encre jaillit de lui en volutes qui grossissent et envahissent tout
            float ink = ink();
            for (int i = 0; i < 14; i++) {
                float start = 0.3f + i * 0.06f, k = Interpolation.pow2Out.apply(progress(start, start + 1.1f));
                if (k <= 0f) continue;
                float a = i * 137.5f, reach = worldWidth() * (0.25f + 0.25f * ((i * 7) % 3));
                float x = portraitX(17f) + MathUtils.cosDeg(a) * reach * k, y = portraitY(30f) + MathUtils.sinDeg(a) * reach * 0.6f * k;
                float size = 200f + 900f * k;
                batch.setColor(INK.r, INK.g, INK.b, 0.85f);
                batch.draw(soft, x - size / 2f, y - size / 2f, size, size);
            }
            fill(batch, INK, 0.6f * ink);
            eyes(batch, progress(1.2f, 1.6f), PURPLE, new float[] {14f, 21f}, new float[] {22f, 21f});
            // ses bras surgissent de l'encre, des bords vers lui
            for (int i = 0; i < EDGES.length; i++) {
                float out = Interpolation.pow2Out.apply(progress(1.4f + i * 0.08f, CLIMAX_PHASE + i * 0.04f));
                if (out <= 0f) continue;
                float x0 = EDGES[i][0] * worldWidth(), y0 = EDGES[i][1] * worldHeight();
                float x1 = MathUtils.lerp(x0, bossX() + (x0 - bossX()) * 0.3f, out), y1 = MathUtils.lerp(y0, bossY() + (y0 - bossY()) * 0.3f, out);
                arm(this, batch, x0, y0, x1, y1, 7f * bossScale(), 50f, i * 1.3f, 0.75f);
            }
            flash(batch, PURPLE, CLIMAX_PHASE, 0.3f, 0.4f);
        }

        @Override protected float decorLight() { return 0.62f - 0.4f * ink(); }
    }

    /**
     * « Le Kraken retourne aux abysses » : ses huit bras tiennent les rideaux rouges du salon VIP et les lâchent un à
     * un (les rideaux ondulent) ; le plancher s'ouvre sur un gouffre noir sous lui et il s'y enfonce ; sa vraie
     * couronne reste à flotter, puis remonte doucement vers le lustre, seule.
     */
    static final class KrakenDeath extends BossScene {
        private static final float SINK = CLIMAX_DEATH;
        private final TextureRegion bare;
        private final TextureRegion[] curtains = new TextureRegion[2];
        private final Piece crown;
        private boolean off;
        private float crownX, crownY;

        KrakenDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.KRAKEN, 0);
            int[][] rest = PlacesPortraits.without(EnemyKind.KRAKEN, PlacesPortraits.Prop.KRAKEN_CROWN);
            crown = lift(PlacesPortraits.pixels(EnemyKind.KRAKEN), rest);
            bare = pixels(rest);
            curtains[0] = decorPart(this, 0, 0, 61, 170);
            curtains[1] = decorPart(this, 299, 0, 61, 170);
        }

        @Override protected void restart() { off = false; }

        private float letGo(int i) { return 0.4f + i * 0.25f; }

        /** @return où le bras {@code i} tient un rideau : quatre prises à gauche, quatre à droite. */
        private float gripX(int i) { return decorX(i < 4 ? 44f : 316f); }
        private float gripY(int i) { return decorY(38f + (i % 4) * 30f); }

        private float sink() { return Interpolation.pow2In.apply(progress(SINK, 4.4f)); }

        @Override
        protected void step(float delta) {
            for (int i = 0; i < 8; i++) if (at(letGo(i))) { rumble(0.12f, 5f); sparks(gripX(i), gripY(i), 10, hex("e8a0a0"), 300f, 12f); }
            if (at(SINK)) { rumble(0.9f, 10f); sparks(bossX(), bossBottom(), 40, hex("5a4a6a"), 600f, 16f); }
        }

        /** @return le balancement du rideau {@code side} (0 à gauche) : chaque bras lâché le fait onduler. */
        private float sway(int side) {
            float sway = 0f;
            for (int i = side * 4; i < side * 4 + 4; i++) {
                float t = time - letGo(i);
                if (t > 0f) sway += MathUtils.sin(t * 11f) * (float) Math.exp(-t * 2.5f) * 10f;
            }
            return side == 0 ? -Math.abs(sway) : Math.abs(sway);
        }

        @Override
        protected void back(Batch batch) {
            for (int side = 0; side < 2; side++) {                      // les rideaux du salon, qui ondulent quand il les lâche
                TextureRegion curtain = curtains[side];
                if (curtain == null) continue;
                float left = side == 0 ? 0f : 299f;
                batch.setColor(litLikeDecor(WHITE, tmp));
                batch.draw(curtain, decorX(left) + sway(side), decorY(170f), curtain.getRegionWidth() * decorScale(), 170f * decorScale());
            }
            // le gouffre qui s'ouvre sous lui
            float hole = Interpolation.pow2Out.apply(progress(SINK - 0.3f, SINK + 0.4f)) * (1f - progress(4.6f, 5.4f));
            if (hole > 0f) {                                            // un tourbillon noir qui l'aspire vers le fond
                float w = bossWidth() * 2.2f * hole, h = 170f * hole, y = bossBottom() + 10f;
                batch.setColor(0f, 0f, 0f, 0.85f);
                batch.draw(soft, bossX() - w * 0.6f, y - h * 0.6f, w * 1.2f, h * 1.2f);
                batch.setColor(0.03f, 0.0f, 0.06f, 1f);
                batch.draw(soft, bossX() - w * 0.4f, y - h * 0.4f, w * 0.8f, h * 0.8f);
                additive(batch);
                for (int r = 0; r < 4; r++) {
                    float k = ((time * 0.8f + r / 4f) % 1f), rw = w * (1f - 0.75f * k), rh = h * 0.7f * (1f - 0.75f * k);
                    float spin = (time * 260f + r * 90f) % 360f;
                    batch.setColor(0.55f, 0.25f, 0.85f, 0.45f * hole * (0.3f + 0.7f * k));
                    batch.draw(ring, bossX() - rw / 2f, y - rh / 2f, rw / 2f, rh / 2f, rw, rh, 1f, 1f, spin * 0.03f);
                }
                normal(batch);
            }
            // ses bras, des rideaux vers lui
            float light = 1f - 0.5f * progress(SINK, 4.4f);
            for (int i = 0; i < 8; i++) {
                float release = Interpolation.pow2Out.apply(progress(letGo(i), letGo(i) + 0.45f));
                if (release >= 1f) continue;
                float x0 = portraitX(i < 4 ? 9f : 25f), y0 = portraitY(30f - (i % 4) * 2f);
                float x1 = MathUtils.lerp(gripX(i) + sway(i < 4 ? 0 : 1), x0, release), y1 = MathUtils.lerp(gripY(i), y0 - 20f, release);
                arm(this, batch, x0, y0, x1, y1, 5f * bossScale(), 20f + 40f * release, i * 1.1f, light);
            }
            poseSink = 0.98f * sink();
            poseShadow = 0.25f + 0.5f * progress(SINK, 4.4f);
            if (time >= SINK && !off && crown != null) {                // sa couronne ne le suit pas : on la retire du portrait
                off = true;
                crownX = pieceX(crown); crownY = pieceY(crown);
                wear(bare);
            }
        }

        @Override
        protected void front(Batch batch) {
            if (crown == null) return;
            if (!off) { attached(batch, crown, 1f); return; }
            float k = progress(SINK, 5.2f);                             // elle flotte, puis remonte seule vers le lustre
            float rise = Interpolation.pow2.apply(progress(SINK + 0.8f, 5.2f));
            float x = crownX + MathUtils.sin(time * 2.4f) * 24f * k;
            float y = MathUtils.lerp(crownY, decorY(CHANDELIER_BOTTOM) - 70f, rise);
            glow(batch, x, y, 220f, GOLD, 0.25f + 0.25f * k);
            draw(batch, crown.region, x, y, bossScale(), MathUtils.sin(time * 3f) * 10f * k, WHITE, 1f);
        }

        @Override protected float decorLight() { return 0.62f - 0.2f * progress(SINK, 4.4f); }
    }
}
