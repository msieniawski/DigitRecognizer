package pl.edu.agh.digitrecognizer;

import java.io.Serializable;

public class NetworkData implements Serializable {

    private int[] sizes;
    private double[][] biasesArr;
    private double[][][] weightsArr;

    public NetworkData(int[] sizes, double[][] biasesArr, double[][][] weightsArr) {
        this.sizes = sizes;
        this.biasesArr = biasesArr;
        this.weightsArr = weightsArr;
    }

    public int[] getSizes() {
        return sizes;
    }

    public double[][] getBiasesArr() {
        return biasesArr;
    }

    public double[][][] getWeightsArr() {
        return weightsArr;
    }
}
