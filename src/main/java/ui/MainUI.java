package ui;

import Cache.CacheBlock;
import Cache.DirectMappedCache;
import Cache.CacheInterface;
import Cache.SetAssociativeCache;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


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

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPromptText("Simulation output...");

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

        root.getChildren().addAll(label, addressInput, simulateBtn, resetBtn, outputArea, table, hitsLabel, missesLabel, ratioLabel,modeLabel,cacheTypeSelector);

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
        }

        cache.reset();
        addressInput.clear();
        outputArea.clear();
        updateTable();
        updateStats();

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

}
