package com.tomiyo.snappy.snappyclient.ipproxy;

/**
 * Created by I322353 on 9/24/2016.
 */
public class ProxyEntity {
    public String host="";
    public int port=0;

    public int getUnlike() {
        return unlike;
    }

    public void setUnlike(int unlike) {
        this.unlike = unlike;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int like=0;
    public int unlike=0;

    public void addLike(){
        this.like++;
    }

    public  void addUnlike(){
        this.unlike++;
    }
}
