package ui;

import Cache.CacheBlock;
import Cache.CacheInterface;
import Cache.DirectMappedCache;
import Cache.SetAssociativeCache;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;



public class MainUI extends Application {
    private ComboBox<String> cacheTypeSelector;

    private int lastAccessedIndex = -1;
    private boolean lastAccessWasHit = false;

    private Label hitsLabel;
    private Label missesLabel;
    private Label ratioLabel;

    private CacheInterface cache;
    private TextArea addressInput;
    private TextArea outputArea;
    private TableView<CacheRow> table;
    private PieChart pieChart;
    private TextField blockCountField;
    private TextField wayCountField;



    public static class CacheRow {
        private final int index;
        private final int tag;
        private final boolean valid;

        public CacheRow(int index, int tag, boolean valid) {
            this.index = index;
            this.tag = tag;
            this.valid = valid;
        }

        public int getIndex() { return index; }
        public int getTag() { return tag; }
        public boolean isValid() { return valid; }
    }

    @Override
    public void start(Stage stage) {
        cache = new SetAssociativeCache(8,2); // 8-block cache

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label label = new Label("Enter Memory Addresses (one per line):");
        addressInput = new TextArea();
        addressInput.setPromptText("e.g.\n4\n8\n4\n0x10");
        Label modeLabel = new Label("Select Cache Type:");
        cacheTypeSelector = new ComboBox<>();
        cacheTypeSelector.getItems().addAll(
                "Direct-Mapped (8 blocks)",
                "2-Way Set-Associative (8 blocks)",
                "4-Way Set-Associative (8 blocks)"
        );
        cacheTypeSelector.getSelectionModel().selectFirst();  // default option


        Button simulateBtn = new Button("Simulate");
        Button resetBtn = new Button("Reset");

        simulateBtn.setOnAction(e -> simulateAccesses());
        resetBtn.setOnAction(e -> reset());
        Button browseBtn = new Button("Browse File");
        browseBtn.setOnAction(e -> loadFile());
        Button saveBtn = new Button("Save Output");
        saveBtn.setOnAction(e -> saveOutputToFile());



        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPromptText("Simulation output...");
        pieChart = new PieChart();
        pieChart.setTitle("Cache Hit vs Miss");
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);
        pieChart.setPrefHeight(300);
        Label configLabel = new Label("Custom Cache Configuration:");

        blockCountField = new TextField("8");
        blockCountField.setPromptText("Total Blocks");

        wayCountField = new TextField("1");
        wayCountField.setPromptText("Ways (1=Direct, 2=2-way...)");



        // Setup cache table
        table = new TableView<>();
        TableColumn<CacheRow, Integer> indexCol = new TableColumn<>("Index");
        indexCol.setCellValueFactory(new PropertyValueFactory<>("index"));

        TableColumn<CacheRow, Integer> tagCol = new TableColumn<>("Tag");
        tagCol.setCellValueFactory(new PropertyValueFactory<>("tag"));

        TableColumn<CacheRow, Boolean> validCol = new TableColumn<>("Valid");
        validCol.setCellValueFactory(new PropertyValueFactory<>("valid"));

        table.getColumns().addAll(indexCol, tagCol, validCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        hitsLabel = new Label("Hits: 0");
        missesLabel = new Label("Misses: 0");
        ratioLabel = new Label("Hit Ratio: 0.00%");

        root.getChildren().addAll(label, addressInput, blockCountField,
                wayCountField, simulateBtn,pieChart,   saveBtn, resetBtn,browseBtn, outputArea, table, hitsLabel, missesLabel, ratioLabel,modeLabel,cacheTypeSelector);

        stage.setTitle("Cache Memory Simulator");
        stage.setScene(new Scene(root, 500, 600));
        stage.show();

        updateTable();
    }

    private void simulateAccesses() {
        String selected = cacheTypeSelector.getValue();



        String[] lines = addressInput.getText().split("\\s+|\\n");
        StringBuilder log = new StringBuilder();

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            try {
                int address = line.startsWith("0x") ? Integer.parseInt(line.substring(2), 16)
                        : Integer.parseInt(line);

                boolean hit = cache.access(address);
                int index = cache.getLastAccessedIndex();
                lastAccessedIndex = index;
                lastAccessWasHit = hit;

                log.append("Address ").append(line).append(" => ").append(hit ? "HIT" : "MISS").append("\n");
            } catch (NumberFormatException ex) {
                log.append("Invalid input: ").append(line).append("\n");
            }
        }

        outputArea.setText(log.toString());
        updateTable();
        updateStats();
        updatePieChart();


    }


        private void reset() {
            try {
                int blocks = Integer.parseInt(blockCountField.getText());
                int ways = Integer.parseInt(wayCountField.getText());

                if (ways <= 1) {
                    cache = new DirectMappedCache(blocks); // 1-way = direct-mapped
                } else {
                    cache = new SetAssociativeCache(blocks, ways);
                }

                addressInput.clear();
                outputArea.clear();
                updateStats();
                updateTable();
                updatePieChart();

            } catch (NumberFormatException ex) {
                outputArea.setText("Invalid input for cache size or ways. Please enter valid numbers.");
            }
        }





    private void loadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Memory Address File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                addressInput.setText(content.toString());
            } catch (IOException ex) {
                outputArea.setText("Error reading file: " + ex.getMessage());
            }
        }
    }


    private void updateTable() {

            ObservableList<CacheRow> rows = FXCollections.observableArrayList();
            CacheBlock[] blocks = cache.getBlocks();

            for (int i = 0; i < blocks.length; i++) {
                CacheBlock block = blocks[i];
                rows.add(new CacheRow(i, block.getTag(), block.isValid()));
            }

            table.setItems(rows);

            // Add row coloring logic
            table.setRowFactory(tv -> new TableRow<>() {
                @Override
                protected void updateItem(CacheRow row, boolean empty) {
                    super.updateItem(row, empty);
                    if (row == null || empty) {
                        setStyle("");
                    } else if (row.getIndex() == lastAccessedIndex) {
                        if (lastAccessWasHit) {
                            setStyle("-fx-background-color: lightgreen;");
                        } else {
                            setStyle("-fx-background-color: lightcoral;");
                        }
                    } else {
                        setStyle(""); // reset
                    }
                }
            });


    }
    private void updateStats() {
    int hits = cache.getHitCount();
        int misses = cache.getMissCount();
        int total = hits + misses;
        double ratio = total == 0 ? 0 : (hits * 100.0) / total;

        hitsLabel.setText("Hits: " + hits);
        missesLabel.setText("Misses: " + misses);
        ratioLabel.setText(String.format("Hit Ratio: %.2f%%", ratio));
    }
    private void updatePieChart() {
        int hits = cache.getHitCount();
        int misses = cache.getMissCount();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Hits", hits),
                new PieChart.Data("Misses", misses)
        );
        pieChart.setData(pieData);
    }
    private void saveOutputToFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Simulation Output");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Cache Simulation Result\n\n");
                writer.write(outputArea.getText());
                writer.write("\n\nHits: " + cache.getHitCount());
                writer.write("\nMisses: " + cache.getMissCount());
                int total = cache.getHitCount() + cache.getMissCount();
                double ratio = total == 0 ? 0.0 : (double) cache.getHitCount() / total;
                writer.write("\nHit Ratio: " + String.format("%.2f", ratio));
                outputArea.appendText("\nOutput saved to: " + file.getAbsolutePath());
            } catch (IOException e) {
                outputArea.appendText("\nError saving file: " + e.getMessage());
            }
        }
    }




}
