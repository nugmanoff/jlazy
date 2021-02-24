package fixtures;

public class AnotherClass implements SomeInterface {

    private ClassWithPrimitiveConstant dc;

    @Override
    public DummyClass someMethod() {
        return new DummyClass();
    }
}
