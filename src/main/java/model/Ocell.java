package model;

import control.Sprite;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Aqui son les animacions que realitza el nostre ocell (personatge del joc al interactuar-hi amb ell)
 */
public class Ocell {
    private Sprite bird;
    private ArrayList<Sprite> flight = new ArrayList<>();
    private int currentBird = 0;
    private double locationX = 70;
    private double locationY = 330;
    private int BIRD_WIDTH = 50;
    private int BIRD_HEIGHT = 45;

    public Ocell() {
        bird = new Sprite();
        bird.resizeImage("/imatges/Ocell/ocell1.png", BIRD_WIDTH, BIRD_HEIGHT);
        bird.setPositionXY(locationX, locationY);
        setAnimacioVol();
    }

    public void setAnimacioVol() {
        Sprite bird2 = new Sprite();
        bird2.resizeImage("/imatges/Ocell/ocell2.png", BIRD_WIDTH, BIRD_HEIGHT);
        bird2.setPositionXY(locationX, locationY);

        Sprite bird3 = new Sprite();
        bird3.resizeImage("/imatges/Ocell/ocell1.png", BIRD_WIDTH, BIRD_HEIGHT);
        bird3.setPositionXY(locationX, locationY);

        Sprite bird4 = new Sprite();
        bird4.resizeImage("/imatges/Ocell/ocell3.png", BIRD_WIDTH, BIRD_HEIGHT);
        bird4.setPositionXY(locationX, locationY);

        flight.addAll(Arrays.asList(bird, bird2, bird3, bird4));
    }

    public Sprite getOcell() {
        return bird;
    }

    public Sprite animate() {
        if (currentBird == flight.size() - 1) {
            currentBird = 0;
        }

        return flight.get(currentBird++);
    }
}
