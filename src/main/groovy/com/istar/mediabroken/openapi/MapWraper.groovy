package com.istar.mediabroken.openapi

class MapWraper {

    Map map

    public MapWraper(Object params) {
        println params
        println params.getClass()
        this.map = params as Map
    }

    Map getMap() {
        return this.map
    }
}
