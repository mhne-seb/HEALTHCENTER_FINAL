import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AdminDashboardPage extends JFrame {
    private static String adminName;

    private DefaultTableModel userTableModel;
    private JTable userTable;

    private JComboBox<String> cbUserType;
    private JTextField tfID, tfName;
    private JPasswordField pfPassword;

    private final Color COLOR_WHITE = new Color(0xFFFFFF);
    private final Color COLOR_ACCENT = new Color(0x00C9D8);
    private final Color COLOR_ACCENT_DARK = new Color(0x0097A7);
    private final Color COLOR_HEADER = new Color(0x0097A7);
    private final Color COLOR_LIGHT_TEAL = new Color(0xE7F7FA);
    private final Color COLOR_TOPBAR = new Color(0xF4F8FB);
    private final Color COLOR_BLACK = new Color(30, 30, 30);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FIELD_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);

    public AdminDashboardPage(String adminName) {
        setTitle("Admin Dashboard");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setUIFont(new FontUIResource("Segoe UI", Font.PLAIN, 13));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_TOPBAR);
        topBar.setPreferredSize(new Dimension(getWidth(), 55));
        JLabel welcomeLabel = new JLabel("Admin Dashboard");
        welcomeLabel.setFont(TITLE_FONT);
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

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnManageUsers = createSidebarTabButton("Manage Users");
        btnManageUsers.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnManageUsers.setBackground(new Color(0xE7F7FA));
        sidebar.add(btnManageUsers);

        sidebar.add(Box.createVerticalGlue());

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
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                    "Logout Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) { dispose(); }
            new RoleSelectionPage().setVisible(true);

        });
        sidebar.add(Box.createRigidArea(new Dimension(0, 16)));
        sidebar.add(btnLogout);

        add(sidebar, BorderLayout.WEST);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_WHITE);
        mainPanel.add(createManageUserPanel(), BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JButton createSidebarTabButton(String text) {
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

    private JPanel createManageUserPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_WHITE);

        JLabel titleLabel = new JLabel("Manage User Accounts", SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(COLOR_HEADER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(22, 0, 12, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel addUserPanel = new JPanel(new GridBagLayout());
        addUserPanel.setBackground(COLOR_WHITE);
        addUserPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT_DARK, 1),
                "Add User",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                LABEL_FONT,
                COLOR_ACCENT_DARK
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 12, 7, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // User Type
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblType = new JLabel("User Type:");
        lblType.setFont(LABEL_FONT);
        addUserPanel.add(lblType, gbc);
        gbc.gridx = 1;
        cbUserType = new JComboBox<>(new String[]{"Doctor", "Clinic Staff", "Pharmacy Staff", "Admin"});
        cbUserType.setFont(FIELD_FONT);
        addUserPanel.add(cbUserType, gbc);

        gbc.gridx = 0; gbc.gridy++;
        JLabel lblID = new JLabel("ID Number:");
        lblID.setFont(LABEL_FONT);
        addUserPanel.add(lblID, gbc);
        gbc.gridx = 1;
        tfID = new JTextField(16); tfID.setFont(FIELD_FONT);
        addUserPanel.add(tfID, gbc);

        // Name
        gbc.gridx = 0; gbc.gridy++;
        JLabel lblName = new JLabel("Name:");
        lblName.setFont(LABEL_FONT);
        addUserPanel.add(lblName, gbc);
        gbc.gridx = 1;
        tfName = new JTextField(16); tfName.setFont(FIELD_FONT);
        addUserPanel.add(tfName, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy++;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(LABEL_FONT);
        addUserPanel.add(lblPass, gbc);
        gbc.gridx = 1;
        pfPassword = new JPasswordField(16); pfPassword.setFont(FIELD_FONT);
        addUserPanel.add(pfPassword, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        JButton btnAdd = createActionBtnTeal("Add User");
        addUserPanel.add(btnAdd, gbc);

        String[] columns = {"User Type", "ID Number", "Name", "Password"};
        userTableModel = new DefaultTableModel(columns, 0);
        userTable = new JTable(userTableModel);
        styleTableTeal(userTable);

        // Load all user data from database
        loadAllUsers();

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setPreferredSize(new Dimension(900, 180));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT_DARK, 1),
                "User Accounts Table",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                LABEL_FONT,
                COLOR_ACCENT_DARK
        ));
        scrollPane.getViewport().setBackground(COLOR_WHITE);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        btnPanel.setBackground(COLOR_WHITE);
        JButton btnEdit = createActionBtnTeal("Edit");
        JButton btnDelete = createActionBtnTeal("Delete");
        btnPanel.add(btnEdit); btnPanel.add(btnDelete);

        btnAdd.addActionListener(e -> {
            String type = cbUserType.getSelectedItem().toString();
            String id = tfID.getText();
            String name = tfName.getText();
            String pass = new String(pfPassword.getPassword());
            if (id.isEmpty() || name.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please fill all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean inserted = false;
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "";
                switch (type) {
                    case "Doctor":
                        sql = "INSERT INTO doctor (prc_license_no, name, password) VALUES (?, ?, ?)";
                        break;
                    case "Clinic Staff":
                        sql = "INSERT INTO staff (staff_id, name, password) VALUES (?, ?, ?)";
                        break;
                    case "Pharmacy Staff":
                        sql = "INSERT INTO pharmacist (pharmacist_id, name, password) VALUES (?, ?, ?)";
                        break;
                    case "Admin":
                        sql = "INSERT INTO admin (admin_id, name, password) VALUES (?, ?, ?)";
                        break;
                }
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, id);
                    stmt.setString(2, name);
                    stmt.setString(3, pass);
                    int rows = stmt.executeUpdate();
                    inserted = rows > 0;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            if (inserted) {
                userTableModel.addRow(new Object[]{type, id, name, pass});
                tfID.setText(""); tfName.setText(""); pfPassword.setText("");
                JOptionPane.showMessageDialog(panel, "User successfully added.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        userTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = userTable.getSelectedRow();
                if (row >= 0) {
                    cbUserType.setSelectedItem(userTableModel.getValueAt(row, 0).toString());
                    tfID.setText(userTableModel.getValueAt(row, 1).toString());
                    tfName.setText(userTableModel.getValueAt(row, 2).toString());
                    pfPassword.setText(userTableModel.getValueAt(row, 3).toString());
                }
            }
        });

        btnEdit.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row >= 0) {
                userTableModel.setValueAt(cbUserType.getSelectedItem().toString(), row, 0);
                userTableModel.setValueAt(tfID.getText(), row, 1);
                userTableModel.setValueAt(tfName.getText(), row, 2);
                userTableModel.setValueAt(new String(pfPassword.getPassword()), row, 3);
                tfID.setText(""); tfName.setText(""); pfPassword.setText("");
            }
        });

        btnDelete.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row >= 0) {
                int confirm = JOptionPane.showConfirmDialog(panel, "Delete this user?", "Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    userTableModel.removeRow(row);
                    tfID.setText(""); tfName.setText(""); pfPassword.setText("");
                }
            }
        });

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(COLOR_WHITE);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(addUserPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        centerPanel.add(scrollPane);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        centerPanel.add(btnPanel);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }


    private void loadAllUsers() {
        // Doctor
        try (Connection conn = DBConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT prc_license_no AS id, name, password FROM doctor")) {
            while (rs.next()) {
                userTableModel.addRow(new Object[]{"Doctor", rs.getString("id"), rs.getString("name"), rs.getString("password")});
            }
        } catch (Exception e) { e.printStackTrace(); }

        // Clinic Staff
        try (Connection conn = DBConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT staff_id AS id, name, password FROM staff")) {
            while (rs.next()) {
                userTableModel.addRow(new Object[]{"Clinic Staff", rs.getString("id"), rs.getString("name"), rs.getString("password")});
            }
        } catch (Exception e) { e.printStackTrace(); }

        // Pharmacy Staff
        try (Connection conn = DBConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT pharmacist_id AS id, name, password FROM pharmacist")) {
            while (rs.next()) {
                userTableModel.addRow(new Object[]{"Pharmacy Staff", rs.getString("id"), rs.getString("name"), rs.getString("password")});
            }
        } catch (Exception e) { e.printStackTrace(); }

        // Admin
        try (Connection conn = DBConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT admin_id AS id, name, password FROM admin")) {
            while (rs.next()) {
                userTableModel.addRow(new Object[]{"Admin", rs.getString("id"), rs.getString("name"), rs.getString("password")});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void styleTableTeal(JTable table) {
        table.setBackground(COLOR_WHITE);
        table.setForeground(COLOR_BLACK);
        table.setSelectionBackground(COLOR_ACCENT_DARK);
        table.setSelectionForeground(COLOR_WHITE);
        table.setGridColor(COLOR_ACCENT);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(22);

        JTableHeader header = table.getTableHeader();
        header.setBackground(COLOR_WHITE);
        header.setForeground(COLOR_ACCENT_DARK);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setForeground(COLOR_ACCENT_DARK);
        centerRenderer.setBackground(COLOR_WHITE);
        centerRenderer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        if (table.getParent() instanceof JViewport) {
            ((JViewport) table.getParent()).setBackground(COLOR_WHITE);
        }
    }

    private JButton createActionBtnTeal(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(COLOR_ACCENT);
        btn.setForeground(COLOR_BLACK);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 18, 5, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(COLOR_ACCENT_DARK); btn.setForeground(COLOR_WHITE);}
            public void mouseExited(MouseEvent e) { btn.setBackground(COLOR_ACCENT); btn.setForeground(COLOR_BLACK);}
        });
        return btn;
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new AdminDashboardPage(adminName).setVisible(true);
        });
    }
}