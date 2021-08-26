import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;

public class ValidationMethodes {

    public static final String MSG_ERREUR_CLE_DOSSIER =
            "la cle dossier n'est pas trouvee dans le fichier d'entree.";
    public static final String MSG_ERREUR_VALEUR_DOSSIER =
            "la valeur de la cle dossier [%s] est invalide.";
    public static final String MSG_ERREUR_CLE_MOIS =
            "la cle mois n'est pas trouvee dans le fichier d'entree.";
    public static final String MSG_ERREUR_VALEUR_MOIS =
            "la valeur de la cle mois [%s] est invalide.";
    public static final String MSG_ERREUR_CLE_RECLAMATIONS =
            "la cle reclamations n'est pas trouvee dans le fichier d'entree.";
    public static final String MSG_ERREUR_CLE_IMBRIQUEE_SOIN =
            "la cle imbriquee soin du fichier d'entree est manquante.";
    public static final String MSG_ERREUR_VALEUR_SOIN =
            "la valeur de la cle imbriquee soin [%d] est invalide.";
    public static final String MSG_ERREUR_CLE_IMBRIQUEE_DATE =
            "la cle imbriquee date du fichier d'entree est manquante.";
    public static final String MSG_ERREUR_VALEUR_DATE =
            "la valeur de la cle imbriquee date [%s] est invalide.";
    public static final String MSG_ERREUR_CLE_IMBRIQUEE_MONTANT =
            "la cle imbriquee montant du fichier d'entree est manquante.";
    public static final String MSG_ERREUR_VALEUR_MONTANT =
            "la valeur de la cle montant [%s] est invalide.";

    public static final String DOSSIER_FORMAT = "^[A-E][0-9]{6}$";
    public static final String DATE_FORMAT = "\\d{4}-\\d{2}-\\d{2}";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String MOIS_FORMAT = "\\d{4}-\\d{2}";
    public static final String MONTANT_FORMAT = "^(((([1-9]\\d{0,2}(,\\d{3})*))+)|(0))(\\.|\\,)(\\d{2})\\$$";
    public static final int[] NUMEROS_SOIN = {0, 100, 150, 175, 200, 500, 600, 700};

    public static final int MOIS_MAX = 12;
    public static final int MOIS_MIN = 1;

    /**
     * Cette methode verifie si le fichier JSON donne contient une valeur pour
     * la cle "dossier" et la valeur de la cle "dossier" est bien composee d'un
     * caractere ('A' a 'E') et de six chiffres ('0' a '9').
     * Si oui, la valeur sera stockee dans le String "dossier" du parametre "client".
     * Si non, le boolean "entreeValide" du parametre "client" deviendra "false" et
     * un message d'erreur sera enregistre dans le String "msgErreur".
     *
     * @param clientObjet JSONObject a valider.
     * @param client      enregistrement de resultat du test ou de donnee validee.
     */
    static void validerDossier(JSONObject clientObjet, Client client) {
        if (!clientObjet.has(JSON.CLE_DOSSIER)) {
            client.setEntreeValide(false);
            client.setMsgErreur(MSG_ERREUR_CLE_DOSSIER);
            return;
        }

        String dossier = clientObjet.getString(JSON.CLE_DOSSIER);

        if (!dossier.matches(DOSSIER_FORMAT)) {
            client.setEntreeValide(false);
            client.setMsgErreur(String.format(MSG_ERREUR_VALEUR_DOSSIER, dossier));
        } else {
            client.setDossier(dossier);
        }
    }

    /**
     * Cette methode verifie si le fichier JSON donne contient une valeur pour
     * la cle "mois" et la valeur obtenue respecte le format (AAAA-MM).
     * Si oui, la valeur sera stockee dans le String "mois" du parametre "client".
     * Si non, le boolean "entreeValide" du parametre "client" deviendra "false" et
     * un message d'erreur sera enregistre dans le String "msgErreur".
     *
     * @param clientObjet JSONObject a valider.
     * @param client      enregistrement de resultat du test ou de donnee validee.
     */
    public static void validerMois(JSONObject clientObjet, Client client) {
        if (!client.isEntreeValide()) return;

        if (!clientObjet.has(JSON.CLE_MOIS)) {
            client.setEntreeValide(false);
            client.setMsgErreur(MSG_ERREUR_CLE_MOIS);
            return;
        }

        String mois = clientObjet.getString(JSON.CLE_MOIS);

        if (!(mois.matches(MOIS_FORMAT)) || ((Integer.parseInt(mois.substring(5))) > MOIS_MAX)
                || ((Integer.parseInt(mois.substring(5))) < MOIS_MIN)) {
            client.setEntreeValide(false);
            client.setMsgErreur(String.format(MSG_ERREUR_VALEUR_MOIS, mois));
        } else {
            client.setMois(mois);
        }
    }

    /**
     * Cette methode verifie si le fichier JSON donne contient une valeur pour
     * la cle "reclamations".
     * Si oui, elle appellera une autre methode pour la configurer et la valeur
     * validee sera stockee dans l'Arraylist "reclamationsArrayListe".
     * Si non, le boolean "entreeValide" du parametre "client" deviendra "false" et
     * un message d'erreur sera enregistre dans le String "msgErreur".
     *
     * @param clientObjet JSONObject a valider.
     * @param client      enregistrement de resultat du test ou de donnee validee.
     */
    public static void validerReclamations(JSONObject clientObjet, Client client) {
        if (!client.isEntreeValide()) return;

        if (!clientObjet.has(JSON.CLE_RECLAMATIONS)) {
            client.setEntreeValide(false);
            client.setMsgErreur(MSG_ERREUR_CLE_RECLAMATIONS);
            return;
        }

        JSONArray reclamationsJSONListe = (JSONArray) clientObjet.get(JSON.CLE_RECLAMATIONS);
        ArrayList<Reclamation> reclamationsArrayListe = new ArrayList<>();

        configurerReclamations(reclamationsJSONListe, client, reclamationsArrayListe);
        client.setReclamations(reclamationsArrayListe);
    }

    /**
     * Cette methode verifie si la cle "reclamations" contient les objets imbriques
     * "soin", "date" et "montant" et les valide en appelant trois autres methodes.
     *
     * @param reclamationsJSONListe  JSONArray a valider.
     * @param client                 enregistrement de resultat du test.
     * @param reclamationsArrayListe enregistrement des donnees validees.
     */
    public static void configurerReclamations(JSONArray reclamationsJSONListe,
                                              Client client, ArrayList<Reclamation> reclamationsArrayListe) {

        for (int i = 0; i < reclamationsJSONListe.size(); i++) {
            JSONObject reclamationObj = reclamationsJSONListe.getJSONObject(i);
            Reclamation reclamation = new Reclamation();
            validerSoin(reclamationObj, client, reclamation);
            validerEtConfigurerDate(reclamationObj, client, reclamation);
            validerMontant(reclamationObj, client, reclamation);
            reclamationsArrayListe.add(reclamation);
        }
    }

    /**
     * Cette methode verifie si le fichier JSON donnee contient une valeur pour
     * la cle imbriquee "soin" et valide si cette derniere correspond a un numero
     * faisant partie de la liste de soin (0, 100, 150, 175, 200, 300-400, 500,
     * 600, 700).
     * Si oui, la valeur sera stockee dans le int "soin" du parametre "client".
     * Si non, le boolean "entreeValide" du parametre "client" deviendra "false" et
     * un message d'erreur sera enregistre dans le String "msgErreur".
     *
     * @param reclamationObj JSONObject a valider.
     * @param client         enregistrement de resultat du test.
     * @param reclamation    enregistrement de donnee validee.
     */
    public static void validerSoin(JSONObject reclamationObj, Client client, Reclamation reclamation) {
        if (!reclamationObj.has(JSON.CLE_SOIN)) {
            client.setEntreeValide(false);
            client.setMsgErreur(MSG_ERREUR_CLE_IMBRIQUEE_SOIN);
            return;
        }

        int soin = reclamationObj.getInt(JSON.CLE_SOIN);
        int[] numeroSoin = NUMEROS_SOIN;

        if (!Arrays.stream(numeroSoin).anyMatch(i -> i == soin) && !(soin >= 300 && soin <= 400)) {
            client.setEntreeValide(false);
            client.setMsgErreur(String.format(MSG_ERREUR_VALEUR_SOIN, soin));
        } else {
            reclamation.setSoin(soin);
        }
    }

    /**
     * Cette methode verifie si le fichier JSON donne contient une valeur pour la
     * cle imbriquee "date" et la valeur obtenue a une la longueur de 10 et respecte
     * le format (AAAA-MM-JJ).
     * Si oui, la methode appellera une autre methode pour faire une comparaison avec
     * la valeur de la cle "mois".
     * Si non, le boolean "entreeValide" du parametre "client" deviendra "false" et
     * un message d'erreur sera enregistre dans le String "msgErreur".
     *
     * @param reclamationObj JSONObject a valider.
     * @param client         enregistrement de resultat du test.
     * @param reclamation    enregistrement de donnee validee.
     */
    public static void validerEtConfigurerDate(JSONObject reclamationObj, Client client, Reclamation reclamation) {
        if (!reclamationObj.has(JSON.CLE_DATE)) {
            client.setEntreeValide(false);
            client.setMsgErreur(MSG_ERREUR_CLE_IMBRIQUEE_DATE);
            return;
        }

        String date = reclamationObj.getString(JSON.CLE_DATE);
        if (!date.matches(DATE_FORMAT)) {
            client.setEntreeValide(false);
            client.setMsgErreur(String.format(MSG_ERREUR_VALEUR_DATE, date));
        } else {
            validerDate(date, client, reclamation);
        }
    }

    /**
     * Cette methode verifie si la chaine donnee represente une date passee en
     * la convertissant en format date et s'assure qu'il s'agit d'une date anterieure
     * a celle retrouvee dans le String mois de la classe Client.
     * Si oui, la valeur sera stockee dans le String "date" du parametre "reclamation".
     * Si non, le boolean "entreeValide" du parametre "client" deviendra "false" et
     * un message d'erreur sera enregistre dans le String "msgErreur".
     *
     * @param date        chaine a valider.
     * @param client      enregistrement de resultat du test.
     * @param reclamation enregistrement de donnee validee.
     */
    public static void validerDate(String date, Client client, Reclamation reclamation) {
        try {
            LocalDate dateForm = LocalDate.parse(date, DateTimeFormatter.ofPattern(DATE_PATTERN));
            if ((YearMonth.parse(client.getMois()).getYear() < dateForm.getYear()) ||
                    ((YearMonth.parse(client.getMois()).getYear() == dateForm.getYear()) &&
                            (YearMonth.parse(client.getMois()).getMonth().getValue() < dateForm.getMonth().getValue()))) {
                client.setEntreeValide(false);
                client.setMsgErreur(String.format(MSG_ERREUR_VALEUR_DATE, date));
            }
        } catch (DateTimeParseException e) {
            client.setEntreeValide(false);
            client.setMsgErreur(String.format(MSG_ERREUR_VALEUR_DATE, date));
        }

        reclamation.setDate(date);
    }

    /**
     * Cette methode verifie si le fichier JSON donne contient une valeur pour la
     * cle imbriquee "montant" et la valeur obtenue respecte le format (100,000.00$).
     * Si oui, la valeur sera stockee dans le String "montant" du parametre "reclamation".
     * Si non, le boolean "entreeValide" du parametre "client" deviendra "false" et
     * un message d'erreur sera enregistre dans le String "msgErreur".
     *
     * @param reclamationObj JSONObject a valider.
     * @param client         enregistrement de resultat du test.
     * @param reclamation    enregistrement de donnee validee.
     */
    public static void validerMontant(JSONObject reclamationObj, Client client, Reclamation reclamation) {
        if (!client.isEntreeValide()) return;

        if (!reclamationObj.has(JSON.CLE_MONTANT)) {
            client.setEntreeValide(false);
            client.setMsgErreur(MSG_ERREUR_CLE_IMBRIQUEE_MONTANT);
            return;
        }

        String montant = reclamationObj.getString(JSON.CLE_MONTANT);
        if (!montant.matches(MONTANT_FORMAT)) {
            client.setEntreeValide(false);
            client.setMsgErreur(String.format(MSG_ERREUR_VALEUR_MONTANT, montant));
        } else {
            reclamation.setMontant(montant);
        }
    }
}
