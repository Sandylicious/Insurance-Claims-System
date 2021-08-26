import java.util.ArrayList;

public class Date {

    private String dateReclamation;
    private int occurences;

    public static String MSG_ERR_MAX_OCCURENCE_DEPASSE =
            "{\n\t\"message\": \"Erreur: Un maximum de 4 soins peuvent etre reclames pour un meme jour. \"\n}";

    public Date(String dateReclamation) {
        this.dateReclamation = dateReclamation;
        this.occurences = 1;
    }

    public void incrementerOccurences () {
        this.occurences++;
    }

    public String getDateReclamation() {
        return dateReclamation;
    }

    /**
     * Verifie le nombre d'occurences de cette date (le nombre de soins reclames pour ce jour meme).
     * Si c'est plus que 4, emet un message d'erreur et termine le programme.
     */
    public void verifierOccurences () {
        if (this.occurences > 4) {
            LireEcrireFichier.ecrireFichierMsgErreur(Main.fichierRemboursements, MSG_ERR_MAX_OCCURENCE_DEPASSE);

            Client clientErreur = new Client("","", new ArrayList<>());
            clientErreur.setEntreeValide(false);

            LireEcrireFichier.ecrireFichierStatistiques(clientErreur);
            System.exit(3);
        }
    }
}
