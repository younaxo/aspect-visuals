package su.aspectvisuals.client.hud;

public enum HudAnchor {
    TOP_LEFT("Слева сверху"),
    TOP_RIGHT("Справа сверху"),
    BOTTOM_LEFT("Слева снизу"),
    BOTTOM_RIGHT("Справа снизу");

    private final String label;

    HudAnchor(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static HudAnchor byLabel(String label) {
        for (HudAnchor anchor : values()) {
            if (anchor.label.equals(label)) {
                return anchor;
            }
        }
        return TOP_LEFT;
    }

    public boolean right() {
        return this == TOP_RIGHT || this == BOTTOM_RIGHT;
    }

    public boolean bottom() {
        return this == BOTTOM_LEFT || this == BOTTOM_RIGHT;
    }
}
