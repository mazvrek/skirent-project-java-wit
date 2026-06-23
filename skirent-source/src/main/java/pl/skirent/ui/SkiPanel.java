package pl.skirent.ui;

import pl.skirent.i18n.I18n;
import pl.skirent.model.Ski;
import pl.skirent.model.SkiType;
import pl.skirent.repository.SkiRepository;
import pl.skirent.repository.SkiTypeRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SkiPanel extends JPanel {
    private final SkiRepository skiRepo;
    private final SkiTypeRepository skiTypeRepo;
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton btnAdd, btnEdit, btnDelete;

    public SkiPanel(SkiRepository skiRepo, SkiTypeRepository skiTypeRepo) {
        this.skiRepo = skiRepo;
        this.skiTypeRepo = skiTypeRepo;
        setLayout(new BorderLayout());
        initComponents();
        loadData();
    }

    private void initComponents() {
        I18n i18n = I18n.getInstance();
        tableModel = new DefaultTableModel(
            new Object[]{"ID", i18n.get("label.type"), i18n.get("label.brand"),
                i18n.get("label.model"), i18n.get("label.bindings"), i18n.get("label.length")}, 0) {
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
            Ski found = skiRepo.findById(id);
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
                skiRepo.delete(id);
                loadData();
            }
        });
    }

    private void showDialog(Ski existing) {
        I18n i18n = I18n.getInstance();
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            existing == null ? i18n.get("button.add") : i18n.get("button.edit"), true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<SkiType> types = skiTypeRepo.findAll();
        JComboBox<SkiType> typeCombo = new JComboBox<>(types.toArray(new SkiType[0]));
        if (existing != null) {
            for (SkiType st : types) {
                if (st.getId() == existing.getSkiTypeId()) { typeCombo.setSelectedItem(st); break; }
            }
        }

        JTextField brandField = new JTextField(existing != null ? existing.getBrand() : "", 20);
        JTextField modelField = new JTextField(existing != null ? existing.getModel() : "", 20);
        JTextField bindField = new JTextField(existing != null ? existing.getBindings() : "", 20);
        JTextField lenField = new JTextField(existing != null ? String.valueOf(existing.getLength()) : "", 10);

        int row = 0;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel(i18n.get("label.type")+":"), gbc);
        gbc.gridx=1; dialog.add(typeCombo, gbc); row++;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel(i18n.get("label.brand")+":"), gbc);
        gbc.gridx=1; dialog.add(brandField, gbc); row++;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel(i18n.get("label.model")+":"), gbc);
        gbc.gridx=1; dialog.add(modelField, gbc); row++;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel(i18n.get("label.bindings")+":"), gbc);
        gbc.gridx=1; dialog.add(bindField, gbc); row++;
        gbc.gridx=0; gbc.gridy=row; dialog.add(new JLabel(i18n.get("label.length")+":"), gbc);
        gbc.gridx=1; dialog.add(lenField, gbc); row++;

        JPanel buttons = new JPanel();
        JButton save = new JButton(i18n.get("button.save"));
        JButton cancel = new JButton(i18n.get("button.cancel"));
        buttons.add(save); buttons.add(cancel);
        gbc.gridx=0; gbc.gridy=row; gbc.gridwidth=2; dialog.add(buttons, gbc);

        save.addActionListener(e -> {
            SkiType type = (SkiType) typeCombo.getSelectedItem();
            int typeId = type != null ? type.getId() : 0;
            double length = 0;
            try { length = Double.parseDouble(lenField.getText().trim()); } catch (NumberFormatException ignored) {}
            if (existing == null) {
                skiRepo.add(new Ski(0, typeId, brandField.getText().trim(),
                    modelField.getText().trim(), bindField.getText().trim(), length));
            } else {
                existing.setSkiTypeId(typeId);
                existing.setBrand(brandField.getText().trim());
                existing.setModel(modelField.getText().trim());
                existing.setBindings(bindField.getText().trim());
                existing.setLength(length);
                skiRepo.update(existing);
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
        for (Ski ski : skiRepo.findAll()) {
            SkiType type = skiTypeRepo.findById(ski.getSkiTypeId());
            tableModel.addRow(new Object[]{ski.getId(),
                type != null ? type.getName() : "",
                ski.getBrand(), ski.getModel(), ski.getBindings(), ski.getLength()});
        }
    }

    public void refreshLabels() {
        I18n i18n = I18n.getInstance();
        btnAdd.setText(i18n.get("button.add"));
        btnEdit.setText(i18n.get("button.edit"));
        btnDelete.setText(i18n.get("button.delete"));
        tableModel.setColumnIdentifiers(new Object[]{"ID", i18n.get("label.type"),
            i18n.get("label.brand"), i18n.get("label.model"),
            i18n.get("label.bindings"), i18n.get("label.length")});
        loadData();
    }
}
