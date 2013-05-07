package eu.brede.common.pipeline;

public interface Controllable {
//	public void pause();
//	public void resume();
//	public void start();
//	public void stop();
	public Controller getController();
	public void setController(Controller controller); 
}
