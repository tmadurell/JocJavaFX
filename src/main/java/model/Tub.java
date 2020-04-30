package model;

import control.*;

public class Tub {
    private Sprite pipe;
    private double locationX;
    private double locationY;
    private double height;
    private double width;

    public Tub(boolean isFaceUp, int height) {
        this.pipe = new Sprite();
        this.pipe.resizeImage(isFaceUp ? "/imatges/Tubs/tub_amunt.png" : "/imatges/Tubs/tub_avall.png", 70, height);
        this.width = 70;
        this.height = height;
        this.locationX = 400;
        this.locationY = isFaceUp? 600 - height : 0;
        this.pipe.setPositionXY(locationX, locationY);
    }

    public Sprite getTub() {
        return pipe;
    }
}
