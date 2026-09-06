package su.aspectvisuals.client.waypoint;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.math.BlockPos;
import su.aspectvisuals.client.config.JsonStore;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Точки хранятся локально: серверной части для них на сайте нет. */
public final class WaypointStore {
    private static final String FILE = "waypoints.json";

    private final Path path = JsonStore.directory().resolve(FILE);
    private final List<Waypoint> waypoints = new ArrayList<>();

    public List<Waypoint> all() {
        return waypoints;
    }

    public void add(Waypoint waypoint) {
        waypoints.add(waypoint);
        save();
    }

    public void remove(int index) {
        if (index >= 0 && index < waypoints.size()) {
            waypoints.remove(index);
            save();
        }
    }

    public void replace(int index, Waypoint waypoint) {
        if (index >= 0 && index < waypoints.size()) {
            waypoints.set(index, waypoint);
            save();
        }
    }

    public void load() {
        waypoints.clear();
        JsonObject root = JsonStore.read(path);
        if (root == null || !root.has("waypoints") || !root.get("waypoints").isJsonArray()) {
            return;
        }

        for (JsonElement element : root.getAsJsonArray("waypoints")) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject json = element.getAsJsonObject();
            String name = JsonStore.string(json, "name");
            if (name.isEmpty()) {
                continue;
            }
            waypoints.add(new Waypoint(
                    name,
                    new BlockPos((int) JsonStore.longValue(json, "x"),
                            (int) JsonStore.longValue(json, "y"),
                            (int) JsonStore.longValue(json, "z")),
                    JsonStore.string(json, "dimension"),
                    JsonStore.string(json, "icon"),
                    (int) JsonStore.longValue(json, "color"),
                    JsonStore.booleanValue(json, "visible", true)));
        }
    }

    public void save() {
        JsonArray array = new JsonArray();
        for (Waypoint waypoint : waypoints) {
            JsonObject json = new JsonObject();
            json.addProperty("name", waypoint.name());
            json.addProperty("x", waypoint.position().getX());
            json.addProperty("y", waypoint.position().getY());
            json.addProperty("z", waypoint.position().getZ());
            json.addProperty("dimension", waypoint.dimension());
            json.addProperty("icon", waypoint.icon());
            json.addProperty("color", waypoint.color());
            json.addProperty("visible", waypoint.visible());
            array.add(json);
        }

        JsonObject root = new JsonObject();
        root.add("waypoints", array);
        JsonStore.write(path, root);
    }
}
