package pt.up.fe.cpd2223.game;

import java.util.Arrays;
import java.util.List;

public final class Elo {

    public static double expectedScore(double ratingA, double ratingB) {
        return 1.0 / (1.0 + Math.pow(10.0, (ratingB - ratingA) / 400.0));
    }

    public static List<Double> expectedScore(double ratingA, Double ...ratings) {
        return Arrays.stream(ratings).map(rating -> expectedScore(ratingA, rating)).toList();
    }

    public static double updateRating(double rating, double expectedScore, double actualScore) {

        double kFactor = rating < 2100 ? 32 : rating > 2400 ? 16 : 24;

        return rating + kFactor * (actualScore - expectedScore);
    }

}
