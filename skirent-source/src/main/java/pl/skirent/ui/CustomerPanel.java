package pl.skirent.ui;

import pl.skirent.i18n.I18n;
import pl.skirent.model.Customer;
import pl.skirent.repository.CustomerRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CustomerPanel extends JPanel {
    private final CustomerRepository repo;
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton btnAdd, btnEdit, btnDelete;

    public CustomerPanel(CustomerRepository repo) {
        this.repo = repo;
        setLayout(new BorderLayout());
        initComponents();
        loadData();
    }

    private void initComponents() {
        I18n i18n = I18n.getInstance();
        tableModel = new DefaultTableModel(
            new Object[]{"ID", i18n.get("label.firstName"), i18n.get("label.lastName"),
                i18n.get("label.documentNumber"), i18n.get("label.description")}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAdd = new JButton(i18n.get("button.add"));
        btnEdit = new JButton(i18n.get("button.edit"));
        btnDelete = new JButton(i18n.get("button.delete"));
        btnPanel.add(btnAdd); btnPanel.add(btnEdit); btnPanel.add(btnDelete);

        add(btnPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnAdd.addActionListener(e -> showDialog(null));
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            int id = (int) tableModel.getValueAt(row, 0);
            Customer found = repo.findById(id);
            if (found != null) showDialog(found);
        });
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) return;
            int confirm = JOptionPane.showConfirmDialog(this,
                I18n.getInstance().get("msg.confirm.delete"),
                I18n.getInstance().get("msg.confirm"),
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                int id = (int) tableModel.getValueAt(row, 0);
                repo.delete(id);
                loadData();
            }
        });
    }

    private void showDialog(Customer existing) {
        I18n i18n = I18n.getInstance();
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? i18n.get("button.add") : i18n.get("button.edit"), true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField firstField = new JTextField(existing != null ? existing.getFirstName() : "", 20);
        JTextField lastField = new JTextField(existing != null ? existing.getLastName() : "", 20);
        JTextField docField = new JTextField(existing != null ? existing.getDocumentNumber() : "", 20);
        JTextField descField = new JTextField(existing != null ? existing.getDescription() : "", 20);

        int row = 0;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel(i18n.get("label.firstName")+":"), gbc);
        gbc.gridx=1; dialog.add(firstField, gbc); row++;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel(i18n.get("label.lastName")+":"), gbc);
        gbc.gridx=1; dialog.add(lastField, gbc); row++;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel(i18n.get("label.documentNumber")+":"), gbc);
        gbc.gridx=1; dialog.add(docField, gbc); row++;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel(i18n.get("label.description")+":"), gbc);
        gbc.gridx=1; dialog.add(descField, gbc); row++;

        JPanel buttons = new JPanel();
        JButton save = new JButton(i18n.get("button.save"));
        JButton cancel = new JButton(i18n.get("button.cancel"));
        buttons.add(save); buttons.add(cancel);
        gbc.gridx=0; gbc.gridy=row; gbc.gridwidth=2; dialog.add(buttons, gbc);

        save.addActionListener(e -> {
            if (existing == null) {
                repo.add(new Customer(0, firstField.getText().trim(), lastField.getText().trim(),
                    docField.getText().trim(), descField.getText().trim()));
            } else {
                existing.setFirstName(firstField.getText().trim());
                existing.setLastName(lastField.getText().trim());
                existing.setDocumentNumber(docField.getText().trim());
                existing.setDescription(descField.getText().trim());
                repo.update(existing);
            }
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
        for (Customer c : repo.findAll()) {
            tableModel.addRow(new Object[]{c.getId(), c.getFirstName(), c.getLastName(),
                c.getDocumentNumber(), c.getDescription()});
        }
    }

    public void refreshLabels() {
        I18n i18n = I18n.getInstance();
        btnAdd.setText(i18n.get("button.add"));
        btnEdit.setText(i18n.get("button.edit"));
        btnDelete.setText(i18n.get("button.delete"));
        tableModel.setColumnIdentifiers(new Object[]{"ID", i18n.get("label.firstName"),
            i18n.get("label.lastName"), i18n.get("label.documentNumber"),
            i18n.get("label.description")});
        loadData();
    }
}
