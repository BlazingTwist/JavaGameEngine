package gamestate.states;

import gamestate.BaseGameState;
import gamestate.IGameStateManager;
import logging.LogbackLoggerProvider;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import utils.input.InputManager;

public class MainState extends BaseGameState {

	private static final Logger logger = LogbackLoggerProvider.getLogger(MainState.class);

	private static void printInfo() {
		logger.info("- Recursion");
		logger.info("-- [0] : enter a new main state");
		logger.info("-- [X] : exit this state");

		logger.info("- Physics Playground");
		logger.info("-- [1] : spring demo");
		logger.info("-- [2] : free fall demo");
		logger.info("-- [3] : orbit demo");

		logger.info("- Rendering Tech-Demos");
		logger.info("-- [4] : parallax occlusion demo");
		logger.info("-- [5] : light demo");
		logger.info("-- [6] : perlin demo");
		logger.info("-- [7] : marching cubes demo");

		logger.info("- Almost-Games");
		logger.info("-- [8] : collision state");
		logger.info("-- [9] : Space-Sim state");
	}

	public MainState() {
		logger.info("Starting Main State");
		printInfo();
	}

	@Override
	public void update() {
		InputManager inputManager = InputManager.getInstance();
		if (inputManager.getKeyDown(GLFW.GLFW_KEY_1)) {
			logger.info("main state -> spring demo");
			IGameStateManager.startGameState(SpringDemo.class);
		} else if (inputManager.getKeyDown(GLFW.GLFW_KEY_2)) {
			logger.info("main state -> free fall demo");
			IGameStateManager.startGameState(FreeFallDemo.class);
		} else if (inputManager.getKeyDown(GLFW.GLFW_KEY_3)) {
			logger.info("main state -> orbit demo");
			IGameStateManager.startGameState(OrbitDemo.class);
		} else if (inputManager.getKeyDown(GLFW.GLFW_KEY_4)) {
			logger.info("main state -> parallax occlusion demo");
			IGameStateManager.startGameState(ParallaxOcclusionDemo.class);
		} else if (inputManager.getKeyDown(GLFW.GLFW_KEY_5)) {
			logger.info("main state -> light demo");
			IGameStateManager.startGameState(LightDemo.class);
		} else if (inputManager.getKeyDown(GLFW.GLFW_KEY_6)) {
			logger.info("main state -> perlin demo");
			IGameStateManager.startGameState(PerlinDemo.class);
		} else if (inputManager.getKeyDown(GLFW.GLFW_KEY_7)) {
			logger.info("main state -> marching cubes demo");
			IGameStateManager.startGameState(MarchingCubesDemo.class);
		} else if (inputManager.getKeyDown(GLFW.GLFW_KEY_8)) {
			logger.info("main state -> collision state");
			IGameStateManager.startGameState(CollisionDemo.class);
		} else if (inputManager.getKeyDown(GLFW.GLFW_KEY_9)) {
			logger.info("main state -> space-sim");
			IGameStateManager.startGameState(SpaceSim.class);
		} else if (inputManager.getKeyDown(GLFW.GLFW_KEY_0)) {
			logger.info("main state -> main state");
			IGameStateManager.startGameState(MainState.class);
		} else if (inputManager.getKeyDown(GLFW.GLFW_KEY_X)) {
			isFinished = true;
		}
	}

	@Override
	public void draw() {
	}

	@Override
	public void onResume() {
		logger.info("Resuming Main State");
		printInfo();
		InputManager.getInstance().reset();
	}

	@Override
	public void onPause() {
		logger.info("===== Main State paused =====");
	}

	@Override
	public void onExit() {
		logger.info("===== Main State closed =====");
	}
}
