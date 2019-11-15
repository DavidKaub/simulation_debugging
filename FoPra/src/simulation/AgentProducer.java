package simulation;


import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;

public class AgentProducer extends Agent {

	int productionUnits;
	public AgentProducer(int agentId, SimulationBuilder simulationBuilder) {
		super(agentId,null, simulationBuilder);
	}

	@Override
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		productionUnits = 1 + 1;
		resetRecentValues();
		tickCount = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		if (tickCount == 1) {
			init();
		}
		this.outboundInventorySize = (1 + 1) * 2;		

		/**
		 * 1. Process incoming Orders * c) Memorize the actually observed reactions to
		 * the selected operations. 2. Order if necessary a) Estimate the flow of
		 * potential interactions from memorized observations of past activities. b)
		 * select actions according to their expected results (i.e. other agent’s
		 * reactions).
		 * 
		 * 3. Produce new stuff
		 */

		readInbox();
		processProduction();

	}

	protected void processProduction() {
		if (producedRecently) {
			//Unser producer produziert sobald das inventar nicht ganz voll ist. Jedoch darf die Inventar größe auch nicht überschritten werden daher hier der test
			int potentialNewOutboundInventoryLevel = outboundInventoryLevel+productionUnits;
			int newOutboundInventoryLevel = Math.min(potentialNewOutboundInventoryLevel, outboundInventorySize);
			int produced = newOutboundInventoryLevel - outboundInventoryLevel;
			outboundInventoryLevel = newOutboundInventoryLevel;
			recentProducedItems += produced;
			totalProducedItems += produced;	
			producedRecently = false;
		}		
			//TODO -> siehe kommentar oben -> produzieren sobaald inventar nicht voll oder sobald es in einem schritt wieder aufgefuellt werden kann -> -1 vs -2
		if (outboundInventoryLevel < outboundInventorySize - 1) {
			// produces 2 at a time!
			// prouduces allways -> has no inbound inventory
			producedRecently = true;
			//MyDebugger.__(
				//	"producer " + this.getId() + " produces - outbound inventory = " + this.outboundInventoryLevel);
		}
	}

}
