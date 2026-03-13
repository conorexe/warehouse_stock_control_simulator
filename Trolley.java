import java.util.EnumMap;

public class Trolley {

    public final int id;
    public final int maxCapacity;

    private final EnumMap<BoxType, Integer> boxes = new EnumMap<>(BoxType.class);

    public Trolley(int id, int maxCapacity) {
        this.id = id;
        this.maxCapacity = maxCapacity;
        for (BoxType type : BoxType.values()) {
            boxes.put(type, 0);
        }
    }

    public void addBoxes(BoxType type, int count) {
        boxes.put(type, boxes.get(type) + count);
    }

    public void removeBoxes(BoxType type, int count) {
        boxes.put(type, boxes.get(type) - count);
    }

    public void removeOneBox(BoxType type) {
        removeBoxes(type, 1);
    }

    public int getBoxCount(BoxType type) {
        return boxes.get(type);
    }

    public int getTotalLoad() {
        int total = 0;
        for (int n : boxes.values()) total += n;
        return total;
    }

    public int getAvailableSpace() {
        return maxCapacity - getTotalLoad();
    }

    public boolean isEmpty() {
        return getTotalLoad() == 0;
    }

    public boolean hasBoxesOfType(BoxType type) {
        return boxes.get(type) > 0;
    }

    public EnumMap<BoxType, Integer> getBoxes() {
        return new EnumMap<>(boxes);
    }

    public void clear() {
        for (BoxType type : BoxType.values()) {
            boxes.put(type, 0);
        }
    }
}
