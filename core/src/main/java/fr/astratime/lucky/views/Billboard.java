package fr.astratime.lucky.views;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;

import java.util.function.Supplier;

/**
 * Vue de face : des éléments « debout » sur la table (cartes, piles, échoppe,
 * Mise, bouclier…), tournés vers le joueur au lieu d'être couchés dans la
 * perspective. Le groupe est posé au point de la table {@link #anchor} (son
 * pied, en bas au milieu), à la taille de la table à cet endroit (plus petit
 * au loin), et à peine incliné : {@code tilt} est sa hauteur par rapport à sa
 * largeur (1 : bien droit).
 *
 * <p>Les clics le suivent (le groupe est réellement déplacé et agrandi). Ses
 * éléments gardent leurs coordonnées de la table à plat : leurs positions « du
 * Stage » restent celles de la vue classique, que les animations à plat
 * reportent sur l'écran ({@link TableProjection#toScreen}). En vue classique,
 * le groupe ne bouge pas.
 */
public class Billboard extends Group {

    protected final TableProjection projection;
    private final float             tilt;
    private final Supplier<Vector2> anchor;
    /** Décalage vertical à l'écran (une machine qui sort de la table), en unités du Stage. */
    protected float                 lift;

    /** @param anchor pied du groupe sur la table (coordonnées de la table à plat) */
    public Billboard(TableProjection projection, float tilt, Supplier<Vector2> anchor) {
        this.projection = projection;
        this.tilt       = tilt;
        this.anchor     = anchor;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        place();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        place();
        super.draw(batch, parentAlpha);
    }

    /** Pose le groupe à sa place à l'écran : son pied sur la table en perspective, à la taille du lieu. */
    protected void place() {
        if (!projection.isEnabled()) {
            setOrigin(0f, 0f);
            setPosition(0f, 0f);
            setScale(1f);
            return;
        }
        Vector2 foot   = anchor.get();
        Vector2 screen = projection.project(foot.cpy());
        float   scale  = projection.scaleAt(foot.x, foot.y);
        setOrigin(foot.x, foot.y);
        setPosition(screen.x - foot.x, screen.y - foot.y + lift);
        setScale(scale, scale * tilt);
    }

    /** @return le pied du groupe sur la table. */
    protected Vector2 getAnchor() { return anchor.get(); }

    /**
     * Les positions des éléments restent celles de la table à plat : les animations
     * dessinées par-dessus les reportent elles-mêmes à l'écran.
     */
    @Override
    public Vector2 localToParentCoordinates(Vector2 localCoords) {
        return localCoords;
    }
}
