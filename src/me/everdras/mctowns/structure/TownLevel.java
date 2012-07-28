package me.everdras.mctowns.structure;

/**
 * A simple enum to hold what sort of town-y thing something is.
 *
 * @author Joshua
 */
public enum TownLevel {
    TOWN,
    TERRITORY,
    PLOT;

    public static final String TERRITORY_INFIX = "_territ_";
    public static final String PLOT_INFIX = "_plot_";

    @Override
    public String toString() {
        switch (this) {
            case TOWN:
                return "TOWN";
            case TERRITORY:
                return "TERRITORY";
            case PLOT:
                return "PLOT";
            default:
                return "y u break me? ;_;";
        }
    }

    public static final TownLevel parseTownLevel(String s) {
        s = s.toUpperCase();

        switch(s) {
            case "TOWN":
                return TOWN;
            case "TERRITORY":
                return TERRITORY;
            case "PLOT":
                return PLOT;
            default:
                throw new TownLevelFormatException(s);
        }
    }

    public static class TownLevelFormatException extends RuntimeException {
        public TownLevelFormatException(String badToken) {
            super("Error: " + badToken + " is not a town level.");
        }
    }
}
