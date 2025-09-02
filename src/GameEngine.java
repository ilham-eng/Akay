public class GameEngine {
    private int score = 0;
    private int health = 100;

    public void showMainMenu() {
        System.out.println("=== Akay Game ===");
        System.out.println("1. Start Game");
        System.out.println("2. Settings");
        System.out.println("3. Exit");
        // Logic untuk menerima input user dan mengatur aksi selanjutnya
    }

    public void startGame() {
        System.out.println("Game Started!");
        score = 0;
        health = 100;
        // Logika utama game di sini
    }

    public void showSettings() {
        System.out.println("Settings menu");
        // Tampilkan dan ubah pengaturan dari config/settings.xml
    }
}