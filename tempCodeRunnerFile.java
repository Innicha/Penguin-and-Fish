
import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    public Main() {
        setTitle("Penguin and Fish");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel title = new JLabel(
            "PENGUIN AND FISH",
            SwingConstants.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 36));

        JButton startButton = new JButton("START");
        startButton.addActionListener(e ->
            JOptionPane.showMessageDialog(
                this, "Welcome to Penguin and Fish!"
            )
        );

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(title, BorderLayout.CENTER);
        panel.add(startButton, BorderLayout.SOUTH);

        add(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}
