package gdd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Loads a 0/1 grid from a text file. "1" cells become star clusters in the
// procedural background (see Scene1.drawStarClusters).
public final class MapLoader {

    private MapLoader() {
    }

    public static int[][] load(String path) {
        List<int[]> rows = new ArrayList<>();
        try {
            for (String raw : Files.readAllLines(Paths.get(path))) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split("[,\\s]+");
                int[] row = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    row[i] = "1".equals(parts[i].trim()) ? 1 : 0;
                }
                rows.add(row);
            }
        } catch (IOException e) {
            System.err.println("Star map unavailable: " + e.getMessage());
        }
        if (rows.isEmpty()) {
            return new int[0][0];
        }
        return rows.toArray(new int[0][]);
    }
}
