package dev.maow.iguana;

public abstract class AbstractConstantVisitor implements ConstantVisitor {
	@Override
	public void visitUtf8(String value) {

	}

	@Override
	public void visitInteger(int value) {

	}

	@Override
	public void visitFloat(float value) {

	}

	@Override
	public void visitLong(long value) {

	}

	@Override
	public void visitDouble(double value) {

	}

	@Override
	public void visitClass(String name) {

	}

	@Override
	public void visitString(String value) {

	}

	@Override
	public void visitFieldref(EntityReference ref) {

	}

	@Override
	public void visitMethodref(EntityReference ref) {

	}

	@Override
	public void visitInterfaceMethodref(EntityReference ref) {

	}

	@Override
	public void visitNameAndType(EntityDescriptor desc) {

	}

	@Override
	public void visitMethodHandle(EntityReferenceHandle handle) {

	}

	@Override
	public void visitMethodType(String type) {

	}

	@Override
	public void visitDynamic(DynamicEntityReference ref) {

	}

	@Override
	public void visitInvokeDynamic(DynamicEntityReference ref) {

	}

	@Override
	public void visitModule(String name) {

	}

	@Override
	public void visitPackage(String name) {

	}
}
