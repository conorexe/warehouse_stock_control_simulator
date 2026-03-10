public enum BoxType {
    ELECTRONICS, BOOKS, MEDICINES, CLOTHES, TOOLS;

    public String getName(){
        return name().toLowerCase();
    }

    public static BoxType fromName(String name) {
        //String caps = name.toUpperCase();
        BoxType result = null;
        for (BoxType box : values() ) {
            if (box.name().equalsIgnoreCase(name)) {
                result = box;
                break;
            }
        }
        return result;
    }
}