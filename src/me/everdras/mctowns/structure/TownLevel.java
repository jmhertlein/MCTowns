package me.everdras.mctowns.structure;

/**
 * A simple enum to hold what sort of town-y thing something is.
 * @author Joshua
 */
public enum TownLevel {

    /**
     * a town
     */
    TOWN,
    /**
     * a territory
     */
    TERRITORY,
    /**
     * a district
     */
    DISTRICT,
    /**
     * a plot
     */
    PLOT;

    @Override
    public String toString() {
        switch (this) {
            case TOWN:
                return "TOWN";
            case TERRITORY:
                return "TERRITORY";
            case DISTRICT:
                return "DISTRICT";
            case PLOT:
                return "PLOT";
            default:
                assert false;
                return "y u break me? ;_;";
        }
    }
}
