package pl.skirent.ui;

import pl.skirent.i18n.I18n;
import pl.skirent.io.FileIOService;
import pl.skirent.repository.*;
import pl.skirent.service.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;

public class MainFrame extends JFrame {
    private final SkiTypeRepository skiTypeRepo = new SkiTypeRepository();
    private final SkiRepository skiRepo = new SkiRepository();
    private final CustomerRepository customerRepo = new CustomerRepository();
    private final RentalRepository rentalRepo = new RentalRepository();

    private final RentalService rentalService = new RentalService(rentalRepo);
    private final ReportService reportService = new ReportService(skiRepo, rentalRepo);
    private final FileIOService fileIOService = FileIOService.getInstance();

    private JTabbedPane tabbedPane;
    private SkiTypePanel skiTypePanel;
    private SkiPanel skiPanel;
    private CustomerPanel customerPanel;
    private RentalPanel rentalPanel;
    private ReportPanel reportPanel;

    private JMenuBar menuBar;
    private JMenu menuFile, menuLanguage;
    private JMenuItem miSave, miLoad, miExit;
    private JRadioButtonMenuItem miPl, miEn;

    public MainFrame() {
        fileIOService.loadAllData(skiTypeRepo, skiRepo, customerRepo, rentalRepo);
        initUI();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                fileIOService.saveAllData(skiTypeRepo, skiRepo, customerRepo, rentalRepo);
                try { Thread.sleep(2000); } catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                fileIOService.shutdown();
                dispose();
                System.exit(0);
            }
        });
    }

    private void initUI() {
        I18n i18n = I18n.getInstance();
        setTitle(i18n.get("app.title"));
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        skiTypePanel = new SkiTypePanel(skiTypeRepo);
        skiPanel = new SkiPanel(skiRepo, skiTypeRepo);
        customerPanel = new CustomerPanel(customerRepo);
        rentalPanel = new RentalPanel(rentalRepo, customerRepo, skiRepo, rentalService);
        reportPanel = new ReportPanel(reportService);

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(i18n.get("tab.skiTypes"), skiTypePanel);
        tabbedPane.addTab(i18n.get("tab.skis"), skiPanel);
        tabbedPane.addTab(i18n.get("tab.customers"), customerPanel);
        tabbedPane.addTab(i18n.get("tab.rentals"), rentalPanel);
        tabbedPane.addTab(i18n.get("tab.reports"), reportPanel);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        menuBar = new JMenuBar();
        menuFile = new JMenu(i18n.get("menu.file"));
        miSave = new JMenuItem(i18n.get("menu.file.save"));
        miLoad = new JMenuItem(i18n.get("menu.file.load"));
        miExit = new JMenuItem(i18n.get("menu.file.exit"));
        menuFile.add(miSave);
        menuFile.add(miLoad);
        menuFile.addSeparator();
        menuFile.add(miExit);

        menuLanguage = new JMenu(i18n.get("menu.language"));
        ButtonGroup langGroup = new ButtonGroup();
        miPl = new JRadioButtonMenuItem(i18n.get("menu.lang.pl"), true);
        miEn = new JRadioButtonMenuItem(i18n.get("menu.lang.en"), false);
        langGroup.add(miPl); langGroup.add(miEn);
        menuLanguage.add(miPl); menuLanguage.add(miEn);

        menuBar.add(menuFile);
        menuBar.add(menuLanguage);
        setJMenuBar(menuBar);

        miSave.addActionListener(e -> {
            fileIOService.saveAllData(skiTypeRepo, skiRepo, customerRepo, rentalRepo);
            JOptionPane.showMessageDialog(this, I18n.getInstance().get("msg.saved"));
        });
        miLoad.addActionListener(e -> {
            fileIOService.loadAllData(skiTypeRepo, skiRepo, customerRepo, rentalRepo);
            refreshAllPanels();
            JOptionPane.showMessageDialog(this, I18n.getInstance().get("msg.loaded"));
        });
        miExit.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        miPl.addActionListener(e -> switchLocale(new Locale("pl")));
        miEn.addActionListener(e -> switchLocale(Locale.ENGLISH));
    }

    private void switchLocale(Locale locale) {
        I18n.getInstance().setLocale(locale);
        I18n i18n = I18n.getInstance();
        setTitle(i18n.get("app.title"));
        menuFile.setText(i18n.get("menu.file"));
        miSave.setText(i18n.get("menu.file.save"));
        miLoad.setText(i18n.get("menu.file.load"));
        miExit.setText(i18n.get("menu.file.exit"));
        menuLanguage.setText(i18n.get("menu.language"));
        miPl.setText(i18n.get("menu.lang.pl"));
        miEn.setText(i18n.get("menu.lang.en"));
        tabbedPane.setTitleAt(0, i18n.get("tab.skiTypes"));
        tabbedPane.setTitleAt(1, i18n.get("tab.skis"));
        tabbedPane.setTitleAt(2, i18n.get("tab.customers"));
        tabbedPane.setTitleAt(3, i18n.get("tab.rentals"));
        tabbedPane.setTitleAt(4, i18n.get("tab.reports"));
        skiTypePanel.refreshLabels();
        skiPanel.refreshLabels();
        customerPanel.refreshLabels();
        rentalPanel.refreshLabels();
        reportPanel.refreshLabels();
    }

    private void refreshAllPanels() {
        skiTypePanel.loadData();
        skiPanel.loadData();
        customerPanel.loadData();
        rentalPanel.loadData();
        reportPanel.refresh();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
