package memory;

import repast.simphony.engine.environment.RunEnvironment;

/**
 * 
 * @author Markus Gierenz & David Kaub Each memory entry mem_i = si, ri consists of a tuple of
 *         messages (s € S, r € R), the second one being the agent’s observed
 *         response to the first one.
 */

public class ObservationTupel{

	private final Message sendMessage;
	private final boolean respMessage; // observed response to the send message
	private int tickCreatedAt;

	// TODO if we use i as an ID we need a additional int that gives represents
	// the
	// current rank of a
	// tupel within the memory-> has to be updatet each time a entry has been
	// deleted -> cf. lookup function!

	public ObservationTupel(Message sendMessage, boolean response) {
		this.sendMessage = sendMessage;
		this.respMessage = response;
		this.tickCreatedAt = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();

	}

	public Message getSendMessage() {
		return sendMessage;
	}

	public boolean getRespMessage() {
		return respMessage;
	}

	public boolean equals(Message s, Boolean r) {
		if (sendMessage.equals(s) && respMessage == r) {
			return true;
		}
		return false;

	}

	@Override
	public String toString() {
		return "ObservationTupel: send = " + sendMessage.toString() + " answer = " + respMessage + " tickCreatedAt = " + tickCreatedAt;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ObservationTupel))
			return false;
		ObservationTupel pairo = (ObservationTupel) o;
		return this.sendMessage.equals(pairo.getSendMessage()) && this.respMessage == pairo.getRespMessage();

		// TODO @Markus: see above. If i = index. Why has it to be equal? ->
		// depends on
		// the context I guess. Or do I miss something. If it has to be the same
		// we
		// could just use == on the object ref ?
	}

	public int getTickCreatedAt() {
		return tickCreatedAt;
	}
	
	

}
