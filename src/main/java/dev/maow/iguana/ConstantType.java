package dev.maow.iguana;

import java.io.DataInput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class ConstantType<T> {
	private static final Map<Integer, ConstantType<?>> CONSTANT_TYPES = new HashMap<>();

	public static final ConstantType<String>  UTF8    = unbounded(1, ConstantVisitor::visitUtf8, primitive(DataInput::readUTF)).register();
	public static final ConstantType<Integer> INTEGER = unbounded(3, ConstantVisitor::visitInteger, primitive(DataInput::readInt)).register();
	public static final ConstantType<Float>   FLOAT   = unbounded(4, ConstantVisitor::visitFloat, primitive(DataInput::readFloat)).register();
	public static final ConstantType<Long>    LONG    = unbounded(5, ConstantVisitor::visitLong, primitive(DataInput::readLong)).register();
	public static final ConstantType<Double>  DOUBLE  = unbounded(6, ConstantVisitor::visitDouble, primitive(DataInput::readDouble)).register();

	public static final ConstantType<String>  CLASS   = bounded(7, Short.BYTES, ConstantVisitor::visitClass, indirectString()).register();
	public static final ConstantType<String>  STRING  = bounded(8, Short.BYTES, ConstantVisitor::visitString, indirectString()).register();

	public static final ConstantType<EntityReference> FIELDREF = bounded(9,
		Short.BYTES + Short.BYTES,
		ConstantVisitor::visitFieldref, entityReference()
	).register();

	public static final ConstantType<EntityReference> METHODREF = bounded(10,
		Short.BYTES + Short.BYTES,
		ConstantVisitor::visitMethodref, entityReference()
	).register();

	public static final ConstantType<EntityReference> INTERFACE_METHODREF = bounded(10,
		Short.BYTES + Short.BYTES,
		ConstantVisitor::visitMethodref, entityReference()
	).register();

	public static final ConstantType<EntityDescriptor> NAME_AND_TYPE = bounded(12,
		Short.BYTES + Short.BYTES, ConstantVisitor::visitNameAndType,
		ctx -> {
			final var stream = ctx.stream();
			final var pool = ctx.pool();
			return new EntityDescriptor(
				pool.get(stream.readUnsignedShort(), ConstantType.UTF8).value(),
				pool.get(stream.readUnsignedShort(), ConstantType.UTF8).value()
			);
		}).register();

	public static final ConstantType<EntityReferenceHandle> METHOD_HANDLE = bounded(15,
		Byte.BYTES + Short.BYTES, ConstantVisitor::visitMethodHandle,
		ctx -> {
			final var stream = ctx.stream();
			final var pool = ctx.pool();
			final var kind = EntityReferenceHandle.Kind
				.fromId(stream.readByte())
				.orElseThrow(() -> new IllegalStateException("unsupported method handle kind"));
			return new EntityReferenceHandle(
				kind,
				pool.get(stream.readUnsignedShort(), kind.types()).value()
			);
		}).register();

	public static final ConstantType<String> METHOD_TYPE = bounded(16, Short.BYTES, ConstantVisitor::visitMethodType, indirectString()).register();

	public static final ConstantType<DynamicEntityReference> DYNAMIC = bounded(17,
		Short.BYTES + Short.BYTES, ConstantVisitor::visitDynamic, dynamicEntityReference()
	).register();
	public static final ConstantType<DynamicEntityReference> INVOKEDYNAMIC = bounded(18,
		Short.BYTES + Short.BYTES, ConstantVisitor::visitInvokeDynamic, dynamicEntityReference()
	).register();

	public static final ConstantType<String> MODULE = bounded(19, Short.BYTES, ConstantVisitor::visitModule, indirectString()).register();
	public static final ConstantType<String> PACKAGE = bounded(19, Short.BYTES, ConstantVisitor::visitPackage, indirectString()).register();

	protected final int id;

	protected ConstantType(int id) {
		this.id = id;
	}

	public static Optional<ConstantType<?>> fromId(int id) {
		return Optional.ofNullable(CONSTANT_TYPES.get(id));
	}

	private static <T> ConstantType<T> bounded(int id, int length,
	                                          BiConsumer<ConstantVisitor, T> visit,
	                                          ThrowingFunction<ConstantParsingContext, T, IOException> parse) {
		return new ConstantType<>(id) {
			@Override
			public Constant<T> parse(ConstantParsingContext ctx) throws IOException {
				final var value = parse.apply(ctx.sliced(length));
				return new Constant<>(this, value);
			}

			@Override
			protected void visit(ConstantVisitor visitor, T value) {
				visit.accept(visitor, value);
			}
		};
	}

	private static <T> ConstantType<T> unbounded(int id,
	                                            BiConsumer<ConstantVisitor, T> visit,
	                                            ThrowingFunction<ConstantParsingContext, T, IOException> parse) {
		return new ConstantType<>(id) {
			@Override
			public Constant<T> parse(ConstantParsingContext ctx) throws IOException {
				final var value = parse.apply(ctx);
				return new Constant<>(this, value);
			}

			@Override
			protected void visit(ConstantVisitor visitor, T value) {
				visit.accept(visitor, value);
			}
		};
	}

	public static <T> ConstantType<T> bounded(int id, int length, ThrowingFunction<ConstantParsingContext, T, IOException> parse) {
		return new ConstantType<>(id) {
			@Override
			public Constant<T> parse(ConstantParsingContext ctx) throws IOException {
				final var value = parse.apply(ctx.sliced(length));
				return new Constant<>(this, value);
			}

			@Override
			protected void visit(ConstantVisitor visitor, T value) {}
		};
	}

	public static <T> ConstantType<T> unbounded(int id, ThrowingFunction<ConstantParsingContext, T, IOException> parse) {
		return new ConstantType<>(id) {
			@Override
			public Constant<T> parse(ConstantParsingContext ctx) throws IOException {
				final var value = parse.apply(ctx);
				return new Constant<>(this, value);
			}

			@Override
			protected void visit(ConstantVisitor visitor, T value) {}
		};
	}

	public static <T> ThrowingFunction<ConstantParsingContext, T, IOException> primitive(
		ThrowingFunction<DataInput, T, IOException> parse
	) {
		return ctx -> parse.apply(ctx.stream());
	}

	public static ThrowingFunction<ConstantParsingContext, String, IOException> indirectString() {
		return ctx -> ctx.pool().get(ctx.stream().readUnsignedShort(), ConstantType.UTF8).value();
	}

	public static ThrowingFunction<ConstantParsingContext, EntityReference, IOException> entityReference() {
		return ctx -> {
			final var stream = ctx.stream();
			final var pool = ctx.pool();
			return new EntityReference(
				pool.get(stream.readUnsignedShort(), ConstantType.CLASS).value(),
				pool.get(stream.readUnsignedShort(), ConstantType.NAME_AND_TYPE).value()
			);
		};
	}

	public static ThrowingFunction<ConstantParsingContext, DynamicEntityReference, IOException> dynamicEntityReference() {
		return ctx -> {
			final var stream = ctx.stream();
			final var pool = ctx.pool();
			return new DynamicEntityReference(
				stream.readUnsignedShort(),
				pool.get(stream.readUnsignedShort(), ConstantType.NAME_AND_TYPE).value()
			);
		};
	}

	public abstract Constant<T> parse(ConstantParsingContext ctx) throws IOException;

	protected abstract void visit(ConstantVisitor visitor, T value);

	public boolean is(Constant<?> constant) {
		return this.equals(constant.type());
	}

	@SuppressWarnings("unchecked")
	public Constant<T> cast(Constant<?> constant) {
		if (!is(constant)) {
			throw new IllegalArgumentException("mismatched types; can't cast");
		}
		return (Constant<T>) constant;
	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	public static <T> Constant<T> cast(Constant<?> constant, ConstantType<T>... types) {
		for (var type : types) {
			if (type.is(constant)) {
				return (Constant<T>) constant;
			}
		}
		throw new IllegalArgumentException("mismatched types; can't cast");
	}

	private ConstantType<T> register() {
		CONSTANT_TYPES.put(id, this);
		return this;
	}

	public int id() {
		return this.id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ConstantType<?> that = (ConstantType<?>) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}
}
