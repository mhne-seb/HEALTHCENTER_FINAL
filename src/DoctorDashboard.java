import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.text.SimpleDateFormat;
import com.toedter.calendar.JCalendar;
import javax.swing.plaf.FontUIResource;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class DoctorDashboard extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private DefaultTableModel appointmentsTableModel;
    private JTable appointmentsTable;
    private JCalendar appointmentCalendar;
    private TableRowSorter<DefaultTableModel> appointmentSorter;

    private DefaultTableModel patientsTableModel;
    private JTable patientsTable;

    private JComboBox<String> cbApptStatus;

    private String PRCLicenseNo;
    private String doctorName;

    // Color Palette
    private final Color COLOR_WHITE = new Color(0xFFFFFF);
    private final Color COLOR_ACCENT = new Color(0x00C9D8);
    private final Color COLOR_ACCENT_DARK = new Color(0x0097A7);
    private final Color COLOR_HEADER = new Color(0x0097A7);
    private final Color COLOR_LIGHT_TEAL = new Color(0xE7F7FA);
    private final Color COLOR_TOPBAR = new Color(0xF4F8FB);
    private final Color COLOR_BLACK = new Color(30, 30, 30);

    // Used to ensure 1 prescription ID per patient+doctor+date+diagnosis session
    private Map<String, String> prescriptionIdMap = new LinkedHashMap<>();

    public DoctorDashboard(String PRCLicenseNo, String doctorName) {
        this.PRCLicenseNo = PRCLicenseNo;
        this.doctorName = doctorName;

        setTitle("Doctor Dashboard");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setUIFont(new FontUIResource("Segoe UI", Font.PLAIN, 13));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_TOPBAR);
        topBar.setPreferredSize(new Dimension(getWidth(), 55));
        JLabel welcomeLabel = new JLabel("Welcome, " + this.doctorName + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcomeLabel.setForeground(COLOR_ACCENT_DARK);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(0, 32, 0, 0));
        topBar.add(welcomeLabel, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(180, getHeight()));
        sidebar.setBackground(COLOR_WHITE);

        ImageIcon logoIcon = new ImageIcon("C:\\Users\\jerma\\IdeaProjects\\HospitalManagementSystem\\src\\medicarelogo.png");
        Image img = logoIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(img));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sidebar.add(logoLabel);

        JButton btnAppointments = createSidebarButton("View Appointments");
        JButton btnPatients = createSidebarButton("View All Patients");
        sidebar.add(btnAppointments); sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnPatients); sidebar.add(Box.createVerticalGlue());

        JButton btnLogout = new JButton("Log out");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setBackground(COLOR_WHITE);
        btnLogout.setForeground(COLOR_ACCENT_DARK);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createLineBorder(COLOR_ACCENT_DARK, 2));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setMaximumSize(new Dimension(150, 40));
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnLogout.setBackground(COLOR_LIGHT_TEAL);}
            public void mouseExited(MouseEvent e) { btnLogout.setBackground(COLOR_WHITE);}
        });
        sidebar.add(Box.createRigidArea(new Dimension(0, 16)));
        sidebar.add(btnLogout);

        add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createAppointmentsPanel(), "Appointments");
        mainPanel.add(createPatientsPanel(), "Patients");

        add(mainPanel, BorderLayout.CENTER);

        loadAppointmentsFromDB();
        loadPatientsFromDB();

        btnAppointments.addActionListener(e -> cardLayout.show(mainPanel, "Appointments"));
        btnPatients.addActionListener(e -> cardLayout.show(mainPanel, "Patients"));

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                    "Logout Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {{ dispose(); }
                new RoleSelectionPage().setVisible(true);
                this.dispose();
            }
        });

        cardLayout.show(mainPanel, "Appointments");
    }

    private void setUIFont(FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(160, 45));
        btn.setBackground(COLOR_WHITE);
        btn.setForeground(COLOR_ACCENT_DARK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(COLOR_LIGHT_TEAL);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(COLOR_WHITE);
            }
        });
        return btn;
    }

    private JPanel createAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_WHITE);

        JPanel topPanel = new JPanel(new BorderLayout(15, 10));
        topPanel.setBackground(COLOR_WHITE);

        appointmentCalendar = new JCalendar();
        appointmentCalendar.setPreferredSize(new Dimension(270, 160));
        topPanel.add(appointmentCalendar, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 12));
        searchPanel.setBackground(COLOR_WHITE);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField searchField = new JTextField(18);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);

        JPanel summaryPanel = new JPanel(new GridLayout(2, 2, 8, 6));
        summaryPanel.setBackground(COLOR_WHITE);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 8));

        JLabel lblTotal = new JLabel();
        JLabel lblPending = new JLabel();
        JLabel lblDone = new JLabel();
        JLabel lblCancelled = new JLabel();

        summaryPanel.add(createInfoCard("Total", lblTotal));
        summaryPanel.add(createInfoCard("Pending", lblPending));
        summaryPanel.add(createInfoCard("Done", lblDone));
        summaryPanel.add(createInfoCard("Cancelled", lblCancelled));

        JPanel rightTopPanel = new JPanel(new BorderLayout());
        rightTopPanel.setBackground(COLOR_WHITE);
        rightTopPanel.add(searchPanel, BorderLayout.NORTH);
        rightTopPanel.add(summaryPanel, BorderLayout.CENTER);

        topPanel.add(rightTopPanel, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        statusPanel.setBackground(COLOR_WHITE);
        statusPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT_DARK, 1),
                "Appointment Status",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                COLOR_ACCENT_DARK
        ));

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbApptStatus = new JComboBox<>(new String[] { "Pending", "Done", "Cancelled" });
        cbApptStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton btnUpdateStatus = createActionBtnTeal("Update Status");
        statusPanel.add(statusLabel);
        statusPanel.add(cbApptStatus);
        statusPanel.add(btnUpdateStatus);

        JPanel topWholePanel = new JPanel();
        topWholePanel.setLayout(new BorderLayout());
        topWholePanel.setBackground(COLOR_WHITE);
        topWholePanel.add(topPanel, BorderLayout.NORTH);
        topWholePanel.add(statusPanel, BorderLayout.CENTER);

        panel.add(topWholePanel, BorderLayout.NORTH);

        String[] columns = {"Appointment ID", "Patient ID", "PRC License No.", "Date", "Time", "Reason", "Status"};
        appointmentsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointmentsTable = new JTable(appointmentsTableModel);
        styleTableTeal(appointmentsTable);

        appointmentSorter = new TableRowSorter<>(appointmentsTableModel);
        appointmentsTable.setRowSorter(appointmentSorter);

        appointmentCalendar.getDayChooser().addPropertyChangeListener("day", evt -> {
            Date selectedDate = appointmentCalendar.getDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = sdf.format(selectedDate);
            appointmentSorter.setRowFilter(RowFilter.regexFilter(dateStr, 3));
            updateSummaryLabels(lblTotal, lblPending, lblDone, lblCancelled);
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { search(); }
            public void removeUpdate(DocumentEvent e) { search(); }
            public void changedUpdate(DocumentEvent e) { search(); }
            private void search() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    appointmentSorter.setRowFilter(null);
                } else {
                    appointmentSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
                updateSummaryLabels(lblTotal, lblPending, lblDone, lblCancelled);
            }
        });

        appointmentSorter.setComparator(4, (o1, o2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                return sdf.parse(o1.toString()).compareTo(sdf.parse(o2.toString()));
            } catch (Exception ex) { return 0; }
        });
        appointmentsTable.getRowSorter().toggleSortOrder(4);

        appointmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = appointmentsTable.getSelectedRow();
                if (row >= 0) {
                    String statusVal = appointmentsTable.getValueAt(row, 6).toString();
                    cbApptStatus.setSelectedItem(statusVal);
                }
            }
        });

        btnUpdateStatus.addActionListener(e -> {
            int row = appointmentsTable.getSelectedRow();
            if (row >= 0) {
                int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to update the status?", "Confirm Update", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    appointmentsTableModel.setValueAt(cbApptStatus.getSelectedItem(), appointmentsTable.convertRowIndexToModel(row), 6);
                    JOptionPane.showMessageDialog(panel, "Appointment status updated!");
                    updateSummaryLabels(lblTotal, lblPending, lblDone, lblCancelled);
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select an appointment to update status.");
            }
        });

        appointmentsTableModel.addTableModelListener(e -> updateSummaryLabels(lblTotal, lblPending, lblDone, lblCancelled));
        updateSummaryLabels(lblTotal, lblPending, lblDone, lblCancelled);

        panel.add(new JScrollPane(appointmentsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInfoCard(String label, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(120, 48));
        Color customLightTeal = new Color(180, 230, 237);
        card.setBackground(customLightTeal);
        card.setBorder(BorderFactory.createLineBorder(COLOR_ACCENT, 2));
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void updateSummaryLabels(JLabel lblTotal, JLabel lblPending, JLabel lblDone, JLabel lblCancelled) {
        int total = 0, pending = 0, done = 0, cancelled = 0;
        for (int i = 0; i < appointmentsTable.getRowCount(); i++) {
            int modelRow = appointmentsTable.convertRowIndexToModel(i);
            String status = appointmentsTableModel.getValueAt(modelRow, 6).toString();
            total++;
            switch (status) {
                case "Pending": pending++; break;
                case "Done": done++; break;
                case "Cancelled": cancelled++; break;
            }
        }
        lblTotal.setText(String.valueOf(total));
        lblPending.setText(String.valueOf(pending));
        lblDone.setText(String.valueOf(done));
        lblCancelled.setText(String.valueOf(cancelled));
    }

    private JPanel createPatientsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_WHITE);

        JLabel titleLabel = new JLabel("All Patient Records", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_HEADER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(18, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(COLOR_WHITE);
        JLabel searchLabel = new JLabel("Search:");
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        panel.add(searchPanel, BorderLayout.BEFORE_FIRST_LINE);

        String[] patientColumns = {"Patient ID", "Patient Name", "Age", "Sex", "Birthdate",
                "Height", "Weight", "Contact",
                "Email", "Blood Type", "Allergies", "Notes"};
        patientsTableModel = new DefaultTableModel(patientColumns, 0);
        patientsTable = new JTable(patientsTableModel);
        styleTableTeal(patientsTable);

        TableRowSorter<DefaultTableModel> patientsSorter = new TableRowSorter<>(patientsTableModel);
        patientsTable.setRowSorter(patientsSorter);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { search(); }
            public void removeUpdate(DocumentEvent e) { search(); }
            public void changedUpdate(DocumentEvent e) { search(); }
            private void search() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    patientsSorter.setRowFilter(null);
                } else {
                    patientsSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        patientsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (patientsTable.getSelectedRow() != -1 && e.getClickCount() == 1) {
                    int row = patientsTable.getSelectedRow();
                    String patientId = patientsTable.getValueAt(row, 0).toString();
                    showPrescriptionDialog(patientId);
                }
            }
        });

        panel.add(new JScrollPane(patientsTable), BorderLayout.CENTER);
        return panel;
    }

    private void showPrescriptionDialog(String patientId) {
        Map<String, String> medicineMap = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT med_id, med_name FROM inventory";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                medicineMap.put(rs.getString("med_id"), rs.getString("med_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load medicines from inventory: " + ex.getMessage());
        }

        JDialog dialog = new JDialog(this, "Prescribe Medicine", true);
        dialog.setSize(1030, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField tfPrescriptionId = new JTextField(12); tfPrescriptionId.setEditable(false);
        JTextField tfPatientId = new JTextField(12); tfPatientId.setText(patientId); tfPatientId.setEditable(false);
        JTextField tfPRCLicenseNo = new JTextField(12); tfPRCLicenseNo.setText(this.PRCLicenseNo); tfPRCLicenseNo.setEditable(false);
        JTextField tfDate = new JTextField(12); tfDate.setText(java.time.LocalDate.now().toString());
        JTextField tfDiagnosis = new JTextField(12);
        JTextField tfInstructions = new JTextField(12);

        // Helper to generate or reuse Prescription ID
        Runnable updatePrescriptionId = () -> {
            String key = tfPatientId.getText() + "|" + tfPRCLicenseNo.getText() + "|" + tfDate.getText() + "|" + tfDiagnosis.getText();
            if (!prescriptionIdMap.containsKey(key)) {
                // Generate a new unique ID
                String id = "RX-" + UUID.randomUUID().toString().substring(0,8);
                prescriptionIdMap.put(key, id);
            }
            tfPrescriptionId.setText(prescriptionIdMap.get(key));
        };

        // Update Prescription ID automatically when diagnosis is entered/changed
        tfDiagnosis.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updatePrescriptionId.run(); }
            public void removeUpdate(DocumentEvent e) { updatePrescriptionId.run(); }
            public void changedUpdate(DocumentEvent e) { updatePrescriptionId.run(); }
        });
        tfDate.addActionListener(e -> updatePrescriptionId.run());

        updatePrescriptionId.run(); // Initial run

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Prescription ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = y; formPanel.add(tfPrescriptionId, gbc);
        gbc.gridx = 2; gbc.gridy = y; formPanel.add(new JLabel("Patient ID:"), gbc);
        gbc.gridx = 3; gbc.gridy = y; formPanel.add(tfPatientId, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1; gbc.gridy = y; formPanel.add(tfDate, gbc);
        gbc.gridx = 2; gbc.gridy = y; formPanel.add(new JLabel("PRC License No.:"), gbc);
        gbc.gridx = 3; gbc.gridy = y; formPanel.add(tfPRCLicenseNo, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Diagnosis:"), gbc);
        gbc.gridx = 1; gbc.gridy = y; formPanel.add(tfDiagnosis, gbc);
        gbc.gridx = 2; gbc.gridy = y; formPanel.add(new JLabel("Special Instructions:"), gbc);
        gbc.gridx = 3; gbc.gridy = y; formPanel.add(tfInstructions, gbc);

        // MEDICINE INPUTS
        JPanel addMedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JLabel lblMedNo = new JLabel("MED 1");
        JComboBox<String> cbMedId = new JComboBox<>(medicineMap.keySet().toArray(new String[0]));
        JTextField tfMedName = new JTextField(12); tfMedName.setEditable(false);

        addMedPanel.add(lblMedNo);
        addMedPanel.add(new JLabel("Medicine ID:"));
        addMedPanel.add(cbMedId);
        addMedPanel.add(new JLabel("Name:"));
        addMedPanel.add(tfMedName);
        addMedPanel.add(new JLabel("Quantity:"));
        JSpinner spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        spnQuantity.setPreferredSize(new Dimension(55, tfMedName.getPreferredSize().height));
        addMedPanel.add(spnQuantity);

        JTextField tfDosage = new JTextField(7);
        JTextField tfFrequency = new JTextField(7);
        JTextField tfDuration = new JTextField(7);
        JButton btnAddMed = new JButton("Add");

        cbMedId.addActionListener(e -> {
            String selected = (String)cbMedId.getSelectedItem();
            tfMedName.setText(medicineMap.getOrDefault(selected, ""));
            String dosage = tfDosage.getText().trim();
            String freq = tfFrequency.getText().trim();
            String dur = tfDuration.getText().trim();
            if (!dosage.isEmpty() && !freq.isEmpty() && !dur.isEmpty()) {
                btnAddMed.doClick();
            }
        });

        if (cbMedId.getItemCount() > 0) {
            cbMedId.setSelectedIndex(0);
            tfMedName.setText(medicineMap.get(cbMedId.getSelectedItem()));
        }
        tfDuration.addActionListener(e -> btnAddMed.doClick());

        addMedPanel.add(new JLabel("Dosage:"));
        addMedPanel.add(tfDosage);
        addMedPanel.add(new JLabel("Frequency:"));
        addMedPanel.add(tfFrequency);
        addMedPanel.add(new JLabel("Duration:"));
        addMedPanel.add(tfDuration);
        addMedPanel.add(btnAddMed);

        String[] medColumns = {"Medicine ID", "Medicine Name", "Quantity", "Dosage", "Frequency", "Duration"};
        DefaultTableModel medTableModel = new DefaultTableModel(medColumns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable medTable = new JTable(medTableModel);
        medTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane medScroll = new JScrollPane(medTable);
        medScroll.setPreferredSize(new Dimension(900, 110));

        btnAddMed.addActionListener(e -> {
            if (medTableModel.getRowCount() >= 30) {
                JOptionPane.showMessageDialog(dialog, "Maximum 30 medicines per prescription.");
                return;
            }
            String medId = (String)cbMedId.getSelectedItem();
            String medName = tfMedName.getText();
            String quantity = spnQuantity.getValue().toString();
            String dosage = tfDosage.getText().trim();
            String freq = tfFrequency.getText().trim();
            String dur = tfDuration.getText().trim();
            if (medId == null || medId.isEmpty() || medName == null || medName.isEmpty() || quantity.isEmpty() || dosage.isEmpty() || freq.isEmpty() || dur.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all medicine fields.");
                return;
            }
            medTableModel.addRow(new Object[]{medId, medName, quantity, dosage, freq, dur});
            tfDosage.setText(""); tfFrequency.setText(""); tfDuration.setText(""); spnQuantity.setValue(1);
            cbMedId.setSelectedIndex(0);
            tfMedName.setText(medicineMap.get(cbMedId.getSelectedItem()));
            tfDosage.requestFocus();
            lblMedNo.setText("MED " + (medTableModel.getRowCount() + 1));
        });

        JButton btnRemoveMed = new JButton("Remove Selected");
        btnRemoveMed.addActionListener(e -> {
            int row = medTable.getSelectedRow();
            if (row != -1) {
                medTableModel.removeRow(row);
                if (medTableModel.getRowCount() == 0) lblMedNo.setText("MED 1");
                else lblMedNo.setText("MED " + (medTableModel.getRowCount() + 1));
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        JButton btnSave = new JButton("Save Prescription");
        JButton btnClear = new JButton("Clear All");
        JButton btnClose = new JButton("Close");
        btnPanel.add(btnSave); btnPanel.add(btnClear); btnPanel.add(btnClose);

        btnSave.addActionListener(e -> {
            if (medTableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(dialog, "Add at least one medicine.");
                return;
            }
            String prescriptionId = tfPrescriptionId.getText().trim();
            String diagnosis = tfDiagnosis.getText().trim();
            if (prescriptionId.isEmpty() || diagnosis.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Diagnosis and Prescription ID required.");
                return;
            }
            try (Connection conn = DBConnection.getConnection()) {
                // Save prescription main record ONCE (if not exists)
                String checkSql = "SELECT COUNT(*) FROM prescriptions WHERE prescription_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, prescriptionId);
                    ResultSet rs = checkStmt.executeQuery();
                    rs.next();
                    int count = rs.getInt(1);
                    if (count == 0) {
                        // Insert the prescription header (adjust to your schema if needed)
                        String mainSql = "INSERT INTO prescriptions (prescription_id, patient_id, prc_license_no, date_prescribed, diagnosis, instructions, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement mainStmt = conn.prepareStatement(mainSql)) {
                            mainStmt.setString(1, prescriptionId);
                            mainStmt.setString(2, tfPatientId.getText().trim());
                            mainStmt.setString(3, this.PRCLicenseNo);
                            mainStmt.setString(4, tfDate.getText().trim());
                            mainStmt.setString(5, tfDiagnosis.getText().trim());
                            mainStmt.setString(6, tfInstructions.getText().trim());
                            mainStmt.setString(7, "Pending");
                            mainStmt.executeUpdate();
                        }
                    }
                }
                // Save all medicine items linked to this prescription id
                for (int i = 0; i < medTableModel.getRowCount(); i++) {
                    String sql = "INSERT INTO prescription_medicines (prescription_id, med_id, medicine_name, quantity, dosage, frequency, duration) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, prescriptionId);
                        pstmt.setString(2, (String) medTableModel.getValueAt(i, 0)); // med_id
                        pstmt.setString(3, (String) medTableModel.getValueAt(i, 1)); // medicine_name
                        pstmt.setString(4, (String) medTableModel.getValueAt(i, 2)); // quantity
                        pstmt.setString(5, (String) medTableModel.getValueAt(i, 3)); // dosage
                        pstmt.setString(6, (String) medTableModel.getValueAt(i, 4)); // frequency
                        pstmt.setString(7, (String) medTableModel.getValueAt(i, 5)); // duration
                        pstmt.executeUpdate();
                    }
                }
                JOptionPane.showMessageDialog(dialog, "Prescription(s) saved for Patient: " + tfPatientId.getText());
                medTableModel.setRowCount(0);
                lblMedNo.setText("MED 1");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage());
            }
        });

        btnClear.addActionListener(e -> {
            tfPrescriptionId.setText(""); tfDiagnosis.setText(""); tfInstructions.setText("");
            medTableModel.setRowCount(0);
            lblMedNo.setText("MED 1");
            cbMedId.setSelectedIndex(0);
            tfMedName.setText(medicineMap.get(cbMedId.getSelectedItem()));
            spnQuantity.setValue(1);
        });
        btnClose.addActionListener(e -> dialog.dispose());

        JPanel medTablePanel = new JPanel(new BorderLayout());
        medTablePanel.add(addMedPanel, BorderLayout.NORTH);
        medTablePanel.add(medScroll, BorderLayout.CENTER);
        medTablePanel.add(btnRemoveMed, BorderLayout.SOUTH);

        JPanel topFormPanel = new JPanel(new BorderLayout());
        topFormPanel.add(formPanel, BorderLayout.CENTER);

        dialog.add(topFormPanel, BorderLayout.NORTH);
        dialog.add(medTablePanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JButton createActionBtnTeal(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(COLOR_ACCENT);
        btn.setForeground(COLOR_BLACK);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(COLOR_ACCENT_DARK);
                btn.setForeground(COLOR_WHITE);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(COLOR_ACCENT);
                btn.setForeground(COLOR_BLACK);
            }
        });
        return btn;
    }

    private void styleTableTeal(JTable table) {
        table.setBackground(COLOR_WHITE);
        table.setForeground(COLOR_BLACK);
        table.setSelectionBackground(COLOR_ACCENT_DARK);
        table.setSelectionForeground(COLOR_WHITE);
        table.setGridColor(COLOR_ACCENT);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(22);
        table.getTableHeader().setBackground(new Color(0xD2F2F8));
        table.getTableHeader().setForeground(COLOR_ACCENT_DARK);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setForeground(COLOR_ACCENT_DARK);
        centerRenderer.setFont(new Font("Segoe UI", Font.BOLD, 12));
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
    }

    private void loadAppointmentsFromDB() {
        if (appointmentsTableModel == null) return;
        appointmentsTableModel.setRowCount(0);
        String sql = "SELECT * FROM appointments WHERE prc_license_no='" + this.PRCLicenseNo + "'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = {
                        rs.getString("appointment_id"),
                        rs.getString("patient_id"),
                        rs.getString("prc_license_no"),
                        rs.getString("date"),
                        rs.getString("time"),
                        rs.getString("reason"),
                        rs.getString("status")
                };
                appointmentsTableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load appointments: " + ex.getMessage());
        }
    }

    private void loadPatientsFromDB() {
        if (patientsTableModel == null) return;
        patientsTableModel.setRowCount(0);
        String sql = "SELECT * FROM patients";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String fullName = rs.getString("last_name") + ", " + rs.getString("first_name") +
                        (rs.getString("middle_name") != null && !rs.getString("middle_name").isEmpty() ? " " + rs.getString("middle_name") : "");
                String dateOfBirthStr = rs.getString("date_of_birth");
                int age = 0;
                try {
                    LocalDate dob = LocalDate.parse(dateOfBirthStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    age = Period.between(dob, LocalDate.now()).getYears();
                } catch (Exception e) { }
                Object[] row = {
                        rs.getString("patient_id"),
                        fullName,
                        String.valueOf(age),
                        rs.getString("sex"),
                        rs.getString("date_of_birth"),
                        rs.getString("height"),
                        rs.getString("weight"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("blood_type"),
                        rs.getString("allergies"),
                        rs.getString("notes")
                };
                patientsTableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load patients: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new DoctorDashboard("PRC-001", "Dr. Maria Reyes").setVisible(true);
        });
    }
}