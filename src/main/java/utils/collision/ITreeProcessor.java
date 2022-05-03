package utils.collision;

public interface ITreeProcessor<T> {
	/**
	 * @param nodeBox offered AABB of the tree node
	 * @return true if node should be entered
	 */
	boolean descend(AxisAlignedBoundingBox nodeBox);

	/**
	 * @param hit element contained in traversed node
	 */
	void process(T hit);
}
