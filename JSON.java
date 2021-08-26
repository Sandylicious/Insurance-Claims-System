import java.util.ArrayList;

import static java.lang.Double.parseDouble;

public class Dollar {

    public static final String MSG_ERR_MONTANT_RECLAME_ZERO =
            "{\n\t\"message\": \"erreur, tous les montants reclames doivent etre superieurs a 0. \"\n}";

    private int cents;

    public Dollar() {
    }

    public Dollar(String montant) {
        this.cents = (int) (100 * parseDouble(montant.trim().replace(",", ".").replace("$", "")));
    }

    public int getCents() {
        return cents;
    }

    public void setCents(int cents) {
        this.cents = cents;
    }


    // Methodes publiques

    /**
     * Ajoute un Dollar (son nombre de cents) a l'objet Dollar courant.
     *
     * @param montant Le Dollar a ajouter a l'objet courant
     */
    public void additionner(Dollar montant) {
        this.cents += montant.cents;
    }

    /**
     * Ajoute a l'object Dollar courant un montant entier qui represente un nombre de cents.
     *
     * @param montant Le nombre de cents a ajouter a l'object courant.
     */
    public void additionner(int montant) {
        this.cents += montant;
    }

    /**
     * Multiplie l'objet Dollar courant par un pourcentage et retourne le resultat.
     *
     * @param pourcentage Le pourcentage de couverture
     * @return Le nombre de cents resultant de la multiplication.
     */
    public int multiplierPourcentage(double pourcentage) {
        return (int) (pourcentage * this.getCents());
    }

    @Override
    public String toString() {
        return this.cents / 100 + "." + String.format("%02d", this.cents % 100) + "$";
    }


    public void verifierMontantReclamePlusQueZero() {
        if (this.cents == 0) {
            LireEcrireFichier.ecrireFichierMsgErreur(Main.fichierRemboursements, MSG_ERR_MONTANT_RECLAME_ZERO);

            Client clientErreur = new Client("","", new ArrayList<>());
            clientErreur.setEntreeValide(false);

            LireEcrireFichier.ecrireFichierStatistiques(clientErreur);

            System.exit(2);
        }
    }
}
