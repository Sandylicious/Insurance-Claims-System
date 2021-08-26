import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LireEcrireFichier {

    public static final String ERREUR_LECTURE = "{\n\t\"erreur\": \"le fichier d'entree n'existe pas.\"\n}";
    public static final String ERREUR_ECRITURE = "{\n\t\"message\": \"erreur d'ecriture\"\n}";
    public static final String ERREUR_NOM = "{\n\t\"message\": \"erreur de nom du fichier donne.\"\n}";

    /**
     * Cette methode lit un fichier et le convertit en String. S'il y a erreur a la
     * lecture du fichier, un message d'erreur s'affiche et le programme se termine.
     *
     * @param nomFichier le nom du fichier.
     * @return le fichier d'entree en format String.
     */
    public static String lireFichier(String nomFichier) {
        File fichierClient = new File(nomFichier);
        String fichierEnString = "";

        try {
            Scanner scan = new Scanner(fichierClient);
            while (scan.hasNextLine())
                fichierEnString = fichierEnString + scan.nextLine();
        } catch (FileNotFoundException e) {
            ecrireFichierMsgErreur(Main.fichierRemboursements, ERREUR_LECTURE);
            System.exit(1);
        }

        return fichierEnString;
    }

    /**
     * Cette methode ecrit le String en parametre dans un fichier. S'il y a
     * erreur a l'ecriture du fichier, un message d'erreur s'affiche et le
     * programme se termine.
     *
     * @param nomFichier     le nom du fichier.
     * @param contenuFichier le String a ecrire.
     */
    public static void ecrireFichier(String nomFichier, String contenuFichier) {
        try {
            FileWriter fileWriter = new FileWriter(nomFichier);
            fileWriter.write(contenuFichier);
            fileWriter.close();
        } catch (IOException e) {
            ecrireFichierMsgErreur(Main.fichierRemboursements, ERREUR_ECRITURE);
            System.exit(1);
        }
    }

    /**
     * Cette methode ecrit au fichier de sortie le String (de format JSON)
     * qui contient les montants de remboursement du client.
     * <p>
     * S'il y a erreur d'ecriture du fichier, un message d'erreur s'affiche et le programme se termine.
     *
     * @param nomFichier   le nom du fichier.
     * @param clientString le String a ecrire au fichier de sortie (contenant les donnees).
     */
    public static void ecrireFichierRemboursement(String nomFichier, String clientString) {
        clientString = clientString.replace("reclamations", "remboursements");

        ecrireFichier(nomFichier, clientString);
    }

    /**
     * Cette methode ecrit au fichier de sortie un message d'erreur specifique.
     *
     * @param nomFichier        le nom du fichier.
     * @param messageSpecifique le message d'erreur specifique a la premiere erreur rencontree.
     */
    public static void ecrireFichierMsgErreur(String nomFichier, String messageSpecifique) {
        try {
            FileWriter remboursementAEcrire = new FileWriter(nomFichier);
            remboursementAEcrire.write(messageSpecifique);
            remboursementAEcrire.close();
        } catch (IOException e) {
            System.out.println(ERREUR_NOM);
            System.exit(1);
        }
    }

    /**
     * Cette methode lit et ecrit dans un fichier JSON le nombre de reclamations valides
     * traitees, le nombre de demandes rejetees et le nombre de soins declares pour
     * chaque type de soin.
     *
     * @param client contient les elements a traiter.
     **/
    public static void ecrireFichierStatistiques(Client client) {
        File fichierStats = new File(Main.NOM_FICHIER_STATISTIQUES);
        String statsString = "";

        try {
            Scanner scan = new Scanner(fichierStats);
            while (scan.hasNextLine())
                statsString = statsString + scan.nextLine();
        } catch (FileNotFoundException e) {
            statsString = JSON.reinitialiserStatistiques();
        }

        JSONObject jsonObjetStats = (JSONObject) JSONSerializer.toJSON(statsString);

        if (!client.isEntreeValide()) {
            int nbrDemandesRejetees = jsonObjetStats.getInt(JSON.NBR_DEMANDES_REJETEES) + 1;
            jsonObjetStats.put(JSON.NBR_DEMANDES_REJETEES, nbrDemandesRejetees);
        } else {
            ecrireFichierStatsSiValide(client, jsonObjetStats);
        }

        ecrireFichier(Main.NOM_FICHIER_STATISTIQUES, jsonObjetStats.toString(2));
    }

    /**
     * Cette methode ecrit dans un fichier JSON le nombre de reclamations valides
     * traitees et le nombre de soins declares pour chaque type de soin.
     *
     * @param client         contient les elements a extraire.
     * @param jsonObjetStats contient les statistiques converties en JSONObject.
     **/
    public static void ecrireFichierStatsSiValide(Client client, JSONObject jsonObjetStats) {
        Map<Integer, List<Reclamation>> reclamationParSoin =
                client.getReclamations().stream().collect(Collectors.groupingBy(w -> w.getSoin()));

        int nbrReclamationsTraitees = jsonObjetStats.getInt(JSON.NBR_RECLAMATIONS_TRAITEES) +
                client.getReclamations().size();

        jsonObjetStats.put(JSON.NBR_RECLAMATIONS_TRAITEES, nbrReclamationsTraitees);

        ecrireFichierStatsReclamationParSoin(reclamationParSoin, jsonObjetStats, client);
    }

    /**
     * Cette ecrit dans le fichier statistiques le nombre de soins declares pour chaque type de soin.
     *
     * @param reclamationParSoin contient les elements a traiter.
     * @param jsonObjetStats     contient les statistiques converties en JSONObject.
     * @param client             contient le montant a traiter.
     **/
    public static void ecrireFichierStatsReclamationParSoin(Map<Integer, List<Reclamation>> reclamationParSoin,
                                                            JSONObject jsonObjetStats, Client client) {

        JSONObject jsonObjSoin = (JSONObject) jsonObjetStats.get(JSON.NBR_SOIN);

        JSONObject jsonObjMontantMaxParSoin = (JSONObject) jsonObjetStats.get(JSON.MONTANT_MAX_PAR_SOIN);

        JSONObject jsonObjMoyMontantParSoin = (JSONObject) jsonObjetStats.get(JSON.MOYENNE_MONTANT_PAR_SOIN);

        // Ecrire dans fichier stats le nombre de reclamations par soin
        for (Map.Entry<Integer, List<Reclamation>> entry : reclamationParSoin.entrySet()) {
            int numeroDeSoin = entry.getKey();
            int compteur = entry.getValue().size();

            // Pour recuperer la valeur qui se trouve dans if 
            int nbrDeSoinParTypeCopy = 1;

            if (jsonObjSoin.has("" + numeroDeSoin)) {
                int nbrDeSoinParType = jsonObjSoin.getInt("" + numeroDeSoin) + compteur;
                jsonObjSoin.put(numeroDeSoin, nbrDeSoinParType);

                // Recuperer la valeur dans if
                nbrDeSoinParTypeCopy = nbrDeSoinParType;

            } else {
                jsonObjSoin.put(numeroDeSoin, compteur);
            }

            // montant maximal reclame par soin
            ecrireFichierStatsMontantMax(jsonObjMontantMaxParSoin, client, numeroDeSoin);

            // moyenne des montants reclames par soin
            ecrireFichierStatsMoyMontant(jsonObjMoyMontantParSoin, client, numeroDeSoin, nbrDeSoinParTypeCopy);

        }
    }


    /**
     * Cette methode trouve le montant maximal reclame pour chaque type de soin et
     * l'ajoute dans le fichier de statistiques.
     *
     * @param jsonObjectMontantMaxParSoin contient les elements a traiter.
     * @param client                      contient le montant a traiter.
     * @param numeroDeSoin                le numero de soin.
     **/
    public static void ecrireFichierStatsMontantMax(JSONObject jsonObjectMontantMaxParSoin, Client client,
                                                    int numeroDeSoin) {

        int maxMontantFichierEntreeParSoin = trouverMaxMontantFichierEntreeParSoin(client, numeroDeSoin);

        // Convertir int en String
        String maxMontantFichierEntreeParSoinString = String.valueOf(maxMontantFichierEntreeParSoin);

        // Convertir #### en ##.##$
        String maxAEcrireString = maxMontantFichierEntreeParSoinString.substring(0,
                maxMontantFichierEntreeParSoinString.length() - 2) + "." +
                maxMontantFichierEntreeParSoinString.substring(maxMontantFichierEntreeParSoinString.length() - 2) + "$";

        if (jsonObjectMontantMaxParSoin.has("" + numeroDeSoin)) {

            // Convertir le montant max venant du fichier stats vers un int
            String ancienMontantMaxString = jsonObjectMontantMaxParSoin.getString("" + numeroDeSoin);
            Dollar ancienMontantMaxDollar = new Dollar(ancienMontantMaxString);
            int ancienMontantMaxCents = ancienMontantMaxDollar.getCents();

            if (maxMontantFichierEntreeParSoin > ancienMontantMaxCents) {
                jsonObjectMontantMaxParSoin.put(numeroDeSoin, maxAEcrireString);
            }
        } else {
            jsonObjectMontantMaxParSoin.put(numeroDeSoin, maxAEcrireString);
        }
    }

    /**
     * Trouve, pour le soin en parametre, le plus grand montant reclame dans le fichier d'entree.
     *
     * @param client       Le client venant du fichier d'entree
     * @param numeroDeSoin Le numero de soin en question
     * @return Le nombre de cents representant le montant max reclame pour ce soin.
     */
    public static int trouverMaxMontantFichierEntreeParSoin(Client client, int numeroDeSoin) {
        int maxMontantFichierEntreeParSoin = 0;

        for (int k = 0; k < client.getReclamations().size(); k++) {
            if (client.getReclamations().get(k).getSoin() == numeroDeSoin) {
                // Convertir en int le montant du fichier d'entree pour ce soin
                String nouveauMontantMaxString = client.getReclamations().get(k).getMontant();
                Dollar nouveauMontantMaxDollar = new Dollar(nouveauMontantMaxString);
                int nouveauMontantMaxCents = nouveauMontantMaxDollar.getCents();

                if (nouveauMontantMaxCents > maxMontantFichierEntreeParSoin)
                    maxMontantFichierEntreeParSoin = nouveauMontantMaxCents;
            }
        }
        return maxMontantFichierEntreeParSoin;
    }


    /**
     * Trouve, pour le soin en parametre, le montant total reclame dans le fichier d'entree.
     *
     * @param client       Le client venant du fichier d'entree
     * @param numeroDeSoin Le numero de soin en question
     * @return Le nombre de cents representant le montant total reclame pour ce soin.
     */
    public static int trouverMontantTotalParSoinInputFile(Client client, int numeroDeSoin) {
        int montantTotalReclameParSoinFichierEntree = 0;

        for (int k = 0; k < client.getReclamations().size(); k++) {
            if (client.getReclamations().get(k).getSoin() == numeroDeSoin) {
                // Convertir en int le montant du fichier d'entree pour ce soin
                String montantDeReclamationString = client.getReclamations().get(k).getMontant();
                Dollar montantDeReclamationDollar = new Dollar(montantDeReclamationString);
                int montantDeReclamationCents = montantDeReclamationDollar.getCents();

                montantTotalReclameParSoinFichierEntree += montantDeReclamationCents;
            }
        }
        return montantTotalReclameParSoinFichierEntree;
    }


    /**
     * Cette methode calcule la moyenne des montants reclames pour chaque type de soin et
     * l'ajoute dans le fichier de statistiques.
     *
     * @param jsonObjMoyMontantParSoin contient les elements a traiter.
     * @param client                   contient le montant a traiter.
     * @param numeroDeSoin             le numero de soin.
     * @param nbrDeSoinParTypeCopy     le nombre de soin traitee pour ce type de soin.
     **/
    public static void ecrireFichierStatsMoyMontant(JSONObject jsonObjMoyMontantParSoin, Client client,
                                                    int numeroDeSoin, int nbrDeSoinParTypeCopy) {

        long nbrDeCeSoinInputFile = client.getReclamations().stream().filter(x -> x.getSoin() == numeroDeSoin).count();
        int moyenneAEcrire;

        // S'il existe deja une reclamation pour ce soin dans le fichier statistiques, calculer la nouvelle moyenne
        if (jsonObjMoyMontantParSoin.has("" + numeroDeSoin)) {
            // Du fichier stats
            String ancienMoyMontantString = jsonObjMoyMontantParSoin.getString("" + numeroDeSoin);
            Dollar ancienMoyMontantDollar = new Dollar(ancienMoyMontantString);
            int ancienMoyMontantCents = ancienMoyMontantDollar.getCents();

            int montantTotalReclameParSoinInputFile = trouverMontantTotalParSoinInputFile(client, numeroDeSoin);

            int nouvMoyMontantParSoinCents = (montantTotalReclameParSoinInputFile + (nbrDeSoinParTypeCopy -
                    (int) nbrDeCeSoinInputFile) * ancienMoyMontantCents) / (nbrDeSoinParTypeCopy);

            moyenneAEcrire = nouvMoyMontantParSoinCents;

        } else {
            // S'il n'existe pas une reclamation pour ce soin dans le fichier statistiques, en ajouter une
            moyenneAEcrire = trouverMontantTotalParSoinInputFile(client, numeroDeSoin) / (int) nbrDeCeSoinInputFile;
        }

        // Convertir int en String
        String moyenneAEcrireString = String.valueOf(moyenneAEcrire);

        // Convertir #### en ##.##$
        String montantFinalString = moyenneAEcrireString.substring(0, moyenneAEcrireString.length() - 2)
                + "." + moyenneAEcrireString.substring(moyenneAEcrireString.length() - 2) + "$";

        // Ajouter la nouvelle valeur dans le fichier stats
        jsonObjMoyMontantParSoin.put(numeroDeSoin, montantFinalString);
    }
}
