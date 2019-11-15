package simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;

public class DemandManager {

	private List<AgentConsumer> consumers = new ArrayList<>();
	private List<AgentManufacturer> manufacturer = new ArrayList<>();
	private int dynamicDemandsPeriod = RunEnvironment.getInstance().getParameters()
			.getInteger("_dynamic_demands_period");
	private int numberOfDemandChanges = 0;

	public DemandManager(Map<Integer, List<Agent>> agents) {

		for (Agent a : agents.get(agents.size())) {
			this.consumers.add((AgentConsumer) a);
		}
		for (int i = 2; i < agents.size(); i++) {

			for (Agent a : agents.get(agents.size() - 1)) {
				this.manufacturer.add((AgentManufacturer) a);
			}
		}

		initAgents();
	}

	public void initAgents() {

		int dynamicDemandsPhaseShift = dynamicDemandsPeriod / consumers.size();
		for (int i = 0; i < consumers.size(); i++) {
			consumers.get(i).setDemandPhaseShift(dynamicDemandsPhaseShift * i);
		}
		setNewDemands();
	}

	private void setNewDemands() {
		// MyDebugger.__("Setting new Demand to agents");
		for (AgentConsumer consumer : consumers) {
			consumer.updateDemand(2);
		}

		for (Agent aManufacturer : manufacturer) {
			aManufacturer.updateDemand(2);
		}
	}	

	public int getTotalDemandChanges() {
		return numberOfDemandChanges;
	}

}
