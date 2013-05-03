package net.jmhertlein.mctowns.remote;

import java.io.Serializable;

/**
 *
 * @author joshua
 */
public enum RemoteAction implements Serializable {
    KEY_EXCHANGE,
    HANDSHAKE,
    GET_TOWN_VIEW,
    GET_TERRITORY_VIEW,
    GET_PLOT_VIEW,
    UPDATE_PLOT,
    UPDATE_TOWN,
    UPDATE_TERRITORY,
    GET_META_VIEW,
    TERMINATE_SESSION;
    
    private String[] args;

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }
    
    
}
