package com.solvia.assistantrh.service;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Exécute une action une fois la transaction validée (ex. supprimer un fichier après la suppression de son entité),
 * ou immédiatement s'il n'y a pas de transaction. Si la transaction est annulée, l'action n'est pas exécutée.
 */
final class AfterCommit {

    private AfterCommit() {
    }

    static void run(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
