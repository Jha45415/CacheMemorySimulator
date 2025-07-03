package ui;

import Cache.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
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
    private TableView<CacheRow> l1Table;
    private TableView<CacheRow> l2Table;
    private TableView<CacheRow> l3Table;
    private Label l1InfoLabel, l2InfoLabel, l3InfoLabel;





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
        cache = new SetAssociativeCache(8,2); // Default cache, can be reset

        VBox root = new VBox(12);
        root.setPadding(new Insets(14));

        Label label = new Label("Enter Memory Addresses (one per line):");
        addressInput = new TextArea();
        addressInput.setPromptText("e.g.\n4\n8\n4\n0x10");

        blockCountField = new TextField("8");
        blockCountField.setPromptText("Total Blocks");

        wayCountField = new TextField("1");
        wayCountField.setPromptText("Ways (1=Direct, 2=2-way...)");

        cacheTypeSelector = new ComboBox<>();
        cacheTypeSelector.getItems().addAll(
                "Direct-Mapped (8 blocks)",
                "2-Way Set-Associative (8 blocks)",
                "4-Way Set-Associative (8 blocks)",
                "Multi-Level Cache",
                "Custom Configuration"
        );
        cacheTypeSelector.getSelectionModel().selectFirst();

        Button simulateBtn = new Button("Simulate");
        Button resetBtn = new Button("Reset");
        Button browseBtn = new Button("Browse File");
        Button saveBtn = new Button("Save Output");

        simulateBtn.setOnAction(e -> simulateAccesses());
        resetBtn.setOnAction(e -> reset());
        browseBtn.setOnAction(e -> loadFile());
        saveBtn.setOnAction(e -> saveOutputToFile());

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPromptText("Simulation output...");

        pieChart = new PieChart();
        pieChart.setTitle("Cache Hit vs Miss");
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);
        pieChart.setPrefHeight(200);
        pieChart.setPrefWidth(280);

        hitsLabel = new Label("Hits: 0");
        missesLabel = new Label("Misses: 0");
        ratioLabel = new Label("Hit Ratio: 0.00%");


        hitsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        missesLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        ratioLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label modeLabel = new Label("Select Cache Type:");

        l1InfoLabel = new Label("L1 Blocks: ");
        l2InfoLabel = new Label("L2 Blocks: ");
        l3InfoLabel = new Label("L3 Blocks: ");

        l1Table = createCacheTable();
        l2Table = createCacheTable();
        l3Table = createCacheTable();

        Label l1Label = new Label("L1 Cache");
        Label l2Label = new Label("L2 Cache");
        Label l3Label = new Label("L3 Cache");


        VBox statsVBox = new VBox(10, hitsLabel, missesLabel, ratioLabel);
        HBox statsBox = new HBox(32, pieChart, statsVBox);
        statsBox.setPadding(new Insets(12));


        root.getChildren().addAll(
                label, addressInput,
                blockCountField, wayCountField,
                cacheTypeSelector, modeLabel,
                simulateBtn, resetBtn, browseBtn, saveBtn,
                statsBox,
                outputArea,
                l1InfoLabel, l1Label, l1Table,
                l2InfoLabel, l2Label, l2Table,
                l3InfoLabel, l3Label, l3Table
        );

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        Scene scene = new Scene(scrollPane, 900, 900);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(900);
        stage.setMinHeight(900);
        stage.setTitle("Cache Memory Simulator");
        stage.show();

        reset();
    }



    private void simulateAccesses() {
        String selected = cacheTypeSelector.getValue();


        switch (selected) {
            case "Direct-Mapped (8 blocks)":
                cache = new DirectMappedCache(8);
                break;
            case "2-Way Set-Associative (8 blocks)":
                cache = new SetAssociativeCache(8, 2);
                break;
            case "4-Way Set-Associative (8 blocks)":
                cache = new SetAssociativeCache(8, 4);
                break;
            case "Multi-Level Cache":
                CacheInterface l1 = new DirectMappedCache(4);
                CacheInterface l2 = new SetAssociativeCache(8, 2);
                CacheInterface l3 = new SetAssociativeCache(16, 4);
                cache = new MultiLevelCache(l1, l2, l3);
                break;
            case "Custom Configuration": 
                try {
                    int totalBlocks = Integer.parseInt(blockCountField.getText());
                    int ways = Integer.parseInt(wayCountField.getText());

                    if (ways <= 0 || totalBlocks <= 0 || totalBlocks % ways != 0) {
                        outputArea.setText("Error: Invalid block/way count.\nWays must be > 0.\nTotal Blocks must be a multiple of Ways.");
                        return; // Stop the simulation
                    }

                    if (ways == 1) {
                        cache = new DirectMappedCache(totalBlocks);
                    } else {
                        cache = new SetAssociativeCache(totalBlocks, ways);
                    }
                } catch (NumberFormatException e) {
                    outputArea.setText("Error: Please enter valid numbers for Total Blocks and Ways.");
                    return; // Stop the simulation
                }
                break;
        }
        outputArea.clear();

        String[] lines = addressInput.getText().split("\\s+|\\n");
        StringBuilder log = new StringBuilder();

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            try {
                int address = line.startsWith("0x") ? Integer.parseInt(line.substring(2), 16) : Integer.parseInt(line);
                boolean hit = cache.access(address);
                int index = cache.getLastAccessedIndex();
                lastAccessedIndex = index;
                lastAccessWasHit = hit;

                String result = hit ? "HIT" : "MISS";
                if (cache instanceof MultiLevelCache) {
                    result = ((MultiLevelCache) cache).getLastHitLevel();
                }

                log.append("Address ").append(line).append(" => ").append(result).append("\n");

                if (cache instanceof MultiLevelCache) {
                    l1Table.setItems(getRows(((MultiLevelCache) cache).getL1Blocks()));
                    l2Table.setItems(getRows(((MultiLevelCache) cache).getL2Blocks()));
                    l3Table.setItems(getRows(((MultiLevelCache) cache).getL3Blocks()));
                } else {
                    l1Table.setItems(getRows(cache.getBlocks()));
                    l2Table.setItems(FXCollections.observableArrayList());
                    l3Table.setItems(FXCollections.observableArrayList());
                }

            } catch (NumberFormatException ex) {
                log.append("Invalid input: ").append(line).append("\n");
            }
        }

        outputArea.setText(log.toString());
        updateStats();
        updatePieChart();
    }



    private void reset() {
        String selected = cacheTypeSelector.getValue();
        switch (selected) {
            case "Direct-Mapped (8 blocks)":
                cache = new DirectMappedCache(8);
                break;
            case "2-Way Set-Associative (8 blocks)":
                cache = new SetAssociativeCache(8, 2);
                break;
            case "4-Way Set-Associative (8 blocks)":
                cache = new SetAssociativeCache(8, 4);
                break;
            case "Multi-Level Cache":
                CacheInterface l1 = new DirectMappedCache(8);
                CacheInterface l2 = new SetAssociativeCache(8, 2);
                CacheInterface l3 = new SetAssociativeCache(16, 4);
                cache = new MultiLevelCache(l1, l2, l3);
                break;
        }
        cache.reset();
        addressInput.clear();
        outputArea.clear();

        if (cache instanceof MultiLevelCache) {
            l1InfoLabel.setText("L1 Blocks: " + ((MultiLevelCache) cache).getL1BlockCount());
            l2InfoLabel.setText("L2 Blocks: " + ((MultiLevelCache) cache).getL2BlockCount());
            l3InfoLabel.setText("L3 Blocks: " + ((MultiLevelCache) cache).getL3BlockCount());
            l1Table.setItems(FXCollections.observableArrayList());
            l2Table.setItems(FXCollections.observableArrayList());
            l3Table.setItems(FXCollections.observableArrayList());
        } else {
            l1InfoLabel.setText("L1 Blocks: " + cache.getBlockCount());
            l2InfoLabel.setText("L2 Blocks: 0");
            l3InfoLabel.setText("L3 Blocks: 0");
            l1Table.setItems(FXCollections.observableArrayList());
            l2Table.setItems(FXCollections.observableArrayList());
            l3Table.setItems(FXCollections.observableArrayList());
        }

        hitsLabel.setText("Hits: 0");
        missesLabel.setText("Misses: 0");
        ratioLabel.setText("Hit Ratio: 0.00%");
        updatePieChart();
        updateStats();
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
    private ObservableList<CacheRow> getRows(CacheBlock[] blocks) {
        ObservableList<CacheRow> rows = FXCollections.observableArrayList();
        for (int i = 0; i < blocks.length; i++) {
            CacheBlock block = blocks[i];
            rows.add(new CacheRow(i, block.getTag(), block.isValid()));
        }
        return rows;
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
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Hits", hits),
                new PieChart.Data("Misses", misses)
        );
        pieChart.setData(pieChartData);


        pieChart.applyCss();
        for (PieChart.Data data : pieChartData) {
            if (data.getName().equals("Hits")) {
                data.getNode().setStyle("-fx-pie-color: green;");
            } else if (data.getName().equals("Misses")) {
                data.getNode().setStyle("-fx-pie-color: red;");
            }
        }
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
    private TableView<CacheRow> createCacheTable() {
        TableView<CacheRow> table = new TableView<>();
        TableColumn<CacheRow, Integer> indexCol = new TableColumn<>("Index");
        indexCol.setCellValueFactory(new PropertyValueFactory<>("index"));
        TableColumn<CacheRow, Integer> tagCol = new TableColumn<>("Tag");
        tagCol.setCellValueFactory(new PropertyValueFactory<>("tag"));
        TableColumn<CacheRow, Boolean> validCol = new TableColumn<>("Valid");
        validCol.setCellValueFactory(new PropertyValueFactory<>("valid"));
        table.getColumns().addAll(indexCol, tagCol, validCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }





}
