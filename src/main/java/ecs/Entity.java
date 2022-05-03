package ecs;

import ecs.components.AABBCollider;
import ecs.components.LifeTime;
import ecs.components.LifeTimeLightIntensity;
import ecs.components.Light;
import ecs.components.MarchingCubesMesh;
import ecs.components.Mesh;
import ecs.components.OrbitalObject;
import ecs.components.RotationalVelocity;
import ecs.components.ScaleVelocity;
import ecs.components.Shockwave;
import ecs.components.ShockwaveExpandingAnimator;
import ecs.components.SphereParticle;
import ecs.components.Transform;
import ecs.components.Velocity;

// TODO store the owning state? this way states can clear all entities without storing them separately
public class Entity {

	int ecsIndex;
	boolean isExpired = false;

	public Transform transform;
	public Mesh meshComponent;
	public MarchingCubesMesh marchingCubesMesh;
	public Light lightComponent;
	public Velocity velocityComponent;
	public ScaleVelocity scaleVelocity;
	public OrbitalObject orbitalComponent;
	public SphereParticle sphereParticleComponent;
	public Shockwave shockwaveComponent;
	public ShockwaveExpandingAnimator shockwaveExpandingAnimator;
	public AABBCollider aabbCollider;
	public RotationalVelocity rotationalVelocity;
	public LifeTime lifeTimeComponent;
	public LifeTimeLightIntensity lifeTimeLightIntensityComponent;

	Entity(int ecsIndex) {
		this.ecsIndex = ecsIndex;
	}

	public boolean isExpired() {
		return isExpired;
	}
}
