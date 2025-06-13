import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.plaf.FontUIResource;
import javax.swing.border.TitledBorder;

public class PharmacistDashboard extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private DefaultTableModel prescriptionsTableModel;
    private JTable prescriptionsTable;

    private DefaultTableModel dispenseTableModel;
    private JTable dispenseTable;

    private JTextField tfInvMedID, tfInvMedName, tfInvBatch, tfInvQty, tfInvPrice;
    private DefaultTableModel inventoryTableModel;
    private JTable inventoryTable;

    private String pharmacistName; // Set from login

    // Colors
    private final Color COLOR_WHITE = new Color(0xFFFFFF);
    private final Color COLOR_ACCENT = new Color(0x00C9D8);
    private final Color COLOR_ACCENT_DARK = new Color(0x0097A7);
    private final Color COLOR_HEADER = new Color(0x0097A7);
    private final Color COLOR_LIGHT_TEAL = new Color(0xE7F7FA);
    private final Color COLOR_TOPBAR = new Color(0xF4F8FB);
    private final Color COLOR_BLACK = new Color(30, 30, 30);

    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/healthcenter?serverTimezone=UTC";
        String user = "root";
        String pass = "@Mhaine1125";
        return DriverManager.getConnection(url, user, pass);
    }

    private void loadIncomingPrescriptions() {
        prescriptionsTableModel.setRowCount(0);
        String sql =
                "SELECT " +
                        "  p.prescription_id, " +
                        "  p.patient_id, " +
                        "  p.prc_license_no, " +
                        "  GROUP_CONCAT(CONCAT(pm.medicine_name, ' (', pm.quantity, ')') SEPARATOR ', ') AS medicines, " +
                        "  p.date_prescribed, " +
                        "  p.status " +
                        "FROM prescriptions p " +
                        "JOIN prescription_medicines pm ON p.prescription_id = pm.prescription_id " +
                        "WHERE p.status = 'Pending' " +
                        "GROUP BY p.prescription_id, p.patient_id, p.prc_license_no, p.date_prescribed, p.status " +
                        "ORDER BY p.date_prescribed DESC";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                prescriptionsTableModel.addRow(new Object[]{
                        rs.getString("prescription_id"),
                        rs.getString("patient_id"),
                        rs.getString("prc_license_no"),
                        rs.getString("medicines"),
                        rs.getString("date_prescribed"),
                        rs.getString("status")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load prescriptions from database.\n" + ex.getMessage());
        }
    }

    private void markPrescriptionAsDispensed(String prescriptionsID) {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE prescriptions SET status = 'Dispensed' WHERE prescription_id = ?")) {
            ps.setString(1, prescriptionsID);
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to update prescription status in database.");
        }
    }

    private void loadInventory() {
        inventoryTableModel.setRowCount(0);
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT med_id, med_name, batch, quantity, price FROM inventory")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                inventoryTableModel.addRow(new Object[]{
                        rs.getString("med_id"),
                        rs.getString("med_name"),
                        rs.getString("batch"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load inventory: " + ex.getMessage());
        }
    }

    private void addMedicineToInventory() {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO inventory (med_id, med_name, batch, quantity, price) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, tfInvMedID.getText());
            ps.setString(2, tfInvMedName.getText());
            ps.setString(3, tfInvBatch.getText());
            ps.setInt(4, Integer.parseInt(tfInvQty.getText()));
            ps.setDouble(5, Double.parseDouble(tfInvPrice.getText()));
            ps.executeUpdate();
            loadInventory();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to add medicine: " + ex.getMessage());
        }
    }

    private void editMedicineInInventory() {
        int row = inventoryTable.getSelectedRow();
        if (row >= 0) {
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "UPDATE inventory SET med_name=?, batch=?, quantity=?, price=? WHERE med_id=?")) {
                String name = tfInvMedName.getText().trim();
                String batch = tfInvBatch.getText().trim();
                int qty;
                double price;
                try {
                    qty = Integer.parseInt(tfInvQty.getText().trim());
                    price = Double.parseDouble(tfInvPrice.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Quantity and Price must be valid numbers.");
                    return;
                }
                if (name.isEmpty() || batch.isEmpty() || tfInvMedID.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill all fields.");
                    return;
                }
                ps.setString(1, name);
                ps.setString(2, batch);
                ps.setInt(3, qty);
                ps.setDouble(4, price);
                ps.setString(5, tfInvMedID.getText().trim());
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    loadInventory();
                    JOptionPane.showMessageDialog(this, "Medicine updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "No matching medicine found to update.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to edit medicine: " + ex.getMessage());
            }
        }
    }

    private boolean deductInventory(String medID, String batch, int quantity) {
        medID = medID.trim();
        batch = batch.trim();
        try (Connection con = getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE inventory SET quantity = quantity - ? WHERE med_id = ? AND batch = ? AND quantity >= ?"
            );
            ps.setInt(1, quantity);
            ps.setString(2, medID);
            ps.setString(3, batch);
            ps.setInt(4, quantity);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                JOptionPane.showMessageDialog(this, "Insufficient stock or medicine not found: " + medID + " batch: " + batch);
                return false;
            }
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            return false;
        }
    }

    public PharmacistDashboard(String pharmacistName) {
        this.pharmacistName = pharmacistName;

        setTitle("Pharmacy Staff Dashboard");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setUIFont(new FontUIResource("Segoe UI", Font.PLAIN, 13));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_TOPBAR);
        topBar.setPreferredSize(new Dimension(getWidth(), 55));
        JLabel welcomeLabel = new JLabel("Welcome, " + this.pharmacistName + "!");
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

        JButton btnViewPrescriptions = createSidebarButton("View Prescriptions");
        JButton btnReceipt = createSidebarButton("Issue Receipt");
        JButton btnInventory = createSidebarButton("Manage Inventory");

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnViewPrescriptions); sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnReceipt); sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnInventory); sidebar.add(Box.createVerticalGlue());

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

        mainPanel.add(createViewPrescriptionsPanel(), "ViewPrescriptions");
        mainPanel.add(createReceiptPanel(), "Receipt");
        mainPanel.add(createInventoryPanel(), "Inventory");

        add(mainPanel, BorderLayout.CENTER);

        btnViewPrescriptions.addActionListener(e -> {
            loadIncomingPrescriptions();
            cardLayout.show(mainPanel, "ViewPrescriptions");
        });
        btnReceipt.addActionListener(e -> cardLayout.show(mainPanel, "Receipt"));
        btnInventory.addActionListener(e -> cardLayout.show(mainPanel, "Inventory"));
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                    "Logout Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) { dispose(); }
            new RoleSelectionPage().setVisible(true);
        });

        inventoryTableModel = new DefaultTableModel(new String[]{"Medicine ID", "Name", "Batch", "Quantity", "Unit Price"}, 0);

        cardLayout.show(mainPanel, "ViewPrescriptions");
        loadIncomingPrescriptions();
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

    private JPanel createViewPrescriptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_WHITE);

        JLabel titleLabel = new JLabel("Incoming Prescriptions", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_HEADER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(18, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {
                "Prescription ID", "Patient ID", "PRC License No", "Medicines", "Date Prescribed", "Status"
        };
        prescriptionsTableModel = new DefaultTableModel(columns, 0);
        prescriptionsTable = new JTable(prescriptionsTableModel);
        styleTableTeal(prescriptionsTable);

        prescriptionsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = prescriptionsTable.getSelectedRow();
                if (row >= 0) {
                    String status = prescriptionsTableModel.getValueAt(row, 5).toString();
                    if ("Pending".equals(status)) {
                        int confirm = JOptionPane.showConfirmDialog(
                                prescriptionsTable,
                                "Dispense this prescription?",
                                "Dispense Confirmation",
                                JOptionPane.YES_NO_OPTION
                        );
                        if (confirm == JOptionPane.YES_OPTION) {
                            String prescriptionID = prescriptionsTableModel.getValueAt(row, 0).toString();
                            String patientID = prescriptionsTableModel.getValueAt(row, 1).toString();
                            String prcLicenseNo = prescriptionsTableModel.getValueAt(row, 2).toString();
                            String medicines = prescriptionsTableModel.getValueAt(row, 3).toString();

                            // --- DEDUCT INVENTORY LOGIC ---
                            String[] meds = medicines.split(",");
                            for (String med : meds) {
                                med = med.trim();
                                int qtyStart = med.lastIndexOf('(');
                                int qtyEnd = med.lastIndexOf(')');
                                if (qtyStart != -1 && qtyEnd != -1) {
                                    String name = med.substring(0, qtyStart).trim();
                                    int qty = Integer.parseInt(med.substring(qtyStart + 1, qtyEnd).trim());
                                    for (int i = 0; i < inventoryTableModel.getRowCount(); i++) {
                                        if (inventoryTableModel.getValueAt(i, 1).toString().equalsIgnoreCase(name)) {
                                            String medID = inventoryTableModel.getValueAt(i, 0).toString();
                                            String batch = inventoryTableModel.getValueAt(i, 2).toString();
                                            deductInventory(medID, batch, qty);
                                            break;
                                        }
                                    }
                                }
                            }
                            loadInventory();

                            markPrescriptionAsDispensed(prescriptionID);

                            String[] medsParsed = medicines.split(",");
                            StringBuilder medNames = new StringBuilder();
                            StringBuilder medQtys = new StringBuilder();
                            for (String med : medsParsed) {
                                med = med.trim();
                                int qtyStart = med.lastIndexOf('(');
                                int qtyEnd = med.lastIndexOf(')');
                                String name = med;
                                String qty = "";
                                if (qtyStart != -1 && qtyEnd != -1 && qtyStart < qtyEnd) {
                                    name = med.substring(0, qtyStart).trim();
                                    qty = med.substring(qtyStart + 1, qtyEnd).trim();
                                }
                                if (medNames.length() > 0) medNames.append(", ");
                                medNames.append(name);

                                if (medQtys.length() > 0) medQtys.append(", ");
                                medQtys.append(qty);
                            }

                            Object[] dispRow = new Object[]{
                                    prescriptionID,
                                    patientID,
                                    prcLicenseNo,// Medicines (names only)
                                    pharmacistName,
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                                    "", // Batch #
                                    medQtys.toString(),
                                    "", // Notes
                                    "View Receipt"
                            };
                            if (dispenseTableModel != null) {
                                dispenseTableModel.addRow(dispRow);
                            }

                            loadIncomingPrescriptions();

                            JOptionPane.showMessageDialog(
                                    prescriptionsTable,
                                    "Prescription dispensed and moved to 'Issue Receipt'."
                            );
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                                prescriptionsTable,
                                "This prescription was already dispensed.",
                                "Info",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                }
            }
        });

        panel.add(new JScrollPane(prescriptionsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReceiptPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_WHITE);

        String[] dispenseCols = {
                "Prescription ID", "Patient ID", "PRC License No Name", "Medicines", "Dispensed By",
                "Date/Time", "Batch #", "Quantity", "Notes", "View Receipt"
        };
        dispenseTableModel = new DefaultTableModel(dispenseCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 9;
            }
        };
        dispenseTable = new JTable(dispenseTableModel);
        styleTableTeal(dispenseTable);
        dispenseTable.setRowHeight(22);

        JLabel titleLabel = new JLabel("Issue Receipt", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(COLOR_HEADER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(8, 18, 0, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JScrollPane dispenseScroll = new JScrollPane(dispenseTable);
        dispenseScroll.setPreferredSize(new Dimension(1000, 160));

        java.util.List<Object[]> issuedReceipts = new java.util.ArrayList<>();

        dispenseTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = dispenseTable.getSelectedRow();
                int col = dispenseTable.getSelectedColumn();
                Object[] found = null;
                for (Object[] receipt : issuedReceipts) {
                    if (receipt[2].equals(dispenseTableModel.getValueAt(row, 0)) && // Prescription ID
                            receipt[1].equals(dispenseTableModel.getValueAt(row, 1))) // Patient Name or ID
                    {
                        found = receipt;
                        break;
                    }
                }

                if (col == dispenseTableModel.getColumnCount() - 1 && found != null) {
                    showReceiptDetailsDialog(found);
                }
                else if (found == null && row != -1) {
                    showIssueReceiptDialog(row, issuedReceipts);
                }
            }
        });

        panel.add(dispenseScroll, BorderLayout.CENTER);
        return panel;
    }

    private void showIssueReceiptDialog(int dispRow, java.util.List<Object[]> issuedReceipts) {
        JDialog dialog = new JDialog(this, "Issue Receipt", true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(COLOR_WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT_DARK, 1),
                "Issue Receipt",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                COLOR_ACCENT_DARK
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField tfReceiptID = new JTextField(12);
        JTextField tfPatientName = new JTextField(12);
        tfPatientName.setText(dispenseTableModel.getValueAt(dispRow, 1).toString());
        tfPatientName.setEditable(false);

        JTextField tfPrescriptionID = new JTextField(12);
        tfPrescriptionID.setText(dispenseTableModel.getValueAt(dispRow, 0).toString());
        tfPrescriptionID.setEditable(false);

        JTextField tfDate = new JTextField(12);
        tfDate.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        tfDate.setEditable(false);

        JTextField tfTotal = new JTextField(12);
        JTextField tfIssuedBy = new JTextField(12);
        tfIssuedBy.setText(pharmacistName);
        tfIssuedBy.setEditable(false);

        JComboBox<String> cbPaymentMode = new JComboBox<>(new String[]{"Cash", "Card", "Online"});

        String medicinesStr = dispenseTableModel.getValueAt(dispRow, 3).toString();
        String[] medItems = medicinesStr.split(",");
        String quantityStr = dispenseTableModel.getValueAt(dispRow, 7).toString();
        String[] qtyItems = quantityStr.split(",");
        String[] medTableCols = {"Medicine Name", "Quantity"};
        Object[][] medTableData = new Object[medItems.length][2];
        for (int i = 0; i < medItems.length; i++) {
            medTableData[i][0] = medItems[i].trim();
            medTableData[i][1] = (i < qtyItems.length) ? qtyItems[i].trim() : "";
        }
        JTable medTable = new JTable(medTableData, medTableCols);
        medTable.setEnabled(false);
        medTable.setRowHeight(24);
        medTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        medTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        JScrollPane medTableScroll = new JScrollPane(medTable);
        medTableScroll.setPreferredSize(new Dimension(300, Math.min(120, 26 * medItems.length + 32)));

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Receipt ID:"), gbc);
        gbc.gridx = 1; formPanel.add(tfReceiptID, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Patient Name:"), gbc);
        gbc.gridx = 3; formPanel.add(tfPatientName, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Prescription ID:"), gbc);
        gbc.gridx = 1; formPanel.add(tfPrescriptionID, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Date of Payment:"), gbc);
        gbc.gridx = 3; formPanel.add(tfDate, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Medicines:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; formPanel.add(medTableScroll, gbc);
        gbc.gridwidth = 1;
        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Total Amount:"), gbc);
        gbc.gridx = 1; formPanel.add(tfTotal, gbc);
        gbc.gridx = 2; formPanel.add(new JLabel("Payment Mode:"), gbc);
        gbc.gridx = 3; formPanel.add(cbPaymentMode, gbc);
        y++;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Issued By:"), gbc);
        gbc.gridx = 1; formPanel.add(tfIssuedBy, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        btnPanel.setBackground(COLOR_WHITE);
        JButton btnIssue = createActionBtnTeal("Issue Receipt");
        btnPanel.add(btnIssue);

        btnIssue.addActionListener(e -> {
            Object[] row = new Object[]{
                    tfReceiptID.getText(),
                    tfPatientName.getText(),
                    tfPrescriptionID.getText(),
                    tfTotal.getText(),
                    tfDate.getText(),
                    cbPaymentMode.getSelectedItem(),
                    tfIssuedBy.getText()
            };
            issuedReceipts.add(row);
            JOptionPane.showMessageDialog(dialog, "Receipt issued!");
            showReceiptDetailsDialog(row);
            dialog.dispose();
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showReceiptDetailsDialog(Object[] receipt) {
        String[] cols = {"Receipt ID", "Patient Name", "Prescription ID", "Total Amount", "Date", "Payment Mode", "Issued By"};
        StringBuilder sb = new StringBuilder("<html><h3>Receipt Details</h3><table>");
        for (int i = 0; i < cols.length; i++) {
            sb.append("<tr><td><b>").append(cols[i]).append(":</b></td><td>")
                    .append(receipt[i]).append("</td></tr>");
        }
        sb.append("</table></html>");
        JOptionPane.showMessageDialog(this, sb.toString(), "View Receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_WHITE);

        JLabel titleLabel = new JLabel("Medicine Inventory", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(COLOR_HEADER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(18, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Medicine ID", "Name", "Batch", "Quantity", "Unit Price"};
        inventoryTableModel = new DefaultTableModel(columns, 0);
        inventoryTable = new JTable(inventoryTableModel);
        styleTableTeal(inventoryTable);

        JPanel invBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        invBtnPanel.setBackground(COLOR_WHITE);
        tfInvMedID = new JTextField(7); tfInvMedName = new JTextField(8); tfInvBatch = new JTextField(6); tfInvQty = new JTextField(4); tfInvPrice = new JTextField(6);
        JButton btnAdd = createActionBtnTeal("Add");
        JButton btnEdit = createActionBtnTeal("Edit");
        JButton btnDelete = createActionBtnTeal("Delete");
        invBtnPanel.add(new JLabel("ID:")); invBtnPanel.add(tfInvMedID);
        invBtnPanel.add(new JLabel("Name:")); invBtnPanel.add(tfInvMedName);
        invBtnPanel.add(new JLabel("Batch:")); invBtnPanel.add(tfInvBatch);
        invBtnPanel.add(new JLabel("Qty:")); invBtnPanel.add(tfInvQty);
        invBtnPanel.add(new JLabel("Price:")); invBtnPanel.add(tfInvPrice);
        invBtnPanel.add(btnAdd); invBtnPanel.add(btnEdit); invBtnPanel.add(btnDelete);

        panel.add(invBtnPanel, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> addMedicineToInventory());
        btnEdit.addActionListener(e -> editMedicineInInventory());
        btnDelete.addActionListener(e -> deleteMedicineFromInventory());
        inventoryTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = inventoryTable.getSelectedRow();
                if (row >= 0) {
                    tfInvMedID.setText(inventoryTableModel.getValueAt(row, 0).toString());
                    tfInvMedName.setText(inventoryTableModel.getValueAt(row, 1).toString());
                    tfInvBatch.setText(inventoryTableModel.getValueAt(row, 2).toString());
                    tfInvQty.setText(inventoryTableModel.getValueAt(row, 3).toString());
                    tfInvPrice.setText(inventoryTableModel.getValueAt(row, 4).toString());
                }
            }
        });

        panel.add(new JScrollPane(inventoryTable), BorderLayout.SOUTH);
        loadInventory();
        return panel;
    }

    private void deleteMedicineFromInventory() {}

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

    private void clearInventoryFields() {
        tfInvMedID.setText("");
        tfInvMedName.setText("");
        tfInvBatch.setText("");
        tfInvQty.setText("");
        tfInvPrice.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new PharmacistDashboard("Pharm. Maria Reyes").setVisible(true);
        });
    }
}