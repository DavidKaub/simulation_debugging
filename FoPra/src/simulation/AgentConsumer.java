package simulation;

import java.util.List;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;

public class AgentConsumer extends Agent {

	private int demandPhaseShift;

	public AgentConsumer(int agentId, List<Agent> suppliers, SimulationBuilder simulationBuilder) {
		super(agentId, suppliers, simulationBuilder);
	}

	private void processConsumption() {
		if (inboundInventoryLevel >= demand) {
			inboundInventoryLevel -= demand;
		}
	}

	public void setDemandPhaseShift(int phaseShiftValue) {
		demandPhaseShift = phaseShiftValue;
	}

	public int getDemandPhaseShift() {
		return demandPhaseShift;
	}

	@Override
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		resetRecentValues();
		tickCount = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		if (tickCount == 1) {
			init();
		}
		this.outboundInventorySize = (1 + 1) * 2;

		readInbox();

		createOrder();

		processConsumption();

	}

}
