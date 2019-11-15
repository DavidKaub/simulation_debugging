package memory;

import repast.simphony.engine.environment.RunEnvironment;
import simulation.Agent;

public class  Message {
	
	private Agent receiver;
	private Agent sender;
	private int orderSize;
	private int tickCreatedAt;
	
	public Message(Agent receiver, Agent sender, int orderSize){
		this.sender = sender;
		this.receiver = receiver;
		this.orderSize = orderSize;
		this.tickCreatedAt = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	}
	
	
	
	public boolean equals(Message message) {		
		return (this.receiver == message.receiver  && this.orderSize == message.orderSize);	
		 		
	}
	/**
	 * Returns the logic difference / Similarity to a other message based on the orderSize. Used for determining alternative Messages
	 * @param message
	 * @return
	 */
	public double getDelta(Message message) {
		if(message.getReceiver() == this.receiver ) {
			return Math.abs(message.getOrderSize()-this.orderSize);
		}
		//if is not compareable the value the distance is maxed
		return Double.MAX_VALUE;				
	}

	public Agent getReceiver() {
		return receiver;
	}
	public int getOrderSize() {
		return orderSize;
	}

	public Agent getSender() {
		return sender;
	}
	public int getTickCreatedAt(){
		return tickCreatedAt;
	}



	@Override
	public String toString() {
		String theSender = "null";
		if(sender != null){
			theSender = ""+sender.getId();
		}
		String theReceiver = "null";
		if(receiver != null){
			theReceiver = ""+receiver.getId();
		}
		return "Message from agent nr: "+theSender+ " to agent nr: "+ theReceiver+  " orderSize = "+ orderSize;
	}
	
	
	public String toJsonFormat(){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{");		
		stringBuilder.append("\"sender\":"+sender.getAgentId()+",");
		stringBuilder.append("\"receiver\":"+receiver.getAgentId()+",");
		stringBuilder.append("\"orderSize\":"+orderSize);
		stringBuilder.append("}");
		//TODO
		return stringBuilder.toString();
	}
	
	
	
	
	

}
