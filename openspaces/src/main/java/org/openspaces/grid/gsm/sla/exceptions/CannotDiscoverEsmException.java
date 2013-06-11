package org.openspaces.grid.gsm.sla.exceptions;

/**
 * This exception is monitored by the GSA, and causes the ESM process to restart itself.
 * It indicates that the ESM cannot discover itself and denotes a severe problem in the LookupService or the Admin API.
 * It is very rare, however a restart fixes the problem.
 * @author itaif
 *
 */
public class CannotDiscoverEsmException extends WrongNumberOfESMComponentsException {

	private static final long serialVersionUID = 1L;

	public CannotDiscoverEsmException(String puName) {
		super(0, puName);
	}
}
