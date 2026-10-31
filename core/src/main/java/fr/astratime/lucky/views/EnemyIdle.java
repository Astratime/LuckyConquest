package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import fr.astratime.lucky.entities.enemy.EnemyKind;

import java.util.ArrayList;
import java.util.List;

/**
 * Vue de face : l'animation propre à chaque ennemi, jouée en boucle sur son
 * portrait agrandi ({@link FrontScene}). Le clown jongle et fait tomber ses
 * balles, la Roue de la fortune tourne, le gardien du phare fait briller sa
 * lanterne...
 *
 * <p>Une animation agit de deux façons, sans redessiner le portrait :
 * <ul>
 *   <li>elle bouge des bandes de rangées du portrait ({@link Pose#band}) : un
 *       chapeau qui se soulève, une mâchoire qui claque, une roue qui tourne ;</li>
 *   <li>elle dessine par-dessus des objets et des effets en pixel art, à la taille
 *       des pixels du portrait ({@link Fx}) : balles, bulles, étincelles, ondes...</li>
 * </ul>
 * Les coordonnées sont en pixels du portrait (34 x 42), origine en haut à gauche.
 */
public abstract class EnemyIdle {

    /** Ce que l'animation change au portrait à l'instant {@code t}. */
    public static final class Pose {
        /** Décalage de tout l'ennemi, et de sa tête seule (pixels du portrait), rotation en plus (degrés). */
        public float dx, dy, headDx, headDy, rotation;
        /** Opacité de l'ennemi (fantôme qui pâlit). */
        public float alpha = 1f;
        /** Bandes du portrait dessinées à part, déplacées et tournées. */
        public final List<Band> bands = new ArrayList<>();

        /** Une bande de rangées {@code [top, bottom)}, décalée et tournée autour de (pivotX, pivotY). */
        public static final class Band {
            public int top, bottom;
            public float dx, dy, rotation, pivotX, pivotY;
        }

        private final List<Band> pool = new ArrayList<>();

        void reset() {
            dx = dy = headDx = headDy = rotation = 0f;
            alpha = 1f;
            pool.addAll(bands);
            bands.clear();
        }

        /** Ajoute une bande à déplacer : les rangées {@code [top, bottom)}. */
        public Band band(int top, int bottom, float dx, float dy, float rotation, float pivotX, float pivotY) {
            Band band = pool.isEmpty() ? new Band() : pool.remove(pool.size() - 1);
            band.top = top;
            band.bottom = bottom;
            band.dx = dx;
            band.dy = dy;
            band.rotation = rotation;
            band.pivotX = pivotX;
            band.pivotY = pivotY;
            bands.add(band);
            return band;
        }
    }

    /** Dessin en pixels du portrait, par-dessus ou derrière l'ennemi. */
    public interface Fx {
        /** Rectangle plein, coin haut gauche en (x, y). */
        void rect(float x, float y, float width, float height, Color color, float alpha);

        /** Halo lumineux (additif) centré en (x, y), de rayon {@code radius}. */
        void glow(float x, float y, float radius, Color color, float alpha);

        /** Disque en pixels, détouré de sombre, centré en (x, y). */
        default void disc(float x, float y, float radius, Color color, float alpha) {
            int r = Math.max(1, Math.round(radius));
            for (int dy = -r - 1; dy <= r; dy++) {
                float half = (float) Math.sqrt(Math.max(0f, (r + 0.5f) * (r + 0.5f) - (dy + 0.5f) * (dy + 0.5f)));
                if (half <= 0f) continue;
                int w = Math.round(half);
                rect(x - w - 1, y + dy, w * 2 + 2, 1, OUTLINE, alpha);
            }
            for (int dy = -r; dy < r; dy++) {
                float half = (float) Math.sqrt(Math.max(0f, r * r - (dy + 0.5f) * (dy + 0.5f)));
                int w = Math.round(half);
                if (w > 0) rect(x - w, y + dy, w * 2, 1, color, alpha);
            }
            rect(x - r * 0.5f, y - r * 0.6f, 1, 1, Color.WHITE, alpha * 0.8f);
        }

        /** Étincelle en croix. */
        default void sparkle(float x, float y, float size, Color color, float alpha) {
            rect(x - size, y - 0.5f, size * 2 + 1, 1, color, alpha);
            rect(x - 0.5f + 0.5f, y - size, 1, size * 2 + 1, color, alpha);
            rect(x, y - 0.5f + 0.5f - 0.5f, 1, 1, Color.WHITE, alpha);
        }
    }

    static final Color OUTLINE = Color.valueOf("22141aff");
    static final Color GOLD    = Color.valueOf("ffd54aff");
    static final Color WHITE   = new Color(Color.WHITE);
    static final Color RED     = Color.valueOf("e8323cff");
    static final Color BLUE    = Color.valueOf("3a6ae8ff");
    static final Color YELLOW  = Color.valueOf("ffd23aff");
    static final Color GREEN   = Color.valueOf("6ae87aff");
    static final Color FOAM    = Color.valueOf("f6f0e2ff");
    static final Color STEEL   = Color.valueOf("dce4f0ff");
    static final Color LIGHT   = Color.valueOf("fff2a8ff");

    /** Change le portrait à l'instant {@code t} (secondes depuis le début du combat). */
    public void pose(float t, Pose pose) {}

    /** Dessine derrière l'ennemi. */
    public void drawBack(Fx fx, float t) {}

    /** Dessine devant l'ennemi. */
    public void drawFront(Fx fx, float t) {}

    /** @return l'animation de l'ennemi {@code kind}, ou {@code null} s'il n'a que la respiration commune. */
    public static EnemyIdle of(EnemyKind kind) {
        return switch (kind) {
            case CLOWN          -> new Juggler();
            case MONSIEUR_LOYAL -> new HatTip(0, 11, 15f, new float[] {26.5f, 18.5f});
            case SQUELETTE      -> new Chatter(21, 26, new float[][] {{12f, 13f}, {19f, 13f}});
            case CONDUCTEUR     -> new GhostLantern();
            case FORAIN         -> new Confetti(29f, 24f);
            case ROUE_FORTUNE   -> new WheelSpin();
            case BONIMENTEUR    -> new Megaphone();
            case ROI_FORAIN     -> new CardTrick();
            case RAT_CALES      -> new Sniff(false);
            case CAPITAINE_RAT  -> new Sniff(true);
            case BUVEUR         -> new Hiccup();
            case TAVERNIER      -> new Mustache();
            case GUETTEUR       -> new Lookout();
            case GARDIEN_PHARE  -> new Lighthouse();
            case PIRATE         -> new PirateLaugh(31f, 15f, 41f, false);
            case CAPITAINE_NOIR -> new PirateLaugh(28.5f, 19f, 41f, true);
            default             -> null;
        };
    }

    // ---------------------------------------------------------------------------------------------------- outils

    /** @return la phase {@code [0, 1)} de {@code t} dans un cycle de {@code period} secondes. */
    static float phase(float t, float period) { return (t % period) / period; }

    /** Montée puis descente douce sur {@code [0, 1]} (0 aux bouts, 1 au milieu). */
    static float bump(float u) { return u <= 0f || u >= 1f ? 0f : MathUtils.sin(u * MathUtils.PI); }

    /** Lueur d'un reflet qui file le long d'une lame verticale, toutes les {@code period} secondes. */
    static void bladeGlint(Fx fx, float t, float x, float top, float bottom, float period) {
        float u = phase(t, period) * 2.5f;
        if (u > 1f) return;
        float y = top + (bottom - top) * u;
        fx.rect(x, y, 1, 2, WHITE, 0.9f);
        if (u < 0.15f) fx.sparkle(x, top, 2, WHITE, 1f - u / 0.15f);
    }

    // ---------------------------------------------------------------------------------------------------- la Foire

    /**
     * Le clown jongle : trois balles passent par-dessus sa tête, de gauche à
     * droite, et reviennent derrière lui ; puis il les fait tomber, a honte, et
     * recommence.
     */
    static final class Juggler extends EnemyIdle {
        private static final float JUGGLE = 5.4f, DROP = 1.2f, SHAME = 1.4f, CYCLE = JUGGLE + DROP + SHAME;
        private static final float LEFT = 1f, RIGHT = 33f, HAND_Y = 24f, PEAK = -1f, BACK_Y = 27f, THROW = 0.9f;
        private static final Color[] BALLS = {RED, BLUE, YELLOW};

        @Override
        public void pose(float t, Pose pose) {
            float c = t % CYCLE;
            if (c > JUGGLE + DROP) { // il secoue la tête, penaud
                float u = (c - JUGGLE - DROP) / SHAME;
                pose.headDx = MathUtils.sin(u * MathUtils.PI2 * 3f) * bump(u) * 1.2f;
                pose.headDy = bump(u) * 1f;
            } else if (c < JUGGLE) {
                pose.headDx = MathUtils.sin(c * MathUtils.PI2 / (THROW * 2f / 3f)) * 0.4f; // il suit les balles des yeux
            } else {
                pose.headDy = -bump((c - JUGGLE) / DROP) * 1.5f; // oups : il se redresse d'un coup
            }
        }

        @Override
        public void drawBack(Fx fx, float t) { draw(fx, t, false); }

        @Override
        public void drawFront(Fx fx, float t) { draw(fx, t, true); }

        private static void draw(Fx fx, float t, boolean front) {
            float c = t % CYCLE;
            for (int i = 0; i < BALLS.length; i++) {
                float s = Math.min(c, JUGGLE) / THROW + i * 2f / 3f; // trois balles décalées, en cascade
                float u = s % 2f;
                boolean over = u < 1f; // par-dessus la tête (devant) ; le retour passe derrière lui
                float x, y;
                if (over) {
                    x = LEFT + (RIGHT - LEFT) * u;
                    y = HAND_Y - (HAND_Y - PEAK) * 4f * u * (1f - u);
                } else {
                    float k = u - 1f;
                    x = RIGHT - (RIGHT - LEFT) * k;
                    y = HAND_Y + (BACK_Y - HAND_Y) * 4f * k * (1f - k);
                }
                if (c > JUGGLE) { // elles tombent de là où elles étaient, toutes devant
                    float f = c - JUGGLE;
                    x += (x < 17f ? -5f : 5f) * f;
                    y += 30f * f * f + 3f * f;
                    if (y > 46f || !front) continue; // derrière la table
                } else if (over != front) continue;
                fx.disc(x, y, 1.5f, BALLS[i], 1f);
            }
        }
    }

    /** Il soulève son chapeau (les rangées du chapeau), le penche, le repose ; sa canne brille. */
    static final class HatTip extends EnemyIdle {
        private final int top, bottom;
        private final float tilt;
        private final float[] cane;

        HatTip(int top, int bottom, float tilt, float[] cane) {
            this.top = top;
            this.bottom = bottom;
            this.tilt = tilt;
            this.cane = cane;
        }

        @Override
        public void pose(float t, Pose pose) {
            float u = phase(t, 4.6f) * 4.6f / 1.6f; // 1,6 s de salut toutes les 4,6 s
            float lift = bump(Math.min(u, 1f));
            if (lift > 0f) pose.band(top, bottom, 0f, -lift * 5f, -lift * tilt, 17f, bottom);
            pose.headDy = -lift * 0.5f; // il incline un peu la tête
        }

        @Override
        public void drawFront(Fx fx, float t) {
            float s = bump(phase(t + 1.3f, 3.1f) * 3.1f / 0.5f);
            if (s > 0f) fx.sparkle(cane[0], cane[1], 2f, LIGHT, s);
        }
    }

    /** Sa mâchoire claque (les rangées des dents), ses yeux verts luisent. */
    static final class Chatter extends EnemyIdle {
        private final int jawTop, jawBottom;
        private final float[][] eyes;

        Chatter(int jawTop, int jawBottom, float[][] eyes) {
            this.jawTop = jawTop;
            this.jawBottom = jawBottom;
            this.eyes = eyes;
        }

        @Override
        public void pose(float t, Pose pose) {
            float c = t % 3.4f;
            if (c < 1.2f) { // il claque des dents, la tête secouée
                float open = MathUtils.sin(c * 40f) > 0f ? 1.5f : 0f;
                pose.band(jawTop, jawBottom, 0f, open, 0f, 17f, jawTop);
                pose.headDx = MathUtils.sin(c * 28f) * 0.4f;
            }
        }

        @Override
        public void drawFront(Fx fx, float t) {
            float a = 0.35f + 0.25f * MathUtils.sin(t * 3f);
            for (float[] e : eyes) fx.glow(e[0], e[1], 4f, GREEN, a);
        }
    }

    /** Le conducteur fantôme pâlit et revient ; sa lanterne verte palpite et lâche des volutes. */
    static final class GhostLantern extends EnemyIdle {
        @Override
        public void pose(float t, Pose pose) {
            pose.alpha = 0.8f + 0.2f * MathUtils.sin(t * 1.7f);
            pose.dy = MathUtils.sin(t * 1.1f) * 0.8f; // il flotte
        }

        @Override
        public void drawBack(Fx fx, float t) {
            for (int i = 0; i < 4; i++) {
                float u = phase(t + i * 0.9f, 3.6f);
                fx.disc(29f + MathUtils.sin(u * 6f + i) * 2f, 26f - u * 24f, 1f + u * 2f, Color.valueOf("9ad8a8ff"), (1f - u) * 0.6f);
            }
        }

        @Override
        public void drawFront(Fx fx, float t) {
            fx.glow(29.5f, 28.5f, 7f, GREEN, 0.45f + 0.2f * MathUtils.sin(t * 5f));
        }
    }

    /** Il fait éclater des confettis de sa main, de temps en temps. */
    static final class Confetti extends EnemyIdle {
        private static final Color[] COLORS = {RED, BLUE, YELLOW, GREEN, Color.valueOf("e86ad8ff")};
        private final float x, y;

        Confetti(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void pose(float t, Pose pose) {
            float u = phase(t, 3.2f);
            if (u < 0.08f) pose.headDy = -bump(u / 0.08f); // il sursaute de joie
        }

        @Override
        public void drawFront(Fx fx, float t) {
            float u = phase(t, 3.2f) * 3.2f;
            if (u > 1.6f) return;
            for (int i = 0; i < 14; i++) {
                float angle = i * 2.4f + 0.3f, speed = 9f + (i * 7 % 5) * 2f;
                float px = x + MathUtils.cos(angle) * speed * u;
                float py = y - MathUtils.sin(angle) * speed * u * 0.8f - 10f * u + 14f * u * u;
                fx.rect(px, py, 1, 1, COLORS[i % COLORS.length], 1f - u / 1.6f);
            }
        }
    }

    /** La roue s'élance, tourne vite, ralentit et s'arrête ; puis elle recommence. */
    static final class WheelSpin extends EnemyIdle {
        private static final float CYCLE = 6f;

        @Override
        public void pose(float t, Pose pose) {
            float c = t % CYCLE;
            float angle;
            if (c < 0.6f) angle = -c * c * 40f; // elle recule un peu, prise d'élan
            else if (c < 4.6f) {
                float u = (c - 0.6f) / 4f;
                angle = -14.4f + 1440f * (1f - (1f - u) * (1f - u)); // vite, puis de plus en plus lentement
            } else angle = 1425.6f;
            pose.band(4, 37, 0f, 0f, -angle, 17f, 21f);
        }

        @Override
        public void drawFront(Fx fx, float t) {
            float c = t % CYCLE;
            if (c > 4.6f && c < 5.6f) fx.sparkle(17f, 2f, 3f, LIGHT, bump((c - 4.6f))); // elle s'arrête : ding
        }
    }

    /** Il crie dans son porte-voix : des ondes partent vers la gauche, sa tête suit. */
    static final class Megaphone extends EnemyIdle {
        @Override
        public void pose(float t, Pose pose) {
            float u = phase(t, 2.4f);
            pose.headDx = -bump(Math.min(u * 3f, 1f)) * 0.8f;
        }

        @Override
        public void drawFront(Fx fx, float t) {
            for (int i = 0; i < 3; i++) {
                float u = phase(t + i * 0.3f, 2.4f) * 2.4f / 1.2f;
                if (u > 1f) continue;
                float r = 2f + u * 7f, a = 1f - u;
                float cx = 0f - u * 6f, cy = 23.5f;
                for (int k = -3; k <= 3; k++) { // arc d'onde tourné vers la gauche
                    float ang = k * 0.22f;
                    fx.rect(cx - MathUtils.cos(ang) * r, cy + MathUtils.sin(ang) * r * 1.4f, 1, 1, WHITE, a);
                }
            }
        }
    }

    /** Le roi forain fait apparaître des cartes en éventail au-dessus de sa main ; sa couronne brille. */
    static final class CardTrick extends EnemyIdle {
        @Override
        public void drawFront(Fx fx, float t) {
            float c = t % 4f;
            if (c < 2.4f) {
                float open = bump(c / 2.4f);
                for (int i = 0; i < 3; i++) {
                    float ang = (i - 1) * 0.5f * open;
                    float x = 29f + MathUtils.sin(ang) * 6f, y = 22f - MathUtils.cos(ang) * 6f * open;
                    fx.rect(x - 1.5f, y - 2f, 4, 5, OUTLINE, open);
                    fx.rect(x - 0.5f, y - 1f, 2, 3, WHITE, open);
                    fx.rect(x, y, 1, 1, i == 1 ? OUTLINE : RED, open);
                }
            }
            fx.sparkle(new float[] {9f, 17f, 25f}[(int) (t / 1.3f) % 3], 4f, 1.5f, WHITE, bump(phase(t, 1.3f)));
        }
    }

    // ---------------------------------------------------------------------------------------------------- le Port

    /** Le rat renifle (petits coups de tête rapides) et ses yeux rouges luisent ; le capitaine fait briller son sabre. */
    static final class Sniff extends EnemyIdle {
        private final boolean captain;

        Sniff(boolean captain) { this.captain = captain; }

        @Override
        public void pose(float t, Pose pose) {
            float c = t % 2.8f;
            if (c < 0.9f) {
                pose.headDy = MathUtils.sin(c * 45f) > 0f ? -0.6f : 0f;
                pose.headDx = 0.4f;
            }
        }

        @Override
        public void drawFront(Fx fx, float t) {
            float a = 0.3f + 0.2f * MathUtils.sin(t * 4f);
            fx.glow(13.5f, 17.5f, 3f, RED, a);
            fx.glow(19.5f, 17.5f, 3f, RED, a);
            if (captain) bladeGlint(fx, t, 31f, 23f, 40f, 3.3f);
            else {
                float u = phase(t, 2.8f); // miettes qui tombent pendant qu'il grignote
                for (int i = 0; i < 3; i++) {
                    float v = (u * 2.8f - 0.2f - i * 0.2f) / 0.9f;
                    if (v > 0f && v < 1f) fx.rect(16f + i * 1.5f, 27f + v * 14f, 1, 1, Color.valueOf("e0c088ff"), 1f - v);
                }
            }
        }
    }

    /** Le buveur a le hoquet : il sursaute ; la mousse de sa chope déborde en bulles. */
    static final class Hiccup extends EnemyIdle {
        @Override
        public void pose(float t, Pose pose) {
            float u = phase(t, 3.3f) * 3.3f;
            if (u < 0.25f) {
                pose.dy = -bump(u / 0.25f) * 2f;
                pose.rotation = -bump(u / 0.25f) * 2f;
            }
            pose.headDx = MathUtils.sin(t * 0.9f) * 0.8f; // il tangue
        }

        @Override
        public void drawFront(Fx fx, float t) {
            for (int i = 0; i < 5; i++) {
                float u = phase(t + i * 0.55f, 2.7f);
                fx.disc(25f + i * 1.1f + MathUtils.sin(u * 9f + i) * 1f, 25f - u * 14f, 0.8f + u * 0.6f, FOAM, 1f - u);
            }
            float u = phase(t, 3.3f) * 3.3f; // la bulle du hoquet
            if (u < 1f) fx.disc(13f - u * 2f, 22f - u * 10f, 1.5f, FOAM, 1f - u);
        }
    }

    /** Le tavernier fait frétiller sa moustache et frotte sa chope, qui brille. */
    static final class Mustache extends EnemyIdle {
        @Override
        public void pose(float t, Pose pose) {
            float c = t % 3f;
            if (c < 0.8f) pose.band(20, 23, MathUtils.sin(c * 30f) * 0.8f, -bump(c / 0.8f) * 0.8f, 0f, 17f, 21f);
        }

        @Override
        public void drawFront(Fx fx, float t) {
            float a = t * 5f; // le chiffon tourne sur la chope
            fx.rect(5f + MathUtils.cos(a) * 3f, 35f + MathUtils.sin(a) * 2f, 2, 1, WHITE, 0.9f);
            fx.sparkle(3f, 33f, 1.5f, WHITE, bump(phase(t, 1.7f)));
        }
    }

    /** Le guetteur balaie l'horizon à la longue-vue : sa tête pivote, la lentille lance un éclat. */
    static final class Lookout extends EnemyIdle {
        @Override
        public void pose(float t, Pose pose) {
            pose.headDx = MathUtils.sin(t * 0.8f) * 1.2f;
            pose.rotation = MathUtils.sin(t * 0.8f) * 1.5f;
        }

        @Override
        public void drawFront(Fx fx, float t) {
            float s = bump(phase(t, 2.5f) * 2.5f / 0.5f);
            if (s > 0f) {
                fx.glow(33f, 17f, 6f, LIGHT, s * 0.8f);
                fx.sparkle(33f, 17f, 3f, WHITE, s);
            }
        }
    }

    /** La lanterne du gardien brille fort et projette des rayons qui tournent. */
    static final class Lighthouse extends EnemyIdle {
        @Override
        public void drawBack(Fx fx, float t) {
            for (int k = 0; k < 3; k++) {
                float ang = t * 1.4f + k * MathUtils.PI2 / 3f;
                for (int r = 4; r < 14; r++) {
                    fx.rect(3.5f + MathUtils.cos(ang) * r, 14f + MathUtils.sin(ang) * r * 0.6f, 1, 1, LIGHT, 0.5f * (1f - r / 14f));
                }
            }
        }

        @Override
        public void drawFront(Fx fx, float t) {
            fx.glow(3.5f, 14f, 10f, LIGHT, 0.55f + 0.2f * MathUtils.sin(t * 6f) + 0.1f * MathUtils.sin(t * 17f));
        }
    }

    /** Le pirate rit (ses épaules sautent), son sabre lance un reflet ; le capitaine noir fume un peu. */
    static final class PirateLaugh extends EnemyIdle {
        private final float bladeX, bladeTop, bladeBottom;
        private final boolean smoke;

        PirateLaugh(float bladeX, float bladeTop, float bladeBottom, boolean smoke) {
            this.bladeX = bladeX;
            this.bladeTop = bladeTop;
            this.bladeBottom = bladeBottom;
            this.smoke = smoke;
        }

        @Override
        public void pose(float t, Pose pose) {
            float c = t % 4.2f;
            if (c < 1.1f) {
                pose.dy = MathUtils.sin(c * 32f) > 0f ? -0.8f : 0f; // ha ha ha
                pose.headDy = -bump(c / 1.1f) * 1f;
                pose.rotation = bump(c / 1.1f) * 1.5f;
            }
        }

        @Override
        public void drawFront(Fx fx, float t) {
            bladeGlint(fx, t, bladeX, bladeTop, bladeBottom, 2.9f);
            if (!smoke) return;
            for (int i = 0; i < 3; i++) {
                float u = phase(t + i * 1.1f, 3.3f);
                fx.disc(4f + MathUtils.sin(u * 5f + i) * 1.5f, 8f - u * 12f, 0.8f + u * 1.8f, Color.valueOf("8a8490ff"), (1f - u) * 0.6f);
            }
        }
    }
}
