package dev.zhihexireng.core.net;

interface KademliaOptions {
    int BUCKET_SIZE = 16;
    int ALPHA = 3;
    int BINS = 256;
    int MAX_STEPS = 8;

    long REQ_TIMEOUT = 300;
    long BUCKET_REFRESH = 7200;     //bucket refreshing interval in millis
    long DISCOVER_CYCLE = 30;       //discovery cycle interval in seconds
}
