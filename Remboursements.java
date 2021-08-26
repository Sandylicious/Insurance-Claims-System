import static java.lang.Integer.MAX_VALUE;

public class Remboursements {

    // Liste des soins disponibles (en ordre d'implementation)
    public static final int[] SOINS = {0, 100, 200, 300, 400, 500, 600, 700, 150, 175};

    // Representation des pourcentages de couverture pour chaque contrat
    private static final double[] CONTRAT_A = {0.25, 0.35, 0.25, 0.00, 0.00, 0.25, 0.40, 0.00, 0.00, 0.50};
    private static final double[] CONTRAT_B = {0.50, 0.50, 1.00, 0.50, 0.00, 0.50, 1.00, 0.70, 0.00, 0.75};
    private static final double[] CONTRAT_C = {0.90, 0.95, 0.90, 0.90, 0.90, 0.90, 0.75, 0.90, 0.85, 0.90};
    private static final double[] CONTRAT_D = {1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 1.00, 0.95};
    private static final double[] CONTRAT_E = {0.15, 0.25, 0.12, 0.60, 0.25, 0.30, 0.15, 0.22, 0.15, 0.25};

    // Representation en tableau 2D de ces pourcentages de couverture pour chaque contrat
    private static final double[][] TABLEAU_REMBOURSEMENTS = {CONTRAT_A, CONTRAT_B, CONTRAT_C, CONTRAT_D, CONTRAT_E};

    // Representation du montant maximum couvert (en cents) pour les contrats B, D, E, et representation en tableau 2D
    private static final int[] CONTRAT_B_MAX =
            {4000, 5000, MAX_VALUE, MAX_VALUE, 0, 5000, MAX_VALUE, MAX_VALUE, MAX_VALUE, MAX_VALUE};
    private static final int[] CONTRAT_D_MAX =
            {8500, 7500, 10000, MAX_VALUE, 6500, MAX_VALUE, 10000, 9000, 15000, MAX_VALUE};
    private static final int[] CONTRAT_E_MAX =
            {MAX_VALUE, MAX_VALUE, MAX_VALUE, MAX_VALUE, 1500, 2000, MAX_VALUE, MAX_VALUE, MAX_VALUE, 2000};

    // Representation en tableau 2D de ces montants maximals couverts
    private static final int[][] TABLEAU_MAX_REMBOURSEMENTS =
            {null, CONTRAT_B_MAX, null, CONTRAT_D_MAX, CONTRAT_E_MAX};

    // Message d'erreur pour type de contrat
    public static final String ERREUR_CONTRAT = "{\n\t\"message\": \"Erreur: Type de contrat invalide\"\n}";


    /**
     * Methode qui calcule le montant a rembourser selon le montant reclame, le numero de soin et le type de contrat.
     * Cette methode utilise le tableau 2D (Tableau de Tableaux de doubles) pour representer la couverture (en %) de
     * chaque soin selon le contrat.
     * Si le contrat est B ou D, le montant rembourse est le plus petit entre la couverture (%) multiplie par le
     * montant reclame et le montant maximal couvert.
     *
     * @param reclamation  L'object reclamation contenant les champs "soin" et "montant"
     * @param indexContrat Le type de contrat converti en numero d'index pour reperage dans le tableau
     * @return Le montant qui sera rembourse au client pour ce soin
     **/
    public static String calculerMontantARembourser(Reclamation reclamation, int indexContrat) {

        Dollar montantRembourse = new Dollar("0");
        Dollar montantReclame = new Dollar(reclamation.getMontant());
        int indexNumeroDeSoin = indexerNumeroDeSoin(reclamation.getSoin());
        double pourcentageCouverture = TABLEAU_REMBOURSEMENTS[indexContrat][indexNumeroDeSoin];

        try {
            // Verifie que la demande de reclamation est bel et bien valide.
            verifierDemande(reclamation, montantReclame);

            // Appliquer le pourcentage au montant Reclame
            montantRembourse.setCents(montantReclame.multiplierPourcentage(pourcentageCouverture));

            // Comparer ce montant avec le montant maximal couvert et prendre le minimum
            montantRembourse.setCents(calculerMinimum(indexContrat, indexNumeroDeSoin, montantRembourse.getCents()));

            // Ajouter le montant 'final' au montant mensuel et verifier si c'est depasse
            // (ajuster le montant final si oui).
            montantRembourse.setCents(Main.maxMensuel.calculerDifference(indexNumeroDeSoin, montantRembourse));

        } catch (ArrayIndexOutOfBoundsException e) {
            LireEcrireFichier.ecrireFichierMsgErreur(Main.fichierRemboursements, ERREUR_CONTRAT);
        }

        // retourner le nombre de $ et cents en string
        return montantRembourse.toString();
    }

    /**
     * Verifie que la demande de reclamation est valide.
     * Pour que la demande soit valide, elle doit avoir un maximum de 4 soins pour une meme date,
     * le montant reclame doit etre plus que 0 pour toute reclamation, et le montant mensuel
     * ne devrait pas depasser 500.00$.
     * <p>
     * Si une condition n'est pas respectee, le programme ecrit un message d'erreur dans le fichier de sortie
     * et le programme s'arrete.
     *
     * @param reclamation    La reclamation a verifier
     * @param montantReclame Le montant de la reclamation
     */
    protected static void verifierDemande(Reclamation reclamation, Dollar montantReclame) {
        int indexNumeroDeSoin = indexerNumeroDeSoin(reclamation.getSoin());

        // S'assurer qu'un maximum de 4 soins soient reclames pour une meme date (un meme jour)
        ajouterDates(reclamation);
        verifierSoinsParJour();

        // Verifier si le montantReclame est de 0. Si oui, quitter le programme.
        montantReclame.verifierMontantReclamePlusQueZero();

        // Ajouter ce montant reclame tel quel aux montants reclames
        Main.maxMensuel.ajouterReclamationMensuel(indexNumeroDeSoin, montantReclame);

        // S'assurer que le montant mensuel reclame pour un type de soin ne depasse pas 500.00$
        Main.maxMensuel.verifierReclamationsMax();
    }


    /**
     * Incremente le nombre d'occurences d'une Date de reclamation de 1.
     * Si c'est la premiere occurence de la Date, une nouvelle Date est creee.
     *
     * @param reclamation La reclamation qui sera traitee
     */
    protected static void ajouterDates(Reclamation reclamation) {

        boolean dateDejaDansListe = false;

        // parcourir chaque élément du arrayList (fouille linéaire) et comparer la date de notre reclamation
        // avec les dates de notre ArrayList.
        for (int i = 0; i < Main.dates.size(); i++) {
            // Si c'est egal, sur cet objet incrementer le nombre d'occurences de 1
            if (reclamation.getDate().equals(Main.dates.get(i).getDateReclamation())) {
                Main.dates.get(i).incrementerOccurences();
                dateDejaDansListe = true;
                i = Main.dates.size() + 1;
            }
        }
        // Si la date n'est pas trouvee apres avoir itere les dates, creer une nouvelle date et l'ajouter
        if (!dateDejaDansListe) {
            Date date = new Date(reclamation.getDate());
            Main.dates.add(date);
        }
    }

    /**
     * Verifie si le nombre de soins pour un meme jour depasse 4 (le nombre maximal de reclamations pour un meme jour).
     */
    protected static void verifierSoinsParJour() {
        for (int i = 0; i < Main.dates.size(); i++)
            Main.dates.get(i).verifierOccurences();
    }

    /**
     * Retourne le minimum entre le montant rembourse et le maximum remboursable pour un soin particulier.
     *
     * @param indexContrat      Le numero d'index correspondant au contrat de la Reclamation
     * @param indexNumeroDeSoin Le numero d'index correspondant au numero de soin de la Reclamation
     * @param montantRembourse  Le montant rembourse a comparer.
     * @return Le minimum entre le montant rembourse et le maximum remboursable.
     */
    protected static int calculerMinimum(int indexContrat, int indexNumeroDeSoin, int montantRembourse) {

        int resultat = montantRembourse;

        if (indexContrat == 1 || indexContrat == 3 || indexContrat == 4) {
            resultat = Math.min(montantRembourse, TABLEAU_MAX_REMBOURSEMENTS[indexContrat][indexNumeroDeSoin]);
        }

        return resultat;
    }

    /**
     * Methode qui convertit le numero de soin en numero d'index pour qu'il soit indexable dans le tableau 2D
     *
     * @param numeroDeSoin Le numero de soin
     * @return Le numero d'index correspondant au numero de soin
     **/
    protected static int indexerNumeroDeSoin(int numeroDeSoin) {
        int indexNumeroDeSoin = -1;

        if (numeroDeSoin >= 300 && numeroDeSoin <= 399)
            numeroDeSoin = 300;

        for (int i = 0; i < SOINS.length; i++) {
            if (numeroDeSoin == SOINS[i]) {
                indexNumeroDeSoin = i;
                i = SOINS.length;
            }
        }

        return indexNumeroDeSoin;
    }
}
