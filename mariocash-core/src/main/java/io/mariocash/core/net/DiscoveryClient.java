package dev.zhihexireng.core.net;

import java.util.List;

public interface DiscoveryClient {
    List<String> getAllActivePeer(String host, int port);
}
