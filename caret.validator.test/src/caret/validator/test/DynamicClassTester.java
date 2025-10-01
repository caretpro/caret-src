package caret.validator.test;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DynamicClassTester {

    private final Class<?> clazz;

    public DynamicClassTester(Class<?> clazz) {
        this.clazz = clazz;
    }

    @TestFactory
    public List<DynamicTest> generateTests() {
        List<DynamicTest> dynamicTests = new ArrayList<>();
        Method[] methods = clazz.getDeclaredMethods();
        
        for (Method method : methods) {
            dynamicTests.add(DynamicTest.dynamicTest("Test " + method.getName(), () -> {
                Object instance = clazz.getDeclaredConstructor().newInstance();
                Object[] params = generateMockParameters(method);
                params [0] = 8;
                params [1] = 4;
                Object result = method.invoke(instance, params);
                System.out.println("Result of " + method.getName() + ": " + result);
            }));
        }
        return dynamicTests;
    }

    private Object[] generateMockParameters(Method method) {
        return new Object[method.getParameterCount()];
    }
}
