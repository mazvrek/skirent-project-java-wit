package pl.skirent.ui;

import pl.skirent.i18n.I18n;
import pl.skirent.model.*;
import pl.skirent.service.ReportService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class ReportPanel extends JPanel {
    private final ReportService reportService;
    private JTabbedPane innerTabs;
    private DefaultTableModel availableModel, rentedModel, overdueModel;
    private JButton btnRefresh;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReportPanel(ReportService reportService) {
        this.reportService = reportService;
        setLayout(new BorderLayout());
        initComponents();
        refresh();
    }

    private void initComponents() {
        I18n i18n = I18n.getInstance();

        btnRefresh = new JButton(i18n.get("button.refresh"));
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(btnRefresh);
        add(topPanel, BorderLayout.NORTH);

        innerTabs = new JTabbedPane();

        availableModel = new DefaultTableModel(
            new Object[]{"ID", i18n.get("label.brand"), i18n.get("label.model"),
                i18n.get("label.bindings"), i18n.get("label.length")}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        innerTabs.addTab(i18n.get("report.available"), new JScrollPane(new JTable(availableModel)));

        rentedModel = new DefaultTableModel(
            new Object[]{"ID", i18n.get("label.brand"), i18n.get("label.model")}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        innerTabs.addTab(i18n.get("report.rented"), new JScrollPane(new JTable(rentedModel)));

        overdueModel = new DefaultTableModel(
            new Object[]{"ID", i18n.get("label.customer"), i18n.get("label.from"),
                i18n.get("label.to"), i18n.get("label.status")}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        innerTabs.addTab(i18n.get("report.overdue"), new JScrollPane(new JTable(overdueModel)));

        add(innerTabs, BorderLayout.CENTER);
        btnRefresh.addActionListener(e -> refresh());
    }

    public void refresh() {
        availableModel.setRowCount(0);
        for (Ski ski : reportService.getAvailableSkis()) {
            availableModel.addRow(new Object[]{ski.getId(), ski.getBrand(), ski.getModel(),
                ski.getBindings(), ski.getLength()});
        }

        rentedModel.setRowCount(0);
        for (Ski ski : reportService.getCurrentlyRentedSkis()) {
            rentedModel.addRow(new Object[]{ski.getId(), ski.getBrand(), ski.getModel()});
        }

        overdueModel.setRowCount(0);
        for (Rental r : reportService.getOverdueRentals()) {
            overdueModel.addRow(new Object[]{r.getId(), r.getCustomerId(),
                r.getFrom() != null ? r.getFrom().format(FMT) : "",
                r.getTo() != null ? r.getTo().format(FMT) : "",
                r.getStatus() != null ? r.getStatus() : ""});
        }
    }

    public void refreshLabels() {
        I18n i18n = I18n.getInstance();
        btnRefresh.setText(i18n.get("button.refresh"));
        innerTabs.setTitleAt(0, i18n.get("report.available"));
        innerTabs.setTitleAt(1, i18n.get("report.rented"));
        innerTabs.setTitleAt(2, i18n.get("report.overdue"));
        availableModel.setColumnIdentifiers(new Object[]{"ID", i18n.get("label.brand"),
            i18n.get("label.model"), i18n.get("label.bindings"), i18n.get("label.length")});
        rentedModel.setColumnIdentifiers(new Object[]{"ID", i18n.get("label.brand"), i18n.get("label.model")});
        overdueModel.setColumnIdentifiers(new Object[]{"ID", i18n.get("label.customer"),
            i18n.get("label.from"), i18n.get("label.to"), i18n.get("label.status")});
        refresh();
    }
}
