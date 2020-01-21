package fyp.app.test;

import fyp.app.objects.Firm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class test {
    public static void main(String[] args){
        List<Firm2> firms = new ArrayList<Firm2>();
        int currentRank = 1;
        double currentFitness = 1.0d;
        firms.add(new Firm2(1,0.0, 1));
        firms.add(new Firm2(2,1.0, 2));
        firms.add(new Firm2(3,0.4, 3));
        firms.add(new Firm2(4,0.6, 4));
        firms.add(new Firm2(5,0.0, 5));
        firms.add(new Firm2(6,0.9, 6));
        Collections.sort(firms);
        for (int i = 0; i < firms.size(); i++) {
            Firm2 f = (Firm2)firms.get(i);
            double focalFitness = f.getFitness();
            if (currentFitness == focalFitness) {
                System.out.println("test: "+f.id);
                f.setRank(currentRank);
            } else {
                currentRank = i + 1;
                f.setRank(currentRank);
                currentFitness = focalFitness;
            }
        }
        System.out.println(currentFitness);
        for (int i = 0; i < firms.size(); i++) {
            System.out.println(firms.get(i).id+" "+firms.get(i).rank+" "+firms.get(i).fitness);
        }
    }
}
class Firm2 implements Comparable<Firm2> {
    double fitness;
    int rank;
    int id;

    public Firm2(int i, double f, int r){id=i;fitness = f;rank = r;}

    public double getFitness(){ return fitness;}
    public void setRank(int r){rank = r;}

    public int compareTo(Firm2 compareFirm) {
        double compareFitness = ((Firm2)compareFirm).getFitness();
        double thisFitness = this.getFitness();
        if(thisFitness < compareFitness) {
            return 1;
        } else if(compareFitness < thisFitness) {
            return -1;
        } else {
            return 0;
        }
    }


}