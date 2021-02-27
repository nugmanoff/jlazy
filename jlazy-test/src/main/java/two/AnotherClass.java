package two;

import one.DummyClass;
import one.SomeInterface;

public class AnotherClass implements SomeInterface {


    private ClassWithPrimitiveConstant dc;
    private NestedClass nc;

    @Override
    public DummyClass someMethod() {
        System.out.println("Hello, world from Dummy Class! asd");
        return new DummyClass();
    }

    class NestedClass {
        NestedClass() {}
    }
}
