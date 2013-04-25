package net.jmhertlein.mctowns.remote;

import java.util.List;

/**
 *
 * @author joshua
 */
public enum RemoteAction {
    GET_TOWN_VIEW,
    GET_TERRITORY_VIEW,
    GET_PLOT_VIEW,
    UPDATE_PLOT,
    UPDATE_TOWN,
    UPDATE_TERRITORY,
    GET_META_VIEW,
    TERMINATE_CONNECTION;
    
    private List<String> args;
    
    public void setArguments(List<String> args) {
        this.args = args;
    }
}
