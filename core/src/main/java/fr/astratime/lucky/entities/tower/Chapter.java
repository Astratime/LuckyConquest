package fr.astratime.lucky.entities.tower;

/**
 * Chapitres de la Tour des épreuves. Un chapitre ouvert enchaîne trois
 * combats (voir {@link TowerRun}) ; les autres sont annoncés, pas encore jouables.
 */
public enum Chapter {

    GENESE(1, "La genèse",
        "Un éclat de lumière frappa le monde entier. Les conflits devinrent des jeux obéissant à une règle : "
            + "celui qui a le plus de chance gagne.",
        true),
    CHAPITRE_2(2, "Bientôt", "Ce chapitre n'est pas encore ouvert. La Tour garde ses secrets.", false),
    CHAPITRE_3(3, "Bientôt", "Ce chapitre n'est pas encore ouvert. La Tour garde ses secrets.", false);

    private final int     number;
    private final String  title;
    private final String  description;
    private final boolean open;

    Chapter(int number, String title, String description, boolean open) {
        this.number      = number;
        this.title       = title;
        this.description = description;
        this.open        = open;
    }

    /** @return le numéro du chapitre (à partir de 1). */
    public int getNumber() { return number; }
    /** @return "Chapitre N". */
    public String getLabel() { return "Chapitre " + number; }
    /** @return le titre du chapitre (ex : "La genèse"). */
    public String getTitle() { return title; }
    /** @return le récit du chapitre. */
    public String getDescription() { return description; }
    /** @return {@code true} si le chapitre peut être joué. */
    public boolean isOpen() { return open; }
}
