# Obfuscation du .jar livré de Lucky Conquest.
#
# Objectif : renommer le code du jeu (fr.astratime.lucky.**) pour compliquer la
# lecture après décompilation, SANS rien retirer ni réorganiser (pas de
# suppression ni d'optimisation), car libGDX et LWJGL chargent beaucoup de
# classes par réflexion et par ressources natives : tout retirer casserait le
# jeu. On garde donc tout, on ne fait que renommer nos propres classes.

# NB : on NE met PAS -dontpreverify. Java 7+ exige les « stackmap frames »,
# que ProGuard régénère pendant la pré-vérification ; les désactiver donne un
# VerifyError au lancement.
-dontshrink
-dontoptimize
-dontnote
-dontwarn
-ignorewarnings

# --- Bibliothèque de base du JDK ----------------------------------------------
# Indispensable : sans elle ProGuard ne trouve même pas java.lang.Object et
# refuse de pré-vérifier. <java.home> est résolu par ProGuard lui-même.
-libraryjars <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
-libraryjars <java.home>/jmods/java.desktop.jmod(!**.jar;!module-info.class)
-libraryjars <java.home>/jmods/java.management.jmod(!**.jar;!module-info.class)
-libraryjars <java.home>/jmods/jdk.unsupported.jmod(!**.jar;!module-info.class)

# Garde les signatures et métadonnées utiles au bon fonctionnement à l'exécution.
-keepattributes Signature,*Annotation*,EnclosingMethod,InnerClasses,SourceFile,LineNumberTable

# --- Point d'entrée (doit rester nommé pour le manifeste Main-Class) ---------
-keep public class fr.astratime.lucky.lwjgl3.Lwjgl3Launcher {
    public static void main(java.lang.String[]);
}
-keep public class fr.astratime.lucky.lwjgl3.StartupHelper { *; }

# --- Tout ce qui n'est pas le jeu : on ne renomme rien -------------------------
# (libGDX, LWJGL, JDK… chargés par réflexion/ressources : laissés intacts.)
-keep class !fr.astratime.lucky.** { *; }

# --- Enums : les noms des constantes servent de clés de sauvegarde -------------
# Les sauvegardes stockent des enums par leur name() ; renommer une constante
# rendrait les anciennes parties illisibles. On garde donc tous les enums intacts.
-keepclassmembers enum * { *; }
-keep enum fr.astratime.lucky.** { *; }
