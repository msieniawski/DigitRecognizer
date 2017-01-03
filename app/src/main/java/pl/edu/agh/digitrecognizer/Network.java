package pl.edu.agh.digitrecognizer;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import java.io.*;
import java.util.Random;

public class Network {

    private int[] sizes;
    private double[][] biasesArr;
    private double[][] biasDeltaArr;
    private double[][][] weightsArr;
    private double[][][] weightDeltaArr;
    private double[][] outputsArr;

    private double learningRate;
    private double momentum;

    private Random random;

    /**
     * @param input array of doubles representing the pixels of a digit image
     * @return the recognized digit
     */
    public int evaluate(double[] input) {
        outputsArr[0] = input;
        activate();
        final double[] output = outputsArr[2];
        final double max = Doubles.max(output);
        return Doubles.indexOf(output, max);
    }

    /**
     * Sigmoid function is used to calculate a neuron output
     */
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    /**
     * Modifies weights and biases of the network, so that it's output is closer to expected
     */
    private void backPropagation(double[] expectedOutputs) {

        //updating output layer
        for(int i = 0; i < sizes[2]; i++) {

            //update bias
            final double output = outputsArr[2][i];
            final double expected = expectedOutputs[i];
            final double partialDerivative = -output * (1 - output) * (expected - output);
            final double deltaBias = -learningRate * partialDerivative;
            biasesArr[2][i] += deltaBias + momentum * biasDeltaArr[2][i];
            biasDeltaArr[2][i] = deltaBias;

            //update incoming weights
            for(int j = 0; j < sizes[1]; j++) {
                final double sourceOutput = outputsArr[1][j];
                final double deltaWeight = -learningRate * partialDerivative * sourceOutput;
                weightsArr[1][j][i] += deltaWeight + momentum * weightDeltaArr[1][j][i];
                weightDeltaArr[1][j][i] = deltaWeight;
            }
        }

        //updating hidden layer
        for(int i = 0; i < sizes[1]; i++) {

            //update bias
            final double output = outputsArr[1][i];
            final double outputsErrorSum = getOutputsErrorSum(i, expectedOutputs);
            final double partialDerivative = output * (1 - output) * outputsErrorSum;
            final double deltaBias = -learningRate * partialDerivative;
            biasesArr[1][i] += deltaBias + momentum * biasDeltaArr[2][i];
            biasDeltaArr[1][i] = deltaBias;

            //update incoming weights
            for(int j = 0; j < sizes[0]; j++) {
                final double sourceOutput = outputsArr[0][j];
                final double deltaWeight = -learningRate * partialDerivative * sourceOutput;
                weightsArr[0][j][i] += deltaWeight + momentum * weightDeltaArr[0][j][i];
                weightDeltaArr[0][j][i] = deltaWeight;
            }
        }
    }

    private double getOutputsErrorSum(int neuronNum, double[] expectedOutputs) {
        double outputsSum = 0;

        for(int k = 0; k < sizes[2]; k++) {
            final double expected = expectedOutputs[k];
            final double weight = weightsArr[1][neuronNum][k];
            final double realOutput = outputsArr[2][k];
            outputsSum += -(expected - realOutput) * realOutput * (1 - realOutput) * weight;
        }
        return outputsSum;
    }

    /**
     * Calculates outputs for the hidden and output layers (the input layer outputs = network input)
     */
    private void activate() {
        calculateOutput(2);
        calculateOutput(3);
    }

    /**
     * @param layerNum calculates output for given layer
     */
    private void calculateOutput(int layerNum) {
        int idx = layerNum - 1;

        for(int i = 0; i < sizes[idx]; i++) {
            double sum = biasesArr[idx][i];
            int prevLayerIdx = idx - 1;

            for(int j = 0; j < sizes[prevLayerIdx]; j++) {
                final double weight = weightsArr[prevLayerIdx][j][i];
                final double prevOutput = outputsArr[prevLayerIdx][j];
                sum += weight * prevOutput;
            }
            outputsArr[idx][i] = sigmoid(sum);
        }
    }

    /**
     * Helper method for network training, returns the benchmark result vector for an input digit
     *
     * @param expectedValue digit
     * @return an optimal result vector, with value 1.0 for the input digit and 0.0 for all other digits
     */
    private double[] getExpectedVector(int expectedValue) {
        final double[] expectedVector = new double[10];

        for(int i = 0; i < 10; i++) {
            if(i == expectedValue) {
                expectedVector[i] = 1.0;
            } else {
                expectedVector[i] = 0.0;
            }
        }
        return expectedVector;
    }

    public Network(NetworkData networkData) {
        this.sizes = networkData.getSizes();
        this.biasesArr = networkData.getBiasesArr();
        this.weightsArr = networkData.getWeightsArr();

        final int layerNum = sizes.length;
        final int maxSize = Ints.max(sizes);

        this.outputsArr = new double[layerNum][maxSize];
    }

    /**
     * Serializes the result data of a network (biases and weights) in /savedNetworks
     *
     * @param name name of the network data file to be created
     */
    public void save(String name) {
        final NetworkData data = new NetworkData(sizes, biasesArr, weightsArr);

        try {
            final String filename = name + ".ser";
            final FileOutputStream fileOut = new FileOutputStream("savedNetworks/" + filename);
            final ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(data);
            out.close();
            fileOut.close();
            System.out.printf("Network data saved in /savedNetworks/" + filename + ".ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

}
