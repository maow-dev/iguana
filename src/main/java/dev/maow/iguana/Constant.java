package dev.maow.iguana;

public record Constant<T>(ConstantType<T> type, T value) {
	public void visit(ConstantVisitor visitor) {
		type.visit(visitor, value);
	}
}
