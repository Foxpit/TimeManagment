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
import org.jfree.chart.plot.PlotOrientation; // Import for PlotOrientation
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;


public class TimeTracker extends JFrame {
    private JComboBox<String> taskNameDropdown;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private DefaultPieDataset pieDataset;
    private DefaultCategoryDataset barDataset;
    private LocalTime startTime;
    private Map<String, Long> taskDurations = new HashMap<>();
    private Random random = new Random(); // For generating random colors
    private JTextArea historyArea; // For displaying task history

    private JFreeChart barChart;

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

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Options");
        JMenuItem manualEntryItem = new JMenuItem("Manual Entry");

        manualEntryItem.addActionListener(this::handleManualEntry);

        menu.add(manualEntryItem);
        menuBar.add(menu);
        return menuBar;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        taskNameDropdown = new JComboBox<>();
        taskNameDropdown.setEditable(true);

        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");

        startButton.addActionListener(this::handleStartAction);
        stopButton.addActionListener(this::handleStopAction);

        panel.add(new JLabel("Task Name:"));
        panel.add(taskNameDropdown);
        panel.add(startButton);
        panel.add(stopButton);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        historyArea = new JTextArea(5, 40);
        historyArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(historyArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createTitledBorder("Task History"));
        return panel;
    }

    private JSplitPane createChartPanel() {
        pieDataset = new DefaultPieDataset();
        JFreeChart pieChart = ChartFactory.createPieChart("Time Distribution", pieDataset, true, true, false);
        ChartPanel pieChartPanel = new ChartPanel(pieChart);

        barDataset = new DefaultCategoryDataset();
        barChart = ChartFactory.createBarChart("Time Spent", "Task", "Time (Seconds)", barDataset, PlotOrientation.VERTICAL, true, true, false);
        ChartPanel barChartPanel = new ChartPanel(barChart);

        // Customize the renderer for color variation
        BarRenderer renderer = (BarRenderer) barChart.getCategoryPlot().getRenderer();
        renderer.setSeriesPaint(0, new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pieChartPanel, barChartPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setContinuousLayout(true);
        return splitPane;
    }



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
        updateTaskDuration();
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

    private void updateTaskDuration() {
        String taskName = (String) taskNameDropdown.getSelectedItem();
        if (taskName == null || taskName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a task name.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        long durationSeconds;
        if (startTime != null) {
            durationSeconds = Duration.between(startTime, LocalTime.now()).getSeconds();
            startTime = null; // Reset start time after stopping the timer
        } else {
            try {
                LocalTime parsedStartTime = LocalTime.parse(startTimeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime parsedEndTime = LocalTime.parse(endTimeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                if (parsedEndTime.isBefore(parsedStartTime)) {
                    JOptionPane.showMessageDialog(this, "End time cannot be before start time.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                durationSeconds = Duration.between(parsedStartTime, parsedEndTime).getSeconds();
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid time format. Please enter time as HH:MM", "Format Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        taskDurations.merge(taskName, durationSeconds, Long::sum);
        pieDataset.setValue(taskName, taskDurations.get(taskName));
        barDataset.setValue(taskDurations.get(taskName), "Time Spent", taskName);

        // Dropdown update logic - prevent duplicate entries
        if (!arrayContains(taskNameDropdown, taskName)) {
            taskNameDropdown.addItem(taskName);
        }

        // Check if the task already has a color assigned
        int taskIndex = -1;
        for (int i = 0; i < barDataset.getRowCount(); i++) {
            if (taskName.equals(barDataset.getColumnKey(i))) {
                taskIndex = i;
                break;
            }
        }

        // Assign a constant color to the task if it doesn't have a color assigned
        Color taskColor = new Color(0, 128, 0); // Change the RGB values to the desired color
        if (taskIndex == -1) {
            taskIndex = barDataset.getRowCount(); // Assign the next available index
            barDataset.addValue(taskDurations.get(taskName), "Time Spent", taskName);
            BarRenderer renderer = (BarRenderer) barChart.getCategoryPlot().getRenderer();
            renderer.setSeriesPaint(taskIndex, taskColor);
        }

        // Update the value and color of the task in the bar chart
        barDataset.setValue(taskDurations.get(taskName), "Time Spent", taskName);
        BarRenderer renderer = (BarRenderer) barChart.getCategoryPlot().getRenderer();
        renderer.setSeriesPaint(taskIndex, taskColor);

        // Update history area
        historyArea.append("Task: " + taskName + ", Duration: " + formatDuration(durationSeconds) + "\n");

        // Update color for each task
        updateChartColors();
    }

    private boolean arrayContains(JComboBox<String> comboBox, String item) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i).equalsIgnoreCase(item)) {
                return true;
            }
        }
        return false;
    }

    private String formatDuration(long durationSeconds) {
        // Converts duration to hours, minutes, and seconds format
        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        long seconds = durationSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void updateChartColors() {
        BarRenderer renderer = (BarRenderer) barChart.getCategoryPlot().getRenderer();
        for (int i = 0; i < barDataset.getRowCount(); i++) {
            Comparable<?> taskName = barDataset.getColumnKey(i);
            if (renderer.getSeriesPaint(i) == null) {
                // Assign a new color to the task if it doesn't have a color assigned
                renderer.setSeriesPaint(i, new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            } else {
                // Keep the previously defined color for the task
                renderer.setSeriesPaint(i, renderer.getSeriesPaint(i));
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TimeTracker timeTracker = new TimeTracker();
            timeTracker.setVisible(true);
        });
    }

}
