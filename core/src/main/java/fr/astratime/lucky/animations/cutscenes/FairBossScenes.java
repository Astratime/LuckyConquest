package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.assets.FairDesertPortraits;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;

/** Les scènes de phase et de mort des quatre rois de la Fête foraine (choix d'Astra, 2026-10-24). */
final class FairBossScenes {

    private FairBossScenes() {}

    static BossScene phase(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case MONSIEUR_LOYAL -> new LoyalPhase(kit);
            case CONDUCTEUR -> new ConductorPhase(kit);
            case ROUE_FORTUNE -> new WheelPhase(kit);
            case ROI_FORAIN -> new FairKingPhase(kit);
            default -> null;
        };
    }

    static BossScene death(CutsceneKit kit, EnemyKind kind) {
        return switch (kind) {
            case MONSIEUR_LOYAL -> new LoyalDeath(kit);
            case CONDUCTEUR -> new ConductorDeath(kit);
            case ROUE_FORTUNE -> new WheelDeath(kit);
            case ROI_FORAIN -> new FairKingDeath(kit);
            default -> null;
        };
    }

    // ----- Accessoires -----

    static final Color RED = c("d8202c"), CURTAIN = c("8a0a1e"), SPOT = c("fff2c0"), BLUE_FIRE = c("5ab4ff");
    static final String[] TOP_HAT = {"..oooooo..", "..obbbbo..", "..obbbbo..", "..orrrro..", "oooooooooo", "oobbbbbboo"};
    static final String[] RABBIT = {
        ".ow..wo.", ".ow..wo.", ".ow..wo.", ".owwwwo.", "owwwwwwo", "owkwwkwo", "owwpwwwo", ".owwwwo.", "owwwwwwo",
        "owwwwwwo", ".oo..oo."};
    static final String[] CAP = {"..oooooo..", ".obbbbbbo.", "obbbyybbbo", "oooooooooo", "....oooooo"};
    static final String[] TICKET = {"oooooooooooo", "oyyyyyyyyyyo", "oyrryyyyrryo", "oyyyyyyyyyyo", "oooooooooooo"};
    static final String[] BEAR = {
        ".oo....oo.", "obbo..obbo", ".obbbbbbo.", "obbbbbbbbo", "obkbbbbkbo", "obbbmmbbbo", ".obbbbbbo.", "obbbbbbbbo",
        "obbbbbbbbo", ".oo....oo."};
    static final String[] BELL = {"....o....", "...ogo...", "..ogggo..", "..oggwo..", ".ogggwgo.", ".ogggggo.", "ogggggggo", "ooooooooo"};

    static final String[] TOMATO = {
        "...g...", ".oogoo.", "orrrrwo", "orrrrro", "orrrrro", ".orrro.", "..ooo.."};
    static final String[] SPLAT = {
        "r...r..r.", ".rr.rrr..", "..rrrrrr.", "rrrrsrrrr", ".rrrrrrr.", "..rr.rrr.", ".r....r.r"};

    /** Roi des forains : la carte qu'il tient, un vrai 2 de Carreau du jeu, posée sur celle de son portrait. */
    static final String FAIR_KING_CARD = "cards/light/2-D.png";
    /** Sa place sur le portrait : coin haut-gauche (pixels du portrait) et taille d'un pixel de la carte. */
    static final float CARD_LEFT = 26f, CARD_TOP = 18.6f, CARD_PIXELS = 0.42f;

    static Color c(String hex) { return Color.valueOf(hex); }

    /** Le projecteur : un cône de lumière {@code color} sur le boss. */
    static void spotlight(BossScene scene, Batch batch, Color color, float alpha) {
        BossScene.additive(batch);
        for (int k = 0; k < 6; k++) {
            float half = 220f + k * 70f;
            scene.rect(batch, color, 0.1f * alpha, scene.bossX() - half / 2f, scene.groundY(), half, scene.worldHeight());
        }
        BossScene.normal(batch);
        scene.glow(batch, scene.bossX(), scene.groundY(), scene.worldWidth() * 0.5f, color, 0.5f * alpha);
    }

    // -------------------------------------------------------------------------
    // Monsieur Loyal
    // -------------------------------------------------------------------------

    /** Roulement de tambour : il arrache son habit, gilet à paillettes ; le projecteur rougit ; ses massues en feu deviennent quatre As. */
    static final class LoyalPhase extends BossScene {
        private final TextureRegion[] aces = new TextureRegion[4];

        LoyalPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.MONSIEUR_LOYAL, 2);
            String[] paths = {"cards/light/1-P.png", "cards/light/1-H.png", "cards/light/1-C.png", "cards/light/1-D.png"};
            for (int i = 0; i < 4; i++) aces[i] = gameCard(paths[i]);
        }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 12; k++) if (at(0.2f + k * 0.08f)) rumble(0.05f, 2f);      // le roulement de tambour
            if (at(1.2f)) sparks(bossX(), bossY(), 60, GOLD, 600f, 10f);                    // l'habit arraché, les paillettes
            if (at(CLIMAX_PHASE)) { rumble(0.6f, 12f); sparks(bossX(), bossY() + bossHeight() * 0.5f, 50, c("ff7a1e"), 700f, 16f); }
        }

        @Override
        protected void back(Batch batch) {
            float red = progress(1.2f, 1.6f);
            tmp.set(SPOT).lerp(RED, red);
            spotlight(this, batch, tmp, 1f);
            poseTint = 0.35f * progress(1.2f, 1.4f) * pulse(14f, 0.4f);                   // le gilet scintille
            poseTintColor.set(GOLD);
            poseY = -10f * gesture(1.0f, 1.4f);
        }

        @Override
        protected void front(Batch batch) {
            float top = bossBottom() + bossHeight() + 80f;
            for (int i = 0; i < 4; i++) {
                float angle = time * 260f + i * 90f, x = bossX() + MathUtils.cosDeg(angle) * 300f, y = top + MathUtils.sinDeg(angle) * 70f;
                float clubs = progress(0.8f, 1.2f) * (1f - progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE));
                if (clubs > 0f) {                                                            // les massues en feu
                    glow(batch, x, y, 180f, c("ff7a1e"), clubs);
                    line(batch, c("f6f0e2"), clubs, x, y - 40f, x, y + 30f, 18f);
                }
                float card = progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.1f);
                if (card > 0f) draw(batch, aces[i], x, y, fit(220f, 27), 10f * MathUtils.sinDeg(angle), WHITE, card);
            }
            eyes(batch, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f), RED, new float[] {14f, 16f}, new float[] {21f, 16f});
            flash(batch, RED, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** Le projecteur grésille, son fouet l'enroule, dernier salut, le rideau tombe : il ne reste que le chapeau, et un lapin avec un Joker. */
    static final class LoyalDeath extends BossScene {
        private final TextureRegion hat, rabbit, joker;

        LoyalDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.MONSIEUR_LOYAL, 0);
            hat = art("obr", new Color[] {c("0a0608"), c("1a1418"), RED}, TOP_HAT);
            rabbit = art("owkp", new Color[] {c("0a0608"), c("f6f0e2"), c("1a1018"), c("e89aa8")}, RABBIT);
            joker = gameCard("cards/special_classic/blanc/joker.png");
        }

        private float curtain() {
            float down = Interpolation.bounceOut.apply(progress(CLIMAX_DEATH - 0.4f, CLIMAX_DEATH));
            float up = Interpolation.pow2In.apply(progress(3.3f, 3.9f));
            return down * (1f - up);
        }

        @Override
        protected void step(float delta) {
            if (at(0.3f)) sparks(bossX(), worldHeight(), 20, SPOT, 300f, 8f);
            if (at(CLIMAX_DEATH)) rumble(0.5f, 10f);
            if (at(4.0f)) sparks(bossX(), groundY() + 60f, 30, Color.WHITE, 400f, 10f);
        }

        @Override
        protected void back(Batch batch) {
            float flicker = time < CLIMAX_DEATH ? (MathUtils.sin(time * 60f) > 0.2f ? 1f : 0.3f) : 0.6f;
            spotlight(this, batch, SPOT, flicker);
            float bow = gesture(1.4f, 2.2f);
            poseTilt = 0f;
            poseStretch = -0.1f * bow;
            poseY = -30f * bow;
            showBoss = time < 3.3f;
        }

        @Override
        protected void front(Batch batch) {
            float coil = progress(0.5f, 1.4f);                                              // son fouet l'enroule
            if (coil > 0f && time < 3.3f) {
                int turns = (int) (6 * coil);
                for (int k = 0; k < turns; k++) {
                    float y = bossBottom() + bossHeight() * (0.2f + k * 0.11f);
                    line(batch, c("3a2a1e"), 1f, bossX() - bossWidth() * 0.4f, y, bossX() + bossWidth() * 0.4f, y + 30f, 10f);
                }
            }
            float drop = curtain();                                                          // le rideau
            if (drop > 0f) {
                float bottom = MathUtils.lerp(worldHeight(), -MARGIN, drop);
                rect(batch, CURTAIN, 1f, -MARGIN, bottom, worldWidth() + 2f * MARGIN, worldHeight() - bottom + MARGIN);
                for (int k = 0; k < 12; k++) rect(batch, c("5a0614"), 0.8f, k * worldWidth() / 11f, bottom, 18f, worldHeight() - bottom + MARGIN);
                rect(batch, GOLD, 1f, -MARGIN, bottom, worldWidth() + 2f * MARGIN, 16f);
            }
            if (time >= 3.6f) {                                                              // le haut-de-forme, et le lapin
                draw(batch, hat, bossX(), groundY() + 50f, fit(110f, 6), 0f, WHITE, 1f);
                float pop = Interpolation.swingOut.apply(progress(4.0f, 4.4f));
                if (pop > 0f) {
                    draw(batch, rabbit, bossX(), groundY() + 60f + 150f * pop, fit(170f, 11), 0f, WHITE, 1f);
                    draw(batch, joker, bossX() + 110f, groundY() + 80f + 150f * pop, fit(150f, 27), -12f, WHITE, pop);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Conducteur fantôme
    // -------------------------------------------------------------------------

    /** Le train entre dans le tunnel : noir, ses yeux et sa lanterne ; il ressort en looping, en flammes bleues, et poinçonne un ticket. */
    static final class ConductorPhase extends BossScene {
        private final TextureRegion ticket;

        ConductorPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.CONDUCTEUR, 2);
            ticket = art("oyr", new Color[] {c("4a3008"), c("ffd23a"), c("1a1018")}, TICKET);
        }

        @Override
        protected void step(float delta) {
            if (at(1.7f)) rumble(0.5f, 10f);
            if (at(CLIMAX_PHASE)) { rumble(0.5f, 12f); sparks(bossX(), bossY(), 50, BLUE_FIRE, 700f, 16f); }
            if (at(3.1f)) sparks(bossX() + 320f, bossY(), 16, GOLD, 300f, 8f);
        }

        @Override
        protected void back(Batch batch) {
            float loop = progress(1.7f, CLIMAX_PHASE);
            poseTilt = 360f * Interpolation.pow2.apply(loop);                                // le looping
            poseTint = progress(1.9f, CLIMAX_PHASE) * pulse(12f, 0.6f);
            poseTintColor.set(BLUE_FIRE);
            poseAlpha = 0.8f;
        }

        @Override
        protected void front(Batch batch) {
            float dark = progress(0.4f, 0.7f) * (1f - progress(1.6f, 1.8f));                 // le tunnel
            if (dark > 0f) {
                fill(batch, Color.BLACK, dark);
                eyes(batch, dark, c("eaffea"), new float[] {14f, 16f}, new float[] {21f, 16f});
                glow(batch, portraitX(29f), portraitY(31f), 200f, c("6ad86a"), dark);
            }
            if (time >= CLIMAX_PHASE) glow(batch, bossX(), bossY(), bossHeight() * 1.6f, BLUE_FIRE, 0.4f * pulse(10f, 0.6f));
            float punch = progress(2.8f, 3.1f);                                              // clac : le ticket poinçonné
            if (punch > 0f) {
                draw(batch, ticket, bossX() + 320f, bossY(), fit(90f, 5), 8f, WHITE, punch);
                if (time >= 3.1f) rect(batch, Color.BLACK, 1f, bossX() + 300f, bossY() - 10f, 22f, 22f);
            }
            flash(batch, BLUE_FIRE, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** La lanterne s'éteint, le train freine dans les étincelles ; il se dissout dans la brume, sa casquette reste ; les wagons repartent, vides. */
    static final class ConductorDeath extends BossScene {
        private final TextureRegion cap;

        ConductorDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.CONDUCTEUR, 0);
            cap = art("oby", new Color[] {c("0a0608"), c("2a2a4a"), c("ffd54a")}, CAP);
        }

        @Override
        protected void step(float delta) {
            if (time > 0.6f && time < 1.9f) {                                                // le train freine
                sparks(bossX() - bossWidth() * 0.6f, groundY(), 3, c("ffb84a"), 500f, 8f);
                sparks(bossX() + bossWidth() * 0.6f, groundY(), 3, c("ffb84a"), 500f, 8f);
            }
            if (at(0.6f)) rumble(1.2f, 6f);
            if (time > CLIMAX_DEATH && time < 3.6f && random.nextFloat() < 0.6f) {           // la brume
                Particle mist = particle(bossX() + (random.nextFloat() - 0.5f) * bossWidth(), bossY() + (random.nextFloat() - 0.5f)
                    * bossHeight(), 90f, 60f, 1.4f, 120f);
                mist.color.set(0.7f, 0.8f, 1f, 0.35f);
                glows.add(mist);
            }
        }

        @Override
        protected void back(Batch batch) {
            float lantern = 1f - progress(0.2f, 0.5f);
            glow(batch, portraitX(29f), portraitY(31f), 240f, c("6ad86a"), lantern);
            poseAlpha = 0.8f * (1f - progress(CLIMAX_DEATH, 3.5f));
            showBoss = poseAlpha > 0f;
        }

        @Override
        protected void front(Batch batch) {
            if (time >= 3.0f) draw(batch, cap, bossX(), groundY() + 40f, fit(70f, 5), 0f, WHITE, progress(3.0f, 3.4f));
            float leave = Interpolation.pow2In.apply(progress(3.6f, 5.0f));                  // les wagons repartent, vides
            for (int w = 0; w < 3; w++) {
                float x = -worldWidth() * 0.6f + w * 360f + leave * worldWidth() * 1.8f;
                rect(batch, c("2a1a3a"), 1f, x, groundY() - 10f, 320f, 160f);
                rect(batch, c("1a1a30"), 1f, x + 20f, groundY() + 150f, 280f, 20f);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Roue de la Fortune
    // -------------------------------------------------------------------------

    /**
     * La Roue de la Fortune en trois calques (pied, roue, visage et flèche), pour faire tourner la roue seule :
     * le pied et son visage ne bougent pas.
     */
    abstract static class WheelScene extends BossScene {
        protected final TextureRegion stand, disc, face;
        protected final int[][] discPixels;

        WheelScene(CutsceneKit kit, Moment moment, int phase) {
            super(kit, moment, EnemyKind.ROUE_FORTUNE, phase);
            stand = pixels(FairDesertPortraits.wheelLayer(0));
            discPixels = FairDesertPortraits.wheelLayer(1);
            disc = pixels(discPixels);
            face = pixels(FairDesertPortraits.wheelLayer(2));
        }

        /** @return le centre de la roue à l'écran (le pixel 17, 19 du portrait). */
        protected float hubX() { return portraitX(17.5f); }
        protected float hubY() { return portraitY(19.5f); }

        /** Le pied, la roue tournée de {@code angle} degrés autour de son moyeu, puis le visage ; teinte {@code tint}. */
        protected void drawWheel(Batch batch, float angle, Color tint, boolean withDisc, boolean withFace) {
            float scale = bossScale() * poseScale;
            float w = boss.getRegionWidth() * scale, h = boss.getRegionHeight() * scale;
            float x = bossX() + poseX - w / 2f, y = bossBottom() + poseY;
            batch.setColor(tint);
            batch.draw(stand, x, y, w, h);
            if (withDisc) batch.draw(disc, x, y, hubX() - x, hubY() - y, w, h, 1f, 1f, angle);
            if (withFace) batch.draw(face, x, y, w, h);
        }
    }

    /** Sa roue tourne de plus en plus vite (lui ne bouge pas) ; ses nacelles se détachent et l'entourent ; toutes ses cases deviennent « Épée x2 ». */
    static final class WheelPhase extends WheelScene {
        private final TextureRegion sword;

        WheelPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, 2);
            sword = symbol(EnemySymbol.SWORD);
        }

        private float angle() { return -140f * time * time; }

        @Override
        protected void step(float delta) {
            if (time > 0.6f && random.nextFloat() < 0.6f) {                                 // les étincelles du cerclage
                float a = random.nextFloat() * 360f, r = bossScale() * 15f;
                sparks(hubX() + MathUtils.cosDeg(a) * r, hubY() + MathUtils.sinDeg(a) * r, 2, GOLD, 600f, 8f);
            }
            if (at(CLIMAX_PHASE)) { rumble(0.6f, 12f); sparks(hubX(), hubY(), 60, Color.WHITE, 800f, 14f); }
        }

        @Override
        protected void back(Batch batch) {
            showBoss = false;
            poseX = 6f * MathUtils.sin(time * 40f) * progress(1.2f, CLIMAX_PHASE);           // elle tremble sur son pied
            drawWheel(batch, angle(), WHITE, true, true);
            if (time > 1.0f) {                                                               // le flou de la vitesse
                tmp.set(1f, 1f, 1f, 0.25f * progress(1.0f, 2.0f));
                batch.setColor(tmp);
                float scale = bossScale(), w = boss.getRegionWidth() * scale, h = boss.getRegionHeight() * scale;
                float x = bossX() + poseX - w / 2f, y = bossBottom();
                batch.draw(disc, x, y, hubX() - x, hubY() - y, w, h, 1f, 1f, angle() + 20f);
                batch.draw(face, x, y, w, h);
            }
        }

        @Override
        protected void front(Batch batch) {
            float out = Interpolation.pow2Out.apply(progress(1.0f, 1.8f));
            float swords = progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.1f);
            for (int i = 0; i < 8; i++) {                                                    // les nacelles qui l'entourent
                float a = -angle() * 0.6f + i * 45f, r = bossWidth() * (0.5f + 0.35f * out);
                float x = hubX() + MathUtils.cosDeg(a) * r, y = hubY() + MathUtils.sinDeg(a) * r;
                if (out <= 0f) continue;
                rect(batch, i % 2 == 0 ? c("e86a8a") : GOLD, 1f - swords, x - 30f, y - 24f, 60f, 48f);
                if (swords > 0f) {
                    draw(batch, sword, x - 18f, y, iconScale(0.06f), 0f, WHITE, swords);
                    draw(batch, sword, x + 18f, y, iconScale(0.06f), 0f, WHITE, swords);
                }
            }
            float blade = progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f);                       // sa flèche devient une lame
            if (blade > 0f) {
                float top = portraitY(0f);
                line(batch, c("dce4ee"), blade, bossX() + poseX, top - 40f, bossX() + poseX, top + 160f * blade, 26f);
                line(batch, WHITE, blade, bossX() + poseX - 4f, top - 40f, bossX() + poseX - 4f, top + 150f * blade, 8f);
            }
            eyes(batch, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f), c("ff3a3a"), new float[] {15f, 18f}, new float[] {20f, 18f});
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /**
     * Le public la bombarde de tomates : elles l'éclaboussent pendant qu'elle ralentit ; la dernière, énorme,
     * la fait voler en éclats. Il ne reste que son pied, couvert de jus.
     */
    static final class WheelDeath extends WheelScene {
        /** Les tomates : lancées à ces moments, chacune vise un point de la roue (rayon, angle). */
        private static final float[] THROWS = {0.15f, 0.45f, 0.7f, 0.95f, 1.15f, 1.35f, 1.5f, 1.65f, 1.8f, 1.95f, 2.05f};
        private static final float FLIGHT = 0.45f;
        private final TextureRegion tomato, splat;
        private final float[] aimRadius = new float[THROWS.length], aimAngle = new float[THROWS.length], fromX = new float[THROWS.length];
        /** Les taches restées collées sur la roue : angle sur la roue (elle tourne avec), rayon, rotation. */
        private final com.badlogic.gdx.utils.FloatArray stains = new com.badlogic.gdx.utils.FloatArray();
        private boolean broken;

        WheelDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, 0);
            tomato = art("ogrw", new Color[] {c("6a0a0a"), c("3a8a2a"), c("d8202c"), c("ff8a8a")}, TOMATO);
            splat = art("rs", new Color[] {c("c8102e"), c("f0d060")}, SPLAT);
            java.util.Random seed = new java.util.Random(11);
            for (int i = 0; i < THROWS.length; i++) {
                aimRadius[i] = 3f + 9f * seed.nextFloat();
                aimAngle[i] = seed.nextFloat() * 360f;
                fromX[i] = seed.nextBoolean() ? -0.1f - 0.2f * seed.nextFloat() : 1.1f + 0.2f * seed.nextFloat();
            }
        }

        @Override
        protected void restart() { stains.clear(); broken = false; }

        /** Elle ralentit et s'arrête pile à l'impact final, revenue à sa position de départ (deux tours). */
        private float angle() {
            float t = Math.min(time, CLIMAX_DEATH);
            return -576f * (t - t * t / (2f * CLIMAX_DEATH));
        }

        /** @return où la tomate {@code i} touche la roue à l'écran, maintenant. */
        private float aimX(int i) { return hubX() + MathUtils.cosDeg(aimAngle[i] + angle()) * aimRadius[i] * bossScale(); }
        private float aimY(int i) { return hubY() + MathUtils.sinDeg(aimAngle[i] + angle()) * aimRadius[i] * bossScale(); }

        @Override
        protected void step(float delta) {
            for (int i = 0; i < THROWS.length; i++) {
                if (!at(THROWS[i] + FLIGHT)) continue;                                       // splatch !
                rumble(0.08f, 3f);
                stains.addAll(aimAngle[i], aimRadius[i], random.nextFloat() * 360f);
                spray(splat, aimX(i), aimY(i), 4, 0f, 360f, 300f, fit(18f, 7), 1400f, 0.7f);
            }
            if (at(CLIMAX_DEATH)) {                                                          // la dernière : la roue vole en éclats
                broken = true;
                rumble(0.8f, 16f);
                shatter();
                spray(splat, hubX(), hubY(), 14, 0f, 360f, 700f, fit(40f, 7), 1400f, 1.4f);
                sparks(hubX(), hubY(), 50, c("ff4a4a"), 800f, 16f);
            }
        }

        /** Les morceaux de la roue et de son visage, projetés de tous côtés. */
        private void shatter() {
            int piece = 4, width = boss.getRegionWidth();
            float scale = bossScale();
            int[][] faceGrid = FairDesertPortraits.wheelLayer(2);
            for (int ty = 0; ty < 36; ty += piece) {
                for (int tx = 0; tx < width; tx += piece) {
                    boolean filled = false;
                    for (int y = ty; y < ty + piece && y < 36; y++) {
                        for (int x = tx; x < tx + piece && x < width; x++) filled |= discPixels[y][x] != 0 || faceGrid[y][x] != 0;
                    }
                    if (!filled) continue;
                    float x = portraitX(tx + piece / 2f), y = portraitY(ty + piece / 2f);
                    float away = MathUtils.atan2(y - hubY(), x - hubX()), speed = 500f + 700f * random.nextFloat();
                    for (TextureRegion layer : new TextureRegion[] {disc, face}) {
                        Particle bit = place(new TextureRegion(layer, tx, ty, piece, piece), x, y,
                            MathUtils.cos(away) * speed, MathUtils.sin(away) * speed + 400f, scale, 1.6f + 0.6f * random.nextFloat());
                        bit.gravity = 1600f;
                        bit.spin = (random.nextFloat() - 0.5f) * 900f;
                    }
                }
            }
        }

        @Override
        protected void back(Batch batch) {
            showBoss = false;
            float juice = Math.min(1f, stains.size / 3f / THROWS.length);
            tmp.set(1f, 1f - 0.25f * juice, 1f - 0.3f * juice, 1f);                          // elle rougit sous les tomates
            drawWheel(batch, angle(), tmp, !broken, !broken);
            if (broken) return;
            float scale = bossScale();
            for (int k = 0; k < stains.size; k += 3) {                                       // les taches tournent avec elle
                float a = stains.get(k) + angle(), r = stains.get(k + 1) * scale;
                draw(batch, splat, hubX() + MathUtils.cosDeg(a) * r, hubY() + MathUtils.sinDeg(a) * r, scale * 0.9f,
                    stains.get(k + 2), WHITE, 1f);
            }
        }

        @Override
        protected void front(Batch batch) {
            for (int i = 0; i < THROWS.length; i++) {                                        // les tomates en vol
                float k = progress(THROWS[i], THROWS[i] + FLIGHT);
                if (k <= 0f || k >= 1f) continue;
                float sx = worldWidth() * fromX[i], sy = -60f;
                float x = MathUtils.lerp(sx, aimX(i), k), y = MathUtils.lerp(sy, aimY(i), k) + 260f * MathUtils.sin(k * MathUtils.PI);
                draw(batch, tomato, x, y, fit(70f, 7) * (1.3f - 0.4f * k), 400f * k, WHITE, 1f);
            }
            float big = progress(CLIMAX_DEATH - 0.5f, CLIMAX_DEATH);                         // la dernière, énorme
            if (big > 0f && big < 1f) {
                float x = MathUtils.lerp(worldWidth() * 0.5f, hubX(), big);
                float y = MathUtils.lerp(-120f, hubY(), big) + 200f * MathUtils.sin(big * MathUtils.PI);
                draw(batch, tomato, x, y, fit(200f, 7) * (1.4f - 0.4f * big), 200f * big, WHITE, 1f);
            }
            flash(batch, c("ff4a4a"), CLIMAX_DEATH, 0.3f, 0.4f);
        }
    }

    // -------------------------------------------------------------------------
    // Roi des forains
    // -------------------------------------------------------------------------

    /** Il frappe encore : la cloche explose, les lampes rougissent ; les peluches ouvrent des yeux méchants ; sa couronne se hérisse. */
    static final class FairKingPhase extends BossScene {
        private final TextureRegion bear, bell;

        private final TextureRegion card;

        FairKingPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.ROI_FORAIN, 2);
            card = gameCard(FAIR_KING_CARD);
            bear = art("obkm", new Color[] {c("3a2412"), c("c8904a"), c("e0283a"), c("5a2a1a")}, BEAR);
            bell = art("ogw", new Color[] {c("4a3008"), GOLD, c("fff0a0")}, BELL);
        }

        @Override
        protected void step(float delta) {
            if (at(1.2f)) rumble(0.3f, 10f);
            if (at(CLIMAX_PHASE)) {
                rumble(0.6f, 14f);
                spray(bell, worldWidth() * 0.85f, worldHeight() * 0.85f, 10, 0f, 360f, 900f, fit(40f, 8), 1600f, 1.4f);
                sparks(worldWidth() * 0.85f, worldHeight() * 0.85f, 50, GOLD, 800f, 14f);
            }
        }

        @Override
        protected void back(Batch batch) {
            float red = progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f);
            for (int l = 0; l < 6; l++) {                                                    // les lampes, qui virent au rouge
                tmp.set(c("ffb84a")).lerp(RED, red);
                glow(batch, worldWidth() * (0.1f + 0.16f * l), worldHeight() * 0.88f, 220f, tmp, 0.7f);
            }
            float rise = Interpolation.swingOut.apply(progress(CLIMAX_PHASE + 0.2f, CLIMAX_PHASE + 0.8f));
            for (int b = 0; b < 6; b++) {                                                    // les peluches, derrière lui
                float x = bossX() + (b - 2.5f) * bossWidth() * 0.42f;
                draw(batch, bear, x, groundY() + 80f + 60f * rise * (b % 2), fit(180f, 10), 0f, WHITE, 0.4f + 0.6f * rise);
                if (rise > 0f) {
                    glow(batch, x - 30f, groundY() + 80f + 60f * rise * (b % 2) + 10f, 60f, RED, rise);
                    glow(batch, x + 30f, groundY() + 80f + 60f * rise * (b % 2) + 10f, 60f, RED, rise);
                }
            }
            if (time < CLIMAX_PHASE) draw(batch, bell, worldWidth() * 0.85f, worldHeight() * 0.85f, fit(120f, 8), 0f, WHITE, 1f);
            poseX = 40f * gesture(0.8f, 1.3f);
            poseTilt = -10f * gesture(0.8f, 1.3f);
        }

        @Override
        protected void front(Batch batch) {
            float shine = progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f);                       // sa carte truquée luit
            if (shine > 0f) glow(batch, portraitX(30f), portraitY(24f), 260f, RED, 0.6f * shine * pulse(8f, 0.5f));
            onPortrait(batch, card, CARD_LEFT, CARD_TOP, CARD_PIXELS, 1f);                  // un vrai 2 de Carreau
            float spikes = progress(CLIMAX_PHASE + 0.3f, CLIMAX_PHASE + 0.6f);               // sa couronne se hérisse
            for (int k = 0; k < 5 && spikes > 0f; k++) {
                float x = portraitX(10f + k * 3.5f), y = portraitY(1f);
                line(batch, c("dce4ee"), spikes, x, y, x + (k - 2) * 10f, y + 70f * spikes, 10f);
            }
            eyes(batch, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f), RED, new float[] {14f, 16f}, new float[] {21f, 16f});
            flash(batch, RED, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** Il frappe une dernière fois et rate ; le palet retombe sur sa couronne ; il s'effondre ; les peluches pleuvent ; un ticket doré flotte vers toi. */
    static final class FairKingDeath extends BossScene {
        private final TextureRegion bear, ticket, card;

        FairKingDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.ROI_FORAIN, 0);
            card = gameCard(FAIR_KING_CARD);
            bear = art("obkm", new Color[] {c("3a2412"), c("c8904a"), c("1a1018"), c("5a2a1a")}, BEAR);
            ticket = art("oyr", new Color[] {c("8a5a10"), c("ffd23a"), c("fff2a0")}, TICKET);
        }

        @Override
        protected void step(float delta) {
            if (at(0.9f)) rumble(0.2f, 5f);
            if (at(CLIMAX_DEATH)) { rumble(0.5f, 10f); sparks(portraitX(17f), portraitY(2f), 30, GOLD, 500f, 12f); }
            if (at(3.0f)) spray(bear, worldWidth() / 2f, worldHeight() + 100f, 18, 250f, 290f, 400f, fit(120f, 10), 900f, 2.2f);
        }

        @Override
        protected void back(Batch batch) {
            poseX = 60f * gesture(0.4f, 1.0f);                                              // il frappe à côté
            poseTilt = -14f * gesture(0.4f, 1.0f) + 25f * progress(CLIMAX_DEATH, CLIMAX_DEATH + 0.5f);
            poseSink = progress(CLIMAX_DEATH + 0.2f, 3.4f);                                  // il s'effondre derrière le comptoir
        }

        @Override
        protected void front(Batch batch) {
            onPortrait(batch, card, CARD_LEFT, CARD_TOP, CARD_PIXELS, 1f);                  // sa carte : un vrai 2 de Carreau
            float fall = Interpolation.pow2In.apply(progress(1.6f, CLIMAX_DEATH));           // le palet retombe sur sa couronne
            if (fall > 0f && time < CLIMAX_DEATH + 0.6f) {
                float y = MathUtils.lerp(worldHeight() + 60f, portraitY(1f), fall);
                rect(batch, c("dce4ee"), 1f, portraitX(17f) - 40f, y, 80f, 40f);
            }
            rect(batch, c("6a3a1a"), 1f, -MARGIN, -MARGIN, worldWidth() + 2f * MARGIN, bossBottom() + MARGIN + 40f); // le comptoir
            rect(batch, GOLD, 1f, -MARGIN, bossBottom() + 30f, worldWidth() + 2f * MARGIN, 12f);
            float drift = progress(3.4f, 5.2f);                                              // le ticket doré
            if (drift > 0f) {
                float x = worldWidth() / 2f + 120f * MathUtils.sin(drift * 6f), y = MathUtils.lerp(worldHeight() * 0.9f, worldHeight() * 0.4f, drift);
                draw(batch, ticket, x, y, fit(70f + 80f * drift, 5), 12f * MathUtils.sin(drift * 8f), WHITE, 1f);
                glow(batch, x, y, 260f, GOLD, 0.5f);
            }
        }
    }
}
