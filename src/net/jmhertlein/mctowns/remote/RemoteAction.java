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
    MODIFY_TOWN_MEMBERSHIP,
    MODIFY_TOWN_ASSISTANTS,
    GET_TOWN_LIST,
    //territs
    GET_TERRITORY_VIEW,
    CREATE_TERRITORY,
    DELETE_TERRITORY,
    MODIFY_TERRITORY_MEMBERSHIP,
    GET_TERRITORY_LIST,
    //plots
    GET_PLOT_VIEW,
    UPDATE_PLOT,
    MODIFY_PLOT_MEMBERSHIP,
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
    GET_VIEW_FOR_PLAYER, 
    GET_PLOTS_LIST,
    UPDATE_CONFIG;
    
    public static final int MODE_NONE = -1;
        
    /**
     * Specifies the MODIFY_*_MEMBERSHIP or MODIFY_TOWN_ASSISTANTS action is in
     * ADD mode (to add a player)
     */
    public static final int MODE_ADD_PLAYER = 0;
    /**
     * Specifies the MODIFY_*_MEMBERSHIP or MODIFY_TOWN_ASSISTANTS action action
     * is in DELETE mode (to delete a player)
     */
    public static final int MODE_DELETE_PLAYER = 1;
    /**
     * Adds or deletes the player as a guest in
     * MODIFY_[TERRITORY|PLOT]_MEMBERSHIP
     */
    public static final int GUEST = 2;
    /**
     * Adds or deletes the player as an owner in
     * MODIFY_[TERRITORY|PLOT]_MEMBERSHIP
     */
    public static final int OWNER = 3;
}