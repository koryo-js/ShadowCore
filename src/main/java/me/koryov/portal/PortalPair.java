package me.koryov.portal;

public class PortalPair {

    private final PortalData first;

    private final PortalData second;

    public PortalPair(
            PortalData first,
            PortalData second
    ){

        this.first=first;
        this.second=second;

    }

    public PortalData getFirst(){

        return first;

    }

    public PortalData getSecond(){

        return second;

    }

}