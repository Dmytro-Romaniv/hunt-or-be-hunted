package oop.hunt.part2;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * The main class that is reponsible for the game loop and the GUI
 */
public class App extends Application {
    private static final Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    public static final int GRID_SIZE = (int) screenBounds.getHeight() / 22 + 1;
    public static final int CELL_SIZE = (int) ((double) GRID_SIZE / 2.5);

    private List<Predator> predators = new ArrayList<>();
    private List<Prey> preys = new ArrayList<>();
    private List<Path> paths= new ArrayList<>();
    private List<Water> waters = new ArrayList<>();
    private List<Plant> plants = new ArrayList<>();
    private List<Hideout> hideouts = new ArrayList<>();

    private Canvas canvas = new Canvas(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);
    private GraphicsContext gc = canvas.getGraphicsContext2D();
    private Thingamajig selectedObject = null;
    private Thingamajig target = new Thingamajig(-1, -1);
    private VBox infoLayout;
    private static MediaPlayer mediaPlayer;
    private boolean pointing;

    /**
     * The main method that runs the simulation.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * The start method that sets up and runs the simulation.
     * @param simulationStage the stage to display the simulation on.
     */
    @Override
    public void start(Stage simulationStage) {
        generateRandomEcosystem(10, 5);
        connectPaths(true);
        playMusic();
        captureMouse();
        buildGUI(simulationStage); // sorry

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateGUI(simulationStage);
            }
        };
        timer.start();
    }

    /**
     * Generates random pairs of x and y coordinates within the grid size.
     * @param numPairs the number of pairs to generate.
     * @return a 2D array of the generated pairs.
     */
    private int[][] generateRandomPairs(int numPairs) {
        int[][] randomPairs = new int[numPairs][2];
        for (int i = 0; i < numPairs; i++) {
            int x = randInt(0, GRID_SIZE - 1);
            int y = randInt(0, GRID_SIZE - 1);
            for (int j = 0; j < i; j++) {
                if (randomPairs[j][0] == x && randomPairs[j][1] == y) {
                    i--;
                    break;
                }
            }
            randomPairs[i][0] = x;
            randomPairs[i][1] = y;
        }
        return randomPairs;
    }

    /**
     * Generates a random name of the specified length.
     * @param length the length of the name to generate.
     * @return the generated name.
     */
    private String generateName(int length) {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (Math.random() < 0.5) {
                name.append((char) (Math.random() * 26 + 97));
            } else {
                name.append((char) (Math.random() * 5 + 97));
            }
        }
        name = new StringBuilder(name.substring(0, 1).toUpperCase() + name.substring(1));
        return name.toString();
    }

    /**
     * Generate a random integer number between min and max.
     * @param min
     * @param max
     * @return the generated integer.
     */
    public static int randInt(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    /**
     * Fill the map with resources and hideouts.
     * @param numOfResources
     * @param numOfHideouts
     */
    private void generateRandomEcosystem(int numOfResources, int numOfHideouts) {
        int[][] randomPairs = generateRandomPairs(numOfResources*2+numOfHideouts);

        // generate random water sources
        for (int i = 0; i < numOfResources; i++) {
            waters.add(new Water(randomPairs[i][0], randomPairs[i][1], generateName(randInt(3, 6)), randInt(1, 3), randInt(1, 5)));
        }

        // generate random plants
        for (int i = 0; i < numOfResources; i++) {
            plants.add(new Plant(randomPairs[i + 10][0], randomPairs[i + 10][1], generateName(randInt(3, 6)), randInt(1, 3), randInt(1, 5)));
        }

        // generate random hideouts
        for (int i = 0; i < numOfHideouts; i++) {
            hideouts.add(new Hideout(randomPairs[i + 20][0], randomPairs[i + 20][1], generateName(randInt(7, 10)), randInt(1, 5), preys));
        }
    }

    /**
     * Connects the paths in the ecosystem.
     * @param all whether or not to connect the paths.
     */
    private void connectPaths(boolean all) {
        for (Hideout h : hideouts) {
            List<Water> watersTemp = waters;
            List<Plant> plantsTemp = plants;
            if (!all) {
                int id = hideouts.indexOf(h) * 2;
                watersTemp = waters.subList(id, id + 2);
                plantsTemp = plants.subList(id, id + 2);
            }
            for (Water w : watersTemp) {
                paths.addAll(Path.generatePath(h.getX(), h.getY(), w.getX(), w.getY()));
            }
            for (Plant p : plantsTemp) {
                paths.addAll(Path.generatePath(h.getX(), h.getY(), p.getX(), p.getY()));
            }
        }
    }

    /**
     * Plays the background music of the simulation.
     */
    private void playMusic() {
        // let's get some nice music in here
        Media song = null;
        try {
            song = new Media(getClass().getResource("/audio/track"+randInt(1,4)+".mp3").toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mediaPlayer = new MediaPlayer(song);
        mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
    }

    /**
     * Captures the mouse's movement and clicks on the canvas.
     */
    private void captureMouse() {
        canvas.setOnMouseClicked(e -> {
            pointing = true;
            // get the x and y coordinates of the mouse click
            int x = (int) e.getX() / CELL_SIZE;
            int y = (int) e.getY() / CELL_SIZE;
            // find the water source at the clicked location
            for (Water water : waters) {
                if (water.getX() == x && water.getY() == y) {
                    selectedObject = water;
                    break;
                }
            }
            // find the plant at the clicked location
            for (Plant plant : plants) {
                if (plant.getX() == x && plant.getY() == y) {
                    selectedObject = plant;
                    break;
                }
            }
            // find the prey at the clicked location
            for (Prey prey : preys) {
                if (prey.getX() == x && prey.getY() == y) {
                    selectedObject = prey;
                    pointing = false;
                    break;
                }
            }
            // find the predator at the clicked location
            for (Predator predator : predators) {
                if (predator.getX() == x && predator.getY() == y) {
                    selectedObject = predator;
                    break;
                }
            }
            // find the hideout at the clicked location
            for (Hideout hideout : hideouts) {
                if (hideout.getX() == x && hideout.getY() == y) {
                    selectedObject = hideout;
                    pointing = true;
                    break;
                }
            }
            // select a new route for the prey
            if (pointing) {
                target.setX(x);
                target.setY(y);
            }
        });
    }

    /**
     * Just pretend this is an FXML file.
     * @param simulationStage
     */
    private void buildGUI(Stage simulationStage) {
        // create the controls for adding predators and prey
        Button addPredatorButton = new Button("Add Predator");
        TextField predatorNameField = new TextField();
        predatorNameField.setPromptText("Predator Name");
        addPredatorButton.setOnAction(e -> {
            String predatorName = predatorNameField.getText();
            if (predatorName.equals("")) {
                predatorName = generateName(randInt(4, 7));
            }
            predatorNameField.setText("");
            Predator predator = new Predator(randInt(0, GRID_SIZE-1), randInt(0, GRID_SIZE-1), predatorName, preys);
            predators.add(predator);
            System.out.println("Added predator " + predatorName);
            new Thread(predator).start();
            new AudioClip(getClass().getResource("/audio/predator.mp3").toExternalForm()).play();
        });

        Button addPreyButton = new Button("Add Prey");
        TextField preyNameField = new TextField();
        preyNameField.setPromptText("Prey Name");
        addPreyButton.setOnAction(e -> {
            int randomHideout = randInt(0, hideouts.size()-1);
            Hideout hideout = hideouts.get(randomHideout);
            String preyName = preyNameField.getText();
            if (preyName.equals("")) {
                preyName = generateName(randInt(4, 7));
            }
            preyNameField.setText("");
            Prey prey = new Prey(hideout.getX(), hideout.getY(), preyName, paths, waters, plants, hideouts, predators);
            preys.add(prey);
            System.out.println("Added prey " + preyName + " at " + hideout.getX() + ", " + hideout.getY());
            new Thread(prey).start();
            new AudioClip(getClass().getResource("/audio/prey.mp3").toExternalForm()).play();
        });

        // Create the layout for the simulation
        BorderPane root = new BorderPane();
        root.setCenter(canvas);

        // Create the first stage
        Scene scene = new Scene(root);
        simulationStage.setScene(scene);
        simulationStage.setOnCloseRequest(e -> System.exit(0));
        simulationStage.getIcons().add(new Image("icon.png"));
        simulationStage.setResizable(false);

        // Create the second stage near the first one
        Stage menuStage = new Stage();
        menuStage.setTitle("Menu");
        menuStage.getIcons().add(new Image("icon.png"));

        // Add the text fields and buttons to a VBox layout container
        VBox layout = new VBox(10); // 10px vertical spacing
        layout.setPadding(new Insets(0, 10, 0, 10));
        layout.getChildren().addAll(preyNameField, addPreyButton, predatorNameField, addPredatorButton);

        // Add the VBox to the scene and show the stage
        menuStage.setScene(new Scene(layout, 200, 150));
        menuStage.setResizable(false);
        menuStage.setOnCloseRequest(e -> System.exit(0));

        // Create the third stage near below the second one
        Stage infoStage = new Stage();
        infoStage.setTitle("Info");
        infoStage.getIcons().add(new Image("icon.png"));

        // Add the text fields and buttons to a VBox layout container
        infoLayout = new VBox(10); // 10px vertical spacing
        infoLayout.setPadding(new Insets(10));
        infoLayout.setSpacing(8);
        infoLayout.getChildren().addAll(new Label("Please select an object :)"));

        // Add the VBox to the scene and show the stage
        infoStage.setScene(new Scene(infoLayout, 200, 250));
        infoStage.setResizable(false);
        infoStage.setOnCloseRequest(e -> System.exit(0));

        // Add stage with Kill button and Change route button
        Stage interactStage = new Stage();
        interactStage.setTitle("Interact");
        interactStage.getIcons().add(new Image("icon.png"));

        Button killButton = new Button("Kill the selected animal");
        killButton.setMaxWidth(Double.MAX_VALUE);
        killButton.setOnAction(e -> {
            if (selectedObject != null) {
                try {
                    Creatura c = (Creatura) selectedObject;
                    c.setHealth(0);
                    new AudioClip(getClass().getResource("/audio/damage.mp3").toExternalForm()).play();
                }
                catch (Exception ex) {
                    System.out.println("You can't kill this object!");
                }
            }
        });

        Button rerouteButton = new Button("Reroute the selected prey");
        rerouteButton.setMaxWidth(Double.MAX_VALUE);
        rerouteButton.setOnAction(e -> {
            if (selectedObject != null && target.getX() != -1 && target.getY() != -1) {
                try {
                    Prey prey = (Prey) selectedObject;
                    prey.reroute(target.getX(), target.getY());
                    System.out.println("Rerouted prey to " + target.getX() + ", " + target.getY());
                }
                catch (Exception ex) {
                    System.out.println("You can't reroute this object!");
                }
            }
        });

        // Add the VBox to the scene and show the stage
        VBox interactLayout = new VBox(20);
        interactLayout.setPadding(new Insets(10));
        interactLayout.getChildren().addAll(killButton, rerouteButton);
        interactStage.setScene(new Scene(interactLayout, 200, 100));
        interactStage.setResizable(false);
        interactStage.setOnCloseRequest(e -> System.exit(0));

        // Add a start screen with start button and start.png as background
        Stage startStage = new Stage();
        startStage.setTitle("Start");
        startStage.getIcons().add(new Image("icon.png"));

        Text welcomeText = new Text("Welcome to Zombie Survival");
        welcomeText.setFont(Font.font("Verdana", FontWeight.BOLD, 50));
        welcomeText.setFill(Color.WHITE);
        Text creditText = new Text("Created by: Dmytro Romaniv");
        creditText.setFont(Font.font("Verdana", FontWeight.BOLD, 40));
        creditText.setFill(Color.WHITE);

        Button startButton = new Button("START");
        startButton.setOnAction(e -> {
            new AudioClip(getClass().getResource("/audio/xp.mp3").toExternalForm()).play();
            startStage.close();
            simulationStage.show();
            menuStage.show();
            menuStage.setX(simulationStage.getX() + simulationStage.getWidth());
            menuStage.setY(simulationStage.getY());
            infoStage.show();
            infoStage.setX(menuStage.getX());
            infoStage.setY(menuStage.getY() + menuStage.getHeight());
            interactStage.show();
            interactStage.setX(infoStage.getX());
            interactStage.setY(infoStage.getY() + infoStage.getHeight());
            mediaPlayer.play();
        });

        ImageView imageView = new ImageView(new Image("start.png"));

        // show the button in the center of the screen in front of the background image and make the button BIGGER
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(imageView, welcomeText, startButton, creditText);
        StackPane.setAlignment(welcomeText, Pos.TOP_CENTER);
        StackPane.setAlignment(startButton, Pos.CENTER);
        StackPane.setAlignment(creditText, Pos.BOTTOM_CENTER);
        startButton.setFont(Font.font("Verdana", 50));
        startButton.setMaxSize(400, 200);

        startStage.setScene(new Scene(stackPane, (int) screenBounds.getWidth() / 2, (int) screenBounds.getHeight() / 2));
        startStage.setResizable(false);
        startStage.setOnCloseRequest(e -> System.exit(0));
        startStage.show();
    }

    /**
     * Clears the GUI and draws everything over and over again.
     * @param simulationStage
     */
    private void updateGUI(Stage simulationStage) {
        simulationStage.setTitle("Villagers " + preys.size() + " - " + predators.size() + " Zombies");
        // clear the canvas
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        // fill the background
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // draw the paths with transparent brown
        gc.setFill(Color.DARKORANGE.deriveColor(0, 1, 1, 0.5));
        for (Path path : paths) {
            gc.fillRect(path.getX() * CELL_SIZE, path.getY() * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
        // draw the grid
        gc.setStroke(Color.LIGHTGRAY);
        for (int i = 1; i < GRID_SIZE; i++) {
            gc.strokeLine(i * CELL_SIZE, 0, i * CELL_SIZE, canvas.getHeight());
            gc.strokeLine(0, i * CELL_SIZE, canvas.getWidth(), i * CELL_SIZE);
        }
        // draw the water sources using the water image
        gc.setFill(Color.BLUE.deriveColor(0, 1, 1, 0.3));
        for (Water water : waters) {
            if (water.getCurrent_animals() > 0) {
                gc.fillOval((water.getX() * CELL_SIZE) - 10, (water.getY() * CELL_SIZE) - 10, CELL_SIZE + 20, CELL_SIZE + 20);
            }
            gc.drawImage(new Image("water.png"), (water.getX() * CELL_SIZE) - 2, (water.getY() * CELL_SIZE) - 2, CELL_SIZE + 4, CELL_SIZE + 4);
        }
        // draw the plants using the plant image
        gc.setFill(Color.GREEN.deriveColor(0, 1, 1, 0.3));
        for (Plant plant : plants) {
            if (plant.getCurrent_animals() > 0) {
                gc.fillOval((plant.getX() * CELL_SIZE) - 10, (plant.getY() * CELL_SIZE) - 10, CELL_SIZE + 20, CELL_SIZE + 20);
            }
            gc.drawImage(new Image("plant.png"), (plant.getX() * CELL_SIZE) - 4, (plant.getY() * CELL_SIZE) - 4, CELL_SIZE + 8, CELL_SIZE + 8);
        }
        // draw the prey using the prey.png image
        ArrayList<Prey> preysCopy = new ArrayList<>(preys);
        for (Prey prey : preysCopy) {
            if (prey.getHealth() <= 0) {
                preys.remove(prey);
            }
            gc.drawImage(new Image("prey.png"), (prey.getX() * CELL_SIZE) - 5, (prey.getY() * CELL_SIZE) - 5, CELL_SIZE + 10, CELL_SIZE + 10);
        }
        // draw the predator using the predator.png image
        ArrayList<Predator> predatorsCopy = new ArrayList<>(predators);
        for (Predator predator : predatorsCopy) {
            if (predator.getHealth() <= 0) {
                predators.remove(predator);
            }
            gc.drawImage(new Image("predator.png"), (predator.getX() * CELL_SIZE) - 5, (predator.getY() * CELL_SIZE) - 5, CELL_SIZE + 10, CELL_SIZE + 10);
        }
        // draw the hideouts using the hideout image
        gc.setFill(Color.YELLOW.deriveColor(0, 1, 1, 0.3));
        for (Hideout hideout : hideouts) {
            if (hideout.getCurrent_animals() > 0) {
                gc.fillOval((hideout.getX() * CELL_SIZE) - 15, (hideout.getY() * CELL_SIZE) - 15, CELL_SIZE + 30, CELL_SIZE + 30);
            }
            gc.drawImage(new Image("hideout.png"), (hideout.getX() * CELL_SIZE) - 15, (hideout.getY() * CELL_SIZE) - 15, CELL_SIZE + 30, CELL_SIZE + 30);
        }
        // draw the selected cell
        if (target.getX() != 1 && target.getY() != 1) {
            gc.setFill(Color.RED.deriveColor(0, 1, 1, 0.5));
            gc.fillOval((target.getX() * CELL_SIZE) + 5, (target.getY() * CELL_SIZE) + 5, CELL_SIZE - 10, CELL_SIZE - 10);
        }

        // update the information in the info stage
        if (selectedObject != null) {
            infoLayout.getChildren().clear();
            String type = selectedObject.getClass().getSimpleName();
            infoLayout.getChildren().add(new Label("Type: " + type));
            infoLayout.getChildren().add(new Label("X: " + selectedObject.getX()));
            infoLayout.getChildren().add(new Label("Y: " + selectedObject.getY()));
            switch (type) {
                case "Prey":
                    Prey prey = (Prey) selectedObject;
                    if (prey.getHealth() <= 0) {
                        infoLayout.getChildren().clear();
                        infoLayout.getChildren().addAll(new Label("Please select an object :)"));
                        selectedObject = null;
                        break;
                    }
                    infoLayout.getChildren().add(new Label("Name: " + prey.getName()));
                    infoLayout.getChildren().add(new Label("Health: " + prey.getHealth()));
                    infoLayout.getChildren().add(new Label("Speed: " + prey.getSpeed()));
                    infoLayout.getChildren().add(new Label("Strength: " + prey.getStrength()));
                    infoLayout.getChildren().add(new Label("Food: " + prey.getFood_level()));
                    infoLayout.getChildren().add(new Label("Water: " + prey.getWater_level()));
                    break;
                case "Predator":
                    Predator predator = (Predator) selectedObject;
                    if (predator.getHealth() <= 0) {
                        infoLayout.getChildren().clear();
                        infoLayout.getChildren().addAll(new Label("Please select an object :)"));
                        selectedObject = null;
                        break;
                    }
                    infoLayout.getChildren().add(new Label("Name: " + predator.getName()));
                    infoLayout.getChildren().add(new Label("Health: " + predator.getHealth()));
                    infoLayout.getChildren().add(new Label("Speed: " + predator.getSpeed()));
                    infoLayout.getChildren().add(new Label("Strength: " + predator.getStrength()));
                    infoLayout.getChildren().add(new Label("Hunting: " + predator.getHunting()));
                    infoLayout.getChildren().add(new Label("Infection chance: " + predator.getInfection_chance()));
                    break;
                case "Water":
                    Water water = (Water) selectedObject;
                    infoLayout.getChildren().add(new Label("Name: " + water.getName()));
                    infoLayout.getChildren().add(new Label("Current animals: " + water.getCurrent_animals()));
                    infoLayout.getChildren().add(new Label("Maximum animals: " + water.getMax_animals()));
                    infoLayout.getChildren().add(new Label("Replenish speed: " + water.getReplenish_speed()));
                    break;
                case "Plant":
                    Plant plant = (Plant) selectedObject;
                    infoLayout.getChildren().add(new Label("Name: " + plant.getName()));
                    infoLayout.getChildren().add(new Label("Current animals: " + plant.getCurrent_animals()));
                    infoLayout.getChildren().add(new Label("Maximum animals: " + plant.getMax_animals()));
                    infoLayout.getChildren().add(new Label("Replenish speed: " + plant.getReplenish_speed()));
                    break;
                case "Hideout":
                    Hideout hideout = (Hideout) selectedObject;
                    infoLayout.getChildren().add(new Label("Name: " + hideout.getName()));
                    infoLayout.getChildren().add(new Label("Current animals: " + hideout.getCurrent_animals()));
                    infoLayout.getChildren().add(new Label("Maximum animals: " + hideout.getMax_animals()));
                    infoLayout.getChildren().add(new Label("Reproduction chance: " + hideout.getReproduction_chance()));
                    break;
            }
        }
    }
}