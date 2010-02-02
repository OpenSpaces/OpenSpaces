package org.openspaces.itest.jotm;

import javax.naming.NamingException;
import javax.transaction.SystemException;

import org.objectweb.jotm.Current;
import org.objectweb.jotm.Jotm;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;

/**
 */
public class JotmFactoryBean implements FactoryBean, DisposableBean {

	private Current jotmCurrent;

	private Jotm jotm;


	public JotmFactoryBean() throws NamingException {
		// Check for already active JOTM instance.
		this.jotmCurrent = Current.getCurrent();

		// If none found, create new local JOTM instance.
		if (this.jotmCurrent == null) {
			// Only for use within the current Spring context:
			// local, not bound to registry.
			this.jotm = new Jotm(true, false);
			this.jotmCurrent = Current.getCurrent();
		}
	}

	/**
	 * Set the default transaction timeout for the JOTM instance.
	 * <p>Should only be called for a local JOTM instance,
	 * not when accessing an existing (shared) JOTM instance.
	 */
	public void setDefaultTimeout(int defaultTimeout) {
		this.jotmCurrent.setDefaultTimeout(defaultTimeout);
		// The following is a JOTM oddity: should be used for demarcation transaction only,
		// but is required here in order to actually get rid of JOTM's default (60 seconds).
		try {
			this.jotmCurrent.setTransactionTimeout(defaultTimeout);
		}
		catch (SystemException ex) {
			// should never happen
		}
	}


	/**
	 * Return the JOTM instance created by this factory bean, if any.
	 * Will be <code>null</code> if an already active JOTM instance is used.
	 * <p>Application code should never need to access this.
	 */
	public Jotm getJotm() {
		return this.jotm;
	}

	public Object getObject() {
		return this.jotmCurrent;
	}

	public Class getObjectType() {
		return this.jotmCurrent.getClass();
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Stop the local JOTM instance, if created by this FactoryBean.
	 */
	public void destroy() {
		if (this.jotm != null) {
			this.jotm.stop();
		}
	}

}
