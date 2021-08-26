public class Reclamation {

    private int soin;
    private String date;
    private String montant;

    public Reclamation() {
    }

    public Reclamation(int soin, String date, String montant) {
        this.soin = soin;
        this.date = date;
        this.montant = montant;
    }

    public int getSoin() {
        return soin;
    }

    public void setSoin(int soin) {
        this.soin = soin;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

    @Override
    public String toString() {
        return "Reclamation{" +
                "soin=" + soin +
                ", date='" + date + '\'' +
                ", montant='" + montant + '\'' +
                '}';
    }
}
