public enum BoxType {
    ELECTRONICS, BOOKS, MEDICINES, CLOTHES, TOOLS;

    public String getName() {
        return name().toLowerCase();
    }

    public static BoxType fromName(String name) {
        for (BoxType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
