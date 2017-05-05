package sg.edu.nus.comp.lms.domain.util;

import org.jblas.DoubleMatrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class DoubleMatrixUtils {

    public static void write(String filename, DoubleMatrix matrix) throws IOException {
        try (PrintWriter writer = new PrintWriter(filename)) {
            writer.println(matrix.toString("%f", "", "\n", ",", "\n"));
        }
    }

    public static DoubleMatrix read(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            double[] firstLine = readLine(reader.readLine());
            double[][] data = new double[firstLine.length][firstLine.length];
            data[0] = firstLine;

            for (int lineNumber = 1; lineNumber < data.length; lineNumber++) {
                data[lineNumber] = readLine(reader.readLine());
            }
            return new DoubleMatrix(data);
        }
    }

    private static double[] readLine(String line) {
        return Arrays.stream(line.split(","))
                .mapToDouble(Double::parseDouble)
                .toArray();
    }
}
