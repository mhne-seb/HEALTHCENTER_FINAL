import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoleSelectionPage extends JFrame {

    public RoleSelectionPage() {
        setTitle("Role Selection");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(Color.WHITE);

        JLabel logo = new JLabel(
                new ImageIcon("C:\\Users\\jerma\\IdeaProjects\\HospitalManagementSystem\\src\\medicarelogo.png")
        );
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        String basePath = "C:\\Users\\jerma\\IdeaProjects\\HospitalManagementSystem\\src\\";
        buttonPanel.add(createRolePanel("Doctor", basePath + "doctor.png"));
        buttonPanel.add(createRolePanel("Pharmacist", basePath + "nurse.png"));
        buttonPanel.add(createRolePanel("Staff", basePath + "staff.png"));
        buttonPanel.add(createRolePanel("Admin", basePath + "admin.png"));

        add(logo, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }

    private JPanel createRolePanel(String role, String imagePath) {

        JPanel boxPanel = new JPanel();
        boxPanel.setPreferredSize(new Dimension(140, 140));
        boxPanel.setMaximumSize(new Dimension(140, 140));
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
        boxPanel.setBackground(Color.WHITE);
        boxPanel.setOpaque(true);
        boxPanel.setBorder(new AestheticBorder(18, new Color(0x0097A7)));

        JLabel imgLabel = new JLabel();
        imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imgLabel.setVerticalAlignment(SwingConstants.CENTER);
        imgLabel.setOpaque(false);
        ImageIcon icon = new ImageIcon(imagePath);
        Image img = icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
        imgLabel.setIcon(new ImageIcon(img));

        JLabel textLabel = new JLabel(role);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        textLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
        textLabel.setForeground(new Color(0x0097A7));
        textLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        boxPanel.add(Box.createVerticalGlue());
        boxPanel.add(imgLabel);
        boxPanel.add(textLabel);
        boxPanel.add(Box.createVerticalGlue());

        // Effects for hover/click
        boxPanel.addMouseListener(new MouseAdapter() {
            Color originalBg = boxPanel.getBackground();

            @Override
            public void mouseEntered(MouseEvent e) {
                boxPanel.setBackground(new Color(220, 247, 255));
                boxPanel.setBorder(new AestheticBorder(18, new Color(0x0097A7), 2.5f));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                boxPanel.setBackground(originalBg);
                boxPanel.setBorder(new AestheticBorder(18, new Color(0x0097A7), 1.5f));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                boxPanel.setBorder(new AestheticBorder(18, new Color(0x0097A7), 3.5f));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                boxPanel.setBorder(new AestheticBorder(18, new Color(0x0097A7), 2.5f));
                openLoginDialog(role);
            }
        });

        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setOpaque(false);
        outerPanel.setPreferredSize(new Dimension(160, 160));
        outerPanel.add(boxPanel);

        return outerPanel;
    }

    private void openLoginDialog(String role) {
        JDialog dialog = null;
        switch (role) {
            case "Doctor":
                dialog = new DoctorLoginDialog(this);
                break;
            case "Pharmacist":
                dialog = new PharmacistLoginDialog(this);
                break;
            case "Staff":
                dialog = new StaffLoginDialog(this);
                break;
            case "Admin":
                dialog = new AdminLoginDialog(this);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown role: " + role);
                return;
        }
        dialog.setVisible(true);
    }

    static class AestheticBorder extends AbstractBorder {
        private final int arc;
        private final Color color;
        private final float thickness;

        public AestheticBorder(int arc, Color color) {
            this(arc, color, 1.5f);
        }

        public AestheticBorder(int arc, Color color, float thickness) {
            this.arc = arc;
            this.color = color;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(0, 0, 0, 30));
            g2.setStroke(new BasicStroke(thickness + 2));
            g2.drawRoundRect(x + 2, y + 2, width - 5, height - 5, arc, arc);

            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, arc, arc);

            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 8, 8, 8);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = 8;
            return insets;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RoleSelectionPage().setVisible(true));
    }
}