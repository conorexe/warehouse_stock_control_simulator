import java.util.EnumMap;
import java.util.Map;

public class Trolley {
    public int id;
    public int maxCapacity;

    EnumMap<BoxType, Integer> boxes = new EnumMap<>(BoxType.class);

    public Trolley(int id, int maxCapacity) {
        this.id = id;
        this.maxCapacity = maxCapacity;
        boxes.put(BoxType.ELECTRONICS, 0);
        boxes.put(BoxType.BOOKS, 0);
        boxes.put(BoxType.MEDICINES, 0);
        boxes.put(BoxType.CLOTHES, 0);
        boxes.put(BoxType.TOOLS, 0);
    }



    public void addBoxes(BoxType type, int num) {
        int current = boxes.get(type);
        int newValue = current + num;
        boxes.put(type, newValue);
    }

    public void removeOneBox(BoxType type) {
        int current = boxes.get(type);
        int newValue = current - 1;
        boxes.put(type, newValue);
    }

    public int getTotalLoad() {
        int total = 0;
        for (int num : boxes.values()) {
            total = total + num;
        }

        return total;
    }

    public boolean isEmpty(){
        int total = getTotalLoad();

        if (total != 0) {
            return false;
        } else {
            return true;
        }
    }
}
