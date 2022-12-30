package oop.hunt;

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
import java.util.Random;

public class Main extends Application {
    private static final Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    public static final int GRID_SIZE = (int) screenBounds.getHeight() / 22 + 1;
    public static final int CELL_SIZE = (int) screenBounds.getWidth() / 96;

    private List<Predator> predators;
    private List<Prey> preys;
    private List<Path> paths;
    private List<Water> waters;
    private List<Plant> plants;
    private List<Hideout> hideouts;

    private Canvas canvas;
    private GraphicsContext gc;
    private Random random;
    private Thingamajig selectedObject = null;
    private VBox infoLayout;
    private static MediaPlayer mediaPlayer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage simulationStage) {
        random = new Random();

        predators = new ArrayList<>();
        preys = new ArrayList<>();
        waters = new ArrayList<>();
        plants = new ArrayList<>();
        paths = new ArrayList<>();
        hideouts = new ArrayList<>();

        // generate 25 random (x, y) 2d array pairs that are unique
        int[][] randomPairs = new int[25][2];
        for (int i = 0; i < 25; i++) {
            int x = random.nextInt(GRID_SIZE);
            int y = random.nextInt(GRID_SIZE);
            for (int j = 0; j < i; j++) {
                if (randomPairs[j][0] == x && randomPairs[j][1] == y) {
                    x = random.nextInt(GRID_SIZE);
                    y = random.nextInt(GRID_SIZE);
                    j = 0;
                }
            }
            randomPairs[i][0] = x;
            randomPairs[i][1] = y;
        }

        // generate 10 random water sources
        for (int i = 0; i < 10; i++) {
            waters.add(new Water(randomPairs[i][0], randomPairs[i][1], generateName(randInt(3, 6)), randInt(1, 3), randInt(1, 5)));
        }

        // generate 10 random plants
        for (int i = 0; i < 10; i++) {
            plants.add(new Plant(randomPairs[i + 10][0], randomPairs[i + 10][1], generateName(randInt(3, 6)), randInt(1, 3), randInt(1, 5)));
        }

        // generate 5 random hideouts
        for (int i = 0; i < 5; i++) {
            hideouts.add(new Hideout(randomPairs[i + 20][0], randomPairs[i + 20][1], generateName(randInt(7, 10)), randInt(1, 5), preys));
        }

        // generate paths from hideouts to water sources and plants
        for (Hideout h : hideouts) {
            for (Water w : waters) {
                paths.addAll(Path.generatePath(h.getX(), h.getY(), w.getX(), w.getY()));
            }
            for (Plant p : plants) {
                paths.addAll(Path.generatePath(h.getX(), h.getY(), p.getX(), p.getY()));
            }
        }

        // create the canvas for drawing the map
        canvas = new Canvas(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);
        gc = canvas.getGraphicsContext2D();

        // let's get some nice music in here
        Media song = null;
        try {
            song = new Media(getClass().getResource("/audio/track"+randInt(1,4)+".mp3").toURI().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mediaPlayer = new MediaPlayer(song);
        mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));

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
            Predator predator = new Predator(random.nextInt(GRID_SIZE), random.nextInt(GRID_SIZE), predatorName, preys);
            predators.add(predator);
            System.out.println("Added predator " + predatorName);
            new Thread(predator).start();
            new AudioClip(getClass().getResource("/audio/predator.mp3").toExternalForm()).play();
        });

        Button addPreyButton = new Button("Add Prey");
        TextField preyNameField = new TextField();
        preyNameField.setPromptText("Prey Name");
        addPreyButton.setOnAction(e -> {
            int randomHideout = random.nextInt(hideouts.size());
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
        simulationStage.setTitle("Zombie Survival");
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
        infoStage.setScene(new Scene(infoLayout, 200, 300));
        infoStage.setResizable(false);
        infoStage.setOnCloseRequest(e -> System.exit(0));

        // Object selection for info
        canvas.setOnMouseClicked(e -> {
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
                    break;
                }
            }
        });

        // Add stage with Kill button and Change route button
        Stage interactStage = new Stage();
        interactStage.setTitle("Interact");
        interactStage.getIcons().add(new Image("icon.png"));

        Button killButton = new Button("Kill the animal");
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

        interactStage.setScene(new Scene(killButton, 200, 100));
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
            interactStage.setY(menuStage.getHeight() + infoStage.getHeight());
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

        startStage.setScene(new Scene(stackPane, 1024, 800));
        startStage.setResizable(false);
        startStage.setOnCloseRequest(e -> System.exit(0));
        startStage.show();

        // create the animation timer for updating the map
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };
        timer.start();
    }


    private void update() {
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
        for (Water water : waters) {
            gc.drawImage(new Image("water.png"), (water.getX() * CELL_SIZE) - 5, (water.getY() * CELL_SIZE) - 5, CELL_SIZE + 10, CELL_SIZE + 10);
        }

        // draw the plants using the plant image
        for (Plant plant : plants) {
            gc.drawImage(new Image("plant.png"), (plant.getX() * CELL_SIZE) - 5, (plant.getY() * CELL_SIZE) - 5, CELL_SIZE + 10, CELL_SIZE + 10);
        }

        // draw the prey using the prey.png image
        for (Prey prey : preys) {
            if (prey.getHealth() <= 0) {
                preys.remove(prey);
            }
            gc.drawImage(new Image("prey.png"), (prey.getX() * CELL_SIZE) - 5, (prey.getY() * CELL_SIZE) - 5, CELL_SIZE + 10, CELL_SIZE + 10);
        }

        // draw the predator using the predator.png image
        for (Predator predator : predators) {
            if (predator.getHealth() <= 0) {
                predators.remove(predator);
            }
            gc.drawImage(new Image("predator.png"), (predator.getX() * CELL_SIZE) - 5, (predator.getY() * CELL_SIZE) - 5, CELL_SIZE + 10, CELL_SIZE + 10);
        }

        // draw the hideouts using the hideout image
        for (Hideout hideout : hideouts) {
            gc.drawImage(new Image("hideout.png"), (hideout.getX() * CELL_SIZE) - 15, (hideout.getY() * CELL_SIZE) - 15, CELL_SIZE + 30, CELL_SIZE + 30);
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
                    // cast the animal to a prey
                    Prey prey = (Prey) selectedObject;
                    if (prey.getHealth() <= 0) {
                        infoLayout.getChildren().clear();
                        infoLayout.getChildren().addAll(new Label("Please select an object :)"));
                        selectedObject = null;
                        break;
                    }

                    // update the info layout
                    infoLayout.getChildren().add(new Label("Name: " + prey.getName()));
                    infoLayout.getChildren().add(new Label("Health: " + prey.getHealth()));
                    infoLayout.getChildren().add(new Label("Speed: " + prey.getSpeed()));
                    infoLayout.getChildren().add(new Label("Strength: " + prey.getStrength()));
                    infoLayout.getChildren().add(new Label("Food: " + prey.getFood_level()));
                    infoLayout.getChildren().add(new Label("Water: " + prey.getWater_level()));

                    break;
                case "Predator":
                    // cast the animal to a predator
                    Predator predator = (Predator) selectedObject;
                    if (predator.getHealth() <= 0) {
                        infoLayout.getChildren().clear();
                        infoLayout.getChildren().addAll(new Label("Please select an object :)"));
                        selectedObject = null;
                        break;
                    }

                    // update the info layout
                    infoLayout.getChildren().add(new Label("Name: " + predator.getName()));
                    infoLayout.getChildren().add(new Label("Health: " + predator.getHealth()));
                    infoLayout.getChildren().add(new Label("Speed: " + predator.getSpeed()));
                    infoLayout.getChildren().add(new Label("Strength: " + predator.getStrength()));
                    infoLayout.getChildren().add(new Label("Hunting: " + predator.getMode()));

                    break;
                case "Water":
                    // cast the animal to a water source
                    Water water = (Water) selectedObject;

                    // update the info layout
                    infoLayout.getChildren().add(new Label("Name: " + water.getName()));
                    infoLayout.getChildren().add(new Label("Current animals: " + water.getCurrent_animals()));
                    infoLayout.getChildren().add(new Label("Maximum animals: " + water.getMax_animals()));
                    infoLayout.getChildren().add(new Label("Replenish speed: " + water.getReplenish_speed()));
                    break;
                case "Plant":
                    // cast the animal to a plant
                    Plant plant = (Plant) selectedObject;

                    // update the info layout
                    infoLayout.getChildren().add(new Label("Name: " + plant.getName()));
                    infoLayout.getChildren().add(new Label("Current animals: " + plant.getCurrent_animals()));
                    infoLayout.getChildren().add(new Label("Maximum animals: " + plant.getMax_animals()));
                    infoLayout.getChildren().add(new Label("Replenish speed: " + plant.getReplenish_speed()));
                    break;
                case "Hideout":
                    // cast the animal to a hideout
                    Hideout hideout = (Hideout) selectedObject;

                    // update the info layout
                    infoLayout.getChildren().add(new Label("Name: " + hideout.getName()));
                    infoLayout.getChildren().add(new Label("Current animals: " + hideout.getCurrent_animals()));
                    infoLayout.getChildren().add(new Label("Maximum animals: " + hideout.getMax_animals()));
                    infoLayout.getChildren().add(new Label("Reproduction chance: " + hideout.getReproduction_chance()));
                    break;
            }
        }
    }

    private String generateName(int length) {
        String name = "";
        for (int i = 0; i < length; i++) {
            if (Math.random() < 0.5) {
                name += (char) (Math.random() * 26 + 97);
            } else {
                name += (char) (Math.random() * 5 + 97);
            }
        }
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return name;
    }

    protected static int randInt(int min, int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }
}