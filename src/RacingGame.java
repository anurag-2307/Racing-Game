import java.util.*;

// Class representing a player
class Player {
    String name;
    int position;
    int pitStops;
    boolean vehicleFailed;
    int consecutiveTurnsWithoutPitStop;
    int failureProbability;
    boolean hasBoost;
    int boostAmount;  // Store the amount of boost to apply

    public Player(String name) {
        this.name = name;
        this.position = 0;
        this.pitStops = 0;
        this.vehicleFailed = false;
        this.consecutiveTurnsWithoutPitStop = 0;
        this.failureProbability = 30;
        this.hasBoost = false;
        this.boostAmount = 0;
    }

    public void checkVehicleFailure() {
        if (consecutiveTurnsWithoutPitStop >= 4) {
            failureProbability = 30 + (consecutiveTurnsWithoutPitStop - 4) * 5;
            if (Math.random() * 100 < failureProbability) {
                vehicleFailed = true;
                pitStops = 0;
                consecutiveTurnsWithoutPitStop = 0;
            }
        }
    }

    public void moveForward() {
        if (hasBoost) {
            // Apply the boost only once, then reset
            position += boostAmount;
            hasBoost = false;  // Reset boost after use
            boostAmount = 0;  // Clear boost amount
            consecutiveTurnsWithoutPitStop++;
        } else if (!vehicleFailed) {
            position += (int) (Math.random() * 6) + 1;  // Regular movement
            consecutiveTurnsWithoutPitStop++;
            checkVehicleFailure();
        }
    }

    public void takePitStop() {
        vehicleFailed = false;
        pitStops++;
        consecutiveTurnsWithoutPitStop = 0;
        failureProbability = 30;
        hasBoost = true;  // Grant boost after pit stop
        boostAmount = (int) (1.5* Math.random() * 6) + 1;  // Random boost between 1 and 9
    }
    public void ForcedPitStop() {
        vehicleFailed = false;
        pitStops++;
        consecutiveTurnsWithoutPitStop = 0;
        failureProbability = 30;
    }

    public boolean hasWon(int finishLine) {
        return position >= finishLine;
    }

    public boolean isVehicleFailed() {
        return vehicleFailed;
    }
}


// Class to manage players in a circular queue
class CircularQueue {
    private ArrayList<Player> players = new ArrayList<>();
    private int currentIndex = 0;

    public void addPlayer(Player player) {
        players.add(player);
    }

    public Player nextPlayer() {
        Player player = players.get(currentIndex);
        currentIndex = (currentIndex + 1) % players.size(); // Move to the next player in a circular manner
        return player;
    }

    public List<Player> getPlayers() {
        return players;
    }
}

// Main Game class
public class RacingGame {
    public static final int FINISH_LINE = 100; // Finish line position
    public CircularQueue playersQueue = new CircularQueue();

    public void addPlayer(String name) {
        playersQueue.addPlayer(new Player(name));
    }
}
