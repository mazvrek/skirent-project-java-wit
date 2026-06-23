package pl.skirent.ui;

import pl.skirent.i18n.I18n;
import pl.skirent.model.*;
import pl.skirent.repository.*;
import pl.skirent.service.RentalService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RentalPanel extends JPanel {
    private final RentalRepository rentalRepo;
    private final CustomerRepository customerRepo;
    private final SkiRepository skiRepo;
    private final RentalService rentalService;
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton btnNew, btnReturn, btnDelete;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public RentalPanel(RentalRepository rentalRepo, CustomerRepository customerRepo,
                       SkiRepository skiRepo, RentalService rentalService) {
        this.rentalRepo = rentalRepo;
        this.customerRepo = customerRepo;
        this.skiRepo = skiRepo;
        this.rentalService = rentalService;
        setLayout(new BorderLayout());
        initComponents();
        loadData();
    }

    private void initComponents() {
        I18n i18n = I18n.getInstance();
        tableModel = new DefaultTableModel(
            new Object[]{"ID", i18n.get("label.customer"), i18n.get("label.skis"),
                i18n.get("label.from"), i18n.get("label.to"),
                i18n.get("label.status"), i18n.get("label.remarks")}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnNew = new JButton(i18n.get("button.add"));
        btnReturn = new JButton(i18n.get("button.return"));
        btnDelete = new JButton(i18n.get("button.delete"));
        btnPanel.add(btnNew); btnPanel.add(btnReturn); btnPanel.add(btnDelete);

        add(btnPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnNew.addActionListener(e -> showNewRentalDialog());
        btnReturn.addActionListener(e -> showReturnDialog());
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            int confirm = JOptionPane.showConfirmDialog(this,
                I18n.getInstance().get("msg.confirm.delete"),
                I18n.getInstance().get("msg.confirm"),
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int id = (int) tableModel.getValueAt(row, 0);
                rentalRepo.delete(id);
                loadData();
            }
        });
    }

    private void showNewRentalDialog() {
        I18n i18n = I18n.getInstance();
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            i18n.get("button.add"), true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<Customer> customers = customerRepo.findAll();
        JComboBox<Customer> customerCombo = new JComboBox<>(customers.toArray(new Customer[0]));

        List<Ski> allSkis = skiRepo.findAll();
        JList<Ski> skiList = new JList<>(allSkis.toArray(new Ski[0]));
        skiList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane skiScroll = new JScrollPane(skiList);
        skiScroll.setPreferredSize(new Dimension(200, 100));

        JTextField fromField = new JTextField("", 20);
        JTextField toField = new JTextField("", 20);

        int row = 0;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel(i18n.get("label.customer")+":"), gbc);
        gbc.gridx=1; dialog.add(customerCombo, gbc); row++;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel(i18n.get("label.skis")+":"), gbc);
        gbc.gridx=1; gbc.fill=GridBagConstraints.BOTH; dialog.add(skiScroll, gbc);
        gbc.fill=GridBagConstraints.HORIZONTAL; row++;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel(i18n.get("label.from")+":"), gbc);
        gbc.gridx=1; dialog.add(fromField, gbc); row++;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel(i18n.get("label.to")+":"), gbc);
        gbc.gridx=1; dialog.add(toField, gbc); row++;

        JPanel buttons = new JPanel();
        JButton save = new JButton(i18n.get("button.save"));
        JButton cancel = new JButton(i18n.get("button.cancel"));
        buttons.add(save); buttons.add(cancel);
        gbc.gridx=0; gbc.gridy=row; gbc.gridwidth=2; dialog.add(buttons, gbc);

        save.addActionListener(e -> {
            try {
                LocalDateTime from = LocalDateTime.parse(fromField.getText().trim(), FMT);
                LocalDateTime to = LocalDateTime.parse(toField.getText().trim(), FMT);
                Customer customer = (Customer) customerCombo.getSelectedItem();
                List<Ski> selected = skiList.getSelectedValuesList();
                if (selected.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, i18n.get("msg.error"), i18n.get("msg.error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int customerId = customer != null ? customer.getId() : 0;
                List<Integer> skiIds = selected.stream().map(Ski::getId).collect(Collectors.toList());
                rentalService.createRental(customerId, skiIds, from, to);
                loadData();
                dialog.dispose();
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, i18n.get("msg.error.date"), i18n.get("msg.error"), JOptionPane.ERROR_MESSAGE);
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(dialog, i18n.get("msg.error.conflict"), i18n.get("msg.error"), JOptionPane.ERROR_MESSAGE);
            }
        });
        cancel.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showReturnDialog() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        I18n i18n = I18n.getInstance();

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            i18n.get("button.return"), true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField remarksField = new JTextField("", 20);
        gbc.gridx=0; gbc.gridy=0; dialog.add(new JLabel(i18n.get("label.remarks")+":"), gbc);
        gbc.gridx=1; dialog.add(remarksField, gbc);

        JPanel buttons = new JPanel();
        JButton ok = new JButton("OK");
        JButton cancel = new JButton(i18n.get("button.cancel"));
        buttons.add(ok); buttons.add(cancel);
        gbc.gridx=0; gbc.gridy=1; gbc.gridwidth=2; dialog.add(buttons, gbc);

        ok.addActionListener(e -> {
            rentalService.returnRental(id, remarksField.getText().trim());
            loadData();
            dialog.dispose();
        });
        cancel.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void loadData() {
        tableModel.setRowCount(0);
        for (Rental r : rentalRepo.findAll()) {
            Customer c = customerRepo.findById(r.getCustomerId());
            String customerStr = c != null ? c.toString() : String.valueOf(r.getCustomerId());
            StringBuilder skiStr = new StringBuilder();
            if (r.getSkiIds() != null) {
                for (int skiId : r.getSkiIds()) {
                    Ski ski = skiRepo.findById(skiId);
                    if (skiStr.length() > 0) skiStr.append(", ");
                    skiStr.append(ski != null ? ski.toString() : String.valueOf(skiId));
                }
            }
            tableModel.addRow(new Object[]{
                r.getId(), customerStr, skiStr.toString(),
                r.getFrom() != null ? r.getFrom().format(FMT) : "",
                r.getTo() != null ? r.getTo().format(FMT) : "",
                r.getStatus() != null ? r.getStatus() : "",
                r.getRemarks() != null ? r.getRemarks() : ""
            });
        }
    }

    public void refreshLabels() {
        I18n i18n = I18n.getInstance();
        btnNew.setText(i18n.get("button.add"));
        btnReturn.setText(i18n.get("button.return"));
        btnDelete.setText(i18n.get("button.delete"));
        tableModel.setColumnIdentifiers(new Object[]{"ID", i18n.get("label.customer"),
            i18n.get("label.skis"), i18n.get("label.from"), i18n.get("label.to"),
            i18n.get("label.status"), i18n.get("label.remarks")});
        loadData();
    }
}
