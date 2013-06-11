package org.openspaces.grid.gsm.sla.exceptions;

/**
 * @author itaif
 * @since 9.6
 */
public class TooManyEsmComponentsException extends WrongNumberOfESMComponentsException {

	private static final long serialVersionUID = 1L;

	public TooManyEsmComponentsException(int numberOfEsms, String puName) {
		super(numberOfEsms, puName);
	}

}
