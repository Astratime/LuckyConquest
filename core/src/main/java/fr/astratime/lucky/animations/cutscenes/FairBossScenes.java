package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
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
            eyes(batch, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f), RED, new float[] {13.5f, 15.5f}, new float[] {20.5f, 15.5f});
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
                eyes(batch, dark, c("eaffea"), new float[] {13.5f, 15.5f}, new float[] {20.5f, 15.5f});
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

    /** Elle tourne de plus en plus vite ; ses nacelles se détachent et l'entourent ; toutes ses cases deviennent « Épée x2 ». */
    static final class WheelPhase extends BossScene {
        private final TextureRegion sword;

        WheelPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.ROUE_FORTUNE, 2);
            sword = symbol(EnemySymbol.SWORD);
        }

        private float angle() { return 140f * time * time; }

        @Override
        protected void step(float delta) {
            if (time > 0.6f && random.nextFloat() < 0.6f) sparks(bossX(), bossY(), 2, GOLD, 600f, 8f);
            if (at(CLIMAX_PHASE)) { rumble(0.6f, 12f); sparks(bossX(), bossY(), 60, Color.WHITE, 800f, 14f); }
        }

        @Override
        protected void back(Batch batch) {
            poseTilt = -angle();
        }

        @Override
        protected void front(Batch batch) {
            float out = Interpolation.pow2Out.apply(progress(1.0f, 1.8f));
            float swords = progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE + 0.1f);
            for (int i = 0; i < 8; i++) {                                                    // les nacelles qui l'entourent
                float a = angle() * 0.6f + i * 45f, r = bossWidth() * (0.5f + 0.35f * out);
                float x = bossX() + MathUtils.cosDeg(a) * r, y = bossY() + MathUtils.sinDeg(a) * r;
                if (out <= 0f) continue;
                rect(batch, i % 2 == 0 ? c("e86a8a") : GOLD, 1f - swords, x - 30f, y - 24f, 60f, 48f);
                if (swords > 0f) {
                    draw(batch, sword, x - 18f, y, iconScale(0.06f), 0f, WHITE, swords);
                    draw(batch, sword, x + 18f, y, iconScale(0.06f), 0f, WHITE, swords);
                }
            }
            float blade = progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f);                       // sa flèche devient une lame
            if (blade > 0f) {
                float top = bossBottom() + bossHeight();
                line(batch, c("dce4ee"), blade, bossX(), top - 40f, bossX(), top + 160f * blade, 26f);
                line(batch, WHITE, blade, bossX() - 4f, top - 40f, bossX() - 4f, top + 150f * blade, 8f);
            }
            eyes(batch, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f), c("ff3a3a"), new float[] {14.5f, 17.5f}, new float[] {19.5f, 17.5f});
            flash(batch, GOLD, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** Elle ralentit, tic... tic... s'arrête sur Rien ; ses lumières s'éteignent ; elle bascule comme une pièce qui tombe, puis se couche. */
    static final class WheelDeath extends BossScene {

        WheelDeath(CutsceneKit kit) { super(kit, Moment.DEATH, EnemyKind.ROUE_FORTUNE, 0); }

        /** Ses crans : de plus en plus espacés, le dernier tombe sur la case Rien. */
        private static final float[] TICKS = {0.2f, 0.45f, 0.75f, 1.1f, 1.5f, 1.95f, 2.45f};

        @Override
        protected void step(float delta) {
            for (float tick : TICKS) if (at(tick)) { rumble(0.06f, 3f); sparks(bossX(), bossBottom() + bossHeight(), 6, GOLD, 200f, 6f); }
            if (at(3.6f)) rumble(0.4f, 10f);
        }

        @Override
        protected void back(Batch batch) {
            int passed = 0;
            for (float tick : TICKS) if (time >= tick) passed++;
            poseTilt = -45f * passed;                                                        // un cran à chaque tic
            float topple = progress(2.9f, 3.6f), wobble = MathUtils.sin(topple * 30f) * (1f - topple);
            poseStretch = -0.9f * topple + 0.1f * wobble;                                   // elle tombe à plat, comme une pièce
            poseTilt += 20f * wobble;
            poseShadow = 0.6f * progress(2.5f, 3.2f);                                        // ses lumières s'éteignent
        }

        @Override
        protected void front(Batch batch) {
            if (time < 2.9f) {
                for (int i = 0; i < 8; i++) {                                                // les lumières, une à une
                    if (time > 2.5f + i * 0.05f) continue;
                    float a = i * 45f + 22f;
                    glow(batch, bossX() + MathUtils.cosDeg(a) * bossWidth() * 0.45f, bossY() + MathUtils.sinDeg(a) * bossWidth() * 0.45f,
                        90f, GOLD, 0.7f);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Roi des forains
    // -------------------------------------------------------------------------

    /** Il frappe encore : la cloche explose, les lampes rougissent ; les peluches ouvrent des yeux méchants ; sa couronne se hérisse. */
    static final class FairKingPhase extends BossScene {
        private final TextureRegion bear, bell;

        FairKingPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, EnemyKind.ROI_FORAIN, 2);
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
            float spikes = progress(CLIMAX_PHASE + 0.3f, CLIMAX_PHASE + 0.6f);               // sa couronne se hérisse
            for (int k = 0; k < 5 && spikes > 0f; k++) {
                float x = portraitX(10f + k * 3.5f), y = portraitY(1f);
                line(batch, c("dce4ee"), spikes, x, y, x + (k - 2) * 10f, y + 70f * spikes, 10f);
            }
            eyes(batch, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f), RED, new float[] {13.5f, 15.5f}, new float[] {20.5f, 15.5f});
            flash(batch, RED, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /** Il frappe une dernière fois et rate ; le palet retombe sur sa couronne ; il s'effondre ; les peluches pleuvent ; un ticket doré flotte vers toi. */
    static final class FairKingDeath extends BossScene {
        private final TextureRegion bear, ticket;

        FairKingDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, EnemyKind.ROI_FORAIN, 0);
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
