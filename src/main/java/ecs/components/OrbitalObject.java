package ecs.components;

public class OrbitalObject {
	private double mass;

	public double getMass() {
		return mass;
	}

	public OrbitalObject setMass(double mass) {
		this.mass = mass;
		return this;
	}

	public OrbitalObject(double mass) {
		this.mass = mass;
	}
}
