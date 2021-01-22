package dev.zhihexireng.contract;

import dev.zhihexireng.core.TransactionHusk;
import dev.zhihexireng.core.TransactionReceipt;

public class ContractEvent {

    private TransactionReceipt transactionReceipt;

    private TransactionHusk transactionHusk;

    public TransactionReceipt getTransactionReceipt() {
        return transactionReceipt;
    }

    public TransactionHusk getTransactionHusk() {
        return transactionHusk;
    }

    public static ContractEvent of(TransactionReceipt transactionReceipt,
                                   TransactionHusk transactionHusk) {
        ContractEvent event = new ContractEvent();
        event.transactionReceipt = transactionReceipt;
        event.transactionHusk = transactionHusk;
        return event;
    }
}
