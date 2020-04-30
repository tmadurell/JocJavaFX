import control.*;
import model.*;

import javafx.application.Application;
import javafx.animation.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;



/**
 * Aplicació central on procesa tot el joc JavaFX
 */
public class MainApp extends Application {
    //Etiquetes de les seves caracteristiques per a cada part del joc
    private int HEIGHT = 700;
    private int WIDTH = 400;
    private int Puntacio_Total = 0;
    private long spaceClickA;
    private double motionTime, elapsedTime;
    private boolean CLICKED, GAME_START, XOC_TUB, GAME_OVER;
    private LongValue startNanoTime;
    private Sprite primerTerra, segonTerra, ocellSprite;
    private Ocell ocell;
    private Text scoreLabel;
    private GraphicsContext gc, ocellGC;
    private AnimationTimer timer;
    private ArrayList<Tub> tubs;
    private Sound punts, xoca, ales, flash, caiguda;
    private ImageView gameOver, startGame, titolGame, consell,consellgameover,taptap;
    private Group root;

    //Aqui arranca la pantall inicial i a cerca les seves dimensions del escenari i pantalla
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Flappy Bird");
        primaryStage.setResizable(false);

        Parent root = getContent();
        Scene main = new Scene(root, WIDTH, HEIGHT);
        setKeyFunctions(main);
        primaryStage.setScene(main);
        primaryStage.show();

        startGame();
    }
    //Quan ja tens el escenari cerca quina funció es executable en el joc en el seu escenari (Exemple:Teclat,Ratolí)
    private void setKeyFunctions(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                setOnUserInput();
            }
        });

        scene.setOnMousePressed(e -> {
            setOnUserInput();
        });
    }
    //Quan el usuari fagi aquesta acció executara tot aquest llistat i començara el joc
    private void setOnUserInput() {
        if (!XOC_TUB) {
            CLICKED = true;
            if (!GAME_START) {
                root.getChildren().remove(titolGame);
                root.getChildren().remove(startGame);
                root.getChildren().remove(consell);
                root.getChildren().remove(taptap);
                flash.playClip();
                GAME_START = true;
            } else {
                ales.playClip();
                spaceClickA = System.currentTimeMillis();
                ocellSprite.setVelocity(0, -250);
            }
        }
        if (GAME_OVER) {
            startNewGame();
        }
    }
    //Tot seguit comença el joc realitza totes aquestes accions al seu entorn sera infinit fins es deixi de realitzar les següents accions
    private void startGame() {
        startNanoTime = new LongValue(System.nanoTime());

        timer = new AnimationTimer() {
            public void handle(long now) {
                elapsedTime = (now - startNanoTime.value) / 1000000000.0;
                startNanoTime.value = now;

                gc.clearRect(0, 0, WIDTH, HEIGHT);
                ocellGC.clearRect(0, 0, WIDTH, HEIGHT);
                movimentTerra();
                checkTimeBetweenSpaceHits();

                if (GAME_START) {
                    renderTubs();
                    checkTubArrosega();
                    updateTotalScore();
                    //Si el jugador es xoca per la canonada mentre vola
                    if (ocellXocTub()) {
                        root.getChildren().add(gameOver);
                        root.getChildren().add(consellgameover);
                        stopArrossega();
                        playXocSo();
                        motionTime += 0.18;
                        if (motionTime > 0.5) {
                            ocellSprite.addVelocity(-200, 400);
                            ocellSprite.render(gc);
                            ocellSprite.update(elapsedTime);
                            motionTime = 0;
                        }
                    }
                    //Si el jugador deixa de realiza la acció del joc en aquest cas volar
                    if (ocellXocTerra()) {
                        if (!root.getChildren().contains(gameOver)) {
                            root.getChildren().add(gameOver);
                            root.getChildren().add(consellgameover);
                            playXocSo();
                            showHitEffect();
                        }
                        timer.stop();
                        GAME_OVER = true;
                        caiguda.playClip();
                    }
                }
            }
        };
        timer.start();
    }
    //Efecte que realitzara al xocar per la canonada
    private boolean ocellXocTub() {
        for (Tub tub : tubs) {
            if (!XOC_TUB && ocellSprite.intersectsSprite(tub.getTub())) {
                XOC_TUB = true;
                showHitEffect();
                return true;
            }
        }
        return false;
    }
    //Efecte que realitzara al xocar per el terra
    private boolean ocellXocTerra() {
        return ocellSprite.intersectsSprite(primerTerra) ||
                ocellSprite.intersectsSprite(segonTerra) ||
                ocellSprite.getPositionX() < 0;
    }
    //Efectes que fara al xocar a un objecte
    private void showHitEffect() {
        ParallelTransition parallelTransition = new ParallelTransition();
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.10), root);
        fadeTransition.setToValue(0);
        fadeTransition.setCycleCount(2);
        fadeTransition.setAutoReverse(true);
        parallelTransition.getChildren().add(fadeTransition);
        parallelTransition.play();
    }
    //So que realitzara al xocar el ocell
    private void playXocSo() {
        xoca.playClip();
    }

    //Quant hagin perdut es reiniciara les següents dades (punts,escenaris y personatge)
    private void resetVariables() {
        updateEtiquetaPuntacio(0);
        Puntacio_Total = 0;
        XOC_TUB = false;
        CLICKED = false;
        GAME_OVER = false;
        GAME_START = false;
    }
    //Quant hem perdut al fer una accio es realitzara la següent llista procesos (Reiniciar el joc)
    private void startNewGame() {
        root.getChildren().remove(gameOver);
        root.getChildren().remove(consellgameover);
        root.getChildren().add(startGame);
        root.getChildren().add(taptap);
        tubs.clear();
        setTerra();
        setTubs();
        setOcell();
        resetVariables();
        startGame();
    }
    //Creació dels elements del joc llistat amb la seva array (Escenari,personatge,efectes de so)
    private Parent getContent() {
        root = new Group();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        Canvas birdCanvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        ocellGC = birdCanvas.getGraphicsContext2D();

        ImageView bg = setFons();
        setTerra();
        tubs = new ArrayList<>();
        setTubs();
        setOcell();
        setEtiquetes();
        setSons();

        root.getChildren().addAll(bg, canvas, birdCanvas, scoreLabel, titolGame,startGame,consell,taptap);
        return root;
    }
    //Aqui posara el fons del joc pot ser dos fons (Com nit o dia)
    private ImageView setFons() {
        Random random = new Random();
        int bg = random.nextInt(2);
        String filePath = bg > 0 ? "/imatges/Fons/fons_dia.png" : "/imatges/Fons/fons_nit.png";
        ImageView imageView = new ImageView(new Image(getClass().getResource(filePath).toExternalForm()));
        imageView.setFitWidth(WIDTH);
        imageView.setFitHeight(HEIGHT);
        return imageView;
    }
    //Imatges que coloca que dona consells al joc en aquest cas etiquetes explicatives com altres posibilitats
    private void setEtiquetes() {
        //Puntacio que es realitza durant la partida
        scoreLabel = new Text("0");
        scoreLabel.setFont(Font.font("Courier", FontWeight.EXTRA_BOLD, 50));
        scoreLabel.setStroke(Color.BLACK);
        scoreLabel.setFill(Color.WHITE);
        scoreLabel.setLayoutX(10);
        scoreLabel.setLayoutY(50);

        //Titol del joc
        titolGame = new ImageView(new Image(getClass().getResource("/imatges/Etiquetes/titol.png").toExternalForm()));
        titolGame.setFitWidth(300);
        titolGame.setFitHeight(80);
        titolGame.setLayoutX(55);
        titolGame.setLayoutY(50);

        //Cabecera de si estem preparat per jugar
        startGame = new ImageView(new Image(getClass().getResource("/imatges/Etiquetes/ready.png").toExternalForm()));
        startGame.setFitWidth(275);
        startGame.setFitHeight(70);
        startGame.setLayoutX(70);
        startGame.setLayoutY(135);

        //Titol de fin del joc
        gameOver = new ImageView(new Image(getClass().getResource("/imatges/Etiquetes/game_over.png").toExternalForm()));
        gameOver.setFitWidth(278);
        gameOver.setFitHeight(80);
        gameOver.setLayoutX(65);
        gameOver.setLayoutY(100);

        //Consell per inciar al joc
        consell = new ImageView(new Image(getClass().getResource("/imatges/Etiquetes/presentacio.png").toExternalForm()));
        consell.setFitWidth(300);
        consell.setFitHeight(100);
        consell.setLayoutX(50);
        consell.setLayoutX(50);
        consell.setLayoutY(600);

        //Consell per reiniciar al joc
        consellgameover = new ImageView(new Image(getClass().getResource("/imatges/Etiquetes/consell_gameover.png").toExternalForm()));
        consellgameover.setFitWidth(300);
        consellgameover.setFitHeight(100);
        consellgameover.setLayoutX(50);
        consellgameover.setLayoutY(625);

        //Altre consell per dispositiu movil
        taptap = new ImageView(new Image(getClass().getResource("/imatges/Etiquetes/tap-tap.png").toExternalForm()));
        taptap.setFitWidth(200);
        taptap.setFitHeight(200);
        taptap.setLayoutX(100);
        taptap.setLayoutY(280);
    }

    //Creacio dels efectes de so en el joc i element que es pot asignar en altre part del joc
    private void setSons() {
        punts = new Sound("/efectesdesons/punts.mp3");
        xoca = new Sound("/efectesdesons/xoca.mp3");
        ales = new Sound("/efectesdesons/ales.mp3");
        flash = new Sound("/efectesdesons/flash.mp3");
        caiguda = new Sound("/efectesdesons/caiguda.mp3");
    }
    //Creacio del ocell en el joc per identificar qui ets
    private void setOcell() {
        ocell = new Ocell();
        ocellSprite = ocell.getOcell();
        ocellSprite.render(gc);
    }
    //Creació del terra del joc en el escenari
    private void setTerra() {
        primerTerra = new Sprite();
        primerTerra.resizeImage("/imatges/Terra/terra.png", 400, 140);
        primerTerra.setPositionXY(0, HEIGHT - 100);
        primerTerra.setVelocity(-.4, 0);
        primerTerra.render(ocellGC);

        segonTerra = new Sprite();
        segonTerra.resizeImage("/imatges/Terra/terra.png", 400, 140);
        segonTerra.setPositionXY(primerTerra.getWidth(), HEIGHT - 100);
        segonTerra.setVelocity(-.4, 0);
        segonTerra.render(gc);
    }



    //Velocitat que el personatge respon a cada acció que es realitzara
    private void checkTimeBetweenSpaceHits() {
        long difference = (System.currentTimeMillis() - spaceClickA) / 350;

        if (difference >= .001 && CLICKED) {
            CLICKED = false;
            ocellSprite.addVelocity(0, 600);
            ocellSprite.render(ocellGC);
            ocellSprite.update(elapsedTime);
        } else {
            animacioOcell();
        }
    }
    //Cada vegada que el ocell(jugador) travesa per una canonada del mig sumara un punt
    private void updateTotalScore() {
        if (!XOC_TUB) {
            for (Tub tub : tubs) {
                if (tub.getTub().getPositionX() == ocellSprite.getPositionX()) {
                    updateEtiquetaPuntacio(++Puntacio_Total);
                    punts.playClip();
                    break;
                }
            }
        }
    }
    //Actualització de la puntació i la mostra per pantalla
    private void updateEtiquetaPuntacio(int score) {
        scoreLabel.setText(Integer.toString(score));
    }
    //Moviment del terra com si fos volant per una direcció
    private void movimentTerra() {
        primerTerra.render(gc);
        segonTerra.render(gc);
        primerTerra.update(5);
        segonTerra.update(5);
        if (primerTerra.getPositionX() <= -WIDTH) {
            primerTerra.setPositionXY(segonTerra.getPositionX() + segonTerra.getWidth(),
                    HEIGHT - 100);
        } else if (segonTerra.getPositionX() <= -WIDTH) {
            segonTerra.setPositionXY(primerTerra.getPositionX() + primerTerra.getWidth(),
                    HEIGHT - 100);
        }
    }
    //Animació del ocell cada vegada que es fa clic
    private void animacioOcell() {
        ocellSprite.render(ocellGC);
        ocellSprite.update(elapsedTime);

        motionTime += 0.18;
        if (motionTime > 0.1 && CLICKED) {
            Sprite temp = ocellSprite;
            ocellSprite = ocell.animate();
            ocellSprite.setPositionXY(temp.getPositionX(), temp.getPositionY());
            ocellSprite.setVelocity(temp.getVelocityX(), temp.getVelocityY());
            motionTime = 0;
        }
    }


    //Quan hem perdut en el joc deixara de moure els segënts elements com les canonades i el terra
    private void stopArrossega() {
        for (Tub pipe : tubs) {
            pipe.getTub().setVelocity(0, 0);
        }
        primerTerra.setVelocity(0, 0);
        segonTerra.setVelocity(0, 0);
    }
    //Si al iniciar al lloc realitzara la següent formula per repetir de manera aleatoria la posicio sorpresa
    private void checkTubArrosega() {
        if (tubs.size() > 0) {
            Sprite p = tubs.get(tubs.size() - 1).getTub();
            if (p.getPositionX() == WIDTH / 2 - 80) {
                setTubs();
            } else if (p.getPositionX() <= -p.getWidth()) {
                tubs.remove(0);
                tubs.remove(0);
            }
        }
    }

    private void setTubs() {
        int height = getRandomTubsAltura();

        Tub pipe = new Tub(true, height);
        Tub downPipe = new Tub(false, 425 - height);

        pipe.getTub().setVelocity(-.4, 0);
        downPipe.getTub().setVelocity(-.4, 0);

        pipe.getTub().render(gc);
        downPipe.getTub().render(gc);

        tubs.addAll(Arrays.asList(pipe, downPipe));
    }
    //Formula que realitza aleatoriament la altura de la canonada
    private int getRandomTubsAltura() {
        return (int) (Math.random() * (410 - 25)) + 25;
    }

    private void renderTubs() {
        for (Tub pipe : tubs) {
            Sprite p = pipe.getTub();
            p.render(gc);
            p.update(5);
        }
    }

    public class LongValue {
        public long value;

        public LongValue(long i) {
            this.value = i;
        }
    }
}
