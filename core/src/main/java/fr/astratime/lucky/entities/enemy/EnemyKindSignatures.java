package fr.astratime.lucky.entities.enemy;

import java.util.List;

import static fr.astratime.lucky.entities.enemy.EnemyKind.*;

/** Les cartes propres de chaque ennemi, liées à ses rouleaux (voir {@link EnemyKind#getSignatures()}). */
final class EnemyKindSignatures {

    private EnemyKindSignatures() { }

    /** @return ses cartes propres, liées à ses rouleaux (voir le document « Cartes des ennemis »). */
    static List<EnemySignature> of(EnemyKind kind) {
        return switch (kind) {
            case GARDIEN, ROI_CARREAU -> List.of(EnemySignature.RONCES);
            case SANGSUE, ROI_COEUR -> List.of(EnemySignature.SAIGNEE);
            case BRETTEUR, ROI_PIQUE, JACKPOT_VIVANT -> List.of(EnemySignature.PROVOCATION);
            case COMETE -> List.of(EnemySignature.QUEUE_DE_COMETE);
            case TRICHEUR -> List.of(EnemySignature.DE_CHARGE);
            case USURIER -> List.of(EnemySignature.TAUX_USURAIRE);
            case ROULETTE, PIEUVRE -> List.of(EnemySignature.BILLE_TRUQUEE);
            case REINE -> List.of(EnemySignature.MAIN_DE_LA_REINE);
            case PASSEUR -> List.of(EnemySignature.COUP_DE_RATEAU, EnemySignature.DE_CHARGE);
            case PELERIN_FER -> List.of(EnemySignature.SERMENT_SOLENNEL);
            case DISEUSE -> List.of(EnemySignature.BOULE_DE_CRISTAL);
            case PORTEUR -> List.of(EnemySignature.AUTEL);
            case PROPHETE -> List.of(EnemySignature.PROCESSION, EnemySignature.QUEUE_DE_COMETE);
            case GARDIENNE -> List.of(EnemySignature.RONCES, EnemySignature.SAIGNEE, EnemySignature.PROVOCATION);
            case MIROIR -> List.of(EnemySignature.MIROIR_DEFORMANT);
            case HORLOGER -> List.of(EnemySignature.REMONTOIR);
            case FOU -> List.of(EnemySignature.TOUT_SUR_LE_ROUGE);
            case PILLEUR -> List.of(EnemySignature.SAC_DE_BUTIN);
            case FAUSSAIRE -> List.of(EnemySignature.PLANCHE_A_BILLETS);
            case CARTOMANCIENNE -> List.of(EnemySignature.TAROT);
            case DUELLISTE -> List.of(EnemySignature.BOTTE_SECRETE);
            case PRETENDANT -> List.of(EnemySignature.ECLAT_VOLE);
            case SERGENT -> List.of(EnemySignature.CLAIRON);
            case CANONNIER -> List.of(EnemySignature.POUDRE_NOIRE);
            case ESPIONNE -> List.of(EnemySignature.MICROFILM);
            case MERCENAIRE -> List.of(EnemySignature.PRIME_DE_RISQUE);
            case GENERAL -> List.of(EnemySignature.ORDRE_DE_CHARGE, EnemySignature.CLAIRON);
            case RAMASSEUR -> List.of(EnemySignature.POCHE_PLEINE);
            case HERITIER -> List.of(EnemySignature.TESTAMENT);
            case VEUVE -> List.of(EnemySignature.DERNIERE_DANSE);
            case NOTAIRE -> List.of(EnemySignature.PARAPHE);
            case COLLECTIONNEUR -> List.of(EnemySignature.POCHE_PLEINE, EnemySignature.TESTAMENT);
            case COMPTABLE -> List.of(EnemySignature.REDRESSEMENT);
            case DIRECTEUR -> List.of(EnemySignature.REGLEMENT_INTERIEUR);
            case SECURITE -> List.of(EnemySignature.CAMERAS);
            case GARDIEN_LEVIER -> List.of(EnemySignature.PLANCHE_A_BILLETS, EnemySignature.REDRESSEMENT,
                EnemySignature.BOTTE_SECRETE);
            case BANQUEROUTE -> List.of(EnemySignature.SAISIE);
            case TEMPS_MORT -> List.of(EnemySignature.COMPTE_A_REBOURS);
            case CHEF -> List.of(EnemySignature.ANNONCE, EnemySignature.COUP_DE_RATEAU);
            case PORTIER -> List.of(EnemySignature.TENUE_EXIGEE, EnemySignature.PAS_CE_SOIR);
            case MAISON -> List.of(EnemySignature.MACONNERIE, EnemySignature.DOUBLE_SERRURE, EnemySignature.BANQUE_GAGNE);
            case LIFTIER_TOUR -> List.of(EnemySignature.BOUTON_D_APPEL);
            case ECHO -> List.of(EnemySignature.RESONANCE);
            case VERTIGE -> List.of(EnemySignature.LE_VIDE);
            case GARGOUILLE -> List.of(EnemySignature.GRANIT);
            case ARCHITECTE -> List.of(EnemySignature.FONDATIONS, EnemySignature.CLE_DE_VOUTE);
            case MACHINE_ORIGINELLE -> List.of(EnemySignature.HUILE_NOIRE, EnemySignature.PLUIE_D_ETOILES,
                EnemySignature.SURTENSION, EnemySignature.GRANDE_ASPIRATION);
            case GOLEM_OR -> List.of(EnemySignature.OR_MASSIF, EnemySignature.POIDS_DE_L_OR);
            case COEUR_MONTAGNE -> List.of(EnemySignature.GRONDEMENT, EnemySignature.FILON);
            case BANDIT_MANCHOT -> List.of(EnemySignature.MONNAIE, EnemySignature.LEVIER_GRILLE);
            case ROI_TREFLE -> List.of(EnemySignature.TAUX_USURAIRE, EnemySignature.DE_CHARGE);
            case RAT_CALES -> List.of(EnemySignature.DENTS_DE_RAT);
            case CAPITAINE_RAT -> List.of(EnemySignature.DENTS_DE_RAT, EnemySignature.SAIGNEE);
            case BUVEUR, TAVERNIER -> List.of(EnemySignature.TOURNEE);
            case GUETTEUR -> List.of(EnemySignature.LANTERNE);
            case GARDIEN_PHARE -> List.of(EnemySignature.LANTERNE, EnemySignature.RONCES);
            case PIRATE -> List.of(EnemySignature.GRAPPIN);
            case CAPITAINE_NOIR -> List.of(EnemySignature.GRAPPIN, EnemySignature.PROVOCATION);
            case CHERCHEUR_OR -> List.of(EnemySignature.TAMIS_NOIR);
            case BARON_OR -> List.of(EnemySignature.TAMIS_NOIR, EnemySignature.TAUX_USURAIRE);
            case FOREUR -> List.of(EnemySignature.MECHE_DE_DIAMANT);
            case GRAND_FOREUR -> List.of(EnemySignature.MECHE_DE_DIAMANT, EnemySignature.PROVOCATION);
            case FORGERON, MAITRE_FORGE -> List.of(EnemySignature.TREMPE);
            case BARMAN_NOYE, SIRENE -> List.of(EnemySignature.REFRAIN);
            case REQUIN -> List.of(EnemySignature.FRENESIE);
            case REQUIN_BANQUIER -> List.of(EnemySignature.FRENESIE, EnemySignature.SAIGNEE);
            case KRAKEN -> List.of(EnemySignature.ENCRE);
            case CLOWN -> List.of(EnemySignature.MASSUES);
            case MONSIEUR_LOYAL -> List.of(EnemySignature.ROULEMENT_DE_TAMBOUR, EnemySignature.PROVOCATION);
            case SQUELETTE -> List.of(EnemySignature.BOUH);
            case CONDUCTEUR -> List.of(EnemySignature.TUNNEL_SANS_FIN, EnemySignature.SAIGNEE);
            case FORAIN -> List.of(EnemySignature.TOUR_DE_MANIVELLE);
            case ROUE_FORTUNE -> List.of(EnemySignature.ROUE_LESTEE, EnemySignature.BILLE_TRUQUEE);
            case BONIMENTEUR -> List.of(EnemySignature.LOT_DE_CONSOLATION, EnemySignature.TAUX_USURAIRE);
            case ROI_FORAIN -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.RONCES);
            case CARAVANIER -> List.of(EnemySignature.OCTROI, EnemySignature.TAUX_USURAIRE);
            case EMIR -> List.of(EnemySignature.USURE, EnemySignature.SAIGNEE);
            case MOMIE -> List.of(EnemySignature.MOMIFICATION);
            case PHARAON -> List.of(EnemySignature.MOMIFICATION, EnemySignature.RONCES);
            case SCORPION -> List.of(EnemySignature.DARD);
            case DJINN -> List.of(EnemySignature.FATA_MORGANA, EnemySignature.MIROIR_DEFORMANT);
            case SALAMANDRE -> List.of(EnemySignature.COULEE_DE_LAVE);
            case DRAGON -> List.of(EnemySignature.BRAISE, EnemySignature.TREMPE);
            case CHAUFFEUR -> List.of(EnemySignature.CHARBON);
            case LOCOMOTIVE -> List.of(EnemySignature.CHARBON, EnemySignature.PLEIN_REGIME);
            case SERVEUR -> List.of(EnemySignature.DOIGTS_AGILES);
            case GRAND_CHEF -> List.of(EnemySignature.DESSERT, EnemySignature.DOIGTS_AGILES);
            case SOMNAMBULE -> List.of(EnemySignature.MARCHAND_DE_SABLE);
            case DAME_EN_NOIR -> List.of(EnemySignature.MARCHAND_DE_SABLE, EnemySignature.DARD);
            case CONVOYEUR -> List.of(EnemySignature.HAUT_LES_MAINS);
            case DESPERADO -> List.of(EnemySignature.HAUT_LES_MAINS, EnemySignature.TOUT_SUR_LE_ROUGE);
            case GROOM -> List.of(EnemySignature.TROUSSEAU);
            case CONCIERGE -> List.of(EnemySignature.TROUSSEAU, EnemySignature.PROVOCATION);
            case PATINEUSE -> List.of(EnemySignature.GIVRE_ETERNEL);
            case IMPERATRICE_GIVRE -> List.of(EnemySignature.GIVRE_ETERNEL, EnemySignature.TEMPETE);
            case LIFTIER -> List.of(EnemySignature.BOULE_DE_NEIGE);
            case YETI -> List.of(EnemySignature.BOULE_DE_NEIGE, EnemySignature.TEMPETE);
            case MAJORDOME -> List.of(EnemySignature.GRAND_CRU);
            case PRINCE_NEIGES -> List.of(EnemySignature.GRAND_CRU, EnemySignature.GIVRE_ETERNEL);
            case MOINE_HASARD -> List.of(EnemySignature.CARILLON);
            case MAITRE_CLOCHES -> List.of(EnemySignature.CARILLON, EnemySignature.TOCSIN);
            case ORACLE_AVEUGLE -> List.of(EnemySignature.QUENOUILLE);
            case TISSEUSE -> List.of(EnemySignature.QUENOUILLE, EnemySignature.CISEAUX_DU_DESTIN);
            case LANCEUR_DES -> List.of(EnemySignature.COUP_DE_DES);
            case SEIGNEUR_DES -> List.of(EnemySignature.COUP_DE_DES, EnemySignature.QUITTE_OU_DOUBLE);
            case STATUE_ANIMEE -> List.of(EnemySignature.OFFRANDE);
            case IDOLE_OR -> List.of(EnemySignature.OFFRANDE, EnemySignature.JUGEMENT);
            case VALET_FANTOME -> List.of(EnemySignature.PACTE_SCELLE);
            case COMTE_MINUIT -> List.of(EnemySignature.PACTE_SCELLE, EnemySignature.DOUZE_COUPS);
            case LIVRE_VIVANT -> List.of(EnemySignature.PAGE_ARRACHEE);
            case BIBLIOTHECAIRE -> List.of(EnemySignature.PAGE_ARRACHEE, EnemySignature.SILENCE);
            case GOULE -> List.of(EnemySignature.TERRE_FRAICHE);
            case FOSSOYEUR -> List.of(EnemySignature.TERRE_FRAICHE, EnemySignature.GLAS);
            case POUPEE_HANTEE -> List.of(EnemySignature.FIL_DE_SOIE);
            case MARIONNETTISTE -> List.of(EnemySignature.FIL_DE_SOIE, EnemySignature.DERNIER_ACTE);
            // Le Joueur Masqué : à chaque rencontre, il garde une carte du lieu traversé.
            case RIVAL_FOIRE -> List.of(EnemySignature.CARTE_BISEAUTEE);
            case RIVAL_RAPIDE -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.HAUT_LES_MAINS);
            case RIVAL_PORT -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.HAUT_LES_MAINS, EnemySignature.GRAPPIN);
            case RIVAL_PALACE, RIVAL_HISTOIRE -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.HAUT_LES_MAINS, EnemySignature.GRAPPIN,
                EnemySignature.TROUSSEAU);
            case RIVAL_MINES -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.HAUT_LES_MAINS, EnemySignature.GRAPPIN, EnemySignature.TROUSSEAU, EnemySignature.MECHE_DE_DIAMANT);
            case RIVAL_MANOIR -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.HAUT_LES_MAINS, EnemySignature.GRAPPIN, EnemySignature.TROUSSEAU, EnemySignature.MECHE_DE_DIAMANT,
                EnemySignature.PACTE_SCELLE);
            case RIVAL_CASINO -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.HAUT_LES_MAINS, EnemySignature.GRAPPIN, EnemySignature.TROUSSEAU, EnemySignature.MECHE_DE_DIAMANT,
                EnemySignature.PACTE_SCELLE, EnemySignature.FRENESIE);
            case RIVAL_TEMPLE -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.HAUT_LES_MAINS, EnemySignature.GRAPPIN, EnemySignature.TROUSSEAU, EnemySignature.MECHE_DE_DIAMANT,
                EnemySignature.PACTE_SCELLE, EnemySignature.FRENESIE, EnemySignature.COUP_DE_DES);
            case RIVAL_DESERT -> List.of(EnemySignature.CARTE_BISEAUTEE, EnemySignature.HAUT_LES_MAINS, EnemySignature.GRAPPIN, EnemySignature.TROUSSEAU, EnemySignature.MECHE_DE_DIAMANT,
                EnemySignature.PACTE_SCELLE, EnemySignature.FRENESIE, EnemySignature.COUP_DE_DES, EnemySignature.USURE);
            default -> List.of();
        };
    }
}
