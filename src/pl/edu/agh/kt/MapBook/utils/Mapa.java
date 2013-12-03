package pl.edu.agh.kt.MapBook.utils;

/**
 * Created with IntelliJ IDEA.
 * User: adba
 * Date: 03.12.13
 */
public class Mapa {
    private int id;
    private String title;
    private String imagePath;
    private String scale;
    private Position position1;
    private Position position2;

    public Mapa(int id, String title, String imagePath, String scale, Position position1, Position position2) {
        this.id = id;
        this.title = title;
        this.imagePath = imagePath;
        this.scale = scale;
        this.position1 = position1;
        this.position2 = position2;
    }

    @Override
    public String toString(){
        return title;
    }
}
