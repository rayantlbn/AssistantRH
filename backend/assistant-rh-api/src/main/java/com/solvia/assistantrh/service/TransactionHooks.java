package com.solvia.assistantrh.service;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Actions à déclencher selon l'issue de la transaction en cours, pour garder fichiers et base cohérents :
 * - afterCommit : supprimer un fichier une fois son entité supprimée pour de bon ;
 * - afterRollback : supprimer un fichier déjà écrit si l'entité qui devait le référencer n'est finalement pas enregistrée.
 * Sans transaction active, afterCommit exécute l'action immédiatement et afterRollback ne fait rien.
 */
final class TransactionHooks {

    private TransactionHooks() {
    }

    static void afterCommit(Runnable action) {
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

    static void afterRollback(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    action.run();
                }
            }
        });
    }
}
