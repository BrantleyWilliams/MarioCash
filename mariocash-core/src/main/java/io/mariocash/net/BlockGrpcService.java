package dev.zhihexireng.net;

import io.grpc.stub.StreamObserver;
import dev.zhihexireng.proto.Block;
import dev.zhihexireng.proto.BlockServiceGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@GRpcService
public class BlockGrpcService extends BlockServiceGrpc.BlockServiceImplBase {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void sync(Block request, StreamObserver<Block> responseObserver) {
        log.debug("request: " + request);
        Block block = Block.newBuilder().setHeight(request.getHeight()).build();
        responseObserver.onNext(block);
        responseObserver.onCompleted();
    }
}
