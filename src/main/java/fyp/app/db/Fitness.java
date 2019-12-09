package fyp.app.db;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table
@SequenceGenerator(name="seq", initialValue=1)
public class Fitness {
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
    @Id
    private long id;

    private int time;
    private String inputFile;
    private int iteration;
    private BigDecimal fitness;
    private int firmId;

    public Fitness() {
    }

    public Fitness(int time, String inputFile, int iteration, BigDecimal fitness, int firmId) {
        this.time = time;
        this.inputFile = inputFile;
        this.iteration = iteration;
        this.fitness = fitness;
        this.firmId = firmId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getInputFile() {
        return inputFile;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public BigDecimal getFitness() {
        return fitness;
    }

    public void setFitness(BigDecimal fitness) {
        this.fitness = fitness;
    }

    public int getFirmId() {
        return firmId;
    }

    public void setFirmId(int firmId) {
        this.firmId = firmId;
    }
}
