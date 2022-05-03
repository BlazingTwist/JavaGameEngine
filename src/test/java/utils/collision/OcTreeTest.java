package utils.collision;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.vector.Vec3f;

public class OcTreeTest {

	private record OcTreeData(AxisAlignedBoundingBox box, int value) {
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			OcTreeData that = (OcTreeData) o;
			return value == that.value;
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}
	}

	private static abstract class TestProcessor implements ITreeProcessor<OcTreeData> {
		public final ArrayList<Integer> found = new ArrayList<>();
		public int descends = 0;
		public int processed = 0;

		public void clear() {
			found.clear();
			descends = 0;
			processed = 0;
		}

		@Override
		public String toString() {
			return "TestProcessor{"
					+ "descends: " + descends
					+ ", processed: " + processed
					+ ", found: " + found
					+ '}';
		}
	}

	private static class AllProcessor extends TestProcessor {
		@Override
		public boolean descend(AxisAlignedBoundingBox nodeBox) {
			descends++;
			return true;
		}

		@Override
		public void process(OcTreeData hit) {
			found.add(hit.value);
			processed++;
		}
	}

	private static class QueryProcessor extends TestProcessor {
		private final AxisAlignedBoundingBox box;

		public QueryProcessor(AxisAlignedBoundingBox box) {
			this.box = box;
		}

		@Override
		public boolean descend(AxisAlignedBoundingBox nodeBox) {
			boolean intersecting = box.isIntersecting(nodeBox);
			if (intersecting) {
				descends++;
			}
			return intersecting;
		}

		@Override
		public void process(OcTreeData hit) {
			processed++;
			if (box.isIntersecting(hit.box)) {
				found.add(hit.value);
			}
		}
	}

	private static class ProcessorStateMatcher extends BaseMatcher<TestProcessor> {

		public final int descends;
		public final int processed;
		public final ArrayList<Integer> found;

		public ProcessorStateMatcher(int descends, int processed, Integer... found) {
			this.descends = descends;
			this.processed = processed;
			this.found = new ArrayList<>();
			this.found.addAll(Arrays.asList(found));
		}

		public ProcessorStateMatcher(int descends, int processed, ArrayList<Integer> found) {
			this.descends = descends;
			this.processed = processed;
			this.found = found;
		}

		@Override
		public boolean matches(Object o) {
			if (!(o instanceof TestProcessor proc)) {
				return false;
			}
			return proc.descends == descends
					&& proc.processed == processed
					&& containsAllHits(proc.found);
		}

		private boolean containsAllHits(ArrayList<Integer> foundList) {
			for (int i : found) {
				if (!foundList.contains(i)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText(String.format(" has descends=%d | processed=%d | found=%s", descends, processed, found.toString()));
		}
	}

	@Test
	public void testOcTree() {
		OcTree<OcTreeData> tree = new OcTree<>(1f);
		AllProcessor processor = new AllProcessor();
		int counter = 0;
		ArrayList<Integer> expectedElements = new ArrayList<>();
		BiConsumer<AxisAlignedBoundingBox, Integer> insert = (box, i) -> {
			tree.insert(box, new OcTreeData(box, i));
			expectedElements.add(i);
		};

		insert.accept(new AxisAlignedBoundingBox(new Vec3f(-0.25f), new Vec3f(0.25f)), counter++);
		tree.traverse(processor);
		assertThat("Insert element in root node", processor, new ProcessorStateMatcher(1, 1, expectedElements));

		processor.clear();
		insert.accept(new AxisAlignedBoundingBox(new Vec3f(-0.5f), new Vec3f(0f, 0.01f, 0f)), counter++);
		tree.traverse(processor);
		assertThat("Insert element at upper edge", processor, new ProcessorStateMatcher(1, 2, expectedElements));

		processor.clear();
		insert.accept(new AxisAlignedBoundingBox(new Vec3f(-0.5f), new Vec3f(0f)), counter++);
		tree.traverse(processor);
		assertThat("Insert subdividing element", processor, new ProcessorStateMatcher(2, 3, expectedElements));

		for (int i = 0; i < 16; i++) {
			AxisAlignedBoundingBox box = new AxisAlignedBoundingBox(new Vec3f(i - 0.4f), new Vec3f(i + 1.01f));
			tree.insert(box, new OcTreeData(box, counter++));
		}
		QueryProcessor queryProcessor = new QueryProcessor(new AxisAlignedBoundingBox(new Vec3f(-0.5f, 3.5f, -10f), new Vec3f(41999.5f, 4.5f, 10f)));
		tree.traverse(queryProcessor);
		assertThat("Query processor", queryProcessor, new ProcessorStateMatcher(7, 7, 6, 7));

		processor.clear();
		AxisAlignedBoundingBox box = new AxisAlignedBoundingBox(new Vec3f(-0.5f), new Vec3f(-0.01f));
		Assertions.assertTrue(tree.remove(box, new OcTreeData(box, 2)), "Remove existing element");
		Assertions.assertFalse(tree.remove(box, new OcTreeData(box, 2)), "Remove not-existing element");
		expectedElements.remove(2);
		tree.traverse(processor);
		assertThat("Remove element", processor, new ProcessorStateMatcher(18, 18, expectedElements));
	}

}
