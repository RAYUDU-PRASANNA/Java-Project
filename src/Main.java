import util.AppTheme;
import ui.SplashScreen;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        AppTheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new SplashScreen().showSplash());
    }
}
