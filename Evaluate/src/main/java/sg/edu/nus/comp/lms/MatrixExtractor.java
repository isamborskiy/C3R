package sg.edu.nus.comp.lms;

import org.jblas.DoubleMatrix;
import sg.edu.nus.comp.lms.algorithm.util.SpectralClusteringUtils;
import sg.edu.nus.comp.lms.domain.CommonMethods;
import sg.edu.nus.comp.lms.domain.Settings;
import sg.edu.nus.comp.lms.domain.util.DoubleMatrixUtils;
import sg.edu.nus.comp.lms.domain.weka.util.InstancesUtils;
import weka.core.Instances;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

public class MatrixExtractor {

    public static void main(String[] args) {
        List<Instances> sources = CommonMethods.readAllIntersectedSources();

        IntStream.range(0, sources.size())
                .parallel()
                .forEach(modalIndex -> {
                    String modalName = Settings.MODAL_NAME[modalIndex];
                    try {
                        Instances instances = sources.get(modalIndex);

                        List<String> ids = InstancesUtils.extractStringAttr(instances, Settings.ID_ATTR);
                        DoubleMatrix laplacian = SpectralClusteringUtils.buildLaplacianNormalize(instances, ids);
                        DoubleMatrixUtils.write(modalName + "_laplacian.csv", laplacian);

                        DoubleMatrix[] eigenmatrix = SpectralClusteringUtils.buildEigenmatrix(laplacian);
                        DoubleMatrixUtils.write(modalName + "_eigenvectors.csv", eigenmatrix[0]);
                        DoubleMatrixUtils.write(modalName + "_eigenvalues.csv", eigenmatrix[1]);
                    } catch (IOException e) {
                        throw new RuntimeException("Exception while extract matrix from " + modalName);
                    }
                });
    }
}
