package sg.edu.nus.comp.lms.algorithm.util;

import org.jblas.DoubleMatrix;
import org.jblas.Eigen;
import sg.edu.nus.comp.lms.algorithm.ClusteringAlgorithmFactory;
import sg.edu.nus.comp.lms.algorithm.single.KMeans;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.util.ArraysUtils;
import sg.edu.nus.comp.lms.domain.util.DoubleMatrixUtils;
import sg.edu.nus.comp.lms.domain.weka.util.InstanceUtils;
import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.lang.Math.exp;
import static java.lang.Math.pow;

public class SpectralClusteringUtils {

    public static final ClusteringAlgorithmFactory DEFAULT_CLUSTERING_FACTORY = new KMeans.KMeansFactory();

    private static final double SIGMA_SCALAR = 1.5;
    private static final double EPS = Math.ulp(1);

    public static DoubleMatrix buildLaplacian(Instances instances, List<String> ids) {
        DoubleMatrix[] matrices = buildAdjacencyAndDegreeMatrices(instances, ids);
        return matrices[1].sub(matrices[0]);
    }

    public static DoubleMatrix buildLaplacianNormalize(Instances instances, List<String> ids) {
        DoubleMatrix[] matrices = buildAdjacencyAndDegreeMatrices(instances, ids);
        DoubleMatrix modifiedD = new DoubleMatrix(ids.size(), ids.size());
        for (int i = 0; i < ids.size(); i++) {
            double value = matrices[1].get(i, i);
            if (-EPS > value || EPS < value) {
                modifiedD.put(i, i, 1 / pow(value, 2));
            }
        }
        return modifiedD.mmul(matrices[1].sub(matrices[0])).mmul(modifiedD);
    }

    private static DoubleMatrix[] buildAdjacencyAndDegreeMatrices(Instances instances, List<String> ids) {
        Map<String, Instance> idToInstance = InstancesUtils.extractStringAttrToInstance(instances, Settings.ID_ATTR);

        double[][] wData = new double[ids.size()][ids.size()];
        double[][] dData = new double[ids.size()][ids.size()];

        double[] distances = new double[(1 + ids.size()) * ids.size() / 2];
        int index = 0;
        for (int r = 0; r < ids.size(); r++) {
            Instance rInstance = idToInstance.get(ids.get(r));
            for (int c = r + 1; c < ids.size(); c++) {
                Instance cInstance = idToInstance.get(ids.get(c));
                double value = InstanceUtils.norm2(rInstance, cInstance);
                wData[r][c] = value;
                distances[index++] = value;
            }
        }

        double sigma = SIGMA_SCALAR * ArraysUtils.getMedianInPlace(distances);

        for (int r = 0; r < ids.size(); r++) {
            for (int c = r + 1; c < ids.size(); c++) {
                wData[r][c] = exp(-pow(wData[r][c], 2) / (2 * pow(sigma, 2)));
                wData[c][r] = wData[r][c];
            }
            dData[r][r] = Arrays.stream(wData[r]).sum();
        }

        return new DoubleMatrix[]{new DoubleMatrix(wData), new DoubleMatrix(dData)};
    }

    public static DoubleMatrix buildEigenvectors(DoubleMatrix laplacian, int k) {
        return buildEigenvectors(buildEigenmatrix(laplacian), k);
    }

    public static DoubleMatrix buildEigenvectors(DoubleMatrix[] eigenmatrix, int k) {
        return getLargestEigenvectors(eigenmatrix, k);
    }

    public static DoubleMatrix[] buildEigenmatrix(DoubleMatrix laplacian) {
        return Eigen.symmetricEigenvectors(laplacian);
    }

    private static DoubleMatrix getLargestEigenvectors(DoubleMatrix[] eigenmatrix, int k) {
        DoubleMatrix eigenvectors = eigenmatrix[0];
        Integer[] largestEigenvaluesIndexes = findLargestEigenvalues(eigenmatrix[1], k);
        double[][] data = new double[eigenvectors.getRows()][k];
        for (int i = 0; i < largestEigenvaluesIndexes.length; i++) {
            for (int j = 0; j < eigenvectors.getRows(); j++) {
                data[j][i] = eigenvectors.get(j, largestEigenvaluesIndexes[i]);
            }
        }
        return new DoubleMatrix(data);
    }

    private static Integer[] findLargestEigenvalues(DoubleMatrix eigenvalues, int k) {
        Integer[] index = IntStream.range(0, eigenvalues.getRows()).mapToObj(i -> i).toArray(Integer[]::new);
        Arrays.sort(index, (o1, o2) -> Double.compare(eigenvalues.get(o1, o1), eigenvalues.get(o2, o2)));
        return Arrays.copyOfRange(index, 0, k);
    }

    public static Function<Integer, DoubleMatrix> getLaplacianFunction() {
        return layerNumber -> {
            try {
                return getLaplacian(layerNumber);
            } catch (IOException e) {
                throw new RuntimeException("Incorrect layer number " + layerNumber);
            }
        };
    }

    public static DoubleMatrix getLaplacian(int layerNumber) throws IOException {
        return DoubleMatrixUtils.read(Settings.LAPLACIANS_MATRICES[layerNumber]);
    }

    public static BiFunction<DoubleMatrix, Integer, DoubleMatrix> getEigenvectorFunction(int k) {
        return (laplacian, layerNumber) -> {
            try {
                return SpectralClusteringUtils.buildEigenvectors(getEigenmatrix(layerNumber), k);
            } catch (IOException e) {
                throw new RuntimeException("Incorrect layer number " + layerNumber);
            }
        };
    }

    public static DoubleMatrix[] getEigenmatrix(int layerNumber) throws IOException {
        DoubleMatrix[] eigenmatrix = new DoubleMatrix[2];
        eigenmatrix[0] = DoubleMatrixUtils.read(Settings.EIGENVECTORS_MATRICES[layerNumber]);
        eigenmatrix[1] = DoubleMatrixUtils.read(Settings.EIGENVALUES_MATRICES[layerNumber]);
        return eigenmatrix;
    }
}
