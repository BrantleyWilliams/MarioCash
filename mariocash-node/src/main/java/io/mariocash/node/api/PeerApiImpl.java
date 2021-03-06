package dev.zhihexireng.node.api;

import com.googlecode.jsonrpc4j.spring.AutoJsonRpcServiceImpl;
import dev.zhihexireng.core.net.PeerGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AutoJsonRpcServiceImpl
public class PeerApiImpl implements PeerApi {

    private final PeerGroup peerGroup;

    @Autowired
    public PeerApiImpl(PeerGroup peerGroup) {
        this.peerGroup = peerGroup;
    }

    @Override
    public List<String> getAllActivePeer() {
        return peerGroup.getActivePeerList();
    }
}
