package libraries.testing;

import java.util.Comparator;

public class WeightedNodeComparator implements Comparator<WeightedNode> {

	@Override
	public int compare(WeightedNode nodeA, WeightedNode nodeB) {
		if (nodeA.getPriority() > nodeB.getPriority())
        {
            return -1;
        }
        if (nodeA.getPriority() < nodeB.getPriority())
        {
            return 1;
        }
        return 0;
	}

}
