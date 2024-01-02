package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation; // Import for PlotOrientation
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;


public class TimeTracker extends JFrame {
    private JComboBox<String> taskNameDropdown;
    private JComboBox<String> taskTypeDropdown;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private DefaultPieDataset pieDataset;
    private DefaultCategoryDataset barDataset;
    private LocalTime startTime;
    private final Map<String, Long> taskDurations = new HashMap<>();
    private JTextArea historyArea;
    private final Map<String, Color> taskColors = new HashMap<>();

    private JFreeChart pieChart;
    private JFreeChart barChart;

    private final Random random = new Random();

    public TimeTracker() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Time Tracker");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setJMenuBar(createMenuBar());
        add(createInputPanel(), BorderLayout.NORTH);
        add(createChartPanel(), BorderLayout.CENTER);
        add(createHistoryPanel(), BorderLayout.SOUTH); // Add a history panel
    }

//Menu Bar

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Options");
        JMenuItem manualEntryItem = new JMenuItem("Manual Entry");
        JMenuItem resetItem = new JMenuItem("Reset");

        manualEntryItem.addActionListener(this::handleManualEntry);
        resetItem.addActionListener(this::handleReset);

        menu.add(resetItem);
        menu.add(manualEntryItem);
        menuBar.add(menu);
        return menuBar;
    }



//Input Panel
    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        taskNameDropdown = new JComboBox<>();
        taskNameDropdown.setEditable(true);

        taskTypeDropdown = new JComboBox<>(); // Make sure this is initialized
        taskTypeDropdown.setEditable(true); // Assuming you want this to be editable

        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");

        startButton.addActionListener(this::handleStartAction);
        stopButton.addActionListener(this::handleStopAction);

        panel.add(new JLabel("Task Name:"));
        panel.add(taskNameDropdown);

        panel.add(new JLabel("Task Type:"));
        panel.add(taskTypeDropdown);

        panel.add(startButton);
        panel.add(stopButton);

        return panel;
    }


    //History Panel
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        historyArea = new JTextArea(6, 40);
        historyArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(historyArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createTitledBorder("Task History"));
        return panel;
    }


//Chart Panel
private JSplitPane createChartPanel() {
    pieDataset = new DefaultPieDataset();
    this.pieChart = ChartFactory.createPieChart("Time Distribution", pieDataset, true, true, false); // Initialize class member
    ChartPanel pieChartPanel = new ChartPanel(pieChart);

    barDataset = new DefaultCategoryDataset();
    this.barChart = ChartFactory.createBarChart("Time Spent", "Task", "Time (Seconds)", barDataset, PlotOrientation.VERTICAL, true, true, false); // Initialize class member
    ChartPanel barChartPanel = new ChartPanel(barChart);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pieChartPanel, barChartPanel);
    splitPane.setResizeWeight(0.5);
    splitPane.setContinuousLayout(true);
    return splitPane;
}

// Update Chart Colors
private void updateChartColors() {
//    BarRenderer barRenderer = (BarRenderer) barChart.getCategoryPlot().getRenderer();
//    PiePlot piePlot = (PiePlot) pieChart.getPlot();
//
//    taskDurations.forEach((taskName, duration) -> {
//        // Check if a color already exists for this task, if not create a new random color
//        Color color = taskColors.computeIfAbsent(taskName, k ->
//                new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
//
//        // Update colors for pie chart
//        piePlot.setSectionPaint(taskName, color);
//
//        // Ensure the task is in the bar dataset before setting the color
//        int rowIndex = barDataset.getRowIndex(taskName);
//        if (rowIndex >= 0) {
//            barRenderer.setSeriesPaint(rowIndex, color);
//        }
//    });

    //update chart colors
    BarRenderer barRenderer = (BarRenderer) barChart.getCategoryPlot().getRenderer();
    PiePlot piePlot = (PiePlot) pieChart.getPlot();

    taskDurations.forEach((taskName, duration) -> {
        // Check if a color already exists for this task, if not create a new random color
        Color color = taskColors.computeIfAbsent(taskName, k ->
                new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));

        // Update colors for pie chart
        piePlot.setSectionPaint(taskName, color);

        // Ensure the task is in the bar dataset before setting the color
        int rowIndex = barDataset.getRowIndex(taskName);
        if (rowIndex >= 0) {
            barRenderer.setSeriesPaint(rowIndex, color);
        }
    });

}





//Action Listeners
    private void handleStartAction(ActionEvent e) {
        String taskName = (String) taskNameDropdown.getSelectedItem();
        if (taskName == null || taskName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a task name before starting the timer.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        startTime = LocalTime.now();
    }

    private void handleStopAction(ActionEvent e) {
        if (startTime == null) {
            JOptionPane.showMessageDialog(this, "Please start the timer before stopping.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            updateTaskDuration();
        } finally {
            startTime = null; // Ensure start time is reset even if exceptions occur
        }
    }

    private void handleManualEntry(ActionEvent e) {
        JPanel manualEntryPanel = new JPanel(new GridLayout(0, 2)); // Grid layout for better alignment
        taskNameDropdown = new JComboBox<>(); // Task name input
        taskNameDropdown.setEditable(true);
        startTimeField = new JTextField(5);
        endTimeField = new JTextField(5);

        manualEntryPanel.add(new JLabel("Task Name:"));
        manualEntryPanel.add(taskNameDropdown);
        manualEntryPanel.add(new JLabel("Start Time (HH:MM):"));
        manualEntryPanel.add(startTimeField);
        manualEntryPanel.add(new JLabel("End Time (HH:MM):"));
        manualEntryPanel.add(endTimeField);

        int result = JOptionPane.showConfirmDialog(this, manualEntryPanel, "Enter Time Details", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            updateTaskDuration();
        }
    }

    private void handleReset(ActionEvent e) {
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to reset?", "Reset", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            taskDurations.clear();
            pieDataset.clear();
            barDataset.clear();
            historyArea.setText("");
        }
    }

//Update Task Duration
    private void updateTaskDuration() {
        String taskName = (String) taskNameDropdown.getSelectedItem();
        if (taskName == null || taskName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a task name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        long durationSeconds = calculateDuration();
        if (durationSeconds < 0) return;

        updateDatasets(taskName, durationSeconds);
        updateChartColors();
        historyArea.append("Task: " + taskName + ", Duration: " + formatDuration(durationSeconds) + "\n");
    }

    // Calculate duration in seconds
    private long calculateDuration() {
        long durationSeconds = -1;
        if (startTime != null) {
            durationSeconds = Duration.between(startTime, LocalTime.now()).getSeconds();
            startTime = null;
        } else {
            try {
                LocalTime parsedStartTime = LocalTime.parse(startTimeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime parsedEndTime = LocalTime.parse(endTimeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                if (parsedEndTime.isBefore(parsedStartTime)) {
                    JOptionPane.showMessageDialog(this, "End time cannot be before start time.", "Error", JOptionPane.ERROR_MESSAGE);
                    startTime = null;
                    return -1;
                }
                durationSeconds = Duration.between(parsedStartTime, parsedEndTime).getSeconds();
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid time format. Please enter time as HH:MM", "Format Error", JOptionPane.ERROR_MESSAGE);
                return -1;
            }
        }
        return durationSeconds;
    }

//Update Datasets
    private void updateDatasets(String taskName, long durationSeconds) {
        taskDurations.merge(taskName, durationSeconds, Long::sum);
        pieDataset.setValue(taskName, taskDurations.get(taskName));
        barDataset.setValue(taskDurations.get(taskName), "Time Spent", taskName);
    }

// Check if array contains item

    private boolean arrayContains(JComboBox<String> comboBox, String item) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).equalsIgnoreCase(item)) {
                return true;
            }
        }
        return false;
    }

// Format duration in HH:MM:SS format
    private String formatDuration(long durationSeconds) {
        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        long seconds = durationSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TimeTracker timeTracker = new TimeTracker();
            timeTracker.setVisible(true);
        });
    }

}
