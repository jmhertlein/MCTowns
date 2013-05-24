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
    GET_TOWN_LIST,
    
    //territs
    GET_TERRITORY_VIEW,
    UPDATE_TERRITORY,
    CREATE_TERRITORY,
    DELETE_TERRITORY,
    UPDATE_TERRITORY_MEMBERSHIP,
    GET_TERRITORY_LIST,
    
    //plots
    GET_PLOT_VIEW,
    UPDATE_PLOT,
    UPDATE_PLOT_MEMBERSHIP,
    CREATE_PLOT,
    DELETE_PLOT,
    
    //security
    GET_IDENTITY_LIST,
    GET_IDENTITY_VIEW,
    ADD_IDENTITY,
    DELETE_IDENTITY,
    
    //other views
    GET_META_VIEW,
    GET_PLAYER_LIST,
    GET_VIEW_FOR_PLAYER;
}
