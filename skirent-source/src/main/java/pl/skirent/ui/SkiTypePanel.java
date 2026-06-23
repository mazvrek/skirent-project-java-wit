package pl.skirent.ui;

import pl.skirent.i18n.I18n;
import pl.skirent.model.SkiType;
import pl.skirent.repository.SkiTypeRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SkiTypePanel extends JPanel {
    private final SkiTypeRepository repo;
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton btnAdd, btnEdit, btnDelete;

    public SkiTypePanel(SkiTypeRepository repo) {
        this.repo = repo;
        setLayout(new BorderLayout());
        initComponents();
        loadData();
    }

    private void initComponents() {
        I18n i18n = I18n.getInstance();
        tableModel = new DefaultTableModel(new Object[]{"ID", i18n.get("label.name"), i18n.get("label.description")}, 0) {
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
            SkiType found = repo.findById(id);
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

    private void showDialog(SkiType existing) {
        I18n i18n = I18n.getInstance();
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? i18n.get("button.add") : i18n.get("button.edit"), true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);

        JTextField nameField = new JTextField(existing != null ? existing.getName() : "", 20);
        JTextField descField = new JTextField(existing != null ? existing.getDescription() : "", 20);

        gbc.gridx=0; gbc.gridy=0; dialog.add(new JLabel(i18n.get("label.name")+":"), gbc);
        gbc.gridx=1; dialog.add(nameField, gbc);
        gbc.gridx=0; gbc.gridy=1; dialog.add(new JLabel(i18n.get("label.description")+":"), gbc);
        gbc.gridx=1; dialog.add(descField, gbc);

        JPanel buttons = new JPanel();
        JButton save = new JButton(i18n.get("button.save"));
        JButton cancel = new JButton(i18n.get("button.cancel"));
        buttons.add(save); buttons.add(cancel);
        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=2; dialog.add(buttons, gbc);

        save.addActionListener(e -> {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            if (existing == null) {
                repo.add(new SkiType(0, name, desc));
            } else {
                existing.setName(name);
                existing.setDescription(desc);
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
        for (SkiType st : repo.findAll()) {
            tableModel.addRow(new Object[]{st.getId(), st.getName(), st.getDescription()});
        }
    }

    public void refreshLabels() {
        I18n i18n = I18n.getInstance();
        btnAdd.setText(i18n.get("button.add"));
        btnEdit.setText(i18n.get("button.edit"));
        btnDelete.setText(i18n.get("button.delete"));
        tableModel.setColumnIdentifiers(new Object[]{"ID", i18n.get("label.name"), i18n.get("label.description")});
        loadData();
    }
}
