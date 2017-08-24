package dev.zhihexireng.node;

import dev.zhihexireng.core.Transaction;
import dev.zhihexireng.core.TransactionPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("txs")
public class TransactionController {
    private final TransactionPool txPool;

    @Autowired
    public TransactionController(TransactionPool txPool) {
        this.txPool = txPool;
    }

    @PostMapping
    public ResponseEntity add(@RequestBody TxDto request) {
        Transaction tx = TxDto.of(request);
        Transaction addedTx = txPool.addTx(tx);
        return ResponseEntity.ok(TxDto.createBy(addedTx));
    }

    @GetMapping("{id}")
    public ResponseEntity get(@PathVariable String id) {
        Transaction tx = txPool.getTxByHash(id);

        if (tx == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(TxDto.createBy(tx));
    }
}
