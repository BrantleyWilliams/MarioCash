package dev.zhihexireng.core.net;

import java.util.Comparator;

public class TimeComparator implements Comparator<Peer> {
    @Override
    public int compare(Peer p1, Peer p2) {
        long t1 = p1.getModified();
        long t2 = p2.getModified();

        return Long.compare(t2, t1);
    }
}
