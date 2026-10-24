package fr.astratime.lucky.security;

/**
 * Révèle une chaîne sensible rangée sous forme codée, pour qu'elle n'apparaisse
 * pas en clair dans le binaire (noms de fichiers de sauvegarde, en-tête du
 * format, clés du mode ADMIN…). Ce n'est pas du chiffrement : un simple voile
 * qui évite qu'une chaîne parlante saute aux yeux dans le jar décompilé.
 *
 * <p>Les tableaux d'entiers passés à {@link #reveal(int[])} sont produits par le
 * même calcul inversé ; un test vérifie que chacun redonne bien la chaîne
 * attendue.
 */
public final class Hidden {

    private static final int SALT = 0x3b;

    private Hidden() {}

    /** @return la chaîne d'origine reconstituée à partir de sa forme codée. */
    public static String reveal(int[] code) {
        char[] out = new char[code.length];
        for (int i = 0; i < code.length; i++) {
            out[i] = (char) (code[i] ^ (SALT + i * 5));
        }
        return new String(out);
    }
}
