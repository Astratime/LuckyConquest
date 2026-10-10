# Lucky Conquest

Un jeu de combat au tour par tour où l'on se bat avec un **deck de cartes** et une **machine à sous**.
Chaque tour, on joue des cartes pour truquer la machine, on tire le levier, et les symboles alignés
attaquent, protègent, soignent ou rapportent des gains. Trois symboles identiques : **Bingo !**

*In English: Lucky Conquest is a turn-based fighting game played with a deck of cards and a slot
machine. Play cards to rig the reels, pull the lever, and let the symbols fight for you. The game is
available in French and English (Options > Language).*

## Le jeu

### Un tour de combat
1. **Les cartes** : on pioche, puis on joue jusqu'à 4 cartes. Elles renforcent un symbole, forcent un
   rouleau, protègent ou attaquent. Plusieurs cartes forment des **combinaisons** (Paire, Brelan,
   Suite, Couleur, Carré) qui multiplient l'attaque et les gains du tirage.
2. **Le tirage** : il coûte des gains (le prix dépend du lieu). On peut aussi **miser** une part de
   ses gains : la Mise ne paie que sur un Bingo.
3. **Le résultat** : chaque symbole agit (épée, bouclier, cœur, pièces…). Deux symboles identiques
   font une Paire, trois font un **Bingo**, avec sa propre mise en scène, et parfois un **Jeu bonus**
   (une grille 6 x 6 et trois tirages).
4. **L'ennemi** joue à son tour, ses cartes et sa propre machine.

### Les modes
- **Entraînement** : un combat contre le Croupier, pour s'exercer. Il ne rapporte rien.
- **Tour des épreuves** : six chapitres de combats et leurs boss, avec une histoire et ses
  cinématiques, puis un mode difficile. Elle ne rapporte pas de pièces.
- **Exploration** : quatre lieux (la Prairie, le Port des Contrebandiers, les Mines d'Or, le Casino
  Englouti), chacun avec quatre donjons, leurs rois, leurs coffres et une règle propre au lieu.
  Ce sont les seuls combats qui rapportent des **pièces**.
- **Table du croupier** : on y compose son deck de 20 cartes et les 11 rouleaux de sa machine, et on
  y fusionne 3 exemplaires d'une carte en sa version **« + »**.
- **Boutique** : les pièces achètent des **rangs** (des bonus pour tous les combats), des cartes et des
  rouleaux.

Un **tutoriel** guidé par le Croupier se lance au premier démarrage (il se rejoue depuis les Options).
Les Options règlent aussi l'affichage, les effets, la vitesse des animations, le son et la langue,
et montrent les **statistiques et les succès**.

## Télécharger et jouer

Les versions à télécharger se trouvent dans les
[Releases](https://github.com/Astratime/LuckyConquest/releases) du dépôt : un zip par système
(Windows, macOS Apple Silicon, macOS Intel, Linux), avec son propre Java. Il n'y a rien à installer :
on dézippe et on lance le jeu.

Le jeu n'est pas signé :
- sur **Windows**, SmartScreen demande de confirmer (*Informations complémentaires* > *Exécuter quand même*) ;
- sur **macOS**, le premier lancement se fait par clic droit > *Ouvrir*.

La partie est enregistrée dans `~/.prefs/lucky-conquest-save` (dans le dossier de l'utilisateur),
avec une copie de secours `.bak`. Le fichier est signé : modifié à la main, il est refusé.

## Pour les développeurs

Le jeu est écrit en Java 21 avec [libGDX](https://libgdx.com/).

| Commande | Ce qu'elle fait |
|---|---|
| `./gradlew lwjgl3:run` | Lance le jeu. |
| `./gradlew test` | Lance les tests. |
| `./gradlew :core:simulate` | Simule des milliers de tours et écrit le rapport d'équilibrage (voir plus bas). |
| `./gradlew lwjgl3:packageWinX64` | Fabrique le zip Windows ; aussi `packageMacM1`, `packageMacX64`, `packageLinuxX64` (dans `lwjgl3/build/construo/dist`). |

### Organisation du code
- `core` : tout le jeu.
  - `entities` : les règles (cartes, symboles, ennemis, combinaisons, Jeu bonus…).
  - `controllers` : le déroulement d'un combat (`GameController`, résolution des tirages).
  - `screens` et `views` : les écrans et leurs éléments ; `animations` : les mises en scène.
  - `progress` : le profil du joueur (collection, deck, pièces, rangs, sauvegarde, statistiques, succès).
- `lwjgl3` : le lanceur pour ordinateur.
- `assets` : images, sons, polices, cartes (`assets/cards`) et traductions (`assets/i18n/en.json`).
  Chaque texte du jeu est écrit en français dans le code et traduit dans `en.json` ; un test vérifie
  qu'aucun ne manque.
- `tools/sounds` : le générateur des bruitages (`python3 generate_sounds.py <nom>`).

### Simulateur d'équilibrage
`./gradlew :core:simulate` fait jouer un robot avec le vrai moteur du jeu et écrit
`core/build/simulation/equilibrage.md` :
1. **Tirages** : ce que rapporte un tirage, en coûts de tirage, par lieu (objectif : 0 à 5 en gains
   de base), avec la part de Paires, de Bingos et de dette ;
2. **Jeu bonus** : ses gains moyens (objectif : environ 100 coûts de tirage sur la machine classique) ;
3. **Donjons** : la part de victoires et les pièces gagnées par donjon, selon le rang du joueur ;
4. **Joueur au maximum** : les donjons du Port, des Mines et du Casino avec des decks de cartes « + »,
   les rouleaux rares et des achats à l'échoppe, du rang Flambeur à Légende du Jackpot ;
5. **Progression** : le premier rang qui gagne chaque donjon, et combien de descentes il faut pour
   payer chaque rang.

Le robot joue moins bien qu'un vrai joueur : le rapport sert à comparer le jeu avant et après un
changement de règles. `-Pquick` donne un essai rapide. Une version courte tourne avec les tests et
vérifie que les objectifs des tirages et du Jeu bonus sont tenus.

### Exécutables
Le workflow GitHub **Exécutables** fabrique les quatre zips :
- lancé à la main depuis l'onglet *Actions* (*Run workflow*), les zips sont en bas de la page du lancement ;
- en poussant une étiquette `v*` (par exemple `git tag v1.0.0 && git push origin v1.0.0`), ils sont
  aussi publiés dans une Release, dont le lien se partage.
