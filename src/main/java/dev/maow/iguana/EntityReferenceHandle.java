package dev.maow.iguana;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static dev.maow.iguana.ConstantType.*;
import static java.util.function.Function.identity;

public record EntityReferenceHandle(Kind kind, EntityReference reference) {
	public enum Kind {
		GET_FIELD                 (1, FIELDREF),
		GET_STATIC_FIELD          (2, FIELDREF),
		PUT_FIELD                 (3, FIELDREF),
		PUT_STATIC_FIELD          (4, FIELDREF),
		INVOKE_VIRTUAL_METHOD     (5, METHODREF),
		INVOKE_STATIC_METHOD      (6, METHODREF, INTERFACE_METHODREF),
		INVOKE_SPECIAL_METHOD     (7, METHODREF, INTERFACE_METHODREF),
		NEW_INVOKE_SPECIAL_METHOD (8, METHODREF),
		INVOKE_INTERFACE_METHOD   (9, INTERFACE_METHODREF)
		;

		private static final Map<Integer, Kind> KINDS;

		private final int id;
		private final ConstantType<EntityReference>[] types;

		@SafeVarargs
		Kind(int id, ConstantType<EntityReference>... types) {
			this.id = id;
			this.types = types;
		}

		public static Optional<Kind> fromId(int id) {
			return Optional.ofNullable(KINDS.get(id));
		}

		public int id() {
			return id;
		}

		public ConstantType<EntityReference>[] types() {
			return types;
		}

		static {
			KINDS = Arrays.stream(values()).collect(Collectors.toMap(Kind::id, identity()));
		}
	}
}