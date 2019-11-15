package simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import memory.Memory;
import memory.Message;
import memory.ObservationTupel;
import repast.simphony.engine.environment.RunEnvironment;

public abstract class Agent {

	private int agentId;
	private Memory memory;
	protected int outboundInventorySize;
	protected int demand;
	protected int outboundInventoryLevel = 0;
	protected int inboundInventoryLevel = 0;
	protected List<ObservationTupel> incomingAnswerMessages = new ArrayList<>();
	protected List<Message> incomingOrderMessages = new ArrayList<>();
	protected boolean producedRecently = false;
	private List<Agent> suppliers = new ArrayList<>();
	private List<Message> unansweredMessages = new ArrayList<>();

	private int recentSentMessages= 0;
	private int totalSentMessages = 0;
	
	private int recentReceivedItems = 0;
	private int totalReceivedItems = 0;
	
	private int recentDeliveredItems = 0;
	private int totalDeliveredItems = 0;
	
	protected int recentProducedItems = 0;
	protected int totalProducedItems = 0;
	
	protected int tickCount = 0;	
	

	protected Agent(int agentId, List<Agent> suppliers,SimulationBuilder simulationBuilder) {
		this.agentId = agentId;
		if(suppliers != null) {
			this.suppliers = suppliers;
		}
		this.outboundInventorySize = (1 + 1) * 2;
		if (this.getClass() != AgentProducer.class) {
			this.memory = new Memory(this, simulationBuilder);
		}
	}


	protected void resetRecentValues() {
		recentDeliveredItems = 0;
		recentProducedItems = 0;
		recentReceivedItems = 0;
		recentSentMessages = 0;		
	}

	/**
	 * ############################ Initialization
	 */
	protected void init() {
		if (this.getClass() != AgentProducer.class) {
			memory.initMemory();
		}
	}

	/**
	 * ############################ Processing
	 */
	protected void createOrder() {
		// TODO only create order after receiving the answer of prior orders

		if (inboundInventoryLevel < getInventoryThreshold() && unansweredMessages.isEmpty()) {
			// dann bestellen
			List<Message> selectedOrderMessages = memory.messageSelection(this.getOrderBatchSize());
			if (selectedOrderMessages == null) {
				//MyDebugger.__("Error!  Collection == null!");
			}
			for (Message message : selectedOrderMessages) {
				Message messageInstance = new Message(message.getReceiver(), this, message.getOrderSize());
				messageInstance.getReceiver().addincomingOrderMessage(messageInstance);
				unansweredMessages.add(messageInstance);
				recentSentMessages++;
				totalSentMessages++;
			}
		}
	}

	protected void readInbox() {
		if (this.getClass() != AgentProducer.class)
			processAnswers();
		if (this.getClass() != AgentConsumer.class)
			processIncommingOrders();
	}

	private void processIncommingOrders() {
		Iterator<Message> iterator = incomingOrderMessages.iterator();
		while (iterator.hasNext()) {
			Message message = iterator.next();
			// //MyDebugger.__("Agent " + agentId + " has a order from " +
			// message.getSender().getId());
			/**
			 * a agent only processes the messages that have been created the tick before
			 * this models the delay a order takes till it reaches the supplier and is
			 * getting processes
			 */
			if (message.getTickCreatedAt() < tickCount) {				
				ObservationTupel answerTupel = null;
				if (message.getOrderSize() <= outboundInventoryLevel) {
					answerTupel = new ObservationTupel(message, true);
					outboundInventoryLevel -= message.getOrderSize();
					recentDeliveredItems += message.getOrderSize();
					totalDeliveredItems += message.getOrderSize();					
				} else {
					answerTupel = new ObservationTupel(message, false);
				}
				// answer
				message.getSender().addincomingAnswerMessage(answerTupel);
				iterator.remove();
			}
		}
	}
	
	public void updateDemand(int demand) {
		
		//called by DemandManager if demand changes
		this.demand = demand;
		
		
	}

	private void processAnswers() {
		Iterator<ObservationTupel> iterator = incomingAnswerMessages.iterator();
		while (iterator.hasNext()) {
			ObservationTupel observationTupel = iterator.next();
			/**
			 * to model the delay of a delivery reaching a customer after the order has been
			 * processes by the producer or manufacturer a agent only processes Answers
			 * who's original message have been created two ticks before. This this copes
			 * with the delay of the order reaching the producer/manufacturer and the
			 * delivery
			 */
			if (observationTupel.getSendMessage()
					.getTickCreatedAt() < (tickCount - 1)) {
				//dann wurde die Bestellung im letzen tick beatnworted und darf jetzt verarbeitet werden
				if (observationTupel.getRespMessage()) {
					//dann wurde die Bestellung erfüllt
					int orderSize = observationTupel.getSendMessage().getOrderSize();
					inboundInventoryLevel += orderSize;
					recentReceivedItems += orderSize;
					totalReceivedItems += orderSize;					
			
				} 
				//memory.storeMemory(observationTupel);
				unansweredMessages.remove(observationTupel.getSendMessage());
				iterator.remove();
			}
		}
	}
	
	

	/**
	 * ############################ Interface
	 */
	protected void addincomingOrderMessage(Message orderMessage) {
		incomingOrderMessages.add(orderMessage);
	}

	protected void addincomingAnswerMessage(ObservationTupel answerTupel) {
		incomingAnswerMessages.add(answerTupel);
	}

	public int getDemand() {
		return this.demand;
	}

	protected int getInventoryThreshold() {
		return demand * 3;
	}
	
	
	protected int getOrderBatchSize() {
		return demand * 2;
	}

	public int getId() {
		return agentId;
	}

	public List<Agent> getSuppliers() {
		return suppliers;
	}

	public int getAgentId() {
		return agentId;
	}

	public int getOutboundInventoryLevel() {
		return outboundInventoryLevel;
	}

	public int getInboundInventoryLevel() {
		return inboundInventoryLevel;
	}
	
	
	

	public int getRecentSentMessages() {
		return recentSentMessages;
	}

	public int getTotalSentMessages() {
		return totalSentMessages;
	}

	public int getRecentReceivedItems() {
		return recentReceivedItems;
	}

	public int getTotalReceivedItems() {
		return totalReceivedItems;
	}

	public int getRecentDeliveredItems() {
		return recentDeliveredItems;
	}

	public int getTotalDeliveredItems() {
		return totalDeliveredItems;
	}

	public int getRecentProducedItems() {
		return recentProducedItems;
	}

	public int getTotalProducedItems() {
		return totalProducedItems;
	}
	
	public void resetStats() {		
		totalSentMessages = 0;
		totalReceivedItems = 0;		
	}

	/**
	 * ############################ Abstract Methods
	 */

	abstract void step();

}
