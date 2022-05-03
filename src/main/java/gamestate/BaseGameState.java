package gamestate;

public abstract class BaseGameState {
	protected boolean isFinished = false;

	public boolean isFinished() {
		return isFinished;
	}

	public abstract void update();

	public abstract void draw();

	public abstract void onResume();

	public abstract void onPause();

	public abstract void onExit();
}
