package net.jmhertlein.mctowns.remote;

import java.io.Serializable;

/**
 *
 * @author joshua
 */
public enum RemoteAction implements Serializable {
    //session and auth
    KEY_EXCHANGE,
    TERMINATE_SESSION,
    
    //towns
    GET_TOWN_VIEW,
    UPDATE_TOWN,
    CREATE_TOWN,
    DELETE_TOWN,
    UPDATE_TOWN_MEMBERSHIP,
    UPDATE_MAYOR,
    
    //territs
    GET_TERRITORY_VIEW,
    UPDATE_TERRITORY,
    CREATE_TERRITORY,
    DELETE_TERRITORY,
    UPDATE_TERRITORY_MEMBERSHIP,
    
    //plots
    GET_PLOT_VIEW,
    UPDATE_PLOT,
    UPDATE_PLOT_MEMBERSHIP,
    CREATE_PLOT,
    DELETE_PLOT,
    
    //other views
    GET_META_VIEW,
    GET_SECURITY_VIEW,
    GET_PLAYER_VIEW,
}
