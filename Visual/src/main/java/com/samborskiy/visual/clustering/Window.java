package com.samborskiy.visual.clustering;

import com.samborskiy.algorithm.Clustering;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.samborskiy.visual.clustering.ClusteringAlgorithm.K_MEANS_PLUS_PLUS;
import static com.samborskiy.visual.clustering.ClusteringAlgorithm.SPECTRAL;

public class Window extends Application {

    private static final Color[] COLORS = {Color.RED, Color.BLUE, Color.YELLOW, Color.BLACK, Color.BROWN,
            Color.ORANGE, Color.CHOCOLATE};
    private static final int MIN_CLUSTER_NUMBER = 2;

    private static final ArrayList<Attribute> ATTRIBUTES = new ArrayList<>();
    private static final Attribute ID_ATTRIBUTE = new Attribute("_id", (FastVector) null);
    private static final Attribute X_ATTRIBUTE = new Attribute("x");
    private static final Attribute Y_ATTRIBUTE = new Attribute("y");

    private static final int SCALE = 10;
    private static final int SIZE = 50;
    private static final int PIXELS = SIZE * SCALE;

    static {
        ATTRIBUTES.add(ID_ATTRIBUTE);
        ATTRIBUTES.add(X_ATTRIBUTE);
        ATTRIBUTES.add(Y_ATTRIBUTE);
    }

    private double[][] bitmap = new double[SIZE][SIZE];
    private Canvas canvas;
    private HBox hBox;
    private ChoiceBox<String> algorithm;
    private ChoiceBox<Integer> clusterNumber;

    private EventHandler<MouseEvent> mouseHandler = mouseEvent -> {
        int x = Math.min((int) (mouseEvent.getX()), PIXELS - 1);
        int y = Math.min((int) (mouseEvent.getY()), PIXELS - 1);
        if (x >= 0 && y >= 0) {
            switch (mouseEvent.getButton()) {
                case PRIMARY:
                    if (mouseEvent.getEventType().equals(MouseEvent.MOUSE_PRESSED)
                            || mouseEvent.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
                        bitmap[y / SCALE][x / SCALE] = 1;
                        draw();
                    }
                    break;
                case SECONDARY:
                    clear();
                    canvas.getGraphicsContext2D().clearRect(0, 0, PIXELS, PIXELS);
                    break;
            }
        }
    };

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setResizable(false);
        Group root = new Group();
        root.getChildren().add(canvas = new Canvas(PIXELS, PIXELS));
        root.getChildren().add(hBox = initHBoxLayout());
        hBox.getChildren().add(algorithm = initAlgorithmChoiceBox());
        hBox.getChildren().add(clusterNumber = initClusterNumberChoiceBox());
        hBox.getChildren().add(initDumpButton(stage));

        Scene scene = new Scene(root);
        stage.setScene(scene);

        scene.setOnMousePressed(mouseHandler);
        scene.setOnMouseReleased(mouseHandler);
        scene.setOnMouseDragged(mouseHandler);

        clear();
        draw();
        stage.show();
    }

    private Button initDumpButton(Stage stage) {
        Button button = new Button("Dump");
        button.setOnAction(event -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save file");
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
                File selectedFile = fileChooser.showSaveDialog(stage);

                try (PrintWriter writer = new PrintWriter(selectedFile)) {
                    writer.println(getInstances());
                }
            } catch (Exception ignored) {
            }
        });
        return button;
    }

    private HBox initHBoxLayout() {
        HBox hBox = new HBox();
        hBox.setSpacing(10);
        return hBox;
    }

    private ChoiceBox<String> initAlgorithmChoiceBox() {
        ChoiceBox<String> algorithm = new ChoiceBox<>(FXCollections.observableArrayList(
                SPECTRAL.getName(), K_MEANS_PLUS_PLUS.getName())
        );
        algorithm.getSelectionModel().selectFirst();
        algorithm.getSelectionModel()
                .selectedIndexProperty()
                .addListener((observable, oldValue, newValue) -> draw(algorithm.getItems().get((Integer) newValue)));
        return algorithm;
    }

    private ChoiceBox<Integer> initClusterNumberChoiceBox() {
        ChoiceBox<Integer> clusterNumber = new ChoiceBox<>(FXCollections.observableArrayList(
                IntStream.range(MIN_CLUSTER_NUMBER, COLORS.length + 1).mapToObj(i -> i).collect(Collectors.toList())
        ));
        clusterNumber.getSelectionModel().selectFirst();
        clusterNumber.getSelectionModel()
                .selectedIndexProperty()
                .addListener((observable, oldValue, newValue) -> draw(clusterNumber.getItems().get((Integer) newValue)));
        return clusterNumber;
    }

    private void clear() {
        for (int i = 0; i < bitmap.length; i++) {
            for (int j = 0; j < bitmap[0].length; j++) {
                bitmap[i][j] = 0;
            }
        }
    }

    private void draw() {
        draw(algorithm.getValue(), clusterNumber.getValue());
    }

    private void draw(String algorithmName) {
        draw(algorithmName, clusterNumber.getValue());
    }

    private void draw(int clusterNumber) {
        draw(algorithm.getValue(), clusterNumber);
    }

    private void draw(String algorithmName, int clusterNumber) {
        try {
            ClusteringAlgorithm clusteringAlgorithm = ClusteringAlgorithm.fromString(algorithmName);
            Instances instances = getInstances();
            Clustering clustering = clusteringAlgorithm.getInstance(instances, clusterNumber);
            clustering.cluster();
            drawClusteredPoints(clustering, instances);
        } catch (Exception e) {
            drawNotClusteredPoints();
        }
    }

    private void drawClusteredPoints(Clustering clustering, Instances instances) {
        GraphicsContext context = canvas.getGraphicsContext2D();
        context.setStroke(Color.BLACK);
        context.setLineWidth(0.5);
        for (Instance instance : instances) {
            int clusterIndex = clustering.get(instance);
            context.setFill(COLORS[clusterIndex]);
            drawPoint(context, (int) instance.value(X_ATTRIBUTE), (int) instance.value(Y_ATTRIBUTE));
        }
    }

    private void drawNotClusteredPoints() {
        GraphicsContext context = canvas.getGraphicsContext2D();
        context.setFill(Color.BLACK);
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (bitmap[y][x] == 1) {
                    drawPoint(context, x, y);
                }
            }
        }
    }

    private void drawPoint(GraphicsContext context, int x, int y) {
        context.fillRect(x * SCALE, y * SCALE, SCALE, SCALE);
        context.strokeRect(x * SCALE, y * SCALE, SCALE, SCALE);
    }

    private Instances getInstances() {
        List<Instance> list = new ArrayList<>();
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (bitmap[y][x] == 1) {
                    Instance instance = new DenseInstance(ATTRIBUTES.size());
                    instance.setValue(ID_ATTRIBUTE, y + "-" + x);
                    instance.setValue(X_ATTRIBUTE, x);
                    instance.setValue(Y_ATTRIBUTE, y);
                    list.add(instance);
                }
            }
        }

        Instances instances = new Instances("2d_dataset", ATTRIBUTES, list.size());
        instances.addAll(list);
        return instances;
    }
}
