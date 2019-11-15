package memory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.SimUtilities;
import simulation.Agent;
import simulation.SimulationBuilder;

public class Memory {
	/**
	 * The observation list sorts the entries in ascending order concerning the age
	 * the first entry is the youngest the last entry is the oldest
	 */
	// private Map<String, ActivationObservation> activationObservations;

	private Agent agent;
	private int memorySize;
	private List<Message> S;
	// private List<List<Message>> allValidOrderCombinations;
	// sorted Map for better performance
	private Map<Integer, List<List<Message>>> allValidOrderCombinationsWithBatchSize = new HashMap<>();// aka X?
	private int amplitude = 1;

	private int expectedDemand = 1 + amplitude;//average Demand
	private int maxDemandSize = expectedDemand + amplitude;
	private int maxBatchSize = maxDemandSize * 2;

	private String FilePath;
	private String FilePathS;
	private String FilePathX;
	private SimulationBuilder simulationBuilder;


	// MEM consists of memory pairs.

	// stores observed others’ reactions to its own messages.
	public Memory(Agent agent, SimulationBuilder simulationBuilder) {
		this.agent = agent;
		this.memorySize = RunEnvironment.getInstance().getParameters().getInteger("_memory_size");
		this.simulationBuilder = simulationBuilder;
	}	
	
	public List<Message> messageSelection(int batchSize) {
		return messageSelectionRandom(batchSize);

	}

	private List<Message> messageSelectionRandom(int batchSize) {
		List<List<Message>> subSetOfX = this.allValidOrderCombinationsWithBatchSize.get(batchSize);
		return subSetOfX.get(RandomHelper.nextIntFromTo(0, subSetOfX.size() - 1));
	}	
	
	/**
	 * ################################
	 * 		END OF MEMORY LOGIC
	 */
	
	
	public void initMemory() {

		determineFilePath();
		loadSAndX();
	}	
	
	
	
	private void determineFilePath() {
		FilePath = System.getProperty("user.dir") + "/data/memory_files/ag"
		// FilePath = "C:/Users/s4dakaub/git/fopra/FoPra/src/memory/files/ag"
				+ agent.getAgentId() + "-amp" + 1 + "-m"
				+ RunEnvironment.getInstance().getParameters().getInteger("_count_agents_per_tier") + "-n"
				+ RunEnvironment.getInstance().getParameters().getInteger("_count_tiers");
		FilePathS = FilePath + "_setS.json";
		FilePathX = FilePath + "_setX.json";
	}

	
	private void loadSAndX() {
		if (!readSetsFromFile()) {
			generateS();
			generateX();
			writeSetsToFile();
			// MyDebugger.__("Generated Messages!");
		} else {
			// MyDebugger.__("Read Messages From File!");
		}
	}

	private void generateS() {
		S = new ArrayList<>();

		List<Agent> suppliers = this.agent.getSuppliers();
		for (Agent ag : suppliers) {
			for (int orderSize = 1; orderSize <= maxBatchSize; orderSize++) {
				Message message = new Message(ag, this.agent, orderSize);
				S.add(message);
			}
		}
		// //MyDebugger.__("possible Messages size = " + S.size());
	}

	private void generateX() {

		// N stores total number of subsets
		long N = (long) Math.pow(2, S.size());

	
		// Set to store subsets
		// allValidOrderCombinations = new ArrayList<>();
		allValidOrderCombinationsWithBatchSize = new HashMap<>();
		List<Agent> containedSuppliers = new ArrayList<>();
		// generate each subset one by one
		for (int i = 0; i < N; i++) {
			List<Message> set = new ArrayList<>();
			int sum = 0;
			// check every bit of i
			for (int j = 0; j < S.size(); j++) {
				// if j'th bit of i is set, add S.get(j) to current set
				if ((i & (1 << j)) != 0) {
				
					Message message = S.get(j);
					sum += message.getOrderSize();
					if (!containedSuppliers.contains(message.getReceiver()) && sum <= maxBatchSize) {
						set.add(message);
						containedSuppliers.add(message.getReceiver());
					} else {
						set.clear();
						break;
					}
				}
			}
			containedSuppliers.clear();

			if (!set.isEmpty()) {
				int batchSizeOfSet = getTotalOrderSizeOfSet(set);

				if (allValidOrderCombinationsWithBatchSize.containsKey(batchSizeOfSet)) {
					allValidOrderCombinationsWithBatchSize.get(batchSizeOfSet).add(set);
				} else {
					List<List<Message>> list = new ArrayList<>();
					list.add(set);
					allValidOrderCombinationsWithBatchSize.put(batchSizeOfSet, list);
				}
			}
		}
	}
	private int getTotalOrderSizeOfSet(List<Message> list) {
		int totalOrderSizeOfX = 0;
		for (Message message : list) {
			totalOrderSizeOfX += message.getOrderSize();
		}
		return totalOrderSizeOfX;

	}
	private boolean readSetsFromFile() {

		JsonParser jsonParser = new JsonParser();
		// S
		try {
			File f = new File(FilePathS);
			if (!(f.exists() && !f.isDirectory())) {
				// MyDebugger.__("File does not exist -> return false");
				return false;
			}
			Object obj = jsonParser.parse(new FileReader(FilePathS));
			JsonArray jsonArray = (JsonArray) obj;
			S = new ArrayList<>();
			for (JsonElement jsonElement : jsonArray) {
				int receiverId = jsonElement.getAsJsonObject().get("receiver").getAsInt();
				Agent receiverAgent = simulationBuilder.getAgentById(receiverId);
				if (receiverAgent == null) {
					return false;
				}
				Message message = new Message(receiverAgent, this.agent,
						jsonElement.getAsJsonObject().get("orderSize").getAsInt());
				S.add(message);
			}
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			// e.printStackTrace();
			return false;
		}

		// X

		try {
			File f = new File(FilePathX);
			if (!(f.exists() && !f.isDirectory())) {
				return false;
			}
			Object obj = jsonParser.parse(new FileReader(FilePathX));
			JsonArray jsonRoot = (JsonArray) obj;
			allValidOrderCombinationsWithBatchSize = new HashMap<>();
			for (JsonElement jsonElement : jsonRoot) {
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				int key = jsonObject.get("key").getAsInt();
				List<List<Message>> listsAtCertainBatchSize = new ArrayList<>();
				allValidOrderCombinationsWithBatchSize.put(key, listsAtCertainBatchSize);
				JsonArray lists = jsonObject.get("lists").getAsJsonArray();
				for (JsonElement dataElement : lists) {
					JsonObject innerObject = dataElement.getAsJsonObject();
					// //MyDebugger.__("#########################");
					// //MyDebugger.__(innerObject.keySet());
					JsonArray dataArray = innerObject.get("data").getAsJsonArray();
					List<Message> setOfOrderCombination = new ArrayList<>();
					listsAtCertainBatchSize.add(setOfOrderCombination);

					for (JsonElement jsonMessage : dataArray) {
						int receiverId = jsonMessage.getAsJsonObject().get("receiver").getAsInt();
						Agent receiverAgent = simulationBuilder.getAgentById(receiverId);
						if (receiverAgent == null) {
							return false;
						}
						Message message = new Message(receiverAgent, this.agent,
								jsonMessage.getAsJsonObject().get("orderSize").getAsInt());
						setOfOrderCombination.add(message);
					}
				}
			}
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void writeSetsToFile() {
		// S

		PrintWriter printWriter;
		try {
			printWriter = new PrintWriter(FilePathS, "UTF-8");
			printWriter.println("[");
			for (int i = 0; i < S.size(); i++) {
				Message message = S.get(i);
				if (i < S.size() - 1) {
					printWriter.println(message.toJsonFormat() + ",");
				} else {
					printWriter.println(message.toJsonFormat());
				}
			}
			printWriter.println("]");
			printWriter.flush();
			printWriter.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		// X

		try {
			printWriter = new PrintWriter(FilePathX, "UTF-8");
			printWriter.println("[");

			for (int i = 1; i <= maxBatchSize; i++) {
				if (!allValidOrderCombinationsWithBatchSize.containsKey(i)) {
					continue;
				}

				printWriter.println("{\"key\":" + i + ",");
				printWriter.println("\"lists\":[");

				List<List<Message>> listsWithListsWithACertainLenght = allValidOrderCombinationsWithBatchSize.get(i);

				for (int k = 0; k < listsWithListsWithACertainLenght.size(); k++) {
					List<Message> list = listsWithListsWithACertainLenght.get(k);

					printWriter.println("{\"data\":[");

					for (int j = 0; j < list.size(); j++) {
						Message message = list.get(j);
						printWriter.println(message.toJsonFormat());
						if (j < list.size() - 1) {
							printWriter.print(",");
						}
					}
					printWriter.println("]}");
					if (k != listsWithListsWithACertainLenght.size() - 1) {
						printWriter.print(",");
					}
				}
				printWriter.println("]}");
				if (i != maxBatchSize) {
					printWriter.print(",");
				}
			}

			printWriter.println("]");
			printWriter.flush();
			printWriter.close();
			// MyDebugger.__("file: " + FilePathX + " written!");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public int getMemSize() {
		// returns value -1 due to the unused [0] position in the array
		return memorySize;
	}

	

}
