package simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repast.simphony.context.Context;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;

public class SimulationBuilder implements ContextBuilder<Object> {
	private int agentIdCounter;
	private int countTiers;
	private int countAgentsPerTier;
	private Map<Integer, List<Agent>> agents;
	private int runLengthTicks = 3000;
	

	@Override
	public Context<Object> build(Context<Object> context) {
		// clearConsole();
		context.clear();		
		
		agentIdCounter = 1;
		context.setId("forschungspraktikum");
		getParams();
		agents = new HashMap<>();
		

/**
 * Initializing Agents
 */
		for (int tier = 1; tier <= countTiers; tier++) {
			agents.put(tier, new ArrayList<Agent>());
			for (int j = 0; j < countAgentsPerTier; j++) {
				Agent agent = null;
				if (tier == 1) {
					agent = new AgentProducer(agentIdCounter++,this);
				} else if (tier == countTiers) {
					agent = new AgentConsumer(agentIdCounter++,agents.get(tier-1),this);
				} else {
					agent = new AgentManufacturer(agentIdCounter++,agents.get(tier-1),this);
				}
				context.add(agent);
				// add agent to grid
				//grid.moveTo(agent, tier * 5, 5 + (j * 5));
				agents.get(tier).add(agent);
			}
		}
		
		new DemandManager(agents);
		
		context.add(this);
		RunEnvironment.getInstance().endAt(runLengthTicks);
		return context;
	}

	private void getParams() {

		Parameters parameters = RunEnvironment.getInstance().getParameters();
		countTiers = parameters.getInteger("_count_tiers");
		countAgentsPerTier = parameters.getInteger("_count_agents_per_tier");	
	}	

	public Agent getAgentById(int id) {
		for (int i = 1; i <= countTiers; i++) {
			List<Agent> agentsAtTier = agents.get(i);
			for (Agent agent : agentsAtTier) {
				if (agent.getAgentId() == id) {
					return agent;
				}
			}
		}
		return null;
	}
	
		
	public int getFinalNetworkThroughput() {
		int sum = 0;		
		for(Agent agent: agents.get(countTiers)) {
			//System.out.println("sum: adding value of " + agent.getAgentId() );
			sum+=agent.getTotalReceivedItems();
		}		
		System.out.println("Final network throughput = "+sum);
		return sum;
	}
	public int getRecentNetworkThroughput() {
		int sum = 0;		
		for(Agent agent: agents.get(countTiers)) {
			//System.out.println("sum: adding value of " + agent.getAgentId() );
			sum+=agent.getRecentReceivedItems();
		}		
		return sum;
	}
	
	public int getFinalCommunicationEffort() {
		int sum = 0;
		for(int i = 1; i <= countTiers; i++) {
			for(Agent agent: agents.get(i)) {
				//System.out.println("sum: adding value of " + agent.getAgentId() );
				sum+=agent.getTotalSentMessages();
			}	
		}			
		return sum;
	}
	
	public int getRecentCommunicationEffort() {
		int sum = 0;
		for(int i = 1; i <= countTiers; i++) {
			for(Agent agent: agents.get(i)) {
				//System.out.println("sum: adding value of " + agent.getAgentId() );
				sum+=agent.getRecentSentMessages();
			}	
		}			
		return sum;
	}
	
	public String getFrequencyIntervals() {
		return "Removed vor Debugging";
	}
	
	public int getTotalDemandChanges() {
		//"Removed vor Debugging"
		return 0;
	}
	public double getAverageFrequency() {
		//"Removed vor Debugging"
		return -1.0d;
	}
	
	public int getRandomSeed() {
		return RandomHelper.getSeed();
	}
	
}
