/*
 *  Project:		Final Project (Wave Game)
 *  Program name:	Game.java
 *  Author:			Megan Ginham
 *  Date:			December 22, 2017
 *  School:			Western Canada High School
 */

package codes;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.Random;

public class Game extends Canvas implements Runnable {

	private static final long serialVersionUID = 2197800983242668289L; // auto-generated by java

	public static final int WIDTH = 640, HEIGHT = WIDTH / 12 * 9;
	private Thread thread; // single-threaded game
	private boolean running = false;
	
	private Random r;
	private Handler handler;
	private HUD hud;
	private Spawn spawner;
	private Menu menu;
	
	public enum STATE { // the different screens that exist within the game
		Menu,
		Help,
		Game,
		End
	}; // end STATE
	
	public static STATE gameState = STATE.Menu; // begin game in the menu
	
	public Game() {
		handler = new Handler();
		hud = new HUD();
		menu = new Menu(this, handler, hud);
		this.addKeyListener(new KeyInput(handler)); // tell the system that keys will be used
		this.addMouseListener(menu); // tell the system that the mouse will be used
		
		new Window(WIDTH, HEIGHT, "Final Project! (Wave game)", this); // will display in the frame
		
		spawner = new Spawn(handler, hud);
		r = new Random(); // generates a random number
		
		if(gameState == STATE.Game) // when the player clicks Play in Menu, switch to Game
		{
			handler.addObject(new Player(WIDTH/2-32, HEIGHT/2-32, ID.Player, handler)); // add Player
			handler.addObject(new BasicEnemy(r.nextInt(Game.WIDTH - 50), r.nextInt(Game.HEIGHT - 50), ID.BasicEnemy, handler)); // add BasicEnemy
		}
		else
			for(int i = 0; i < 20; i++)
			{
				handler.addObject(new MenuParticle(r.nextInt(WIDTH), r.nextInt(HEIGHT), ID.MenuParticle, handler)); // while outside of Game, have random particle effect in the background
			}
	} // end Game
	
	public synchronized void start() { // begin the thread
		thread = new Thread(this);
		thread.start();
		running = true;
	} // end start
	
	public synchronized void stop() { // kill the thread
		try
		{
			thread.join();
			running = false;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	} // end stop
	
	public void run() { // game loop
		this.requestFocus(); // not required to click on the window to "activate" it
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		int frames = 0;
		
		while(running)
		{
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			
			while(delta >= 1)
			{
				tick();
				delta--;
			}
			if(running)
				render();
			frames++;
			
			if(System.currentTimeMillis() - timer > 1000)
			{
				timer += 1000;
				frames = 0;
			}
		}
		stop();
	} // end run
	
	private void tick() {
		handler.tick();
		if(gameState == STATE.Game)
		{
			hud.tick();
			spawner.tick();
			
			if(HUD.HEALTH <= 0) // when HEALTH depletes
			{
				HUD.HEALTH = 100; // restore HEALTH for future tries
				gameState = STATE.End; // change to End screen
				handler.clearEnemies(); // remove all enemies
				for(int i = 0; i < 20; i++)
					handler.addObject(new MenuParticle(r.nextInt(WIDTH), r.nextInt(HEIGHT), ID.MenuParticle, handler)); // create random particle effect in End screen
			}
		}
		else if(gameState == STATE.Menu || gameState == STATE.End)
			menu.tick();
	} // end tick
	
	private void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null)
		{
			this.createBufferStrategy(3); // create 3 buffers to slow FPS
			return;
		}
		
		Graphics g = bs.getDrawGraphics();
		
		g.setColor(Color.black); // set background color of window
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		handler.render(g);
		
		if(gameState == STATE.Game)
			hud.render(g);
		else
			if(gameState == STATE.Menu || gameState == STATE.Help || gameState == STATE.End)
			{
				menu.render(g);
			}
		
		g.dispose();
		bs.show();
	} // end render
	
	public static float clamp(float var, float min, float max) { // give things boundaries
		if(var >= max)
			return var = max;
		else if(var <= min)
			return var = min;
		else
			return var;
	} // end clamp
	
	public static void main(String[] args) {
		new Game();
	} // end main
}
