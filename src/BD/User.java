package BD;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class User {
	private int value;

	public User() {

	}

	public User(int value) {
		this.value = value;
	}

	public int getOriginalValue() {
		return value;
	}

	public void setFakeValue(Set<Integer> targets) {
		List<Integer> list = new ArrayList<Integer>();
		list.addAll(targets);
		int rand = (int) (Math.random() * targets.size());
		value = list.get(rand);
	}
}
