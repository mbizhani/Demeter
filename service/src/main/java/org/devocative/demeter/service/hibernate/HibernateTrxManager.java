package org.devocative.demeter.service.hibernate;

import org.devocative.demeter.DemeterErrorCode;
import org.devocative.demeter.DemeterException;
import org.devocative.demeter.iservice.persistor.IPersistorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

public class HibernateTrxManager extends AbstractPlatformTransactionManager {
	private static final Logger logger = LoggerFactory.getLogger(HibernateTrxManager.class);

	@Autowired
	private IPersistorService persistorService;

	// ------------------------------

	public HibernateTrxManager() {
		logger.info("HibernateTrxManager Instantiated");
	}

	// ------------------------------

	@Override
	protected Object doGetTransaction() throws TransactionException {
		logger.debug("HibernateTrxManager.doGetTransaction");

		return "TRX";
	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
		logger.debug("HibernateTrxManager.doBegin: {}", transaction);

		if (validPropagation(definition.getPropagationBehavior())) {
			persistorService.startTrx();
		}

		assertUnsupportedPropagation(definition.getPropagationBehavior());

	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
		logger.debug("HibernateTrxManager.doCommit");

		persistorService.commitOrRollback();
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
		logger.debug("HibernateTrxManager.doRollback");

		persistorService.rollback();
	}

	// ------------------------------

	private boolean validPropagation(int propagation) {
		switch (propagation) {
			case TransactionDefinition.PROPAGATION_MANDATORY:
			case TransactionDefinition.PROPAGATION_NESTED:
			case TransactionDefinition.PROPAGATION_REQUIRED:
			case TransactionDefinition.PROPAGATION_SUPPORTS:
				return true;

		}

		return false;
	}

	private void assertUnsupportedPropagation(int propagation) {
		switch (propagation) {
			case TransactionDefinition.PROPAGATION_REQUIRES_NEW:
			case TransactionDefinition.PROPAGATION_NOT_SUPPORTED:
			case TransactionDefinition.PROPAGATION_NEVER:
				throw new DemeterException(DemeterErrorCode.TrxPropagationNotSupported);

		}
	}
}
