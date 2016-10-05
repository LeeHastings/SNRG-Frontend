package org.snrg_nyc.util;

/**
 * A class for containing one object from two possibilities.
 * @author Devin Hastings
 *
 * @param <Left>
 * @param <Right>
 */
public class Either<Left, Right> {
	private Left left;
	private Right right;
	
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
	
	public Left left(){
		return left;
	}
	public Right right(){
		return right;
	}
	public boolean hasLeft(){
		return left != null && right == null;
	}
	public boolean hasRight(){
		return right != null && left == null;
	}
}
