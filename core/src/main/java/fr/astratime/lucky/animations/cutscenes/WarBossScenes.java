package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

import static fr.astratime.lucky.assets.Palette.hex;

/**
 * Les scènes de phase et de mort du Général Va-Tout, boss du chapitre « La Guerre des Tables » :
 * ses lieutenants arrivent (phase 2), il fait Va-tout (phase 3), il hisse le drapeau blanc (mort).
 */
final class WarBossScenes {

    private WarBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind, int phase) {
        if (kind != EnemyKind.GENERAL) return null;
        return phase == 2 ? new GeneralLieutenants(kit) : new GeneralAllOut(kit);
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return kind == EnemyKind.GENERAL ? new GeneralDeath(kit) : null;
    }

    static final Color POWDER = hex("8a7e76"), FLAG = hex("f6f0e2");

    /**
     * Une scène où le Général perd ce qu'il porte : ses deux piles de jetons (colonnes 0 à 4 et 29 à 33 de son
     * portrait 34 x 42, lignes 32 à 41) et son bicorne (lignes 0 à 10). Ce sont les vrais morceaux de son portrait :
     * on les y découpe, ils ne restent pas dessinés sur lui. Sa taille reste celle du portrait entier.
     */
    abstract static class GeneralScene extends BossScene {
        static final int FULL_W = 34, FULL_H = 42, HAT_ROWS = 11, PILE_COLS = 5, PILE_TOP = 32, PILE_ROWS = 10;
        protected final TextureRegion full, hat, leftPile, rightPile;
        private final TextureRegion leftEdge, rightEdge;
        private TextureRegion leftStrip, rightStrip;
        protected boolean hatOff, pilesOff;

        GeneralScene(CutsceneKit kit, Moment moment, int phase) {
            super(kit, moment, EnemyKind.GENERAL, phase);
            full = new TextureRegion(boss);
            hat = new TextureRegion(full, 0, 0, FULL_W, HAT_ROWS);
            leftPile = new TextureRegion(full, 0, PILE_TOP, PILE_COLS, PILE_ROWS);
            rightPile = new TextureRegion(full, FULL_W - PILE_COLS, PILE_TOP, PILE_COLS, PILE_ROWS);
            leftEdge = new TextureRegion(full, 3, PILE_TOP - 1, 2, 1);           // son habit, juste au-dessus des piles
            rightEdge = new TextureRegion(full, FULL_W - PILE_COLS, PILE_TOP - 1, 3, 1);
        }

        @Override protected float bossScale()  { return worldHeight() * 0.42f / FULL_H; }
        @Override protected float bossWidth()  { return FULL_W * bossScale(); }
        @Override protected float bossHeight() { return FULL_H * bossScale(); }

        private int cropX() { return pilesOff ? PILE_COLS : 0; }
        protected int cropY() { return hatOff ? HAT_ROWS : 0; }

        /** Ses piles quittent ses mains : on les retire du portrait. */
        protected void takePiles() { pilesOff = true; crop(); }

        /** Son bicorne quitte sa tête : on le retire du portrait. */
        protected void takeHat() { hatOff = true; crop(); }

        private void crop() {
            boss.setRegion(full, cropX(), cropY(), FULL_W - 2 * cropX(), FULL_H - cropY());
            int rows = PILE_TOP - 1 - cropY();                                // le bout de ses épaulettes, à côté des piles
            leftStrip = new TextureRegion(full, 0, cropY(), PILE_COLS, rows);
            rightStrip = new TextureRegion(full, FULL_W - PILE_COLS, cropY(), PILE_COLS, rows);
        }

        /** @return l'abscisse à l'écran du pixel {@code px} du portrait entier. */
        protected float fullX(float px) { return bossX() + poseX + (px - FULL_W / 2f) * bossScale() * poseScale * (1f - poseStretch * 0.5f); }
        /** @return l'ordonnée à l'écran du pixel {@code py} du portrait entier, compté depuis le haut. */
        protected float fullY(float py) { return bossBottom() + poseY + (FULL_H - py) * bossScale() * poseScale * (1f + poseStretch); }

        /**
         * Dessine {@code region} sur lui, son coin haut-gauche au pixel ({@code left}, {@code top}) du portrait
         * entier : elle bascule, s'étire, pâlit et se teinte avec lui.
         */
        protected void onHim(Batch batch, TextureRegion region, float left, float top) {
            if (!showBoss || region == null) return;
            float scale = bossScale() * poseScale, sx = 1f - poseStretch * 0.5f, sy = 1f + poseStretch;
            float footX = bossX() + poseX, footY = bossBottom() + poseY;
            float w = region.getRegionWidth() * scale * sx, h = region.getRegionHeight() * scale * sy;
            float rx = footX + (left - FULL_W / 2f) * scale * sx, ry = footY + (FULL_H - top) * scale * sy - h;
            float light = 1f - poseShadow;
            batch.setColor(light, light, light, poseAlpha);
            batch.draw(region, rx, ry, footX - rx, footY - ry, w, h, 1f, 1f, poseTilt);
            if (poseTint > 0f) {
                additive(batch);
                batch.setColor(poseTintColor.r, poseTintColor.g, poseTintColor.b, poseTint * poseAlpha);
                batch.draw(region, rx, ry, footX - rx, footY - ry, w, h, 1f, 1f, poseTilt);
                normal(batch);
            }
        }

        /** Ce qui reste de lui sur les côtés une fois ses piles parties (le haut de ses épaulettes). */
        protected void drawSides(Batch batch) {
            if (!pilesOff) return;
            onHim(batch, leftStrip, 0, cropY());
            onHim(batch, rightStrip, FULL_W - PILE_COLS, cropY());
            onHim(batch, leftEdge, 3, PILE_TOP - 1);
            onHim(batch, rightEdge, FULL_W - PILE_COLS, PILE_TOP - 1);
        }
    }

    // -------------------------------------------------------------------------
    // Phase 2 : ses lieutenants arrivent
    // -------------------------------------------------------------------------

    /**
     * « Ses lieutenants arrivent » : un coup de clairon, l'Espionne glisse depuis la gauche et le Mercenaire depuis la
     * droite ; ils se postent à ses côtés, leur symbole (Espionnage, Contrat) s'allume au-dessus d'eux.
     */
    static final class GeneralLieutenants extends BossScene {
        private final TextureRegion spy, mercenary, spyIcon, contractIcon;

        GeneralLieutenants(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.GENERAL, 2);
            spy = kit.enemies() == null ? null : kit.portrait(EnemyKind.ESPIONNE);
            mercenary = kit.enemies() == null ? null : kit.portrait(EnemyKind.MERCENAIRE);
            spyIcon = symbol(EnemySymbol.SPY);
            contractIcon = symbol(EnemySymbol.CONTRACT);
        }

        private float side(int s) {
            float arrive = Interpolation.pow2Out.apply(progress(0.4f + (s > 0 ? 0.3f : 0f), 1.5f + (s > 0 ? 0.3f : 0f)));
            return bossX() + s * MathUtils.lerp(worldWidth() * 0.7f, bossWidth() * 0.95f, arrive);
        }

        @Override
        protected void step(float delta) {
            if (at(0.3f)) rumble(0.15f, 4f);
            if (at(1.5f)) sparks(side(-1), bossY(), 20, POWDER, 300f, 10f);
            if (at(1.8f)) sparks(side(1), bossY(), 20, POWDER, 300f, 10f);
            if (at(CLIMAX_PHASE)) {
                rumble(0.5f, 10f);
                sparks(bossX(), bossY() + bossHeight() * 0.3f, 50, GOLD, 700f, 12f);
            }
        }

        @Override
        protected void back(Batch batch) {
            fill(batch, Color.BLACK, 0.3f * progress(0.2f, 1.0f));
            poseY = 14f * gesture(1.6f, CLIMAX_PHASE + 0.4f);
            float scale = bossScale() * 0.8f;
            for (int s = -1; s <= 1; s += 2) {
                TextureRegion who = s < 0 ? spy : mercenary;
                if (who == null) continue;
                float x = side(s);
                glow(batch, x, bossBottom() + who.getRegionHeight() * scale * 0.5f, 160f, s < 0 ? hex("7ab8ff") : GOLD, 0.3f);
                draw(batch, who, x, bossBottom() + who.getRegionHeight() * scale / 2f, scale, 0f, WHITE, 1f);
            }
        }

        @Override
        protected void front(Batch batch) {
            float icons = progress(CLIMAX_PHASE - 0.2f, CLIMAX_PHASE + 0.2f);
            for (int s = -1; s <= 1; s += 2) {
                TextureRegion icon = s < 0 ? spyIcon : contractIcon;
                if (icon == null || icons <= 0f) continue;
                float x = side(s), y = bossBottom() + bossHeight() * 1.05f + 10f * pulse(4f, 0.5f);
                glow(batch, x, y, 140f, GOLD, 0.4f * icons);
                draw(batch, icon, x, y, iconScale(0.09f), 0f, WHITE, icons);
            }
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.3f);
        }
    }

    // -------------------------------------------------------------------------
    // Phase 3 : Va-tout
    // -------------------------------------------------------------------------

    /**
     * « Va-tout » : il jette son bouclier, qui tourne et se brise au sol ; les deux piles de jetons qu'il tient
     * quittent ses mains et glissent devant lui, où elles se rejoignent ; une lueur rouge le couvre, il se gonfle,
     * prêt à tout miser.
     */
    static final class GeneralAllOut extends GeneralScene {
        /** Ses piles quittent ses mains, puis se rejoignent devant lui. */
        static final float PUSH = 1.4f;
        private final TextureRegion shield;
        private float handLeftX, handRightX, handY;

        GeneralAllOut(CutsceneKit kit) {
            super(kit, Moment.PHASE, 3);
            shield = symbol(EnemySymbol.SHIELD);
        }

        private float throwT() { return progress(0.4f, 1.3f); }

        /** @return où est la pile {@code side} (-1 gauche, 1 droite) : de sa main jusque devant lui. */
        private float pileX(int side) {
            float push = Interpolation.pow2In.apply(progress(PUSH, CLIMAX_PHASE));
            float to = bossX() + side * PILE_COLS * bossScale() * 0.55f;
            return MathUtils.lerp(side < 0 ? handLeftX : handRightX, to, push);
        }

        @Override
        protected void step(float delta) {
            if (at(1.3f)) {                                                  // le bouclier se brise
                rumble(0.3f, 8f);
                sparks(bossX() + worldWidth() * 0.32f, groundY(), 40, hex("b0b8c8"), 600f, 10f);
            }
            if (at(PUSH)) {                                                  // ses vraies piles quittent ses mains
                handLeftX = fullX(PILE_COLS / 2f);
                handRightX = fullX(FULL_W - PILE_COLS / 2f);
                handY = fullY(PILE_TOP + PILE_ROWS / 2f);
                takePiles();
            }
            if (at(CLIMAX_PHASE)) {
                rumble(0.7f, 14f);
                sparks(bossX(), handY, 80, GOLD, 900f, 14f);
                sparks(bossX(), bossY(), 40, BLOOD, 700f, 12f);
            }
        }

        @Override
        protected void back(Batch batch) {
            fill(batch, Color.BLACK, 0.45f * progress(0.2f, 1.0f));
            glow(batch, bossX(), bossY(), bossHeight() * 1.6f, BLOOD, 0.35f * progress(1.4f, CLIMAX_PHASE) * pulse(5f, 0.6f));
            poseTint = 0.4f * progress(1.4f, CLIMAX_PHASE);
            poseTintColor.set(BLOOD);
            poseStretch = 0.08f * progress(1.6f, CLIMAX_PHASE);
            poseTilt = -8f * gesture(0.3f, 0.9f);
        }

        @Override
        protected void front(Batch batch) {
            drawSides(batch);
            if (shield != null && time < 1.3f) {                              // le bouclier jeté, qui tournoie
                float t = throwT();
                float x = MathUtils.lerp(bossX(), bossX() + worldWidth() * 0.32f, t);
                float y = MathUtils.lerp(bossY(), groundY(), t) + MathUtils.sin(t * MathUtils.PI) * worldHeight() * 0.2f;
                draw(batch, shield, x, y, iconScale(0.12f), time * 720f, WHITE, 1f);
            }
            if (pilesOff) {                                                   // ses deux piles, poussées au milieu
                for (int s = -1; s <= 1; s += 2) draw(batch, s < 0 ? leftPile : rightPile, pileX(s), handY, bossScale(), 0f, WHITE, 1f);
            }
            if (time >= CLIMAX_PHASE) glow(batch, bossX(), handY, 300f, GOLD, 0.5f * (1f - progress(CLIMAX_PHASE, 3.6f)));
            flash(batch, BLOOD, CLIMAX_PHASE, 0.3f, 0.4f);
        }
    }

    // -------------------------------------------------------------------------
    // Mort : le drapeau blanc
    // -------------------------------------------------------------------------

    /**
     * Il a tout misé, il a tout perdu : les deux piles de jetons qu'il tient glissent de ses mains et roulent au loin,
     * son bicorne s'envole, un drapeau blanc monte à côté de lui ; il s'efface dans la fumée de poudre. Les jetons et
     * le bicorne sont ceux de son portrait : on les y découpe, ils ne restent pas dessinés sur lui.
     */
    static final class GeneralDeath extends GeneralScene {
        private static final int CHIPS_PER_PILE = PILE_ROWS / 2;
        /** Sous le bicorne : ses cheveux gris, sur le haut du crâne (colonnes 11 à 23, au-dessus de la ligne 11). */
        private static final int SCALP_LEFT = 11;
        static final float CRUMBLE = 0.5f, HAT_OFF = 0.8f;
        private final TextureRegion scalp;
        private final TextureRegion[] pileChips = new TextureRegion[CHIPS_PER_PILE * 2];

        GeneralDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, 0);
            for (int k = 0; k < CHIPS_PER_PILE; k++) {                        // ses vrais jetons, un par un
                pileChips[k] = new TextureRegion(full, 0, PILE_TOP + 2 * k, PILE_COLS, 2);
                pileChips[CHIPS_PER_PILE + k] = new TextureRegion(full, FULL_W - PILE_COLS, PILE_TOP + 2 * k, PILE_COLS, 2);
            }
            scalp = art("oi", new Color[] {hex("1a0f0f"), hex("a8a4a0")},
                "...ooooooo...",
                ".ooiiiiiiioo.",
                "oiiiiiiiiiiio");
        }

        private float flagX()   { return bossX() + bossWidth() * 0.85f; }
        private float flagTop() { return groundY() + MathUtils.lerp(0f, bossHeight() * 1.2f, Interpolation.pow2Out.apply(progress(1.0f, CLIMAX_DEATH))); }

        @Override
        protected void step(float delta) {
            if (at(CRUMBLE)) {                                                // ses piles glissent de ses mains
                rumble(0.4f, 8f);
                for (int i = 0; i < pileChips.length; i++) {
                    int k = i % CHIPS_PER_PILE, side = i < CHIPS_PER_PILE ? -1 : 1;
                    float px = side < 0 ? PILE_COLS / 2f : FULL_W - PILE_COLS / 2f;
                    Cutscene.Particle falling = place(pileChips[i], fullX(px), fullY(PILE_TOP + 2 * k + 1f),
                        side * (250f + 350f * random.nextFloat()), 150f + 250f * random.nextFloat(), bossScale(), 2.2f);
                    falling.gravity = 900f;
                    falling.spin = 360f * (random.nextFloat() - 0.5f) * 4f;
                }
                takePiles();
            }
            if (at(HAT_OFF)) {                                                // son propre bicorne s'envole
                rumble(0.15f, 5f);
                Cutscene.Particle flying = place(hat, fullX(FULL_W / 2f), fullY(HAT_ROWS / 2f), -220f, 950f, bossScale(), 2.2f);
                flying.gravity = 1300f;
                flying.spin = -300f;
                takeHat();
            }
            if (time > 1.2f && time < 3.6f && random.nextFloat() < delta * 20f) {
                sparks(bossX() + (random.nextFloat() - 0.5f) * bossWidth(), bossBottom() + bossHeight() * random.nextFloat(), 3,
                    POWDER, 150f, 14f);
            }
            if (at(CLIMAX_DEATH)) {
                rumble(0.5f, 8f);
                spray(coin, bossX(), bossY(), 20, 40f, 140f, 800f, coinScale(0.04f), 1400f, 1.5f);
            }
        }

        @Override
        protected void back(Batch batch) {
            fill(batch, POWDER, 0.2f * progress(1.0f, 3.0f));
            poseTilt = -6f * progress(0.3f, 1.2f);
            poseTint = 0.5f * progress(1.6f, 3.0f);
            poseTintColor.set(POWDER);
            poseAlpha = 1f - progress(2.4f, 3.8f);
            showBoss = time < 3.9f;
        }

        @Override
        protected void front(Batch batch) {
            drawSides(batch);
            if (hatOff) onHim(batch, scalp, SCALP_LEFT, HAT_ROWS - scalp.getRegionHeight());   // tête nue
            float x = flagX(), top = flagTop();                               // le drapeau blanc qui monte
            rect(batch, hex("4a2e1a"), 1f, x - 3f, groundY(), 6f, top - groundY());
            float wave = MathUtils.sin(time * 8f) * 6f;
            float flagW = bossWidth() * 0.5f, flagH = bossHeight() * 0.22f;
            if (top > groundY() + flagH) {
                for (int k = 0; k < 6; k++) {
                    float w = flagW / 6f;
                    rect(batch, k % 2 == 0 ? FLAG : hex("d8d0c4"), 1f, x + 3f + k * w, top - flagH + MathUtils.sin(time * 8f + k) * 4f + wave * 0.2f,
                        w + 1f, flagH);
                }
            }
            flash(batch, FLAG, CLIMAX_DEATH, 0.3f, 0.35f);
        }
    }
}
