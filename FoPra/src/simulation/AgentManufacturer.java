package simulation;

import java.util.List;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;

public class AgentManufacturer extends Agent{
	
	int productionUnits;

	public AgentManufacturer(int agentId, List<Agent> suppliers, SimulationBuilder simulationBuilder) {
		super(agentId, suppliers, simulationBuilder);
	}

	@Override
	@ScheduledMethod (start = 1, interval = 1)
	public void step() {
		
		productionUnits = 1 + 1;
		
		resetRecentValues();
		tickCount = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		if (tickCount == 1) {
			init();
		}
		this.outboundInventorySize = (1 + 1) * 2;	
		
		readInbox();		
		createOrder();
		processProduction();
		
	}
	
	protected void processProduction() {
		if (producedRecently) {			
			outboundInventoryLevel += productionUnits;
			recentProducedItems += productionUnits;
			totalProducedItems += productionUnits;			
			producedRecently = false;	
			if(outboundInventoryLevel>outboundInventorySize) {
				//MyDebugger.__("Error!!! - outbound Inventory overflow");
				System.out.println("Error!!! - outbound Inventory overflow");
				throw new IllegalStateException();
			}
		}
		if (outboundInventoryLevel < outboundInventorySize - productionUnits) {
			// produces productionUnits at a time!
			if (inboundInventoryLevel >= productionUnits) {
				// then prouduce if enough inbound materials
				producedRecently = true;
				//MyDebugger.__("manufacturer "+this.getId()+ " produces - outbound inventory = "+this.outboundInventoryLevel);
				inboundInventoryLevel -= productionUnits;
			}
		}
	}
}
