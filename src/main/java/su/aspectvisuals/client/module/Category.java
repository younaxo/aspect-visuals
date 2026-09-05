package su.aspectvisuals.client.module;

/** Вкладки фильтра из Figma: HUD / Info / Visual / All. */
public enum Category {
    HUD("HUD"),
    INFO("Info"),
    VISUAL("Visual");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
