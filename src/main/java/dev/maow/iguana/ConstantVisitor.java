package dev.maow.iguana;

public interface ConstantVisitor {
	void visitUtf8(String value);

	void visitInteger(int value);

	void visitFloat(float value);

	void visitLong(long value);

	void visitDouble(double value);

	void visitClass(String name);

	void visitString(String value);

	void visitFieldref(EntityReference ref);

	void visitMethodref(EntityReference ref);

	void visitInterfaceMethodref(EntityReference ref);

	void visitNameAndType(EntityDescriptor desc);

	void visitMethodHandle(EntityReferenceHandle handle);

	void visitMethodType(String type);

	void visitDynamic(DynamicEntityReference ref);

	void visitInvokeDynamic(DynamicEntityReference ref);

	void visitModule(String name);

	void visitPackage(String name);
}
