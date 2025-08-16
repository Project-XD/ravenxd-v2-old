package keystrokesmod.utility.interact.moveable;

public interface Moveable {

    void render();

    int getMinX();

    int getMaxX();

    int getMinY();

    int getMaxY();

    void moveX(int amount);

    void moveY(int amount);

}
