package org.openspaces.admin.gsa;

import java.util.UUID;

/**
 * @The An identifier for the request to start a new grid service agent.
 * Used to correlate a request to start a new agent with an already running agent.
 * @since 9.1.1
 * @author Itai Frenkel 
 */
public class GSAReservationId {

	private String reservationId;
	
	public GSAReservationId(String reservationId) {
		if (reservationId == null) {
			throw new IllegalArgumentException("reservationId cannot be null");
		}
		this.reservationId = reservationId;
	}
	
	@Override
	public String toString() {
		return this.reservationId;
	}

	public static GSAReservationId randomGSAReservationId() {
	    return new GSAReservationId(UUID.randomUUID().toString());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((reservationId == null) ? 0 : reservationId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GSAReservationId other = (GSAReservationId) obj;
		if (reservationId == null) {
			if (other.reservationId != null)
				return false;
		} else if (!reservationId.equals(other.reservationId))
			return false;
		return true;
	}
}
