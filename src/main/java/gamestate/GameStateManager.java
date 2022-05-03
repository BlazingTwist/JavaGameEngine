package gamestate;

import ecs.EntityRegistry;
import java.lang.reflect.InvocationTargetException;
import java.util.Stack;
import logging.LogbackLoggerProvider;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL45;
import org.slf4j.Logger;
import utils.input.InputManager;

public class GameStateManager implements IGameStateManager{
	private static final Logger logger = LogbackLoggerProvider.getLogger(GameStateManager.class);
	private static final long noStateWaitDelayMillis = 1000L / 10L;
	private static GameStateManager instance;

	public static GameStateManager getInstance() {
		if (instance == null) {
			instance = new GameStateManager();
		}
		return instance;
	}

	private final Stack<BaseGameState> gameStates = new Stack<>();
	private Class<? extends BaseGameState> gameStateToStart = null;
	private long lastUpdateMillis;
	private long lastDrawMillis;

	private GameStateManager() {
		lastUpdateMillis = System.currentTimeMillis();
		lastDrawMillis = System.currentTimeMillis();
	}

	@Override
	public boolean _hasGameStates() {
		return !gameStates.isEmpty() || gameStateToStart != null;
	}

	@Override
	public void _startGameState(Class<? extends BaseGameState> gameState) {
		gameStateToStart = gameState;
	}

	/**
	 * @param window glfw window index (pointer?)
	 * @return amount of time in milliseconds until the next event
	 */
	@Override
	public long _updateGameStates(long window) {
		if (!_hasGameStates()) {
			return noStateWaitDelayMillis;
		}

		long now = System.currentTimeMillis();
		// TODO if the pc is too slow for the target intervals, we're just building up time deficits that never get depleted

		long millisUntilUpdate = Time.physicsDeltaMilliseconds - (now - lastUpdateMillis);
		/*if (millisUntilUpdate < (-30 * Time.physicsDeltaMilliseconds)) {
			logger.warn("Simulation is more than 30 frames behind! millisUntilUpdate: {}", millisUntilUpdate);
		}*/

		BaseGameState currentGameState = null;

		boolean newStateStarted = false;
		if (gameStateToStart != null) {
			try {
				if (!gameStates.isEmpty()) {
					gameStates.peek().onPause();
				}
				logger.info("starting state: {}", gameStateToStart);
				currentGameState = gameStateToStart.getDeclaredConstructor().newInstance();
				gameStateToStart = null;
				newStateStarted = true;
				gameStates.push(currentGameState);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				logger.error("Failed to create game state '{}'", gameStateToStart, e);
			}
		}

		if (millisUntilUpdate <= 0 || newStateStarted) {
			millisUntilUpdate += Time.physicsDeltaMilliseconds;
			lastUpdateMillis += Time.physicsDeltaMilliseconds;

			InputManager.getInstance().reset();
			GLFW.glfwPollEvents();

			currentGameState = currentGameState == null ? gameStates.peek() : currentGameState;
			while (true) {
				currentGameState.update();
				if (currentGameState.isFinished()) {
					currentGameState.onExit();
					gameStates.pop();
					if (gameStates.isEmpty()) {
						return noStateWaitDelayMillis;
					}
					currentGameState = gameStates.peek();
					currentGameState.onResume();
				} else {
					break;
				}
			}
			EntityRegistry.getInstance().executeUpdate();
		}

		long millisUntilDraw = Time.graphicsDeltaMilliseconds - (now - lastDrawMillis);
		if (millisUntilDraw <= 0 || newStateStarted) {
			GL45.glClear(GL45.GL_COLOR_BUFFER_BIT | GL45.GL_DEPTH_BUFFER_BIT);
			millisUntilDraw += Time.graphicsDeltaMilliseconds;
			lastDrawMillis += Time.graphicsDeltaMilliseconds;
			currentGameState = currentGameState == null ? gameStates.peek() : currentGameState;
			currentGameState.draw();
			EntityRegistry.getInstance().executeDraw();
			GLFW.glfwSwapBuffers(window);
			GL45.glClear(GL45.GL_COLOR_BUFFER_BIT | GL45.GL_DEPTH_BUFFER_BIT);
		}

		return Math.min(millisUntilUpdate, millisUntilDraw);
	}
}
