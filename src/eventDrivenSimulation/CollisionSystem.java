package eventDrivenSimulation;

import java.awt.Color;
import java.util.Scanner;

public class CollisionSystem {

	private static final double HZ = 0.5;	//number of redraw events per clock tick
	
	private MinPQ<Event> pq;	//the priority queue
	private double t = 0.0;		//simulation clock time
	private Particle[] particles;	//array of particles
	
	
	public CollisionSystem(Particle[] particles) {
		this.particles = particles.clone();
	}
	
	
	//updates priority queue with all new events for particle a
	private void predict(Particle a, double limit) {
		if(a == null) return;
		
		//particle-particle collision
		for(int i = 0; i < particles.length; i++) {
			double dt = a.timeToHit(particles[i]);
			if(t + dt <= limit)
				pq.insert(new Event(t + dt, a, particles[i]));
		}
		
		//particle-wall collision
		double dtX = a.timeToHitVerticalWall();
		double dtY = a.timeToHitHorizontalWall();
		if(t + dtX < limit) pq.insert(new Event(t + dtX, a, null));
		if(t + dtY < limit) pq.insert(new Event(t + dtY, null, a));
	}
	
	//redraw all particles
	private void redraw(double limit) {
		StdDraw.clear();
		for(int i = 0; i < particles.length; i++) {
			particles[i].draw();
		}
		StdDraw.show();
		StdDraw.pause(20);
		if(t < limit) {
			pq.insert(new Event(t + 1.0 / HZ, null, null));
		}
	}
	
	//simulates the system of particles for specified amount of time
	public void simulate(double limit) {
		
		//initialize PQ with collision events and redraw event
		pq = new MinPQ<Event>();
		for(int i = 0; i < particles.length; i++) {
			predict(particles[i], limit);
		}
		
		pq.insert(new Event(0, null, null));    //redraw event
		
		
		//main event-driven simulation loop
		while(!pq.isEmpty()) {
			
			//get impending event, discard if invalidated
			Event e = pq.delMin();
			if(!e.isValid()) continue;
			Particle a = e.a;
			Particle b = e.b;
			
			//physical collision, so update positions and then simulation clock
			for(int i = 0; i < particles.length; i++) {
				particles[i].move(e.time - t);
			}
			t = e.time;
			
			//process event
			if (a != null && b != null) a.bounceOff(b);
			else if (a != null && b == null) a.bounceOffVerticalWall();
			else if (a == null && b != null) b.bounceOffHorizontalWall();
			else if (a == null && b == null) redraw(limit);
			
			//update pq with new collisions
			predict(a, limit);
			predict(b, limit);
		}
	}
	
	
	private static class Event implements Comparable<Event> {
        private final double time;         // time that event is scheduled to occur
        private final Particle a, b;       // particles involved in event, possibly null
        private final int countA, countB;  // collision counts at event creation
                
        
        // create a new event to occur at time t involving a and b
        public Event(double t, Particle a, Particle b) {
            this.time = t;
            this.a    = a;
            this.b    = b;
            if (a != null) countA = a.count();
            else           countA = -1;
            if (b != null) countB = b.count();
            else           countB = -1;
        }

        // compare times when two events will occur
        public int compareTo(Event that) {
            return Double.compare(this.time, that.time);
        }
        
        // has any collision occurred between when event was created and now?
        public boolean isValid() {
            if (a != null && a.count() != countA) return false;
            if (b != null && b.count() != countB) return false;
            return true;
        }
   
    }
	
	public static void main(String[] args) {

        StdDraw.setCanvasSize(600, 600);

        // enable double buffering
        StdDraw.enableDoubleBuffering();

        // the array of particles
        Particle[] particles;
        
        Scanner sc = new Scanner(System.in);

        // create n random particles
        if (args.length == 1) {
            int n = Integer.parseInt(args[0]);
            particles = new Particle[n];
            for (int i = 0; i < n; i++)
                particles[i] = new Particle();
        }

        // or read from standard input
        else {
            int n = sc.nextInt();
            particles = new Particle[n];
            for (int i = 0; i < n; i++) {
                double rx     = sc.nextDouble();
                double ry     = sc.nextDouble();
                double vx     = sc.nextDouble();
                double vy     = sc.nextDouble();
                double radius = sc.nextDouble();
                double mass   = sc.nextDouble();
                int r         = sc.nextInt();
                int g         = sc.nextInt();
                int b         = sc.nextInt();
                Color color   = new Color(r, g, b);
                particles[i] = new Particle(rx, ry, vx, vy, radius, mass, color);
            }
        }
        
        sc.close();

        // create collision system and simulate
        CollisionSystem system = new CollisionSystem(particles);
        system.simulate(10000);
    }
}
