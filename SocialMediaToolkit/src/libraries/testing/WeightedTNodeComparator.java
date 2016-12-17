package libraries.testing;

import java.util.Comparator;

public class WeightedTNodeComparator implements Comparator<WeightedTNode> {

	@Override
	public int compare(WeightedTNode nodeA, WeightedTNode nodeB) {
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