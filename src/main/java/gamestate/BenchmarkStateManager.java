package gamestate;

import ecs.EntityRegistry;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;
import logging.LogbackLoggerProvider;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL45;
import org.slf4j.Logger;
import utils.input.InputManager;

/**
 * Plug in this StateManager to run without frame-rate caps for benchmark purposes
 */
public class BenchmarkStateManager implements IGameStateManager {
	private static final Logger logger = LogbackLoggerProvider.getLogger(BenchmarkStateManager.class);
	private static BenchmarkStateManager instance;

	public static BenchmarkStateManager getInstance() {
		if (instance == null) {
			instance = new BenchmarkStateManager();
		}
		return instance;
	}

	private final long[] tickDeltaTimes = new long[1000];
	private int currentTickIndex = 0;
	private int measureStartTickIndex = 0;

	private final Stack<BaseGameState> gameStates = new Stack<>();
	private Class<? extends BaseGameState> gameStateToStart = null;
	private long lastTickMillis;

	private BenchmarkStateManager() {
		lastTickMillis = System.currentTimeMillis();
	}

	@Override
	public boolean _hasGameStates() {
		return !gameStates.isEmpty() || gameStateToStart != null;
	}

	@Override
	public void _startGameState(Class<? extends BaseGameState> gameState) {
		gameStateToStart = gameState;
	}

	public void startMeasure() {
		measureStartTickIndex = currentTickIndex;
	}

	public long[] getMeasuredTickDeltas() {
		if (currentTickIndex > measureStartTickIndex) {
			long[] result = new long[currentTickIndex - measureStartTickIndex];
			for (int i = measureStartTickIndex, resultIndex = 0; i < currentTickIndex; i++, resultIndex++) {
				result[resultIndex] = tickDeltaTimes[i];
			}
			return result;
		} else if (currentTickIndex == measureStartTickIndex) {
			return new long[0];
		} else {
			int measuredTickCount = tickDeltaTimes.length - (measureStartTickIndex - currentTickIndex);
			long[] result = new long[measuredTickCount];
			for (int i = 0; i < measuredTickCount; i++) {
				result[i] = tickDeltaTimes[(i + measureStartTickIndex) % tickDeltaTimes.length];
			}
			return result;
		}
	}

	public void printBenchmarkStats() {
		long[] tickDeltas = BenchmarkStateManager.getInstance().getMeasuredTickDeltas();
		int tickDeltaCount = tickDeltas.length;
		double averageDeltaMS = 0;
		for (long tickDelta : tickDeltas) {
			averageDeltaMS += (tickDelta / ((double) tickDeltaCount));
		}
		long minMS = Long.MAX_VALUE;
		long maxMS = Long.MIN_VALUE;
		long minMS10c = Long.MAX_VALUE;
		long maxMS10c = Long.MIN_VALUE;
		for (int i = 0; i < tickDeltaCount; i++) {
			if (tickDeltas[i] < minMS) {
				minMS = tickDeltas[i];
			}
			if (tickDeltas[i] > maxMS) {
				maxMS = tickDeltas[i];
			}
			long totalMS10c = 0;
			for (int i2 = 0; i2 < 10; i2++) {
				int index = (i + i2) % tickDeltaCount;
				totalMS10c += tickDeltas[index];
			}
			if (totalMS10c < minMS10c) {
				minMS10c = totalMS10c;
			}
			if (totalMS10c > maxMS10c) {
				maxMS10c = totalMS10c;
			}
		}
		logger.info("""
				benchmark measure result:
				  tickCount : {}
				  average ms: {}
				  min     ms: {}
				  max     ms: {}
				  min 10c ms: {}
				  max 10c ms: {}""", tickDeltaCount, averageDeltaMS, minMS, maxMS, minMS10c, maxMS10c);
	}

	@Override
	public long _updateGameStates(long window) {
		if (!_hasGameStates()) {
			return -1;
		}

		long now = System.currentTimeMillis();
		tickDeltaTimes[currentTickIndex] = (now - lastTickMillis);
		currentTickIndex = (currentTickIndex + 1) % tickDeltaTimes.length;
		if (currentTickIndex == measureStartTickIndex) {
			measureStartTickIndex = (measureStartTickIndex + 1) % tickDeltaTimes.length;
		}
		lastTickMillis = now;

		BaseGameState currentGameState = null;

		if (gameStateToStart != null) {
			try {
				if (!gameStates.isEmpty()) {
					gameStates.peek().onPause();
				}
				logger.info("starting state: {}", gameStateToStart);
				currentGameState = gameStateToStart.getDeclaredConstructor().newInstance();
				gameStateToStart = null;
				gameStates.push(currentGameState);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				logger.error("Failed to create game state '{}'", gameStateToStart, e);
			}
		}

		InputManager.getInstance().reset();
		GLFW.glfwPollEvents();

		currentGameState = currentGameState == null ? gameStates.peek() : currentGameState;
		while (true) {
			currentGameState.update();
			if (currentGameState.isFinished()) {
				currentGameState.onExit();
				gameStates.pop();
				if (gameStates.isEmpty()) {
					return -1;
				}
				currentGameState = gameStates.peek();
				currentGameState.onResume();
			} else {
				break;
			}
		}
		EntityRegistry.getInstance().executeUpdate();

		GL45.glClear(GL45.GL_COLOR_BUFFER_BIT | GL45.GL_DEPTH_BUFFER_BIT);
		currentGameState.draw();
		EntityRegistry.getInstance().executeDraw();
		GLFW.glfwSwapBuffers(window);
		GL45.glClear(GL45.GL_COLOR_BUFFER_BIT | GL45.GL_DEPTH_BUFFER_BIT);

		return -1;
	}
}
