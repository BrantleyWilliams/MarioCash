package dev.zhihexireng.node.api;

import dev.zhihexireng.core.net.Peer;

public class PeerDto {
    private String branchId;
    private String peerId;
    private String ip;
    private int port;

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    static PeerDto valueOf(String branchId, Peer peer) {
        PeerDto dto = new PeerDto();
        dto.branchId = branchId;
        dto.peerId = peer.getPubKey().toString();
        dto.ip = peer.getHost();
        dto.port = peer.getPort();
        return dto;
    }

    public Peer toPeer() {
        return Peer.valueOf(peerId, ip, port);
    }
}
