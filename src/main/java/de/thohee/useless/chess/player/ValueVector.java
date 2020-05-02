package de.thohee.useless.chess.player;

import java.util.ArrayList;

class ValueVector extends ArrayList<Integer> implements Value {
	private static final long serialVersionUID = 5403861563080301920L;

	final static ValueVector MINIMUM = new ValueVector().setMin();
	final static ValueVector MAXIMUM = new ValueVector().setMax();
	final static ValueVector INVALID = new ValueVector().setInvalid();

	private boolean min = false;
	private boolean max = false;
	private boolean invalid = false;

	private ValueVector setMax() {
		this.max = true;
		return this;
	}

	private ValueVector setMin() {
		this.min = true;
		return this;
	}

	private ValueVector setInvalid() {
		this.invalid = true;
		return this;
	}

	// Not representing the minimum and maximum vectors by actual vectors of fixed
	// length allows to flexibly add new components when evaluating board positions
	// in a subclass.
	// However, it requires this tedious comparison.
	@Override
	public int compareTo(Value o) {
		assert (!isInvalid() && !o.isInvalid());
		if (isMin()) {
			if (o.isMin()) {
				return 0;
			} else if (o.isMax()) {
				return -1;
			} else {
				ValueVector other = (ValueVector) o;
				if (other.isEmpty()) {
					return -1;
				}
				for (Integer v : other) {
					if (Integer.MIN_VALUE < v) {
						return -1;
					}
				}
				return 0;
			}
		} else if (isMax()) {
			if (o.isMax()) {
				return 0;
			} else if (o.isMin()) {
				return 1;
			} else {
				ValueVector other = (ValueVector) o;
				if (other.isEmpty()) {
					return 1;
				}
				for (Integer v : other) {
					if (Integer.MAX_VALUE > v) {
						return 1;
					}
				}
				return 0;
			}
		} else {
			if (o.isMin() || o.isMax()) {
				return -1 * o.compareTo(this);
			} else {
				ValueVector other = (ValueVector) o;
				assert (other.size() == size());
				for (int i = 0; i < size(); ++i) {
					int c = get(i).compareTo(other.get(i));
					if (c != 0) {
						return c;
					}
				}
				return 0;
			}
		}
	}

	@Override
	public boolean isInvalid() {
		return this.invalid;
	}

	@Override
	public String toString() {
		if (isInvalid()) {
			return "invalid";
		} else if (isMin()) {
			return "minimum";
		} else if (isMax()) {
			return "maximum";
		} else {
			StringBuilder builder = new StringBuilder();
			builder.append('(');
			for (int i = 0; i < size(); ++i) {
				if (i > 0) {
					builder.append(", ");
				}
				builder.append(Integer.toString(get(i)));
			}
			builder.append(')');
			return builder.toString();
		}
	}

	@Override
	public boolean isMin() {
		return min;
	}

	@Override
	public boolean isMax() {
		return max;
	}
}