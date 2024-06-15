# 🦎 Iguana
*The casually overengineered constant pool parsing library...*

## Examples

**Retrieving a typed constant from a pool.**
```java
ConstantPool pool = new ConstantPool(/*...*/);

Constant<String> constant = pool.get(0, ConstantType.UTF8);
String value = constant.value();
```

**Safely retrieving a value from a untyped constant.**
```java
ConstantPool pool = new ConstantPool(/*...*/);

Constant<?> constant = pool.getUntyped(0);
if (ConstantType.UTF8.is(constant)) {
    String value = ConstantType.UTF8.cast(constant).value();
}
```

**Iterating over a pool through a visitor.**
```java
class MyVisitor extends AbstractConstantVisitor { 
    @Override
    public void visitUtf8(String value) {
        System.out.println(value);
    }
    
    @Override
    public void visitNameAndType(EntityDescriptor desc) {
        System.out.println(desc.name() + ": " + desc.type());
    }
}

// ...

ConstantPool pool = new ConstantPool(/*...*/);

pool.visit(new MyVisitor());
```

**Defining a custom constant type.**
```java
ConstantType<String> CUSTOM = ConstantType
    .bounded(69, Short.BYTES,
        ctx -> {
	    DataInputStream stream = ctx.stream();
	    ConstantPool pool = ctx.pool();
	    return pool.get(stream.readUnsignedShort(), ContextType.STRING).value();
        }
    );
```