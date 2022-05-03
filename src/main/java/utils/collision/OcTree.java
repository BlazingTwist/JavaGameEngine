package utils.collision;

import java.util.ArrayList;
import utils.vector.Vec3f;

public class OcTree<T> {

	public static final float minBoxSize = 1f / (1 << 4);

	private final float initialRootSize;

	private Node<T> rootNode;

	/**
	 * @param initialRootSize initial edge length of the root node
	 */
	public OcTree(float initialRootSize) {
		this.initialRootSize = initialRootSize;
		initRootNode(initialRootSize);
	}

	public OcTree(AxisAlignedBoundingBox initialRootBox) {
		initialRootSize = initialRootBox.averageSize();
		rootNode = new Node<>(initialRootBox);
	}

	/**
	 * Remove all elements while maintaining current rootSize
	 */
	public void clear() {
		rootNode.clear();
	}

	/**
	 * Remove all elements and reset root size
	 */
	public void reset() {
		initRootNode(initialRootSize);
	}

	public void traverse(ITreeProcessor<T> processor) {
		rootNode.traverse(processor);
	}

	public void insert(AxisAlignedBoundingBox box, T element) {
		// enlarge top
		while (!rootNode.nodeBox.containsFully(box)) {
			int childIndex = 0;
			AxisAlignedBoundingBox newRootBox = rootNode.nodeBox.copy();
			float currentRootSize = newRootBox.max().data[0] - newRootBox.min().data[0];
			for (int i = 0; i < Vec3f.DATA_LEN; i++) {
				if (newRootBox.min().data[i] > box.min().data[i]) {
					newRootBox.min().data[i] -= currentRootSize;
					childIndex |= (0b1 << i);
				} else {
					newRootBox.max().data[i] += currentRootSize;
				}
			}
			Node<T> newRoot = new Node<>(newRootBox);
			newRoot.children[childIndex] = rootNode;
			rootNode = newRoot;
		}
		rootNode.insert(box, element);
	}

	public boolean remove(AxisAlignedBoundingBox box, T element) {
		return rootNode.remove(box, element);
	}

	private void initRootNode(float size) {
		final float halfSize = size * 0.5f;
		rootNode = new Node<>(new AxisAlignedBoundingBox(new Vec3f(-halfSize), new Vec3f(halfSize)));
	}

	private static class Node<NodeT> {
		public final ArrayList<NodeT> elements = new ArrayList<>();
		public final AxisAlignedBoundingBox nodeBox;
		@SuppressWarnings("unchecked")
		public final Node<NodeT>[] children = (Node<NodeT>[]) new Node[8];

		public Node(AxisAlignedBoundingBox box) {
			this.nodeBox = box;
		}

		public void clear() {
			elements.clear();
			for (int i = 0; i < 8; i++) {
				children[i] = null;
			}
		}

		public void traverse(ITreeProcessor<NodeT> processor) {
			if (processor.descend(nodeBox)) {
				for (NodeT element : elements) {
					processor.process(element);
				}
				for (Node<NodeT> child : children) {
					if (child != null) {
						child.traverse(processor);
					}
				}
			}
		}

		public void insert(AxisAlignedBoundingBox box, NodeT element) {
			if (nodeBox.max().data[0] - nodeBox.min().data[0] <= minBoxSize) {
				elements.add(element);
				return;
			}

			final float centerX = nodeBox.min().data[0] + ((nodeBox.max().data[0] - nodeBox.min().data[0]) * 0.5f);
			final float centerY = nodeBox.min().data[1] + ((nodeBox.max().data[1] - nodeBox.min().data[1]) * 0.5f);
			final float centerZ = nodeBox.min().data[2] + ((nodeBox.max().data[2] - nodeBox.min().data[2]) * 0.5f);
			if ((box.min().data[0] < centerX && box.max().data[0] > centerX)
					|| (box.min().data[1] < centerY && box.max().data[1] > centerY)
					|| (box.min().data[2] < centerZ && box.max().data[2] > centerZ)) {
				elements.add(element);
				return;
			}

			final boolean greaterX = box.min().data[0] >= centerX;
			final boolean greaterY = box.min().data[1] >= centerY;
			final boolean greaterZ = box.min().data[2] >= centerZ;
			final int childIndex = (greaterX ? 0b001 : 0) | (greaterY ? 0b010 : 0) | (greaterZ ? 0b100 : 0);
			Node<NodeT> targetNode = children[childIndex];
			if (targetNode == null) {
				Vec3f newBoxMin = new Vec3f(
						greaterX ? centerX : nodeBox.min().data[0],
						greaterY ? centerY : nodeBox.min().data[1],
						greaterZ ? centerZ : nodeBox.min().data[2]
				);
				Vec3f newBoxMax = new Vec3f(
						greaterX ? nodeBox.max().data[0] : centerX,
						greaterY ? nodeBox.max().data[1] : centerY,
						greaterZ ? nodeBox.max().data[2] : centerZ
				);
				targetNode = new Node<>(new AxisAlignedBoundingBox(newBoxMin, newBoxMax));
				children[childIndex] = targetNode;
			}
			targetNode.insert(box, element);
		}

		public boolean remove(AxisAlignedBoundingBox box, NodeT element) {
			if ((nodeBox.max().data[0] - nodeBox.min().data[0]) <= minBoxSize) {
				return elements.remove(element);
			}

			final float centerX = nodeBox.min().data[0] + ((nodeBox.max().data[0] - nodeBox.min().data[0]) * 0.5f);
			final float centerY = nodeBox.min().data[1] + ((nodeBox.max().data[1] - nodeBox.min().data[1]) * 0.5f);
			final float centerZ = nodeBox.min().data[2] + ((nodeBox.max().data[2] - nodeBox.min().data[2]) * 0.5f);
			if ((box.min().data[0] < centerX && box.max().data[0] > centerX)
					|| (box.min().data[1] < centerY && box.max().data[1] > centerY)
					|| (box.min().data[2] < centerZ && box.max().data[2] > centerZ)) {
				return elements.remove(element);
			}

			final boolean greaterX = box.min().data[0] >= centerX;
			final boolean greaterY = box.min().data[1] >= centerY;
			final boolean greaterZ = box.min().data[2] >= centerZ;
			final int childIndex = (greaterX ? 0b001 : 0) | (greaterY ? 0b010 : 0) | (greaterZ ? 0b100 : 0);
			Node<NodeT> child = children[childIndex];
			return child != null && child.remove(box, element);
		}
	}
}
