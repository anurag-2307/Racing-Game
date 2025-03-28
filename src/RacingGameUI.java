import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class RacingGameUI extends JFrame {
    private JTextField numPlayersField;
    private JTextArea gameLog;
    private JButton startButton, moveForwardButton, takePitStopButton, resetButton;
    private JPanel playerNamePanel, progressPanel;
    private RacingGame game;
    private Player currentPlayer;
    private ArrayList<JTextField> playerNameFields = new ArrayList<>();
    private ArrayList<JProgressBar> playerProgressBars = new ArrayList<>();

    public RacingGameUI() {
        showRulesDialog(); // Display rules at the start
        setupUIComponents();
        setVisible(true);
    }

    private void showRulesDialog() {
        String rules = "Welcome to the Racing Game!\n\n" +
                "Game Rules:\n" +
                "1. Finish Line is at Position 50.\n" +
                "2. Each player takes turns to either move forward or take a pit stop.\n" +
                "3. Moving forward advances the player by 1-6 steps.\n" +
                "4. Taking a pit stop resets the wear-and-tear counter and provides a boost.\n" +
                "5. If a player continues without a pit stop, there's a 30% chance of a penalty after the 4th turn since the last pitstop which grows by 5% further.\n" +
                "6. The first player to reach the finish line wins!\n\n" +
                "Enjoy the race and may the best player win!";
        JOptionPane.showMessageDialog(this, rules, "Game Rules", JOptionPane.INFORMATION_MESSAGE);
    }

    private void setupUIComponents() {
        setTitle("Racing Game");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel for player input
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());
        inputPanel.add(new JLabel("Number of Players: "));
        numPlayersField = new JTextField(5);
        numPlayersField.setFont(new Font("Arial", Font.PLAIN, 16)); // Increased font size for the number input
        inputPanel.add(numPlayersField);
        startButton = new JButton("Enter Player Names");
        resetButton = new JButton("Reset Game");
        inputPanel.add(startButton);
        inputPanel.add(resetButton);
        add(inputPanel, BorderLayout.NORTH);

        // Player name input panel
        playerNamePanel = new JPanel();
        playerNamePanel.setLayout(new GridLayout(0, 1));
        add(playerNamePanel, BorderLayout.WEST);

        // Center panel for game log and progress bar
        gameLog = new JTextArea(15, 40);
        gameLog.setEditable(false);
        gameLog.setFont(new Font("Monospaced", Font.PLAIN, 16)); // Increased font size for the game log
        JScrollPane scrollPane = new JScrollPane(gameLog);
        add(scrollPane, BorderLayout.CENTER);

        // Panel for player progress bars
        progressPanel = new JPanel();
        progressPanel.setLayout(new GridLayout(0, 1));
        add(progressPanel, BorderLayout.EAST);

        // Bottom panel for actions
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new FlowLayout());
        moveForwardButton = new JButton("Move Forward");
        takePitStopButton = new JButton("Take Pit Stop");

        // Style action buttons with larger font size
        moveForwardButton.setBackground(Color.GREEN);
        moveForwardButton.setForeground(Color.WHITE);
        moveForwardButton.setFont(new Font("Arial", Font.PLAIN, 18));
        takePitStopButton.setBackground(Color.BLUE);
        takePitStopButton.setForeground(Color.WHITE);
        takePitStopButton.setFont(new Font("Arial", Font.PLAIN, 18));
        resetButton.setBackground(Color.RED);
        resetButton.setForeground(Color.WHITE);
        resetButton.setFont(new Font("Arial", Font.PLAIN, 18));

        actionPanel.add(moveForwardButton);
        actionPanel.add(takePitStopButton);
        add(actionPanel, BorderLayout.SOUTH);

        // Disable action buttons initially
        moveForwardButton.setEnabled(false);
        takePitStopButton.setEnabled(false);

        // Set up action listeners
        startButton.addActionListener(new StartButtonListener());
        moveForwardButton.addActionListener(new MoveForwardButtonListener());
        takePitStopButton.addActionListener(new TakePitStopButtonListener());
        resetButton.addActionListener(e -> resetGame());
    }

    // Listener for Start Game button
    private class StartButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Initialize the game object here
            game = new RacingGame(); // Add this line to initialize the game object

            playerNamePanel.removeAll();
            progressPanel.removeAll();
            playerNameFields.clear();
            playerProgressBars.clear();

            int numPlayers;
            try {
                numPlayers = Integer.parseInt(numPlayersField.getText());
            } catch (NumberFormatException ex) {
                gameLog.append("Invalid number of players. Defaulting to 2 players.\n");
                numPlayers = 2;
            }

            // Prompt the user to enter each player's name individually
            for (int i = 1; i <= numPlayers; i++) {
                String playerName = JOptionPane.showInputDialog("Enter name for Player " + i + ": ");
                JTextField playerNameField = new JTextField(playerName != null ? playerName : "Player " + i);
                playerNameField.setFont(new Font("Arial", Font.PLAIN, 16)); // Increased font size for player name field
                playerNameFields.add(playerNameField);
                playerNamePanel.add(new JLabel("Player " + i + " Name: "));
                playerNamePanel.add(playerNameField);

                // Set up the player's progress bar
                JProgressBar progressBar = new JProgressBar(0, RacingGame.FINISH_LINE);
                progressBar.setStringPainted(true);
                progressBar.setForeground(Color.BLACK);
                playerProgressBars.add(progressBar);

                // Create a label with player's name to display next to progress bar
                JLabel playerLabel = new JLabel(playerName);
                playerLabel.setFont(new Font("Arial", Font.PLAIN, 16)); // Optional: increase font size for player's name label

                // Add the label and progress bar to the progress panel
                progressPanel.add(new JLabel("Progress: "));
                progressPanel.add(playerLabel); // Add player name label
                progressPanel.add(progressBar); // Add progress bar
            }

            // After all players are set up, add them to the game
            for (JTextField playerNameField : playerNameFields) {
                game.addPlayer(playerNameField.getText());
            }

            // Get the first player and log it
            currentPlayer = game.playersQueue.nextPlayer();

            gameLog.append("Game started with " + numPlayers + " players.\n");
            gameLog.append("First turn: " + currentPlayer.name + "\n");

            // Enable action buttons
            moveForwardButton.setEnabled(true);
            takePitStopButton.setEnabled(true);

            revalidate();
            repaint();
        }
    }
    // Listener for Move Forward button
    private class MoveForwardButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (game == null || currentPlayer == null) {
                gameLog.append("Start the game first.\n");
                return;
            }

            // Check if player is unable to move due to vehicle failure
            if (currentPlayer.isVehicleFailed()) {
                JOptionPane.showMessageDialog(RacingGameUI.this, currentPlayer.name + " cannot move forward due to vehicle failure.", "Vehicle Failure", JOptionPane.WARNING_MESSAGE);
                gameLog.append(currentPlayer.name + " cannot move forward due to vehicle failure. Forced Pit stop taken\n");
                // Reset pit stop counter and consecutive turns
                currentPlayer.ForcedPitStop();  // Take a pit stop to reset failure
                currentPlayer = game.playersQueue.nextPlayer();
                gameLog.append("Next turn: " + currentPlayer.name + "\n");
                return;
            }

            // If the player has a boost, display the boost value
            if (currentPlayer.hasBoost) {
                JOptionPane.showMessageDialog(RacingGameUI.this, currentPlayer.name + " has received a boost of " + currentPlayer.boostAmount + " positions!", currentPlayer.name + " boosted!", JOptionPane.INFORMATION_MESSAGE);
            }

            // Move the player forward
            currentPlayer.moveForward();

            // Log the current position
            gameLog.append(currentPlayer.name + " moves to position " + currentPlayer.position + "\n");

            // Update progress bar
            int currentPlayerIndex = game.playersQueue.getPlayers().indexOf(currentPlayer);
            JProgressBar currentProgressBar = playerProgressBars.get(currentPlayerIndex);
            currentProgressBar.setValue(currentPlayer.position);

            // Check if the player has won
            if (currentPlayer.hasWon(RacingGame.FINISH_LINE)) {
                JOptionPane.showMessageDialog(RacingGameUI.this, currentPlayer.name + " has won the race!", "Game Over!", JOptionPane.WARNING_MESSAGE   );
                gameLog.append("Congratulations! " + currentPlayer.name + " has won the race!\n");
                moveForwardButton.setEnabled(false);
                takePitStopButton.setEnabled(false);
            } else {
                currentPlayer = game.playersQueue.nextPlayer();
                gameLog.append("Next turn: " + currentPlayer.name + "\n");
            }
        }
    }


    // Listener for Take Pit Stop button
    private class TakePitStopButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (game == null || currentPlayer == null) {
                gameLog.append("Start the game first.\n");
                return;
            }

            currentPlayer.takePitStop();
            gameLog.append(currentPlayer.name + " takes a pit stop. Total pit stops: " + currentPlayer.pitStops + "\n");

            currentPlayer = game.playersQueue.nextPlayer();
            gameLog.append("Next turn: " + currentPlayer.name + "\n");
        }
    }

    private void resetGame() {
        game = null;
        currentPlayer = null;
        numPlayersField.setText("");
        gameLog.setText("");
        playerNamePanel.removeAll();
        progressPanel.removeAll();
        playerNameFields.clear();
        playerProgressBars.clear();
        moveForwardButton.setEnabled(false);
        takePitStopButton.setEnabled(false);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        new RacingGameUI();
    }
}
