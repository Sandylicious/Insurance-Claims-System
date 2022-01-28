import net.sf.json.JSONObject;
import java.util.ArrayList;

/**
 * Cette classe represente la methode principale de notre application servant a generer un fichier de remboursements a partir d'un
 * fichier de reclamation de soins de sante d'un client.
 *
 * @author Fang, Xin Ran
 * @author Ah-Lan, Steven Chia
 */

public class Main {
    public static final String MSG_USAGE =
            "{\n\t\"usage\": \"<fichier d'entree> <fichier de sortie> ou <option -S> ou <option -SR>\"\n}";
    public static final String MSG_STATS_REINITIALISEES = "{\n\t\"message\": \"statistiques reinitialisees.\"\n}";
    public static final String NOM_FICHIER_STATISTIQUES = "reclamations_stats.json";
    public static final String ARG_AFFICHER_STATS = "-S";
    public static final String ARG_REINITIALISER_STATS = "-SR";
    public static final String ARG_PREDIRE_REMB = "-p";
    public static final String CLE_ERREUR = "erreur";

    public static String fichierReclamations;
    public static String fichierRemboursements;

    public static MaxMensuel maxMensuel = new MaxMensuel();
    public static ArrayList<Date> dates = new ArrayList<Date>();

    public static void main(String[] args) {
        if (args.length <= 0 || args.length > 3) {
            System.out.println(MSG_USAGE);
        } else if (args.length == 2) {
            genererFichierRemboursement(args, 0, 1);
        } else if (args.length == 3 && args[0].equals(ARG_PREDIRE_REMB)) {
            predireRemboursement(args, 1, 2);
        } else {
            if (args[0].equals(ARG_AFFICHER_STATS)) {
                afficherConsoleStatistiques();
            } else if (args[0].equals(ARG_REINITIALISER_STATS)) {
                reinitialiserFichierStats();
            } else {
                System.out.println(MSG_USAGE);
            }
        }
    }


    /**
     * Cette methode permet de convertir les noms de fichier en chaines de caracteres.
     *
     * @param args         les arguments a traiter.
     * @param positionArg1 la position du premier argument.
     * @param positionArg2 la position du deuxieme argument.
     */
    public static void convertirNomFichierEnString(String[] args, int positionArg1, int positionArg2) {
        fichierReclamations = args[positionArg1];
        fichierRemboursements = args[positionArg2];
    }

    /**
     * Cette methode permet de convertir le contenu du fichier d'entree en chaines de
     * caracteres et creer un object client.
     *
     * @param client l'enregistrement de donnees.
     * @return client donnees analysées et enregistrées.
     */
    public static Client convertirContenuFichierEnClient(Client client) {
        client = JSON.convertirStringEnClient(fichierRemboursements,
                LireEcrireFichier.lireFichier(fichierReclamations));

        return client;
    }

    /**
     * Cette methode genere un fichier de remboursement selon les arguments recus.
     *
     * @param args         les arguments a traiter.
     * @param positionArg1 la position du premier argument.
     * @param positionArg2 la position du deuxieme argument.
     * @return client enregistrement de donnees.
     */
    public static Client predireRemboursement(String[] args, int positionArg1, int positionArg2) {
        convertirNomFichierEnString(args, positionArg1, positionArg2);

        Client client = new Client();

        client = convertirContenuFichierEnClient(client);

        if (client.isEntreeValide()) {
            LireEcrireFichier.ecrireFichierRemboursement(fichierRemboursements,
                    JSON.convertirClientEnString(client));
        } else {
            JSONObject jsonMsgErreur = new JSONObject();
            jsonMsgErreur.put(CLE_ERREUR, client.getMsgErreur());
            String msgErreurString = jsonMsgErreur.toString(2);
            LireEcrireFichier.ecrireFichier(fichierRemboursements, msgErreurString);
        }

        return client;
    }

    /**
     * Cette methode genere un fichier de remboursement et un fichier de statistiques
     * selon les arguments recus.
     *
     * @param args les arguments a traiter.
     */
    public static void genererFichierRemboursement(String[] args, int positionArg1, int positionArg2) {
        Client client = predireRemboursement(args, positionArg1, positionArg2);

        LireEcrireFichier.ecrireFichierStatistiques(client);
    }

    /**
     * Cette methode lire le fichier de statistiques et affiche le contenu a la console.
     */
    public static void afficherConsoleStatistiques() {
        String statistiques = LireEcrireFichier.lireFichier(NOM_FICHIER_STATISTIQUES);

        JSON.afficherStatistiques(statistiques);
    }

    /**
     * Cette methode appelle la methode "ecrireFichier" pour ecrire les statistiques reinitialisees
     * dans un fichier et affiche un message de confirmation a la console.
     */
    public static void reinitialiserFichierStats() {
        LireEcrireFichier.ecrireFichier(NOM_FICHIER_STATISTIQUES, JSON.reinitialiserStatistiques());

        System.out.println(MSG_STATS_REINITIALISEES);
    }
}
