package fr.astratime.lucky.assets;

import fr.astratime.lucky.entities.enemy.EnemyKind;

/**
 * Les portraits des boss de l'Histoire que leurs cinématiques de phase et de mort défont : la Reine des Tables
 * ({@link Chapter2Art}), le Prétendant et la Maison ({@link Chapter456Art}). Une cinématique en tire le portrait
 * sans un accessoire ({@link #without}) pour en détacher le vrai morceau, jamais une copie.
 */
public final class StoryPortraits {

    /** Ce qu'une cinématique peut retirer d'un portrait. */
    public enum Prop {
        /** La couronne de la Reine des Tables. */
        QUEEN_CROWN,
        /** La couronne du Prétendant, et ses trois éclats : à gauche, à droite, sur la poitrine. */
        PRETENDER_CROWN, PRETENDER_SHARD_LEFT, PRETENDER_SHARD_RIGHT, PRETENDER_SHARD_CHEST,
        /** La façade de la Maison (son corps, ses fenêtres, sa porte, son blason) et la porte de son coffre. */
        HOUSE_FACADE, HOUSE_VAULT
    }

    /** Les accessoires retirés du portrait en cours de dessin (vide hors de {@link #without}). */
    private static java.util.Set<Prop> gone = java.util.EnumSet.noneOf(Prop.class);

    private StoryPortraits() {}

    static boolean has(Prop prop) { return !gone.contains(prop); }

    /** @return les pixels du portrait de {@code kind} ({@code [y][x]}, RGBA8888), {@code null} s'il n'est pas l'un d'eux. */
    public static int[][] pixels(EnemyKind kind) {
        return switch (kind) {
            case REINE      -> Chapter2Art.reine();
            case PRETENDANT -> Chapter456Art.pretendant();
            case MAISON     -> Chapter456Art.maison();
            default         -> null;
        };
    }

    /** @return l'illustration du chapitre de la Maison ({@code [y][x]}, RGBA8888) : le manoir sur les nuages, devant son ciel du soir. */
    public static int[][] houseArt() { return Chapter456Art.house(); }

    /** @return le portrait de {@code kind} dessiné sans les accessoires {@code props} (ce qu'il y avait dessous réapparaît). */
    public static synchronized int[][] without(EnemyKind kind, Prop... props) {
        gone = java.util.EnumSet.noneOf(Prop.class);
        java.util.Collections.addAll(gone, props);
        try {
            return pixels(kind);
        } finally {
            gone = java.util.EnumSet.noneOf(Prop.class);
        }
    }
}
