package org.snrg_nyc.util;

/**
 * A class for containing one object from two classes.
 * @author Devin Hastings
 *
 * @param <Left>
 * @param <Right>
 */
public class Either<Left, Right> {
	public final Left left;
	public final Right right;
	
	private Either(Left left, Right right){
		if((left == null) == (right == null)){
			throw new IllegalArgumentException(
					"There must be exactly one non-null value in the pair");
		}
		else {
			this.left = left;
			this.right = right;
		}
	};
	
	public static <L, R> Either<L, R>
	left(L left){
		return new Either<L, R>(left, null);
	}
	
	public static <L, R> Either<L, R>
	right(R right){
		return new Either<L, R>(null, right);
	}

	public boolean hasLeft(){
		return left != null && right == null;
	}
	public boolean hasRight(){
		return right != null && left == null;
	}
}
