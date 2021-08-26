import java.util.ArrayList;

public class Client {

    private String dossier;
    private String mois;
    private ArrayList<Reclamation> reclamations;
    private boolean entreeValide = true;
    private String msgErreur;

    public Client() {
    }

    public Client(String dossier, String mois, ArrayList<Reclamation> reclamations) {
        this.dossier = dossier;
        this.mois = mois;
        this.reclamations = reclamations;
    }

    public String getDossier() {
        return dossier;
    }

    public void setDossier(String dossier) {
        this.dossier = dossier;
    }

    public String getMois() {
        return mois;
    }

    public void setMois(String mois) {
        this.mois = mois;
    }

    public ArrayList<Reclamation> getReclamations() {
        return reclamations;
    }

    public void setReclamations(ArrayList<Reclamation> reclamations) {
        this.reclamations = reclamations;
    }

    public boolean isEntreeValide() {
        return entreeValide;
    }

    public void setEntreeValide(boolean entreeValide) {
        this.entreeValide = entreeValide;
    }

    public String getMsgErreur() {
        return msgErreur;
    }

    public void setMsgErreur(String msgErreur) {
        this.msgErreur = msgErreur;
    }

    @Override
    public String toString() {
        return "Client{" +
                "dossier='" + dossier + '\'' +
                ", mois='" + mois + '\'' +
                ", reclamations=" + reclamations +
                ", entreeValide=" + entreeValide +
                '}';
    }
}
