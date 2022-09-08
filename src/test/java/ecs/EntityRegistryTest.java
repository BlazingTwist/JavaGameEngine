package ecs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import utils.matrix.MatrixArithmeticIP;
import utils.matrix.Mat4f;
import utils.operator.Operator;
import utils.vector.Vec2f;
import utils.vector.Vec3f;
import utils.vector.Vec4f;

public class EntityRegistryTest {
	static class Components {
		static class TestComponent {
			int size;
			float fontSize;
			Vec3f position;
			Vec4f color;
			float rotation;
			Vec2f alignment;
			boolean roundToPixel;

			public TestComponent(String text) {
				size = text.length();
				fontSize = 10f;
				position = new Vec3f(0f, 0f, -0.5f);
				color = new Vec4f(1f, 1f, 1f, 1f);
				rotation = 0f;
				alignment = new Vec2f(0f, 0f);
				roundToPixel = false;
			}
		}
	}

	static class Entity {
		Components.TestComponent testComponent;
		Vec3f position;
		Vec3f velocity;
		Vec3f positionAlt;
		Vec3f velocityAlt;
		Vec2f position2D;
		Float rotation2D;
		Mat4f transform;
	}

	static class Timer {
		private long startTime;

		public void start() {
			startTime = System.currentTimeMillis();
		}

		public long stop() {
			long endTime = System.currentTimeMillis();
			return endTime - startTime;
		}
	}

	static class BenchResult {
		public long insert;
		public long insert_con;
		public long insert_big;
		public long simple_op;
		public long complex_op;
		public long remove_comp;
		public long remove_ent;

		public void printStats(int numRuns) {
			System.out.println("" + (insert / numRuns) + "  \tinsert");
			System.out.println("" + (insert_con / numRuns) + "  \tinsert_con");
			System.out.println("" + (insert_big / numRuns) + "  \tinsert_big");
			System.out.println("" + (simple_op / numRuns) + "  \tsimple_op");
			System.out.println("" + (complex_op / numRuns) + "  \tcomplex_op");
			System.out.println("" + (remove_comp / numRuns) + "  \tremove_comp");
			System.out.println("" + (remove_ent / numRuns) + "  \tremove_ent");
		}
	}

	private static void benchmark(BenchResult result, int targetEntityCount) {
		ArrayList<Entity> entities = new ArrayList<>();
		for (int i = 0; i < targetEntityCount; i++) {
			entities.add(new Entity());
		}

		Timer timer = new Timer();

		{
			timer.start();
			for (int i = 0; i < targetEntityCount; i++) {
				Entity entity = entities.get(i);
				entity.position = new Vec3f(i);
				entity.velocity = new Vec3f(1, 0, i);
			}
			result.insert += timer.stop();
		}

		{
			timer.start();
			IntStream.range(0, targetEntityCount).parallel()
					.forEach(i -> {
						Entity entity = entities.get(i);
						entity.positionAlt = new Vec3f(i);
						entity.velocityAlt = new Vec3f(1, 0, i);
					});
			result.insert_con += timer.stop();
		}

		Random random = new Random(13567);
		Collections.shuffle(entities, random);

		{
			timer.start();
			IntStream.range(0, targetEntityCount).parallel()
					.forEach(i -> {
						Entity entity = entities.get(i);
						if (i % 7 == 0) {
							entity.testComponent = new Components.TestComponent("" + i + "2poipnrpuipo");
						}
						if (i % 3 == 0) {
							entity.transform = new Mat4f();
						}
						if (i % 6 == 0) {
							entity.rotation2D = 1f / i;
						}
						if (i % 11 == 0) {
							entity.position2D = new Vec2f(2f / i, i / 2f);
						}
					});
			result.insert_big += timer.stop();
		}

		{
			timer.start();
			entities.parallelStream()
					.filter(entity -> entity.velocity != null && entity.position != null)
					.forEach(entity -> entity.position.apply(Operator.Add, entity.velocity.apply(Operator.Mul, 0.035f)));
			result.simple_op += timer.stop();
		}

		{
			timer.start();
			entities.parallelStream()
					.filter(entity -> entity.testComponent != null && entity.transform != null
							&& entity.position != null && entity.velocity != null)
					.forEach(entity -> {
						Vec4f v = MatrixArithmeticIP.mul(entity.transform, new Vec4f(entity.position, 1f));
						entity.testComponent.size = ("" + v.data[0] + ", " + v.data[1] + v.data[2]).length();
					});
			result.complex_op += timer.stop();
		}

		Collections.shuffle(entities, random);

		{
			timer.start();
			for (Entity entity : entities) {
				entity.positionAlt = null;
				entity.velocityAlt = null;
			}
			result.remove_comp += timer.stop();
		}

		{
			timer.start();
			//noinspection ListRemoveInLoop
			for (int i = entities.size() - 1; i >= 0; i--) {
				entities.remove(i);
			}
			result.remove_ent += timer.stop();
		}
	}

	@Test
	public void floatTest() {
		int targetEntityCount = 2 << 20;
		int numRuns = 8;

		// warmUp
		BenchResult result = new BenchResult();
		benchmark(result, targetEntityCount);
		benchmark(result, targetEntityCount);

		// benchmark
		result = new BenchResult();
		for (int i = 0; i < numRuns; i++) {
			benchmark(result, targetEntityCount);
		}
		result.printStats(numRuns);
	}
}
