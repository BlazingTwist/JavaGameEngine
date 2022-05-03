package main;

import ecs.EntityRegistry;
import ecs.systems.ApplyScaleVelocitySystem;
import ecs.systems.ApplyVelocitySystem;
import ecs.systems.ComputeDataSystem;
import ecs.systems.LifeTimeLightIntensitySystem;
import ecs.systems.LifeTimeSystem;
import ecs.systems.LightManagerSystem;
import ecs.systems.OrbitalSystem;
import ecs.systems.RotationalVelocitySystem;
import ecs.systems.ShockwaveExpandingAnimatorSystem;
import ecs.systems.ShockwaveManagerSystem;
import ecs.systems.SphereParticleManagerSystem;
import gamestate.IGameStateManager;
import gamestate.states.MainState;
import logging.GLFWErrorLogback;
import logging.LogbackLoggerProvider;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL45;
import org.slf4j.Logger;
import utils.WindowInfo;
import utils.input.InputManager;

public class Main {
	public static void main(String[] args) {
		new Main().run();
	}

	public static long getWindow() {
		return window;
	}

	private static final Logger logger = LogbackLoggerProvider.getLogger(Main.class);

	private static long window;

	public Main() {
		GLFWErrorCallback.create(new GLFWErrorLogback()).set();

		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
		GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

		window = GLFW.glfwCreateWindow(1366, 1366, "Java Engine", 0, 0);
		if (window == 0) {
			throw new RuntimeException("Failed to create the GLFW window");
		}

		WindowInfo.getInstance().setWindowPtr(window);

		GLFW.glfwSetKeyCallback(window, InputManager.getInstance()::keyCallback);
		GLFW.glfwSetMouseButtonCallback(window, InputManager.getInstance()::mouseButtonCallback);
		GLFW.glfwSetCursorPosCallback(window, InputManager.getInstance()::cursorPositionCallback);

		GLFW.glfwMakeContextCurrent(window);
		//GLFW.glfwSwapInterval(0); // TODO this disables VSYNC
		GLFW.glfwShowWindow(window);
	}

	public static void checkError() {
		int glError = GL45.glGetError();
		if (glError != GL45.GL_NO_ERROR) {
			switch (glError) {
				case GL45.GL_INVALID_ENUM -> logger.error("GL_INVALID_ENUM");
				case GL45.GL_INVALID_VALUE -> logger.error("GL_INVALID_VALUE");
				case GL45.GL_INVALID_OPERATION -> logger.error("GL_INVALID_OPERATION");
				case GL45.GL_INVALID_FRAMEBUFFER_OPERATION -> logger.error("GL_INVALID_FRAMEBUFFER_OPERATION");
				case GL45.GL_OUT_OF_MEMORY -> logger.error("GL_OUT_OF_MEMORY");
				default -> logger.error("Unknown Error: " + glError);
			}
			logger.error("bad!");
		}
	}

	public void run() {
		GL.createCapabilities();
		GL45.glClearColor(0.05f, 0f, 0.05f, 1f);
		GL45.glEnable(GL45.GL_DEPTH_TEST);

		EntityRegistry.getInstance().registerSystem(ApplyVelocitySystem.getInstance());
		EntityRegistry.getInstance().registerSystem(ApplyScaleVelocitySystem.getInstance());
		EntityRegistry.getInstance().registerSystem(OrbitalSystem.getInstance());
		EntityRegistry.getInstance().registerSystem(ShockwaveExpandingAnimatorSystem.getInstance());
		EntityRegistry.getInstance().registerSystem(RotationalVelocitySystem.getInstance());
		EntityRegistry.getInstance().registerSystem(LifeTimeLightIntensitySystem.getInstance());
		EntityRegistry.getInstance().registerSystem(LifeTimeSystem.getInstance());

		EntityRegistry.getInstance().registerSystem(ComputeDataSystem.getInstance());
		EntityRegistry.getInstance().registerSystem(LightManagerSystem.getInstance());
		EntityRegistry.getInstance().registerSystem(SphereParticleManagerSystem.getInstance());
		EntityRegistry.getInstance().registerSystem(ShockwaveManagerSystem.getInstance());

		IGameStateManager.startGameState(MainState.class);

		while (!GLFW.glfwWindowShouldClose(window) && IGameStateManager.hasGameStates()) {
			checkError();
			if (InputManager.getInstance().getKeyDown(GLFW.GLFW_KEY_ESCAPE)) {
				GLFW.glfwSetWindowShouldClose(window, true);
				break;
			}

			long timeUntilNextEvent = IGameStateManager.updateGameStates(window);
			if (timeUntilNextEvent > 0) {
				try {
					//noinspection BusyWait
					Thread.sleep(timeUntilNextEvent);
				} catch (InterruptedException e) {
					logger.error("Failed to sleep until next state event", e);
				}
			}
		}

		Callbacks.glfwFreeCallbacks(window);
		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
		GLFW.glfwSetErrorCallback(null).free();
	}
}
