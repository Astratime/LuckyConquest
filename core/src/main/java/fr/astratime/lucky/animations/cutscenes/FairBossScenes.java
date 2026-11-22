package fr.astratime.lucky.animations.cutscenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.assets.FairDesertPortraits;
import fr.astratime.lucky.entities.enemy.EnemyKind;
import fr.astratime.lucky.entities.enemy.EnemySymbol;
import java.util.Random;

import static fr.astratime.lucky.assets.Palette.hex;

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

    static final Color RED = hex("d8202c"), CURTAIN = hex("8a0a1e"), SPOT = hex("fff2c0"), BLUE_FIRE = hex("5ab4ff");
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
            if (at(CLIMAX_PHASE)) { rumble(0.6f, 12f); sparks(bossX(), bossY() + bossHeight() * 0.5f, 50, hex("ff7a1e"), 700f, 16f); }
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
                    glow(batch, x, y, 180f, hex("ff7a1e"), clubs);
                    line(batch, hex("f6f0e2"), clubs, x, y - 40f, x, y + 30f, 18f);
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
            Piece real = lift(FairDesertPortraits.pixels(EnemyKind.MONSIEUR_LOYAL),            // son vrai haut-de-forme
                FairDesertPortraits.without(EnemyKind.MONSIEUR_LOYAL, FairDesertPortraits.Prop.LOYAL_HAT));
            hat = real != null ? real.region : art("obr", new Color[] {hex("0a0608"), hex("1a1418"), RED}, TOP_HAT);
            rabbit = art("owkp", new Color[] {hex("0a0608"), hex("f6f0e2"), hex("1a1018"), hex("e89aa8")}, RABBIT);
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
                int turns = (int) (5 * coil);
                for (int k = 0; k < turns; k++) {                                            // autour de sa veste, pas de son visage
                    float y = portraitY(40f - k * 2.4f);
                    line(batch, hex("6e4524"), 1f, portraitX(4f), y, portraitX(30f), y + bossScale() * 1.5f, bossScale() * 0.9f);
                }
            }
            float drop = curtain();                                                          // le rideau
            if (drop > 0f) {
                float bottom = MathUtils.lerp(worldHeight(), -MARGIN, drop);
                rect(batch, CURTAIN, 1f, -MARGIN, bottom, worldWidth() + 2f * MARGIN, worldHeight() - bottom + MARGIN);
                for (int k = 0; k < 12; k++) rect(batch, hex("5a0614"), 0.8f, k * worldWidth() / 11f, bottom, 18f, worldHeight() - bottom + MARGIN);
                rect(batch, GOLD, 1f, -MARGIN, bottom, worldWidth() + 2f * MARGIN, 16f);
            }
            if (time >= 3.6f) {                                                              // son haut-de-forme, renversé, et le lapin
                float hatScale = bossScale() * 1.1f;
                draw(batch, hat, bossX(), groundY() + hat.getRegionHeight() * hatScale / 2f + 6f, hatScale, 180f, WHITE, 1f);
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

    static final String[] CART = {
        "oooooooooooooooooooooooooooo",
        "oyyyyyyyyyyyyyyyyyyyyyyyyyyo",
        "obbbbbbbbbbbbbbbbbbbbbbbbbbo",
        "obbbbbbbbbbbwwwwbbbbbbbbbbbo",
        "obbbbbbbbbbwkwwkwbbbbbbbbbbo",
        "obbbbbbbbbbwwwwwwbbbbbbbbbbo",
        "obbbbbbbbbbbwkkwbbbbbbbbbbbo",
        "obbbbbbbbbbbbbbbbbbbbbbbbbbo",
        "oyyyyyyyyyyyyyyyyyyyyyyyyyyo",
        "oooooooooooooooooooooooooooo",
        ".oddo..................oddo.",
        ".oooo..................oooo."};
    static final String[] GHOST = {
        "..ooooo..", ".owwwwwo.", "owwwwwwwo", "owkkwkkwo", "owkkwkkwo", "owwwwwwwo", "owwwwwwwo", "owwwwwwwo", "owowowowo",
        "o.o.o.o.o"};
    static final String[] BUFFER = {
        "oooooooooooo", "orrwwrrwwrro", "orrwwrrwwrro", "oooooooooooo", ".oko....oko.", ".oko....oko.", ".oko....oko.", ".ooo....ooo."};
    /** Le point de fuite des rails du décor du train fantôme, au fond du tunnel (pixels du décor). */
    static final float RAILS_X = 180f, RAILS_Y = 160f;
    static final Color LANTERN = hex("6ad86a");

    /** Le Conducteur dans son wagonnet du train fantôme, sur les rails du décor. */
    abstract static class ConductorScene extends BossScene {
        protected final TextureRegion cart;

        ConductorScene(CutsceneKit kit, Moment moment, int phase) {
            super(kit, moment, EnemyKind.CONDUCTEUR, phase);
            cart = art("oybwkd", new Color[] {hex("0a0608"), hex("c4891e"), hex("3a1a4a"), hex("e8e0d0"), hex("1a1018"), hex("4a4a5a")}, CART);
        }

        /** Le place à {@code depth} (0 = devant, 1 = au fond du tunnel) : plus petit, plus haut, sur les rails. */
        protected void ride(float depth) {
            poseScale = MathUtils.lerp(1f, 0.22f, depth);
            poseY = (decorY(RAILS_Y) - bossBottom()) * depth;
            poseX = (decorX(RAILS_X) - bossX()) * depth;
        }

        protected float cartScale() { return bossScale() * poseScale; }
        protected float wheelY()    { return bossBottom() + poseY; }
        protected float wheelX(int side) { return bossX() + poseX + side * 11.5f * cartScale(); }

        /** Le wagonnet, devant son buste. */
        protected void drawCart(Batch batch, Color tint) {
            float scale = cartScale(), w = 28f * scale, h = 12f * scale;
            batch.setColor(tint);
            batch.draw(cart, bossX() + poseX - w / 2f, bossBottom() + poseY - 2f * scale, w, h);
        }

        /** Sa lanterne verte, qui se balance au bout de sa main. */
        protected void lantern(Batch batch, float alpha) {
            if (alpha > 0f) glow(batch, portraitX(29f), portraitY(31f), 240f * poseScale, LANTERN, alpha * pulse(9f, 0.25f));
        }
    }

    /**
     * « Terminus : Frayeur ! » : son wagonnet sort du fond du tunnel et fonce sur les rails, freine dans les
     * étincelles contre le heurtoir du terminus ; tout s'éteint, il ne reste que ses yeux et sa lanterne, et les
     * fantômes du tunnel jaillissent sur toi ; il se rallume en flammes bleues et poinçonne ton ticket.
     */
    static final class ConductorPhase extends ConductorScene {
        private static final float ARRIVE = 1.2f, DARK = 1.35f, PUNCH = 3.1f;
        private final TextureRegion ticket, ghost, buffer;

        ConductorPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, 2);
            ticket = art("oyr", new Color[] {hex("4a3008"), hex("ffd23a"), hex("1a1018")}, TICKET);
            ghost = art("owk", new Color[] {hex("3a4a6a"), hex("dce8f4"), hex("1a1018")}, GHOST);
            buffer = art("orwk", new Color[] {hex("1a1018"), hex("d8202c"), hex("f6f0e2"), hex("6a6a7a")}, BUFFER);
        }

        private float dark() { return progress(DARK, DARK + 0.15f) * (1f - progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE)); }

        @Override
        protected void step(float delta) {
            for (int k = 0; k < 8; k++) if (at(0.1f + k * 0.14f)) rumble(0.05f, 2f + k * 0.5f);   // le wagonnet qui cahote
            if (time > ARRIVE - 0.25f && time < ARRIVE + 0.15f) {                               // il freine
                sparks(wheelX(-1), wheelY(), 3, hex("ffb84a"), 500f, 8f);
                sparks(wheelX(1), wheelY(), 3, hex("ffb84a"), 500f, 8f);
            }
            if (at(ARRIVE)) rumble(0.4f, 10f);
            if (at(CLIMAX_PHASE)) { rumble(0.5f, 12f); sparks(bossX(), bossY(), 50, BLUE_FIRE, 700f, 16f); }
            if (at(PUNCH)) sparks(bossX() + bossWidth() * 0.9f, bossY(), 16, GOLD, 300f, 8f);
        }

        @Override
        protected void back(Batch batch) {
            ride(1f - Interpolation.pow2Out.apply(progress(0f, ARRIVE)));
            poseY += 4f * Math.abs(MathUtils.sin(time * 22f)) * (1f - progress(ARRIVE - 0.2f, ARRIVE));
            poseTilt = -6f * gesture(ARRIVE - 0.05f, ARRIVE + 0.35f);                         // le coup de frein
            poseTint = progress(CLIMAX_PHASE - 0.1f, CLIMAX_PHASE) * pulse(12f, 0.6f);
            poseTintColor.set(BLUE_FIRE);
            lantern(batch, 1f - dark());
        }

        @Override
        protected void front(Batch batch) {
            drawCart(batch, WHITE);
            float bs = bossScale();                                                           // le heurtoir du terminus, au bout des rails
            stretch(batch, buffer, bossX(), bossBottom() - 5f * bs, 40f * bs / 12f, 9f * bs / 8f, 0f, WHITE, 1f);
            float dark = dark();
            if (dark > 0f) {                                                                  // tout s'éteint
                fill(batch, Color.BLACK, 0.92f * dark);
                eyes(batch, dark, hex("eaffea"), new float[] {14f, 16f}, new float[] {21f, 16f});
                glow(batch, portraitX(29f), portraitY(31f), 200f, LANTERN, dark);
                for (int g = 0; g < 4; g++) {                                                 // les fantômes jaillissent du tunnel
                    float k = progress(DARK + 0.1f + g * 0.15f, DARK + 0.85f + g * 0.15f);
                    if (k <= 0f || k >= 1f) continue;
                    float side = (g % 2 == 0 ? -1f : 1f) * (0.6f + 0.25f * g);
                    float x = MathUtils.lerp(decorX(RAILS_X), bossX() + side * worldWidth() * 0.45f, k);
                    float y = MathUtils.lerp(decorY(110f), worldHeight() * (0.35f + 0.12f * g), k);
                    draw(batch, ghost, x, y, fit(40f + 520f * k * k, 10), 10f * MathUtils.sin(time * 9f + g), WHITE, 0.85f * (1f - k * k));
                }
            }
            if (time >= CLIMAX_PHASE) glow(batch, bossX(), bossY(), bossHeight() * 1.6f, BLUE_FIRE, 0.4f * pulse(10f, 0.6f));
            float punch = progress(PUNCH - 0.3f, PUNCH);                                      // clac : le ticket poinçonné
            if (punch > 0f) {
                float x = bossX() + bossWidth() * 0.9f;
                draw(batch, ticket, x, bossY(), fit(90f, 5), 8f, WHITE, punch);
                if (time >= PUNCH) rect(batch, Color.BLACK, 1f, x - 20f, bossY() - 10f, 22f, 22f);
            }
            flash(batch, BLUE_FIRE, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /**
     * « Le train fantôme finit sa course » : sa lanterne s'éteint, les freins lâchent, et son wagonnet repart à
     * reculons au fond du tunnel ; le vent arrache sa vraie casquette, qui vole vers toi et retombe sur les rails ;
     * le noir du tunnel l'avale, un dernier souffle de brume en sort.
     */
    static final class ConductorDeath extends ConductorScene {
        private static final float ROLL = 0.9f, CAP_OFF = 1.5f, SWALLOW = 3.3f;
        private final TextureRegion bare;
        private final Piece cap;
        private float capX, capY, capScale;
        private boolean off;

        ConductorDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, 0);
            int[][] rest = FairDesertPortraits.without(EnemyKind.CONDUCTEUR, FairDesertPortraits.Prop.CAP);
            cap = lift(FairDesertPortraits.pixels(EnemyKind.CONDUCTEUR), rest);
            bare = pixels(rest);
        }

        @Override protected void restart() { off = false; }
        @Override protected float decorLight() { return 0.62f - 0.2f * progress(SWALLOW - 0.4f, SWALLOW + 0.4f); }

        private float roll() { return Interpolation.pow2In.apply(progress(ROLL, SWALLOW)); }

        @Override
        protected void step(float delta) {
            if (at(ROLL)) rumble(0.3f, 6f);
            if (time > ROLL && time < 2.4f) {                                               // les freins qui crissent en vain
                sparks(wheelX(-1), wheelY(), 2, hex("ffb84a"), 400f * poseScale + 100f, 6f);
                sparks(wheelX(1), wheelY(), 2, hex("ffb84a"), 400f * poseScale + 100f, 6f);
            }
            if (at(CAP_OFF + 1.1f)) rumble(0.1f, 3f);
            if (time > SWALLOW && time < SWALLOW + 0.8f && random.nextFloat() < 0.5f) {       // le souffle de brume
                Particle mist = particle(decorX(RAILS_X) + (random.nextFloat() - 0.5f) * 200f, decorY(RAILS_Y + 10f), 90f, 140f, 1.6f, 140f);
                mist.color.set(0.7f, 0.8f, 1f, 0.3f);
                glows.add(mist);
            }
        }

        @Override
        protected void back(Batch batch) {
            ride(roll());
            poseTilt = 4f * MathUtils.sin(time * 18f) * progress(ROLL, ROLL + 0.3f) * (1f - roll());
            lantern(batch, 1f - progress(0.2f, 0.5f));
            if (time >= CAP_OFF && !off && cap != null) {                                    // le vent emporte sa vraie casquette
                off = true;
                capX = pieceX(cap); capY = pieceY(cap); capScale = cartScale();
                wear(bare);
            }
            showBoss = time < SWALLOW + 0.3f;
        }

        @Override
        protected void front(Batch batch) {
            if (showBoss) drawCart(batch, WHITE);
            float swallow = progress(SWALLOW - 0.5f, SWALLOW + 0.3f);                       // le noir du tunnel l'avale
            if (swallow > 0f) {
                float s = decorScale();
                rect(batch, hex("05030a"), swallow, decorX(78f), decorY(160f), 204f * s, 108f * s);
            }
            if (cap == null) return;
            if (!off) { attached(batch, cap, 1f); return; }
            float k = progress(CAP_OFF, CAP_OFF + 1.1f), land = Interpolation.bounceOut.apply(k);
            float x = MathUtils.lerp(capX, bossX() - bossWidth() * 0.35f, Interpolation.pow2Out.apply(k));
            float rest = groundY() + 3f * bossScale();
            float y = MathUtils.lerp(capY, rest, land) + worldHeight() * 0.25f * MathUtils.sin(Math.min(1f, k * 1.6f) * MathUtils.PI) * (1f - k);
            float scale = MathUtils.lerp(capScale, bossScale() * 1.15f, Interpolation.pow2Out.apply(k));
            draw(batch, cap.region, x, y, scale, 540f * Interpolation.pow2Out.apply(Math.min(1f, k * 1.3f)) - 8f * land, WHITE, 1f);
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
                rect(batch, i % 2 == 0 ? hex("e86a8a") : GOLD, 1f - swords, x - 30f, y - 24f, 60f, 48f);
                if (swords > 0f) {
                    draw(batch, sword, x - 18f, y, iconScale(0.06f), 0f, WHITE, swords);
                    draw(batch, sword, x + 18f, y, iconScale(0.06f), 0f, WHITE, swords);
                }
            }
            float blade = progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f);                       // sa flèche devient une lame
            if (blade > 0f) {
                float top = portraitY(0f);
                line(batch, hex("dce4ee"), blade, bossX() + poseX, top - 40f, bossX() + poseX, top + 160f * blade, 26f);
                line(batch, WHITE, blade, bossX() + poseX - 4f, top - 40f, bossX() + poseX - 4f, top + 150f * blade, 8f);
            }
            eyes(batch, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f), hex("ff3a3a"), new float[] {15f, 18f}, new float[] {20f, 18f});
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
            tomato = art("ogrw", new Color[] {hex("6a0a0a"), hex("3a8a2a"), hex("d8202c"), hex("ff8a8a")}, TOMATO);
            splat = art("rs", new Color[] {hex("c8102e"), hex("f0d060")}, SPLAT);
            Random seed = new Random(11);
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
                sparks(hubX(), hubY(), 50, hex("ff4a4a"), 800f, 16f);
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
            flash(batch, hex("ff4a4a"), CLIMAX_DEATH, 0.3f, 0.4f);
        }
    }

    // -------------------------------------------------------------------------
    // Roi des forains
    // -------------------------------------------------------------------------

    static final String[] BALL = {".ooo.", "owwho", "owwwo", "owwwo", ".ooo."};
    static final String[] CAN = {
        "ooooooooooo", "ohhcccccsso", "ohcccccccso", "ohcccccccso", "odddddddddo", "ohcccccccso", "ohcccccccso", "ohcccccccso",
        "ohhcccccsso", "ooooooooooo"};
    /** Le chamboule-tout du décor des stands : chaque boîte (coin haut-gauche en pixels du décor, rouge ou non). */
    static final float[][] CANS = {
        {292f, 102f, 1f}, {304f, 102f, 0f},
        {286f, 112f, 0f}, {297f, 112f, 1f}, {308f, 112f, 0f},
        {280f, 122f, 1f}, {292f, 122f, 0f}, {303f, 122f, 1f}, {315f, 122f, 0f}};
    /** Le centre de la pyramide de boîtes, et le fond du stand derrière elle (pixels du décor). */
    static final float CANS_X = 303f, CANS_Y = 116f;
    static final Color STALL = hex("2e1a1a");

    /** Le Roi des forains, son stand de chamboule-tout (celui du décor) et ses balles. */
    abstract static class FairKingScene extends BossScene {
        protected final TextureRegion ball, redCan, greyCan, card;

        FairKingScene(CutsceneKit kit, Moment moment, int phase) {
            super(kit, moment, EnemyKind.ROI_FORAIN, phase);
            ball = art("owh", new Color[] {hex("3a2a1e"), hex("f6f0e2"), hex("ffffff")}, BALL);
            redCan = art("ohcsd", new Color[] {hex("1a1018"), hex("f08070"), hex("c84634"), hex("8a2a20"), hex("ffd54a")}, CAN);
            greyCan = art("ohcsd", new Color[] {hex("1a1018"), hex("e0e0e8"), hex("a8a8b0"), hex("6a6a74"), hex("d8202c")}, CAN);
            card = gameCard(FAIR_KING_CARD);
        }

        protected float handX() { return portraitX(29f); }
        protected float handY() { return portraitY(29f); }

        /** Une balle lancée de sa main à {@code start}, qui arrive en ({@code tx}, {@code ty}) en {@code flight} secondes. */
        protected void throwBall(Batch batch, float start, float flight, float tx, float ty, float arc) {
            float k = progress(start, start + flight);
            if (k <= 0f || k >= 1f) return;
            float x = MathUtils.lerp(handX(), tx, k), y = MathUtils.lerp(handY(), ty, k) + arc * MathUtils.sin(k * MathUtils.PI);
            draw(batch, ball, x, y, decorScale() * 2.6f, 720f * k, WHITE, 1f);
        }

        /** La boîte {@code i} du chamboule-tout, à sa place dans le décor (déplacée de dx, dy, tournée de rot). */
        protected void drawCan(Batch batch, int i, float dx, float dy, float rot) {
            float[] c = CANS[i];
            float s = decorScale();
            draw(batch, c[2] > 0f ? redCan : greyCan, decorX(c[0] + 5.5f) + dx, decorY(c[1] + 5f) + dy, s, rot,
                litLikeDecor(WHITE, tmp), 1f);
        }
    }

    /**
     * « Tout est truqué : Lot truqué ! » : il lance deux balles sur le chamboule-tout de son stand ; elles
     * rebondissent, pas une boîte ne bouge ; les boulons qui les tiennent rougissent, les lampes aussi, les
     * peluches du lot ouvrent des yeux méchants, sa couronne se hérisse.
     */
    static final class FairKingPhase extends FairKingScene {
        private static final float[] THROWS = {0.3f, 0.95f};
        private static final float FLIGHT = 0.4f, BOLTS = 1.5f;
        private final TextureRegion bear;

        FairKingPhase(CutsceneKit kit) {
            super(kit, Moment.PHASE, 2);
            bear = art("obkm", new Color[] {hex("3a2412"), hex("c8904a"), hex("e0283a"), hex("5a2a1a")}, BEAR);
        }

        @Override
        protected void step(float delta) {
            for (float t : THROWS) if (at(t + FLIGHT)) {                                     // clang : la balle rebondit
                rumble(0.12f, 4f);
                sparks(decorX(CANS_X), decorY(CANS_Y), 14, Color.WHITE, 400f, 7f);
            }
            if (at(BOLTS)) sparks(decorX(CANS_X), decorY(CANS_Y), 24, RED, 300f, 8f);
            if (at(CLIMAX_PHASE)) { rumble(0.6f, 14f); sparks(bossX(), bossY(), 40, RED, 700f, 14f); }
        }

        @Override
        protected void back(Batch batch) {
            float red = progress(BOLTS, CLIMAX_PHASE);
            for (int l = 0; l < 6; l++) {                                                    // les lampes, qui virent au rouge
                tmp.set(hex("ffb84a")).lerp(RED, red);
                glow(batch, worldWidth() * (0.1f + 0.16f * l), worldHeight() * 0.88f, 220f, tmp, 0.5f);
            }
            float bolts = progress(BOLTS, BOLTS + 0.3f);                                      // les boulons qui tiennent les boîtes
            for (int i = 0; i < CANS.length && bolts > 0f; i++) {
                float x = decorX(CANS[i][0] + 5.5f), y = decorY(CANS[i][1] + 9f), s = decorScale();
                glow(batch, x, y, 22f * s, RED, bolts * pulse(8f, 0.5f));
                rect(batch, hex("dce4ee"), bolts, x - 2f * s, y - 1.5f * s, 4f * s, 3f * s);
            }
            float rise = Interpolation.swingOut.apply(progress(CLIMAX_PHASE + 0.2f, CLIMAX_PHASE + 0.8f));
            for (int b = 0; b < 6; b++) {                                                    // les peluches du lot, derrière lui
                float x = bossX() + (b - 2.5f) * bossWidth() * 0.42f, y = groundY() + 80f + 60f * rise * (b % 2);
                draw(batch, bear, x, y, fit(180f, 10), 0f, WHITE, 0.4f + 0.6f * rise);
                if (rise > 0f) {
                    glow(batch, x - 30f, y + 10f, 60f, RED, rise);
                    glow(batch, x + 30f, y + 10f, 60f, RED, rise);
                }
            }
            float pitch = 0f;
            for (float t : THROWS) pitch = Math.max(pitch, gesture(t - 0.2f, t + 0.15f));
            poseX = 30f * pitch;
            poseTilt = -8f * pitch;
        }

        @Override
        protected void front(Batch batch) {
            for (float t : THROWS) {
                throwBall(batch, t, FLIGHT, decorX(CANS_X), decorY(CANS_Y), worldHeight() * 0.12f);
                float back = progress(t + FLIGHT, t + FLIGHT + 0.5f);                          // elle rebondit, la pyramide ne bouge pas
                if (back > 0f && back < 1f) {
                    float x = decorX(CANS_X) - back * worldWidth() * 0.3f;
                    float y = decorY(CANS_Y) + worldHeight() * 0.1f * MathUtils.sin(back * MathUtils.PI) - back * back * worldHeight() * 0.5f;
                    draw(batch, ball, x, y, decorScale() * 2.6f, -500f * back, WHITE, 1f);
                }
            }
            float shine = progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.3f);                       // sa carte truquée luit
            if (shine > 0f) glow(batch, portraitX(30f), portraitY(24f), 260f, RED, 0.6f * shine * pulse(8f, 0.5f));
            onPortrait(batch, card, CARD_LEFT, CARD_TOP, CARD_PIXELS, 1f);                  // un vrai 2 de Carreau
            float spikes = progress(CLIMAX_PHASE + 0.3f, CLIMAX_PHASE + 0.6f);               // sa couronne se hérisse
            for (int k = 0; k < 5 && spikes > 0f; k++) {
                float x = portraitX(10f + k * 3.5f), y = portraitY(1f);
                line(batch, hex("dce4ee"), spikes, x, y, x + (k - 2) * 10f, y + 70f * spikes, 10f);
            }
            eyes(batch, progress(CLIMAX_PHASE, CLIMAX_PHASE + 0.2f), RED, new float[] {14f, 16f}, new float[] {21f, 16f});
            flash(batch, RED, CLIMAX_PHASE, 0.3f, 0.35f);
        }
    }

    /**
     * « Le Roi des Forains a perdu la main » : sa dernière balle rate tout ; les boulons sautent et son
     * chamboule-tout s'écroule tout seul ; ses manches se vident de toutes ses cartes cachées, sa vraie carte
     * truquée lui glisse des doigts, sa vraie couronne tombe sur le comptoir ; il s'affaisse, et un ticket doré
     * flotte vers toi.
     */
    static final class FairKingDeath extends FairKingScene {
        private static final float MISS = 0.3f, TOPPLE = 1.1f, SLEEVES = 1.5f, DROP_CARD = 2.0f, DROP_CROWN = CLIMAX_DEATH;
        private static final String[] HIDDEN = {
            "cards/light/1-P.png", "cards/light/1-H.png", "cards/light/1-C.png", "cards/light/1-D.png", "cards/light/13-H.png",
            "cards/light/12-P.png", "cards/light/13-C.png", "cards/light/1-H.png"};
        private final TextureRegion ticket, noCard, bare;
        private final TextureRegion[] hidden = new TextureRegion[HIDDEN.length];
        private final Piece crown;
        private final float[] canVx = new float[CANS.length], canSpin = new float[CANS.length];
        private float cardX, cardY, crownX, crownY;
        private boolean cardOff, crownOff;

        FairKingDeath(CutsceneKit kit) {
            super(kit, Moment.DEATH, 0);
            ticket = art("oyr", new Color[] {hex("8a5a10"), hex("ffd23a"), hex("fff2a0")}, TICKET);
            for (int i = 0; i < HIDDEN.length; i++) hidden[i] = gameCard(HIDDEN[i]);
            int[][] withoutCard = FairDesertPortraits.without(EnemyKind.ROI_FORAIN, FairDesertPortraits.Prop.KING_CARD);
            int[][] none = FairDesertPortraits.without(EnemyKind.ROI_FORAIN, FairDesertPortraits.Prop.KING_CARD, FairDesertPortraits.Prop.CROWN);
            crown = lift(withoutCard, none);
            noCard = pixels(withoutCard);
            bare = pixels(none);
            Random seed = new Random(5);
            for (int i = 0; i < CANS.length; i++) { canVx[i] = 80f + 260f * seed.nextFloat(); canSpin[i] = (seed.nextFloat() - 0.3f) * 500f; }
        }

        @Override protected void restart() { cardOff = crownOff = false; }

        @Override
        protected void step(float delta) {
            if (at(TOPPLE)) { rumble(0.3f, 6f); sparks(decorX(CANS_X), decorY(CANS_Y), 30, hex("dce4ee"), 400f, 6f); }
            if (at(SLEEVES)) {                                                                // ses manches se vident
                for (int i = 0; i < hidden.length; i++) {
                    boolean left = i % 2 == 0;
                    float x = portraitX(left ? 6f : 27f), y = portraitY(33f), dir = left ? -1f : 1f;
                    Particle c = place(hidden[i], x, y, dir * (80f + 220f * random.nextFloat()), 250f + 300f * random.nextFloat(),
                        fit(110f, 27), 2.6f);
                    c.gravity = 900f;
                    c.spin = dir * (100f + 300f * random.nextFloat());
                }
            }
            if (at(DROP_CROWN + 0.55f)) { rumble(0.3f, 6f); sparks(crownRestX(), crownRestY(), 24, GOLD, 400f, 10f); }
        }

        private float crownRestX() { return bossX() - bossWidth() * 0.75f; }
        private float crownRestY() { return bossBottom() + 16f + (crown == null ? 0f : crown.height * bossScale() / 2f); }

        @Override
        protected void back(Batch batch) {
            poseX = 50f * gesture(MISS - 0.2f, MISS + 0.2f);                               // il lance... à côté
            poseTilt = -12f * gesture(MISS - 0.2f, MISS + 0.2f) + 10f * progress(DROP_CROWN, DROP_CROWN + 0.8f);
            poseSink = 0.38f * Interpolation.pow2Out.apply(progress(DROP_CROWN + 0.2f, 3.4f)); // il s'affaisse derrière son comptoir
            poseShadow = 0.35f * progress(DROP_CROWN, 3.4f);
            if (time >= DROP_CARD && !cardOff) {                                               // sa vraie carte lui glisse des doigts
                cardOff = true;
                cardX = portraitX(CARD_LEFT + 19f * CARD_PIXELS / 2f); cardY = portraitY(CARD_TOP + 27f * CARD_PIXELS / 2f);
                wear(noCard);
            }
            if (time >= DROP_CROWN && !crownOff && crown != null) {                            // puis sa vraie couronne
                crownOff = true;
                crownX = pieceX(crown); crownY = pieceY(crown);
                wear(bare);
            }
            if (time >= TOPPLE) {                                                             // le chamboule-tout s'écroule tout seul
                decorRect(batch, STALL, 279f, 101f, 48f, 31f);
                for (int i = 0; i < CANS.length; i++) {
                    float t = Math.max(0f, time - TOPPLE - 0.04f * (CANS.length - i));
                    drawCan(batch, i, canVx[i] * t * (i % 2 == 0 ? 1f : -0.6f), 200f * t - 1100f * t * t, canSpin[i] * t);
                }
            }
        }

        @Override
        protected void front(Batch batch) {
            throwBall(batch, MISS, 0.6f, worldWidth() + 100f, worldHeight() * 0.95f, worldHeight() * 0.1f);   // ratée
            if (!cardOff) onPortrait(batch, card, CARD_LEFT, CARD_TOP, CARD_PIXELS, 1f);
            rect(batch, hex("6a3a1a"), 1f, -MARGIN, -MARGIN, worldWidth() + 2f * MARGIN, bossBottom() + MARGIN + 14f); // le comptoir
            rect(batch, GOLD, 1f, -MARGIN, bossBottom() + 4f, worldWidth() + 2f * MARGIN, 12f);
            if (cardOff) {                                                                    // la carte truquée, sur le comptoir
                float k = progress(DROP_CARD, DROP_CARD + 0.7f);
                float x = MathUtils.lerp(cardX, bossX() + bossWidth() * 0.7f, k);
                float y = MathUtils.lerp(cardY, bossBottom() + 26f + 13.5f * CARD_PIXELS * bossScale(), Interpolation.pow2In.apply(k)) + 30f * MathUtils.sin(k * MathUtils.PI);
                draw(batch, card, x, y, bossScale() * CARD_PIXELS * MathUtils.lerp(1f, 1.3f, k), 360f * k + 80f * k, WHITE, 1f);
            }
            if (crownOff) {                                                                   // la couronne roule sur le comptoir
                float k = progress(DROP_CROWN, DROP_CROWN + 0.55f), roll = progress(DROP_CROWN + 0.55f, DROP_CROWN + 1.2f);
                float x = MathUtils.lerp(crownX, crownRestX(), Interpolation.pow2Out.apply(Math.min(1f, (k + roll) / 1.6f)));
                float y = MathUtils.lerp(crownY, crownRestY(), Interpolation.bounceOut.apply(k));
                draw(batch, crown.region, x, y, bossScale(), 35f * k - 35f * Interpolation.pow2Out.apply(roll) + 20f * roll, WHITE, 1f);
            }
            float drift = progress(3.4f, 5.2f);                                              // le ticket doré
            if (drift > 0f) {
                float x = worldWidth() / 2f + 120f * MathUtils.sin(drift * 6f), y = MathUtils.lerp(worldHeight() * 0.9f, worldHeight() * 0.4f, drift);
                draw(batch, ticket, x, y, fit(70f + 80f * drift, 5), 12f * MathUtils.sin(drift * 8f), WHITE, 1f);
                glow(batch, x, y, 260f, GOLD, 0.5f);
            }
        }
    }
}
