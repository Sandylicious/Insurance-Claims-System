import java.util.ArrayList;
import static java.lang.Integer.MAX_VALUE;

public class MaxMensuel {

    public static final String MSG_MAX_DEPASSE =
            "{\n\t\"message\": \"erreur, depassement du maximum mensuel reclame de 500.00$ pour un type de soin.\"\n}";

    private static final int[] maximumsMensuels =
            {MAX_VALUE, 25000, 25000, MAX_VALUE, MAX_VALUE, 15000, 30000, MAX_VALUE, MAX_VALUE, 20000};
            //Masso,    Osteo, Psycho,Dentaire,  Natu-Acu,  Chiro, Physio,Orth/ergo, Kinesi,    MD prive
            //0,        100,   200,   300-399,   400,       500,   600,   700,       150,       175

    // montants mensuels actuels par soin (debutent tous a 0 lorsque construit au lancement du programme)
    private Dollar[] montantsMensuelsActuels = new Dollar[maximumsMensuels.length];

    // tableau qui represente le montant mensuel reclame (demande) par type de soin (debute aussi a 0 pour chaque case)
    private Dollar[] montantsMensuelsReclames = new Dollar[maximumsMensuels.length];

    public MaxMensuel() {
        for (int i = 0; i < montantsMensuelsActuels.length; i++) {
            montantsMensuelsActuels[i] = new Dollar("0");
            montantsMensuelsReclames[i] = new Dollar("0");
        }
    }

    protected void setMontantsMensuelsActuels(int[] montantsMensuelsActuels) {
        for (int i = 0; i < montantsMensuelsActuels.length; i++)
            this.montantsMensuelsActuels[i].setCents(montantsMensuelsActuels[i]);
    }

    /**
     * Calcule la difference dans le cas ou le montant maximal mensuel serait depasse par l'ajout du montant rembourse.
     * Si pour un soin X le montant maximal mensuel est de 25000, que le montant actuel est de 23000 pour ce soin et
     * que le montant rembourse est de 5000, cette methode retourne 2000 et non 5000 (car 25000 - 23000 = + 2000).
     *
     * @param index            Le numero d'index du soin
     * @param montantRembourse Le montant de remboursement qu'on verifie si son ajout depasse le montant mensuel max
     * @return Le vrai montant rembourse.
     */
    public int calculerDifference(int index, Dollar montantRembourse) {
        int vraiMontantRembourse = montantRembourse.getCents();

        // Si montant max sera atteint par la prochaine, rembourser seulement le montant jusqu'a ce max.
        if (vraiMontantRembourse + this.montantsMensuelsActuels[index].getCents() > maximumsMensuels[index])
            vraiMontantRembourse = maximumsMensuels[index] - this.montantsMensuelsActuels[index].getCents();

        this.montantsMensuelsActuels[index].additionner(vraiMontantRembourse);

        return vraiMontantRembourse;
    }

    /**
     * Additionne les montants rembourses de tous les soins reclames pour faire un grand total.
     *
     * @return Le total rembourse pour tous les soins reclames
     */
    public String obtenirTotal() {
        Dollar total = new Dollar("0");

        for (Dollar montantsMensuelsActuel : this.montantsMensuelsActuels) total.additionner(montantsMensuelsActuel);

        return total.toString();
    }

    /**
     * Ajoute un montant reclame au montant mensuel reclame pour un type de soin.
     * @param index Index qui represente le type de soin pour lequel la reclamation se fait
     * @param montantReclame Le montant reclame tel quel
     */
    public void ajouterReclamationMensuel (int index, Dollar montantReclame) {
        this.montantsMensuelsReclames[index].additionner(montantReclame);
    }

    /**
     * Verifie le montant mensuel reclame pour chaque soin.
     * Si le montant reclame d'un soin depasse 500.00$, emet un message d'erreur.
     */
    public void verifierReclamationsMax() {

        for (int i = 0; i < montantsMensuelsReclames.length; i++) {
            if (this.montantsMensuelsReclames[i].getCents() > 50000) {
                LireEcrireFichier.ecrireFichierMsgErreur(Main.fichierRemboursements, MSG_MAX_DEPASSE);

                Client clientErreur = new Client("","", new ArrayList<>());
                clientErreur.setEntreeValide(false);

                LireEcrireFichier.ecrireFichierStatistiques(clientErreur);

                System.exit(1);
            }
        }
    }
}
