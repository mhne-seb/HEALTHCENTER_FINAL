import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.FontUIResource;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClinicStaffDashboard extends JFrame {
    private static String staffName;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private DefaultTableModel patientTableModel;
    private JTable patientTable;

    private DefaultTableModel appointmentTableModel;
    private JTable appointmentTable;

    private JTextField tfPatientID, tfFirstName, tfMiddleName, tfLastName, tfDateofBirth, tfHeight, tfWeight, tfBloodType, tfAllergies, tfEmail, tfPhone, tfNotes;
    private JComboBox<String> cbSex;

    private JTextField tfApptID, tfApptPatientID, tfApptPRCLicenseNo, tfApptDate, tfApptTime, tfApptReason, tfApptNotes;
    private JComboBox<String> cbApptStatus;

    private final Color COLOR_WHITE = new Color(0xFFFFFF);
    private final Color COLOR_ACCENT = new Color(0x00C9D8);
    private final Color COLOR_ACCENT_DARK = new Color(0x0097A7);
    private final Color COLOR_HEADER = new Color(0x0097A7);
    private final Color COLOR_LIGHT_TEAL = new Color(0xE7F7FA);
    private final Color COLOR_TOPBAR = new Color(0xF4F8FB);
    private final Color COLOR_BLACK = new Color(30, 30, 30);

    public ClinicStaffDashboard(String staffName) {
        ClinicStaffDashboard.staffName = staffName;
        setTitle("Clinic Staff Dashboard");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setUIFont(new FontUIResource("Segoe UI", Font.PLAIN, 13));

        String[] apptColumns = {"Appointment ID", "Patient ID", "PRCLicenseNo", "Date", "Time", "Reason", "Status", "Notes"};
        appointmentTableModel = new DefaultTableModel(apptColumns, 0);
        appointmentTable = new JTable(appointmentTableModel);

        String[] columns = {"Patient ID", "First Name", "Middle Name", "Last Name", "Date of Birth", "Sex", "Height", "Weight", "Blood Type", "Allergies", "Email", "Phone", "Notes"};
        patientTableModel = new DefaultTableModel(columns, 0);
        patientTable = new JTable(patientTableModel);

        //  Load data from database
        loadPatientsFromDB();
        loadAppointmentsFromDB();

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_TOPBAR);
        topBar.setPreferredSize(new Dimension(getWidth(), 55));
        JLabel welcomeLabel = new JLabel("Welcome, " + (staffName != null && !staffName.trim().isEmpty() ? staffName : "Staff") + "!");
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

        JButton btnPatients = createSidebarButton("Manage Patients");
        JButton btnAppointments = createSidebarButton("Schedule Appointment");
        JButton btnManageAppt = createSidebarButton("Manage Appointment");

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnPatients); sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnAppointments); sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnManageAppt); sidebar.add(Box.createVerticalGlue());

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

        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);

        mainPanel.add(createPatientPanel(), "Patients");
        mainPanel.add(createScheduleApptPanel(), "Appointments");
        mainPanel.add(createManageApptPanel(), "ManageAppt");

        add(mainPanel, BorderLayout.CENTER);

        btnPatients.addActionListener(e -> cardLayout.show(mainPanel, "Patients"));
        btnAppointments.addActionListener(e -> cardLayout.show(mainPanel, "Appointments"));
        btnManageAppt.addActionListener(e -> cardLayout.show(mainPanel, "ManageAppt"));
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                    "Logout Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) { dispose(); }
            new RoleSelectionPage().setVisible(true);
        });

        cardLayout.show(mainPanel, "Patients");
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
            public void mouseEntered(MouseEvent e) { btn.setBackground(COLOR_LIGHT_TEAL);}
            public void mouseExited(MouseEvent e) { btn.setBackground(COLOR_WHITE);}
        });
        return btn;
    }

    private JPanel createPatientPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_WHITE);

        JLabel titleLabel = new JLabel("Manage Patients", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_HEADER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(18, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        searchPanel.setBackground(COLOR_WHITE);
        JLabel searchLabel = new JLabel("Search:");
        JTextField tfSearch = new JTextField(28);
        searchPanel.add(searchLabel);
        searchPanel.add(tfSearch);
        panel.add(searchPanel, BorderLayout.BEFORE_FIRST_LINE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(COLOR_WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT_DARK, 1),
                "Patient Registration",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                COLOR_ACCENT_DARK
        ));

        tfPatientID = new JTextField(10); tfFirstName = new JTextField(10); tfMiddleName = new JTextField(10);
        tfLastName = new JTextField(10); tfDateofBirth = new JTextField(10); tfHeight = new JTextField(6);
        tfWeight = new JTextField(6); tfBloodType = new JTextField(5); tfAllergies = new JTextField(10);
        tfEmail = new JTextField(12); tfPhone = new JTextField(10); tfNotes = new JTextField(12);
        cbSex = new JComboBox<>(new String[]{"Male", "Female", "Other"});

        int y = 0;
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Patient ID:"), gbc);
        gbc.gridx = 1; formPanel.add(tfPatientID, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 3; formPanel.add(tfFirstName, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Middle Name:"), gbc);
        gbc.gridx = 1; formPanel.add(tfMiddleName, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 3; formPanel.add(tfLastName, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Date of Birth (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; formPanel.add(tfDateofBirth, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Sex/Gender:"), gbc);
        gbc.gridx = 3; formPanel.add(cbSex, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Height:"), gbc);
        gbc.gridx = 1; formPanel.add(tfHeight, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Weight (kg):"), gbc);
        gbc.gridx = 3; formPanel.add(tfWeight, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Blood Type:"), gbc);
        gbc.gridx = 1; formPanel.add(tfBloodType, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Allergies):"), gbc);
        gbc.gridx = 3; formPanel.add(tfAllergies, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; formPanel.add(tfEmail, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 3; formPanel.add(tfPhone, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; formPanel.add(tfNotes, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        btnPanel.setBackground(COLOR_WHITE);
        JButton btnAdd = createActionBtnTeal("Add");
        JButton btnEdit = createActionBtnTeal("Edit");
        JButton btnDelete = createActionBtnTeal("Delete");
        btnPanel.add(btnAdd); btnPanel.add(btnEdit); btnPanel.add(btnDelete);

        JPanel formAndBtns = new JPanel(new BorderLayout());
        formAndBtns.setBackground(COLOR_WHITE);
        formAndBtns.add(formPanel, BorderLayout.CENTER);
        formAndBtns.add(btnPanel, BorderLayout.SOUTH);

        panel.add(formAndBtns, BorderLayout.PAGE_START);

        styleTableTeal(patientTable);

        JScrollPane scrollPane = new JScrollPane(patientTable);
        scrollPane.setPreferredSize(new Dimension(1000, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(patientTableModel);
        patientTable.setRowSorter(rowSorter);
        tfSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = tfSearch.getText();
                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        patientTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = patientTable.getSelectedRow();
                if (row >= 0) {
                    int modelRow = patientTable.convertRowIndexToModel(row);
                    tfPatientID.setText(patientTableModel.getValueAt(modelRow, 0).toString());
                    tfFirstName.setText(patientTableModel.getValueAt(modelRow, 1).toString());
                    tfMiddleName.setText(patientTableModel.getValueAt(modelRow, 2).toString());
                    tfLastName.setText(patientTableModel.getValueAt(modelRow, 3).toString());
                    tfDateofBirth.setText(patientTableModel.getValueAt(modelRow, 4).toString());
                    cbSex.setSelectedItem(patientTableModel.getValueAt(modelRow, 5).toString());
                    tfHeight.setText(patientTableModel.getValueAt(modelRow, 6).toString());
                    tfWeight.setText(patientTableModel.getValueAt(modelRow, 7).toString());
                    tfBloodType.setText(patientTableModel.getValueAt(modelRow, 8).toString());
                    tfAllergies.setText(patientTableModel.getValueAt(modelRow, 9).toString());
                    tfEmail.setText(patientTableModel.getValueAt(modelRow, 10).toString());
                    tfPhone.setText(patientTableModel.getValueAt(modelRow, 11).toString());
                    tfNotes.setText(patientTableModel.getValueAt(modelRow, 12).toString());
                }
            }
        });

        btnAdd.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    panel,
                    "Are you sure to add this patient?",
                    "Add Confirmation",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                boolean dbSuccess = savePatientToDB();
                if (dbSuccess) {
                    loadPatientsFromDB(); // Reload table from DB
                    clearPatientFields();
                }
            }
        });
        btnEdit.addActionListener(e -> {
            int row = patientTable.getSelectedRow();
            if (row >= 0) {
                int confirm = JOptionPane.showConfirmDialog(
                        panel,
                        "Are you sure to edit this patient?",
                        "Edit Confirmation",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean dbSuccess = updatePatientInDB();
                    if (dbSuccess) {
                        loadPatientsFromDB(); // Reload table from DB
                        clearPatientFields();
                    }
                }
            }
        });
        btnDelete.addActionListener(e -> {
            int row = patientTable.getSelectedRow();
            if (row >= 0) {
                int confirm = JOptionPane.showConfirmDialog(
                        panel,
                        "Are you sure to delete this patient?",
                        "Delete Confirmation",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean dbSuccess = deletePatientFromDB(tfPatientID.getText());
                    if (dbSuccess) {
                        loadPatientsFromDB(); // Reload table from DB
                        clearPatientFields();
                    }
                }
            }
        });

        return panel;
    }

    private JPanel createScheduleApptPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_WHITE);

        JLabel titleLabel = new JLabel("Schedule Appointment", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_HEADER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(18, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel viewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        viewPanel.setBackground(COLOR_WHITE);
        JLabel lblSearch = new JLabel("Selected Patient:");
        JTextField tfSearchPatient = new JTextField(20);
        tfSearchPatient.setEditable(false);
        JButton btnViewPatients = createActionBtnTeal("View Patients");
        viewPanel.add(lblSearch);
        viewPanel.add(tfSearchPatient);
        viewPanel.add(btnViewPatients);

        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.setBackground(COLOR_WHITE);
        containerPanel.add(viewPanel);

        JPanel patientDetailsPanel = new JPanel(new BorderLayout());
        patientDetailsPanel.setBackground(COLOR_WHITE);
        patientDetailsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT_DARK, 1),
                "Patient Details",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                COLOR_ACCENT_DARK
        ));
        patientDetailsPanel.setVisible(false);

        JLabel patientDetailsLabel = new JLabel();
        patientDetailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        patientDetailsPanel.add(patientDetailsLabel, BorderLayout.CENTER);

        containerPanel.add(patientDetailsPanel);

        panel.add(containerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(COLOR_WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT_DARK, 1),
                "Add/Edit Appointment",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                COLOR_ACCENT_DARK
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        tfApptID = new JTextField(10); tfApptPatientID = new JTextField(10); tfApptPRCLicenseNo = new JTextField(10);
        tfApptDate = new JTextField(10); tfApptTime = new JTextField(8); tfApptReason = new JTextField(14); tfApptNotes = new JTextField(14);
        cbApptStatus = new JComboBox<>(new String[]{"Scheduled", "Cancelled", "Completed", "No-Show"});
        tfApptPatientID.setEditable(false);

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Appointment ID:"), gbc);
        gbc.gridx = 1; formPanel.add(tfApptID, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Patient ID:"), gbc);
        gbc.gridx = 3; formPanel.add(tfApptPatientID, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("PRCLicenseNo:"), gbc);
        gbc.gridx = 1; formPanel.add(tfApptPRCLicenseNo, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 3; formPanel.add(tfApptDate, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Time:"), gbc);
        gbc.gridx = 1; formPanel.add(tfApptTime, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Reason for Visit:"), gbc);
        gbc.gridx = 3; formPanel.add(tfApptReason, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; formPanel.add(cbApptStatus, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 3; formPanel.add(tfApptNotes, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        btnPanel.setBackground(COLOR_WHITE);
        JButton btnAdd = createActionBtnTeal("Add");
        JButton btnEdit = createActionBtnTeal("Edit");
        JButton btnDelete = createActionBtnTeal("Delete");
        btnPanel.add(btnAdd); btnPanel.add(btnEdit); btnPanel.add(btnDelete);

        JScrollPane apptScrollPane = new JScrollPane(appointmentTable);
        apptScrollPane.setPreferredSize(new Dimension(1000, 120));

        setAppointmentFormEnabled(false, tfApptID, tfApptPatientID, tfApptPRCLicenseNo, tfApptDate, tfApptTime, tfApptReason, tfApptNotes, cbApptStatus, btnAdd, btnEdit, btnDelete);

        JPanel formAndBtns = new JPanel(new BorderLayout());
        formAndBtns.setBackground(COLOR_WHITE);
        formAndBtns.add(formPanel, BorderLayout.CENTER);
        formAndBtns.add(btnPanel, BorderLayout.SOUTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(COLOR_WHITE);
        centerPanel.add(formAndBtns);
        centerPanel.add(apptScrollPane);

        panel.add(centerPanel, BorderLayout.CENTER);

        btnViewPatients.addActionListener(e -> {
            JDialog dialog = new JDialog(this, "Select Patient", true);
            dialog.setSize(900, 350);
            dialog.setLocationRelativeTo(this);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(COLOR_WHITE);

            DefaultTableModel dialogModel = new DefaultTableModel();
            for (int c = 0; c < patientTableModel.getColumnCount(); c++)
                dialogModel.addColumn(patientTableModel.getColumnName(c));
            for (int r = 0; r < patientTableModel.getRowCount(); r++) {
                Object[] row = new Object[patientTableModel.getColumnCount()];
                for (int c = 0; c < patientTableModel.getColumnCount(); c++)
                    row[c] = patientTableModel.getValueAt(r, c);
                dialogModel.addRow(row);
            }
            JTable dialogTable = new JTable(dialogModel);
            styleTableTeal(dialogTable);

            JScrollPane dialogScroll = new JScrollPane(dialogTable);
            dialogScroll.setPreferredSize(new Dimension(850, 180));
            mainPanel.add(dialogScroll, BorderLayout.CENTER);

            JLabel tip = new JLabel("Double click or select a patient and press Enter");
            tip.setForeground(COLOR_HEADER);
            tip.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            mainPanel.add(tip, BorderLayout.SOUTH);

            dialog.setContentPane(mainPanel);

            Runnable selectPatient = () -> {
                int row = dialogTable.getSelectedRow();
                if (row >= 0) {
                    int modelRow = dialogTable.convertRowIndexToModel(row);
                    String patientId = dialogModel.getValueAt(modelRow, 0).toString();
                    String fName = dialogModel.getValueAt(modelRow, 1).toString();
                    String mName = dialogModel.getValueAt(modelRow, 2).toString();
                    String lName = dialogModel.getValueAt(modelRow, 3).toString();
                    String DateofBirth = dialogModel.getValueAt(modelRow, 4).toString();
                    String sex = dialogModel.getValueAt(modelRow, 5).toString();
                    String phone = dialogModel.getValueAt(modelRow, 11).toString();
                    String email = dialogModel.getValueAt(modelRow, 10).toString();
                    String height = dialogModel.getValueAt(modelRow, 6).toString();
                    String weight = dialogModel.getValueAt(modelRow, 7).toString();
                    String blood = dialogModel.getValueAt(modelRow, 8).toString();
                    String allergies = dialogModel.getValueAt(modelRow, 9).toString();

                    tfSearchPatient.setText(patientId);
                    tfApptPatientID.setText(patientId);

                    LocalDateTime now = LocalDateTime.now();
                    tfApptDate.setText(now.format(DateTimeFormatter.ofPattern("YYYY-MM-DD")));
                    tfApptTime.setText(now.format(DateTimeFormatter.ofPattern("HH:mm")));

                    StringBuilder sb = new StringBuilder();
                    sb.append("<html>");
                    sb.append("<div style='font-family:Segoe UI; font-size:11px;'>");
                    sb.append("<table style='font-size:11px;line-height:1.6;'>");
                    sb.append("<tr><td style='font-weight:bold;'>Patient ID:</td><td>").append(patientId).append("</td></tr>");
                    sb.append("<tr><td style='font-weight:bold;'>Name:</td><td>").append(lName).append(", ").append(fName);
                    if (!mName.isEmpty()) sb.append(" ").append(mName);
                    sb.append("</td></tr>");
                    sb.append("<tr><td style='font-weight:bold;'>Date of Birth:</td><td>").append(DateofBirth).append("</td></tr>");
                    sb.append("<tr><td style='font-weight:bold;'>Sex:</td><td>").append(sex).append("</td></tr>");
                    sb.append("<tr><td style='font-weight:bold;'>Phone:</td><td>").append(phone).append("</td></tr>");
                    sb.append("<tr><td style='font-weight:bold;'>Email:</td><td>").append(email).append("</td></tr>");
                    sb.append("<tr><td style='font-weight:bold;'>Ht:</td><td>")
                            .append(height).append(" cm, Wt: ").append(weight).append(" kg, Blood: ").append(blood)
                            .append(", Allergies: ").append(allergies).append("</td></tr>");
                    sb.append("</table>");
                    sb.append("</div></html>");
                    patientDetailsLabel.setText(sb.toString());
                    patientDetailsPanel.setVisible(true);

                    setAppointmentFormEnabled(true, tfApptID, tfApptPatientID, tfApptPRCLicenseNo, tfApptDate, tfApptTime, tfApptReason, tfApptNotes, cbApptStatus, btnAdd, btnEdit, btnDelete);
                    dialog.dispose();
                }
            };

            dialogTable.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        selectPatient.run();
                    }
                }
            });
            dialogTable.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        selectPatient.run();
                    }
                }
            });

            dialog.setVisible(true);
        });

        appointmentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = appointmentTable.getSelectedRow();
                if (row >= 0) {
                    tfApptID.setText(appointmentTableModel.getValueAt(row, 0).toString());
                    tfApptPatientID.setText(appointmentTableModel.getValueAt(row, 1).toString());
                    tfApptPRCLicenseNo.setText(appointmentTableModel.getValueAt(row, 2).toString());
                    tfApptDate.setText(appointmentTableModel.getValueAt(row, 3).toString());
                    tfApptTime.setText(appointmentTableModel.getValueAt(row, 4).toString());
                    tfApptReason.setText(appointmentTableModel.getValueAt(row, 5).toString());
                    cbApptStatus.setSelectedItem(appointmentTableModel.getValueAt(row, 6).toString());
                    tfApptNotes.setText(appointmentTableModel.getValueAt(row, 7).toString());
                }
            }
        });

        btnAdd.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    panel,
                    "Are you sure to add this appointment?",
                    "Add Confirmation",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                boolean dbSuccess = saveAppointmentToDB();
                if (dbSuccess) {
                    loadAppointmentsFromDB();
                    clearApptFields();
                    setAppointmentFormEnabled(false, tfApptID, tfApptPatientID, tfApptPRCLicenseNo, tfApptDate, tfApptTime, tfApptReason, tfApptNotes, cbApptStatus, btnAdd, btnEdit, btnDelete);
                    tfSearchPatient.setText(""); patientDetailsPanel.setVisible(false);
                }
            }
        });
        btnEdit.addActionListener(e -> {
            int row = appointmentTable.getSelectedRow();
            if (row >= 0) {
                int confirm = JOptionPane.showConfirmDialog(
                        panel,
                        "Are you sure to edit this appointment?",
                        "Edit Confirmation",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean dbSuccess = updateAppointmentInDB();
                    if (dbSuccess) {
                        loadAppointmentsFromDB();
                        clearApptFields();
                        setAppointmentFormEnabled(false, tfApptID, tfApptPatientID, tfApptPRCLicenseNo, tfApptDate, tfApptTime, tfApptReason, tfApptNotes, cbApptStatus, btnAdd, btnEdit, btnDelete);
                        tfSearchPatient.setText(""); patientDetailsPanel.setVisible(false);
                    }
                }
            }
        });
        btnDelete.addActionListener(e -> {
            int row = appointmentTable.getSelectedRow();
            if (row >= 0) {
                int confirm = JOptionPane.showConfirmDialog(
                        panel,
                        "Are you sure to delete this appointment?",
                        "Delete Confirmation",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean dbSuccess = deleteAppointmentFromDB(tfApptID.getText());
                    if (dbSuccess) {
                        loadAppointmentsFromDB();
                        clearApptFields();
                        setAppointmentFormEnabled(false, tfApptID, tfApptPatientID, tfApptPRCLicenseNo, tfApptDate, tfApptTime, tfApptReason, tfApptNotes, cbApptStatus, btnAdd, btnEdit, btnDelete);
                        tfSearchPatient.setText(""); patientDetailsPanel.setVisible(false);
                    }
                }
            }
        });

        return panel;
    }

    private JPanel createManageApptPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_WHITE);

        JLabel titleLabel = new JLabel("Manage Appointments", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_HEADER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(18, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setPreferredSize(new Dimension(1000, 300));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel managePanel = new JPanel(new GridBagLayout());
        managePanel.setBackground(COLOR_WHITE);
        managePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT_DARK, 1),
                "Update Appointment",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                COLOR_ACCENT_DARK
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField tfEditDate = new JTextField(10);
        JTextField tfEditTime = new JTextField(8);
        JComboBox<String> cbEditStatus = new JComboBox<>(new String[]{"Confirmed", "Cancelled", "Rescheduled"});
        JTextField tfEditNotes = new JTextField(16);

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; managePanel.add(new JLabel("New Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; managePanel.add(tfEditDate, gbc);
        gbc.gridx = 2; managePanel.add(new JLabel("New Time (HH:mm):"), gbc);
        gbc.gridx = 3; managePanel.add(tfEditTime, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; managePanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; managePanel.add(cbEditStatus, gbc);
        gbc.gridx = 2; managePanel.add(new JLabel("Notes / Remarks:"), gbc);
        gbc.gridx = 3; managePanel.add(tfEditNotes, gbc);

        JButton btnUpdate = createActionBtnTeal("Update");
        managePanel.add(btnUpdate, new GridBagConstraints());

        panel.add(managePanel, BorderLayout.SOUTH);

        btnUpdate.addActionListener(e -> {
            int row = appointmentTable.getSelectedRow();
            if (row >= 0) {
                int confirm = JOptionPane.showConfirmDialog(
                        panel,
                        "Are you sure to update this appointment?",
                        "Update Confirmation",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    if (!tfEditDate.getText().trim().isEmpty()) appointmentTableModel.setValueAt(tfEditDate.getText(), row, 3);
                    if (!tfEditTime.getText().trim().isEmpty()) appointmentTableModel.setValueAt(tfEditTime.getText(), row, 4);
                    appointmentTableModel.setValueAt(cbEditStatus.getSelectedItem(), row, 6);
                    appointmentTableModel.setValueAt(tfEditNotes.getText(), row, 7);
                    tfEditDate.setText(""); tfEditTime.setText(""); tfEditNotes.setText("");
                }
            }
        });

        appointmentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = appointmentTable.getSelectedRow();
                    if (row >= 0) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < appointmentTable.getColumnCount(); i++) {
                            sb.append(appointmentTable.getColumnName(i)).append(": ")
                                    .append(appointmentTableModel.getValueAt(row, i).toString()).append("\n");
                        }
                        JOptionPane.showMessageDialog(panel, sb.toString(), "Appointment Details", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        return panel;
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
            public void mouseEntered(MouseEvent e) { btn.setBackground(COLOR_ACCENT_DARK); btn.setForeground(COLOR_WHITE);}
            public void mouseExited(MouseEvent e) { btn.setBackground(COLOR_ACCENT); btn.setForeground(COLOR_BLACK);}
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

    private void clearPatientFields() {
        tfPatientID.setText(""); tfFirstName.setText(""); tfMiddleName.setText(""); tfLastName.setText(""); tfDateofBirth.setText("");
        tfHeight.setText(""); tfWeight.setText(""); tfBloodType.setText(""); tfAllergies.setText("");
        tfEmail.setText(""); tfPhone.setText(""); tfNotes.setText(""); cbSex.setSelectedIndex(0);
    }

    private void clearApptFields() {
        tfApptID.setText(""); tfApptPatientID.setText(""); tfApptPRCLicenseNo.setText("");
        tfApptDate.setText(java.time.LocalDate.now().toString());
        tfApptTime.setText(java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        tfApptReason.setText(""); tfApptNotes.setText(""); cbApptStatus.setSelectedIndex(0);
    }

    private void setAppointmentFormEnabled(boolean enabled, JTextField tfApptID, JTextField tfApptPatientID, JTextField tfApptPRCLicenseNo,
                                           JTextField tfApptDate, JTextField tfApptTime, JTextField tfApptReason, JTextField tfApptNotes,
                                           JComboBox cbApptStatus, JButton btnAdd, JButton btnEdit, JButton btnDelete) {
        tfApptID.setEnabled(enabled);
        tfApptPRCLicenseNo.setEnabled(enabled);
        tfApptDate.setEnabled(enabled);
        tfApptTime.setEnabled(enabled);
        tfApptReason.setEnabled(enabled);
        tfApptNotes.setEnabled(enabled);
        cbApptStatus.setEnabled(enabled);
        btnAdd.setEnabled(enabled);
        btnEdit.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
    }

    //  DATABASE METHODS

    private boolean savePatientToDB() {
        String sql = "INSERT INTO patients (patient_id, first_name, middle_name, last_name, date_of_birth, sex, height, weight, blood_type, allergies, email, phone, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tfPatientID.getText());
            stmt.setString(2, tfFirstName.getText());
            stmt.setString(3, tfMiddleName.getText());
            stmt.setString(4, tfLastName.getText());
            stmt.setString(5, tfDateofBirth.getText());
            stmt.setString(6, cbSex.getSelectedItem().toString());
            stmt.setString(7, tfHeight.getText());
            stmt.setString(8, tfWeight.getText());
            stmt.setString(9, tfBloodType.getText());
            stmt.setString(10, tfAllergies.getText());
            stmt.setString(11, tfEmail.getText());
            stmt.setString(12, tfPhone.getText());
            stmt.setString(13, tfNotes.getText());
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patient saved to database!");
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error (save patient): " + ex.getMessage());
            return false;
        }
    }

    private boolean updatePatientInDB() {
        String sql = "UPDATE patients SET first_name=?, middle_name=?, last_name=?, date_of_birth=?, sex=?, height=?, weight=?, blood_type=?, allergies=?, email=?, phone=?, notes=? WHERE patient_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tfFirstName.getText());
            stmt.setString(2, tfMiddleName.getText());
            stmt.setString(3, tfLastName.getText());
            stmt.setString(4, tfDateofBirth.getText());
            stmt.setString(5, cbSex.getSelectedItem().toString());
            stmt.setString(6, tfHeight.getText());
            stmt.setString(7, tfWeight.getText());
            stmt.setString(8, tfBloodType.getText());
            stmt.setString(9, tfAllergies.getText());
            stmt.setString(10, tfEmail.getText());
            stmt.setString(11, tfPhone.getText());
            stmt.setString(12, tfNotes.getText());
            stmt.setString(13, tfPatientID.getText());
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patient updated in database!");
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error (update patient): " + ex.getMessage());
            return false;
        }
    }

    private boolean deletePatientFromDB(String patientId) {
        String sql = "DELETE FROM patients WHERE patient_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, patientId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patient deleted from database!");
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error (delete patient): " + ex.getMessage());
            return false;
        }
    }

    private boolean saveAppointmentToDB() {
        String sql = "INSERT INTO appointments (appointment_id, patient_id, prc_license_no, date, time, reason, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tfApptID.getText());
            stmt.setString(2, tfApptPatientID.getText());
            stmt.setString(3, tfApptPRCLicenseNo.getText());
            stmt.setString(4, tfApptDate.getText());
            stmt.setString(5, tfApptTime.getText());
            stmt.setString(6, tfApptReason.getText());
            stmt.setString(7, cbApptStatus.getSelectedItem().toString());
            stmt.setString(8, tfApptNotes.getText());
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Appointment saved to database!");
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error (save appointment): " + ex.getMessage());
            return false;
        }
    }

    private boolean updateAppointmentInDB() {
        String sql = "UPDATE appointments SET patient_id=?, prc_license_no=?, date=?, time=?, reason=?, status=?, notes=? WHERE appointment_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tfApptPatientID.getText());
            stmt.setString(2, tfApptPRCLicenseNo.getText());
            stmt.setString(3, tfApptDate.getText());
            stmt.setString(4, tfApptTime.getText());
            stmt.setString(5, tfApptReason.getText());
            stmt.setString(6, cbApptStatus.getSelectedItem().toString());
            stmt.setString(7, tfApptNotes.getText());
            stmt.setString(8, tfApptID.getText());
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Appointment updated in database!");
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error (update appointment): " + ex.getMessage());
            return false;
        }
    }

    private boolean deleteAppointmentFromDB(String apptId) {
        String sql = "DELETE FROM appointments WHERE appointment_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, apptId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Appointment deleted from database!");
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error (delete appointment): " + ex.getMessage());
            return false;
        }
    }

    //  LOAD DATA FROM DATABASE
    private void loadPatientsFromDB() {
        patientTableModel.setRowCount(0);
        String sql = "SELECT * FROM patients";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = {
                        rs.getString("patient_id"),
                        rs.getString("first_name"),
                        rs.getString("middle_name"),
                        rs.getString("last_name"),
                        rs.getString("date_of_birth"),
                        rs.getString("sex"),
                        rs.getString("height"),
                        rs.getString("weight"),
                        rs.getString("blood_type"),
                        rs.getString("allergies"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("notes")
                };
                patientTableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load patients: " + ex.getMessage());
        }
    }

    private void loadAppointmentsFromDB() {
        appointmentTableModel.setRowCount(0);
        String sql = "SELECT * FROM appointments";
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
                        rs.getString("status"),
                        rs.getString("notes")
                };
                appointmentTableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load appointments: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        String loggedInStaffName = "Alex Cruz"; // Replace with your login logic
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new ClinicStaffDashboard(loggedInStaffName).setVisible(true);
        });
    }
}