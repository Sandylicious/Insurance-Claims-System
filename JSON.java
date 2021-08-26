import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import java.util.ArrayList;
import java.util.Iterator;

public class JSON {

    public static final String NBR_RECLAMATIONS_TRAITEES = "nombre de reclamations traitees";
    public static final String NBR_DEMANDES_REJETEES = "nombre de demandes rejetees";
    public static final String NBR_SOIN = "nombre de reclamations traitees par soin";
    public static final String MONTANT_MAX_PAR_SOIN = "montant maximal reclame par soin";
    public static final String MOYENNE_MONTANT_PAR_SOIN = "moyenne des montants reclames par soin";

    public static final String CLE_DOSSIER = "dossier";
    public static final String CLE_MOIS = "mois";
    public static final String CLE_RECLAMATIONS = "reclamations";
    public static final String CLE_SOIN = "soin";
    public static final String CLE_DATE = "date";
    public static final String CLE_MONTANT = "montant";
    public static final String CLE_TOTAL = "total";

    public static final String MSG_ERREUR = "{\n\t\"message\": \"Donnees invalides\"\n}";

    /**
     * Cette methode convertit un String (de format JSON, venant du fichier d'entree) en
     * objet Client avec ses attributs (numeroClient, mois, contrat, et liste de Reclamation)
     * ayant les valeurs qui se trouvent dans le fichier d'entree.
     * S'il y a une erreur dans le String en entree, cette methode appelle une autre methode
     * qui ecrit le message d'erreur dans le fichier de sortie et termine le programme.
     *
     * @param nomFichier       le nom du fichier.
     * @param jsonFormatString le fichier d'entree JSON en format String
     * @return l'objet Client et ses attributs comme dans le fichier d'entree
     */
    public static Client convertirStringEnClient(String nomFichier, String jsonFormatString) {
        Client client = new Client();

        try {
            JSONObject clientObjet = (JSONObject) JSONSerializer.toJSON(jsonFormatString);

            if (!validerEntrees(clientObjet, client)) {
                LireEcrireFichier.ecrireFichierMsgErreur(nomFichier, MSG_ERREUR);
            } else {
                client = new Client(clientObjet.getString(CLE_DOSSIER),
                        clientObjet.getString(CLE_MOIS),
                        parcourirReclamations(clientObjet));
            }
        } catch (JSONException e) {
            LireEcrireFichier.ecrireFichierMsgErreur(nomFichier, MSG_ERREUR);
        }

        return client;
    }

    /**
     * Cette methode sert a verifier que le numero de client, le contrat, le mois et
     * la liste de Reclamation du Client sont valides en appelant les methodes de validation
     * sur chaque champ du Client. Une seule donnee invalide rend l'entierete invalide.
     *
     * @param clientObjet l'objet JSON pour verification de la presence des pairs cle-valeur appropries
     * @param client      l'objet Client contenant les 4 champs avec les valeurs a valider
     * @return true si c'est valide, false si c'est invalide
     */
    protected static boolean validerEntrees(JSONObject clientObjet, Client client) {
        ValidationMethodes.validerDossier(clientObjet, client);
        ValidationMethodes.validerMois(clientObjet, client);
        ValidationMethodes.validerReclamations(clientObjet, client);

        return client.isEntreeValide();
    }

    /**
     * Cette methode sert a convertir la valeur (liste d'objet JSON) de la cle "reclamations"
     * de l'objet JSON d'entree en ArrayList, et ce, pour chaque element de la liste d'objet JSON.
     * La methode parcourt donc toute la liste d'objet JSON en quoi consiste la valeur de la cle
     * "reclamations" de l'objet JSON d'entree pour y extraire le numero du soin, la date du soin
     * et le montant debourse par le client.
     *
     * @param clientObjet l'objet JSON contenant la cle "reclamations" et sa valeur (liste d'objet JSON)
     * @return l'ArrayList de Reclamation, les soin/date/montant de chaque Reclamation identiques au fichier d'entree
     */
    protected static ArrayList<Reclamation> parcourirReclamations(JSONObject clientObjet) {
        JSONArray jsonReclamations = clientObjet.getJSONArray(CLE_RECLAMATIONS);
        ArrayList<Reclamation> reclamations = new ArrayList<>();

        for (int i = 0; i < jsonReclamations.size(); i++) {
            JSONObject jsonReclamation = jsonReclamations.getJSONObject(i);
            reclamations.add(new Reclamation(jsonReclamation.getInt(CLE_SOIN),
                    jsonReclamation.getString(CLE_DATE),
                    jsonReclamation.getString(CLE_MONTANT)));
        }

        return reclamations;
    }

    /**
     * Cette methode prend un objet Client avec ses attributs a jour afin de batir un
     * objet JSON, qui est convertit en String (en format JSON) pour le fichier de sortie.
     *
     * @param client l'object Client qui contient les donnees mises-a-jour
     * @return le String qui sera sous format JSON et qui sera ecrit au fichier de sortie
     */
    public static String convertirClientEnString(Client client) {
        JSONObject jsonClient = new JSONObject();

        if (client.isEntreeValide()) {
            jsonClient.put(CLE_DOSSIER, client.getDossier());
            jsonClient.put(CLE_MOIS, client.getMois());
            jsonClient.put(CLE_RECLAMATIONS, mettreReclamations(client));
            jsonClient.put(CLE_TOTAL, Main.maxMensuel.obtenirTotal());
        }

        String clientString = jsonClient.toString(2);

        return clientString;
    }

    /**
     * Cette methode prend chaque reclamation du Client pour y mettre a jour les soin/date/montant.
     * Le soin et la date demeurent identiques, mais le montant reclame est remplace par le montant
     * rembourse (calcule en appelant la methode qui effectue les calculs).
     *
     * @param client l'objet Client avec l'attribut reclamations (ArrayList de Reclamation) a mettre a jour
     * @return un tableau d'objet JSON, chaque objet JSON contenant le soin, la date et le montant rembourse.
     */
    protected static JSONArray mettreReclamations(Client client) {
        ArrayList<Reclamation> reclamations = client.getReclamations();
        JSONArray jsonReclamations = new JSONArray();

        int numeroContrat = indexerContrat(client.getDossier().substring(0, 1));

        for (Reclamation reclamation : reclamations) {
            JSONObject jsonReclamation = new JSONObject();
            jsonReclamation.put(CLE_SOIN, reclamation.getSoin());
            jsonReclamation.put(CLE_DATE, reclamation.getDate());
            jsonReclamation.put(CLE_MONTANT,
                    Remboursements.calculerMontantARembourser(reclamation, numeroContrat));
            jsonReclamations.add(jsonReclamation);
        }

        return jsonReclamations;
    }

    /**
     * Methode qui convertit le contrat en numero d'index pour qu'il soit indexable dans le tableau 2D
     * de la classe Remboursements (0, 1, 2, 3 ou 4 pour A, B, C, D ou E respectivement).
     *
     * @param contrat le type de contrat du client
     * @return le numero d'index correspondant au type de contrat
     **/
    protected static int indexerContrat(String contrat) {

        int indexContrat = -1;
        String[] tableauContrats = {"A", "B", "C", "D", "E"};

        for (int i = 0; i < tableauContrats.length; i++) {
            if (contrat.equals(tableauContrats[i])) {
                indexContrat = i;
                i = tableauContrats.length;
            }
        }

        return indexContrat;
    }

    /**
     * Cette methode reinitialise le fichier de statistiques "reclamations_stats" en mettant
     * la valeur de chaque cle a zero et le convertit en String.
     *
     * @return statistiques reinitialisees.
     */
    public static String reinitialiserStatistiques() {
        JSONObject jsonStatistiques = new JSONObject();
        jsonStatistiques.put(NBR_RECLAMATIONS_TRAITEES, 0);
        jsonStatistiques.put(NBR_DEMANDES_REJETEES, 0);
        jsonStatistiques.put(NBR_SOIN, "{}");
        jsonStatistiques.put(MONTANT_MAX_PAR_SOIN, "{}");
        jsonStatistiques.put(MOYENNE_MONTANT_PAR_SOIN, "{}");

        String statsString = jsonStatistiques.toString(2);

        return statsString;
    }

    /**
     * Cette methode affiche la premiere partie des statistiques, c'est-a-dire le nombre de
     * reclamations traitees et le nombre de demandes rejettees.
     *
     * @param statsString les statiques a afficher.
     **/
    public static void afficherStatistiques(String statsString) {
        JSONObject statsObjet = (JSONObject) JSONSerializer.toJSON(statsString);
        String contenuStats = NBR_RECLAMATIONS_TRAITEES + ": " + statsObjet.get(NBR_RECLAMATIONS_TRAITEES) + "\n";
        contenuStats += NBR_DEMANDES_REJETEES + ": " + statsObjet.get(NBR_DEMANDES_REJETEES) + "\n";

        System.out.print(contenuStats);

        generererStatsObject(statsObjet, NBR_SOIN);
        generererStatsObject(statsObjet, MONTANT_MAX_PAR_SOIN);
        generererStatsObject(statsObjet, MOYENNE_MONTANT_PAR_SOIN);
    }

    /**
     * Cette methode affiche la deuxieme partie des statistiques, c'est-a-dire le nombre de
     * soins declares pour chaque type de soin, le montant maximal reclame par soin et la
     * moyenne des montants reclames par soin.
     *
     * @param statsObjet les statiques a afficher.
     * @param cle        la cle a traiter.
     **/
    public static void generererStatsObject(JSONObject statsObjet, String cle) {
        System.out.println(cle + ":");

        String contenuStats = "";

        JSONObject objectCherche = (JSONObject) statsObjet.get(cle);
        Iterator<String> keys = objectCherche.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            contenuStats += " " + CLE_SOIN + " " + key + ": " + objectCherche.get(key) + "\n";
        }

        System.out.print(contenuStats);
    }
}
