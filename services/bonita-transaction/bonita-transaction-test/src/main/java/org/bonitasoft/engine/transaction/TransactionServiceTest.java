package org.bonitasoft.engine.transaction;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class TransactionServiceTest {

    protected abstract TransactionService getTxService() throws Exception;
    TransactionService txService;

    @Before
    public void before() throws Exception {
        txService = getTxService();
    }

    @After
    public void afterTest() throws Exception {
        try {
            txService.complete();
        } catch (Exception e) {
            // Do nothing
        }
    }

    @Test(expected = STransactionCreationException.class)
    public void testCantCreateATransactionWithActiveTx() throws Exception {
        txService.begin();
        txService.begin();
    }

    @Test(expected = STransactionCreationException.class)
    public void testCantCreateATransactionWithCreatedTx() throws Exception {
        txService.begin();
        txService.begin();
    }

    @Test(expected = STransactionCreationException.class)
    public void testCantCreateATransactionWithRollbackOnlyTx() throws Exception {
        txService.begin();
        txService.setRollbackOnly();

        try {
            txService.begin();
        } finally {
            txService.complete();
        }
    }

    @Test
    public void getNumberOfActiveTransactionsWithTransactionOpen() throws Exception {
        txService.begin();
        assertEquals(1, txService.getNumberOfActiveTransactions());
        txService.complete();
        assertEquals(0, txService.getNumberOfActiveTransactions());
    }

    @Test
    public void getNumberOfActiveTransactionsWithTransactionMarkedAsRollback() throws Exception {
        txService.begin();
        assertEquals(1, txService.getNumberOfActiveTransactions());
        txService.setRollbackOnly();
        assertEquals(1, txService.getNumberOfActiveTransactions());
        txService.complete();
        assertEquals(0, txService.getNumberOfActiveTransactions());
    }

    @Test
    public void getNumberOfActiveTransactionsWith2TransactionsOpen() throws Exception {
        CountDownLatch lock1 = new CountDownLatch(1);
        CountDownLatch lock2 = new CountDownLatch(1);

        CountDownLatch startSignal = new CountDownLatch(2);

        TransactionWorker txWorker1 = new TransactionWorker(lock1, txService, startSignal);
        TransactionWorker txWorker2 = new TransactionWorker(lock2, txService, startSignal);

        Thread t1 = new Thread(txWorker1);
        t1.setName("txWorker1");
        Thread t2 = new Thread(txWorker2);
        t2.setName("txWorker2");

        t1.start();
        t2.start();
        startSignal.await();
        assertEquals(2, txService.getNumberOfActiveTransactions());

        lock1.countDown();
        t1.join();
        assertEquals(1, txService.getNumberOfActiveTransactions());

        lock2.countDown();
        t2.join();
        assertEquals(0, txService.getNumberOfActiveTransactions());
    }


    private static class TransactionWorker implements Runnable {

        private final CountDownLatch lock;
        private final TransactionService transactionService;
        private final CountDownLatch startSignal;

        public TransactionWorker(final CountDownLatch lock, final TransactionService transactionService, final CountDownLatch startSignal) {
            this.lock = lock;
            this.transactionService = transactionService;
            this.startSignal = startSignal;
        }

        @Override
        public void run() {
            try {
                transactionService.begin();
                // Signal the caller that we are have started and the transaction has begun
                startSignal.countDown();
                // Wait until the caller tells us to
                lock.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    transactionService.complete();
                } catch (STransactionCommitException e) {
                    e.printStackTrace();
                } catch (STransactionRollbackException e) {
                    e.printStackTrace();
                }
            }

        }

    }

}
