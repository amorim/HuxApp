package org.lamorim.huxflooderapp.models;

import java.util.Date;

/**
 * Created by lucas on 22/12/2016.
 */

public class Server {
    private String serverName;
    private Date serverLastSeen;
    private boolean isServerOnline;
    private int serverMaxLoad;
    private int load;
    private String serverLocation;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Date getServerLastSeen() {
        return serverLastSeen;
    }

    public void setServerLastSeen(Date serverLastSeen) {
        this.serverLastSeen = serverLastSeen;
    }

    public boolean isServerOnline() {
        return isServerOnline;
    }

    public void setServerOnline(boolean serverOnline) {
        isServerOnline = serverOnline;
    }

    public int getServerMaxLoad() {
        return serverMaxLoad;
    }

    public void setServerMaxLoad(int serverMaxLoad) {
        this.serverMaxLoad = serverMaxLoad;
    }

    public int getLoad() {
        return load;
    }

    public void setLoad(int load) {
        this.load = load;
    }

    public String getServerLocation() {
        return serverLocation;
    }

    public void setServerLocation(String serverLocation) {
        this.serverLocation = serverLocation;
    }
}
