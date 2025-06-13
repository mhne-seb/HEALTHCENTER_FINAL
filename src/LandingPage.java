import javax.swing.*;
import java.awt.*;

public class LandingPage extends JFrame {

    private JLabel LoadingLabel;
    private JLabel LoadingValue;
    private JLabel Logo;
    private JLabel Tagline;
    private JProgressBar jProgressBar1;
    private JPanel loadingPanel;

    public LandingPage() {
        initComponents();
        startLoading();
    }

    private void startLoading() {
        new Thread(() -> {
            for (int i = 0; i <= 100; i++) {
                try {
                    Thread.sleep(40);
                    final int value = i;
                    SwingUtilities.invokeLater(() -> {
                        jProgressBar1.setValue(value);
                        LoadingValue.setText(value + " %");
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            SwingUtilities.invokeLater(() -> {
                new RoleSelectionPage().setVisible(true);
                dispose();
            });
        }).start();
    }

    private void initComponents() {
        loadingPanel = new JPanel();
        loadingPanel.setBackground(Color.WHITE);

        Logo = new JLabel();
        Tagline = new JLabel();
        jProgressBar1 = new JProgressBar();
        LoadingLabel = new JLabel();
        LoadingValue = new JLabel();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 768));
        setResizable(false);

        ImageIcon originalIcon = new ImageIcon("C:\\Users\\jerma\\IdeaProjects\\HospitalManagementSystem\\src\\medicarelogo.png");
        Image originalImage = originalIcon.getImage();

        int originalWidth = originalIcon.getIconWidth();
        int originalHeight = originalIcon.getIconHeight();

        int maxWidth = 700;
        int maxHeight = 400;

        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double scale = Math.min(widthRatio, heightRatio);

        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);

        Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        Logo.setIcon(new ImageIcon(scaledImage));
        Logo.setHorizontalAlignment(SwingConstants.CENTER);

        Tagline.setFont(new Font("Segoe UI", Font.BOLD, 24));
        Tagline.setForeground(new Color(0x0097A7));
        Tagline.setText("Health Center Management System");
        Tagline.setHorizontalAlignment(SwingConstants.CENTER);

        LoadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        LoadingLabel.setForeground(new Color(153, 153, 153));
        LoadingLabel.setText("Loading...");

        LoadingValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        LoadingValue.setForeground(new Color(153, 153, 153));
        LoadingValue.setText("0 %");

        GroupLayout layout = new GroupLayout(loadingPanel);
        loadingPanel.setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(146)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(Logo, GroupLayout.PREFERRED_SIZE, scaledWidth, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(Tagline, GroupLayout.PREFERRED_SIZE, 600, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                .addGroup(layout.createSequentialGroup()
                                                        .addComponent(LoadingLabel)
                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(LoadingValue))
                                                .addComponent(jProgressBar1, GroupLayout.PREFERRED_SIZE, 719, GroupLayout.PREFERRED_SIZE)))
                                .addGap(146))
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGap(80)
                        .addComponent(Logo, GroupLayout.PREFERRED_SIZE, scaledHeight, GroupLayout.PREFERRED_SIZE)
                        .addGap(40)
                        .addComponent(Tagline)
                        .addGap(60)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(LoadingLabel)
                                .addComponent(LoadingValue))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jProgressBar1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(50)
        );

        setContentPane(loadingPanel);
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LandingPage().setVisible(true));
    }
}
