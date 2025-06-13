import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class PharmacistLoginDialog extends JDialog {

    private final String url = "jdbc:mysql://localhost:3306/healthcenter?serverTimezone=UTC";
    private final String user = "root";
    private final String password = "@Mhaine1125";

    public PharmacistLoginDialog(JFrame parent) {
        super(parent, "Pharmacist Login", true);
        setSize(420, 390);
        setResizable(false);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Colors and fonts
        Color teal = new Color(0, 168, 176);
        Color accentText = new Color(0x085979);
        Font headingFont = new Font("Segoe UI", Font.BOLD, 22);
        Font labelFont = new Font("Segoe UI", Font.BOLD, 15);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(18, 0, 2, 0));

        // Icon
        ImageIcon logoIcon = new ImageIcon("C:\\Users\\jerma\\IdeaProjects\\HospitalManagementSystem\\src\\nurse.png");
        Image img = logoIcon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
        logoIcon = new ImageIcon(img);
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        JLabel titleLabel = new JLabel("Pharmacist Login");
        titleLabel.setFont(headingFont);
        titleLabel.setForeground(teal);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(logoLabel);
        topPanel.add(Box.createVerticalStrut(4));
        topPanel.add(titleLabel);
        add(topPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(246, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 9, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Custom UI for rounded and focus teal border fields (smaller)
        class ModernTextField extends JTextField {
            ModernTextField() {
                setFont(fieldFont);
                setBackground(Color.WHITE);
                setBorder(BorderFactory.createLineBorder(new Color(200, 224, 224), 1, true));
                setPreferredSize(new Dimension(145, 28));
                setMinimumSize(getPreferredSize());
                setMaximumSize(getPreferredSize());
                setMargin(new Insets(4, 7, 4, 7));
                setOpaque(true);
                addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) {
                        setBorder(BorderFactory.createLineBorder(teal, 2, true));
                    }
                    public void focusLost(FocusEvent e) {
                        setBorder(BorderFactory.createLineBorder(new Color(200, 224, 224), 1, true));
                    }
                });
            }
        }
        class ModernPasswordField extends JPasswordField {
            ModernPasswordField() {
                setFont(fieldFont);
                setBackground(Color.WHITE);
                setBorder(BorderFactory.createLineBorder(new Color(200, 224, 224), 1, true));
                setPreferredSize(new Dimension(145, 28));
                setMinimumSize(getPreferredSize());
                setMaximumSize(getPreferredSize());
                setMargin(new Insets(4, 7, 4, 7));
                setOpaque(true);
                addFocusListener(new FocusAdapter() {
                    public void focusGained(FocusEvent e) {
                        setBorder(BorderFactory.createLineBorder(teal, 2, true));
                    }
                    public void focusLost(FocusEvent e) {
                        setBorder(BorderFactory.createLineBorder(new Color(200, 224, 224), 1, true));
                    }
                });
            }
        }

        // Pharmacist ID row
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel idLabel = new JLabel("Pharmacist ID");
        idLabel.setFont(labelFont);
        idLabel.setForeground(accentText);
        idLabel.setIcon(new ImageIcon(new ImageIcon("src/id.png").getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH)));
        formPanel.add(idLabel, gbc);

        gbc.gridx = 1;
        ModernTextField idField = new ModernTextField();
        formPanel.add(idField, gbc);

        // Name row
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel nameLabel = new JLabel("Name");
        nameLabel.setFont(labelFont);
        nameLabel.setForeground(accentText);
        nameLabel.setIcon(new ImageIcon(new ImageIcon("src/name.png").getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH)));
        formPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        ModernTextField nameField = new ModernTextField();
        formPanel.add(nameField, gbc);

        // Password row
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(labelFont);
        passLabel.setForeground(accentText);
        passLabel.setIcon(new ImageIcon(new ImageIcon("src/password.png").getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH)));
        formPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        ModernPasswordField passField = new ModernPasswordField();
        formPanel.add(passField, gbc);

        // Center the formPanel
        JPanel centerWrap = new JPanel();
        centerWrap.setOpaque(false);
        centerWrap.setLayout(new BoxLayout(centerWrap, BoxLayout.Y_AXIS));
        centerWrap.add(formPanel);
        add(centerWrap, BorderLayout.CENTER);

        // Bottom Panel (links + buttons)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 26, 16, 26));

        JLabel forgotPassLabel = new JLabel("<html><u>Forgot Password?</u></html>");
        forgotPassLabel.setForeground(teal);
        forgotPassLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotPassLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        forgotPassLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(PharmacistLoginDialog.this,
                        "Password recovery instructions sent to your registered email.");
            }
        });
        bottomPanel.add(forgotPassLabel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton goBackBtn = new JButton("<html><u>Back to Role Select</u></html>");
        goBackBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        goBackBtn.setBackground(Color.WHITE);
        goBackBtn.setForeground(teal);
        goBackBtn.setFocusPainted(false);
        goBackBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        goBackBtn.setBorder(BorderFactory.createLineBorder(teal, 1, true));
        goBackBtn.addActionListener(e -> dispose());

        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginBtn.setBackground(teal);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.setBorder(BorderFactory.createEmptyBorder(6, 24, 6, 24));
        loginBtn.addActionListener(e -> {
            String pharmacistId = idField.getText().trim();
            String name = nameField.getText().trim();
            String pass = new String(passField.getPassword());

            if (pharmacistId.isEmpty() || name.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
            } else {
                try {
                    String pharmacistName = checkLoginFromDB(pharmacistId, name, pass);
                    if (pharmacistName != null) {
                        JOptionPane.showMessageDialog(this, "Login successful!");
                        SwingUtilities.invokeLater(() -> {
                            PharmacistDashboard dashboard = new PharmacistDashboard(pharmacistName);
                            dashboard.setVisible(true);
                        });
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid credentials!");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Database error occurred.");
                }
            }
        });

        btnPanel.add(goBackBtn);
        btnPanel.add(loginBtn);

        bottomPanel.add(btnPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Returns pharmacist name if valid, else null
    private String checkLoginFromDB(String pharmacistId, String name, String password) throws SQLException {
        String pharmacistName = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "MySQL Driver not found.");
            return null;
        }

        String sql = "SELECT name FROM pharmacist WHERE pharmacist_id = ? AND name = ? AND password = ?";

        try (Connection conn = DriverManager.getConnection(url, user, this.password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pharmacistId);
            pstmt.setString(2, name);
            pstmt.setString(3, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    pharmacistName = rs.getString("name");
                }
            }
        }
        return pharmacistName;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame dummyParent = new JFrame();
            dummyParent.setSize(300, 200);
            dummyParent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            dummyParent.setVisible(true);

            PharmacistLoginDialog dialog = new PharmacistLoginDialog(dummyParent);
            dialog.setVisible(true);
        });
    }
}