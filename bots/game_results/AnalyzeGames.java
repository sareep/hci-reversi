import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class AnalyzeGames {

    public static HashMap<String, Integer[]> scoreboard = new HashMap<String, Integer[]>();

    public static void main(String[] args) throws IOException {

        BufferedReader br = new BufferedReader(
                new FileReader("ai_game_results.txt"));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                String[] black_arr = values[0].split("_");
                int b_score = Integer.parseInt(values[1]);
                String[] white_arr = values[2].split("_");
                int w_score = Integer.parseInt(values[3]);

                String black = black_arr[2] + black_arr[3];
                String white = white_arr[2] + white_arr[3];

                if (b_score == w_score) {
                    safe_put(black, "tie");
                    safe_put(white, "tie");
                } else if (b_score > w_score) {
                    safe_put(black, "win");
                    safe_put(white, "loss");
                } else if (b_score < w_score) {
                    safe_put(black, "loss");
                    safe_put(white, "win");
                }

            }
        } finally {
            br.close();
        }

        for (HashMap.Entry<String, Integer[]> entry : scoreboard.entrySet()) {
            Integer[] val = entry.getValue();
            System.out.println(entry.getKey() + " had a win/loss/tie record of " + val[0].toString() + "/"
                    + val[1].toString() + "/" + val[2].toString());
        }
    }

    public static void safe_put(String name, String status) {
        Integer[] wlt = scoreboard.get(name);

        if (wlt == null) {
            wlt = new Integer[] { 0, 0, 0 };
        }

        if (status.equals("win")) {
            wlt[0] += 1;
        } else if (status.equals("loss")) {
            wlt[1] += 1;
        } else if (status.equals("tie")) {
            wlt[2] += 1;
        }

        scoreboard.put(name, wlt);
    }
}