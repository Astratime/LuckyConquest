package fr.astratime.lucky.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

/**
 * Effet de particules joué à l'endroit cliqué sur une carte : à chaque appel de
 * {@link #play(float, float)}, PARTICLE_COUNT particules sont tirées, chacune
 * vient du pool d'une couleur prise au hasard (une seule particule par fichier,
 * voir son Count max:1), dont la direction (0-360°, champ Angle des fichiers .p)
 * est elle-même tirée au hasard par LibGDX.
 *
 * Chaque couleur est pré-chauffée (pool.fill()) à la construction pour qu'aucune
 * allocation n'ait lieu au moment du clic — seule source possible d'un décalage
 * visible entre la disparition de la carte et l'apparition des particules.
 */
public class CardClickParticles implements Disposable {

    private static final String   PARTICLE_DIR = "particles/";
    private static final String[] EFFECT_PATHS = {
        PARTICLE_DIR + "jackpot.p",
        PARTICLE_DIR + "jackpot-red.p",
        PARTICLE_DIR + "jackpot-blue.p",
        PARTICLE_DIR + "jackpot-green.p",
        PARTICLE_DIR + "jackpot-purple.p",
    };
    private static final int PARTICLE_COUNT        = 10;
    // Nombre d'instances pré-allouées (et rechargées) par couleur : évite toute
    // allocation au moment du clic, seule source possible d'un décalage visible
    // entre la disparition de la carte et l'apparition des particules.
    private static final int POOL_INITIAL_CAPACITY = 10;
    private static final int POOL_MAX              = 20;

    /** Un gabarit par couleur, chargé une fois ; sert uniquement à construire son pool et à libérer sa texture dans dispose(). */
    private final List<ParticleEffect> sources = new ArrayList<>();
    /** Un pool par couleur : obtain() renvoie une instance déjà démarrée, neuve ou recyclée, sans allocation une fois pré-chauffé. */
    private final List<ParticleEffectPool> pools = new ArrayList<>();
    /** Instances en cours d'animation, libérées vers leur pool d'origine au fur et à mesure qu'elles se terminent. */
    private final List<ParticleEffectPool.PooledEffect> active = new ArrayList<>();

    public CardClickParticles() {
        for (String path : EFFECT_PATHS) {
            ParticleEffect source = new ParticleEffect();
            source.load(Gdx.files.internal(path), Gdx.files.internal(PARTICLE_DIR));
            sources.add(source);

            ParticleEffectPool pool = new ParticleEffectPool(source, POOL_INITIAL_CAPACITY, POOL_MAX);
            pool.fill(POOL_INITIAL_CAPACITY); // alloue maintenant, pas au premier clic
            pools.add(pool);
        }
    }

    /** Tire PARTICLE_COUNT particules à la position donnée (coordonnées du Stage). */
    public void play(float stageX, float stageY) {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            ParticleEffectPool pool = pools.get(MathUtils.random(pools.size() - 1));
            ParticleEffectPool.PooledEffect effect = pool.obtain();
            effect.setPosition(stageX, stageY);
            active.add(effect);
        }
    }

    /**
     * Met à jour et dessine toutes les particules actives, puis libère celles qui sont
     * terminées vers leur pool. Parcours indexé à l'envers (plutôt qu'un Iterator) pour
     * ne rien allouer dans cette boucle appelée à chaque frame.
     */
    public void render(SpriteBatch batch, float delta) {
        for (int i = active.size() - 1; i >= 0; i--) {
            ParticleEffectPool.PooledEffect effect = active.get(i);
            effect.update(delta);
            effect.draw(batch);
            if (effect.isComplete()) {
                active.remove(i);
                effect.free(); // revient dans son pool d'origine, prête à être réobtenue sans allocation
            }
        }
    }

    /** Libère les textures des gabarits (seuls à les posséder réellement — voir ParticleEffect.dispose()). */
    @Override
    public void dispose() {
        sources.forEach(ParticleEffect::dispose);
    }
}
