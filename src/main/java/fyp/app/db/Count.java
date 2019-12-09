package fyp.app.db;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table
@SequenceGenerator(name="seq", initialValue=1)
public class Count {
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq")
    @Id
    private long id;

    private int time;
    private String inputFile;

    private int countExp;
    private int countAdd;
    private int countDrop;
    private int countBorrow;
    private int countSwitch;

    private int firmId;
    private BigDecimal finalFitness;

    public Count() {
    }

    public Count(int time, String inputFile, int countExp, int countAdd, int countDrop, int countBorrow, int countSwitch, int firmId, BigDecimal finalFitness) {
        this.time = time;
        this.inputFile = inputFile;
        this.countExp = countExp;
        this.countAdd = countAdd;
        this.countDrop = countDrop;
        this.countBorrow = countBorrow;
        this.countSwitch = countSwitch;
        this.firmId = firmId;
        this.finalFitness = finalFitness;
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

    public int getCountExp() {
        return countExp;
    }

    public void setCountExp(int countExp) {
        this.countExp = countExp;
    }

    public int getCountAdd() {
        return countAdd;
    }

    public void setCountAdd(int countAdd) {
        this.countAdd = countAdd;
    }

    public int getCountDrop() {
        return countDrop;
    }

    public void setCountDrop(int countDrop) {
        this.countDrop = countDrop;
    }

    public int getCountBorrow() {
        return countBorrow;
    }

    public void setCountBorrow(int countBorrow) {
        this.countBorrow = countBorrow;
    }

    public int getCountSwitch() {
        return countSwitch;
    }

    public void setCountSwitch(int countSwitch) {
        this.countSwitch = countSwitch;
    }

    public int getFirmId() {
        return firmId;
    }

    public void setFirmId(int firmId) {
        this.firmId = firmId;
    }

    public BigDecimal getFinalFitness() {
        return finalFitness;
    }

    public void setFinalFitness(BigDecimal finalFitness) {
        this.finalFitness = finalFitness;
    }
}
