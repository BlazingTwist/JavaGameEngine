package gamestate;

public interface IGameStateManager {
	static IGameStateManager getInstance() {
		//return BenchmarkStateManager.getInstance();
		return GameStateManager.getInstance();
	}

	static boolean hasGameStates() {
		return getInstance()._hasGameStates();
	}

	static void startGameState(Class<? extends BaseGameState> gameState) {
		getInstance()._startGameState(gameState);
	}

	static long updateGameStates(long window) {
		return getInstance()._updateGameStates(window);
	}

	boolean _hasGameStates();

	void _startGameState(Class<? extends BaseGameState> gameState);

	long _updateGameStates(long window);
}
