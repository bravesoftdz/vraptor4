package br.com.caelum.vraptor4.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.weld.resources.DefaultReflectionCache;

import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

import net.vidageek.mirror.dsl.Mirror;
import net.vidageek.mirror.list.dsl.Matcher;
import net.vidageek.mirror.list.dsl.MirrorList;
import net.vidageek.mirror.provider.AnnotatedElementReflectionProvider;
import net.vidageek.mirror.provider.ClassReflectionProvider;
import net.vidageek.mirror.provider.ConstructorBypassingReflectionProvider;
import net.vidageek.mirror.provider.ConstructorReflectionProvider;
import net.vidageek.mirror.provider.FieldReflectionProvider;
import net.vidageek.mirror.provider.GenericTypeAccessor;
import net.vidageek.mirror.provider.MethodReflectionProvider;
import net.vidageek.mirror.provider.ParameterizedElementReflectionProvider;
import net.vidageek.mirror.provider.ProxyReflectionProvider;
import net.vidageek.mirror.provider.ReflectionProvider;
import net.vidageek.mirror.provider.java.DefaultMirrorReflectionProvider;
import net.vidageek.mirror.provider.java.PureJavaClassReflectionProvider;
import net.vidageek.mirror.proxy.dsl.MethodInterceptor;

public class StepInvoker {
	
	private class InvokeMatcher implements Matcher<Method> {

		private Class<? extends Annotation> step;

		public InvokeMatcher(Class<? extends Annotation> step) {
			this.step = step;
		}

		@Override
		public boolean accepts(Method element) {
			return element.isAnnotationPresent(this.step);
		}

	}
	

	public Object tryToInvoke(Object interceptor,Class<? extends Annotation> step,Object... params) {
		Method stepMethod = findMethod(step, interceptor);
		if(stepMethod==null){
			return null;
		}	
		Object returnObject = new Mirror().on(interceptor).invoke().method(stepMethod).withArgs(params);
		if(stepMethod.getReturnType().equals(void.class)){
			return new VoidReturn();
		}
		return returnObject;				
	}


	public Method findMethod(Class<? extends Annotation> step,Object interceptor) {		
		MirrorList<Method> possibleMethods = new Mirror(new MyReflectionProvider()).on(interceptor.getClass()).reflectAll().methods().matching(new InvokeMatcher(step));
		if(possibleMethods.size() > 1){
			throw new IllegalStateException("You should not have more than one @"+step.getSimpleName()+" annotated method");
		}		
		if(possibleMethods.isEmpty()){
			return null;
		}
		Method stepMethod = possibleMethods.get(0);
		return stepMethod;
	}
	
	private class MyReflectionProvider implements ReflectionProvider{
		private DefaultMirrorReflectionProvider provider = new DefaultMirrorReflectionProvider();

		public boolean equals(Object obj) {
			return provider.equals(obj);
		}

		public ClassReflectionProvider<?> getClassReflectionProvider(
				String className) {
			return new MyReflectionClassProvider(className);
		}

		public <T> ClassReflectionProvider<T> getClassReflectionProvider(
				Class<T> clazz) {
			return new MyReflectionClassProvider(clazz);
		}

		public FieldReflectionProvider getFieldReflectionProvider(
				Object target, Class<?> clazz, Field field) {
			return provider.getFieldReflectionProvider(target, clazz, field);
		}

		public <T> ConstructorReflectionProvider<T> getConstructorReflectionProvider(
				Class<T> clazz, Constructor<T> constructor) {
			return provider
					.getConstructorReflectionProvider(clazz, constructor);
		}

		public AnnotatedElementReflectionProvider getAnnotatedElementReflectionProvider(
				AnnotatedElement element) {
			return provider.getAnnotatedElementReflectionProvider(element);
		}

		public MethodReflectionProvider getMethodReflectionProvider(
				Object target, Class<?> clazz, Method method) {
			return provider.getMethodReflectionProvider(target, clazz, method);
		}

		public ParameterizedElementReflectionProvider getParameterizedElementProvider(
				GenericTypeAccessor accessor) {
			return provider.getParameterizedElementProvider(accessor);
		}

		public GenericTypeAccessor getClassGenericTypeAccessor(Class<?> clazz) {
			return provider.getClassGenericTypeAccessor(clazz);
		}

		public GenericTypeAccessor getFieldGenericTypeAccessor(Field field) {
			return provider.getFieldGenericTypeAccessor(field);
		}

		public <T> ConstructorBypassingReflectionProvider<T> getConstructorBypassingReflectionProvider(
				Class<T> clazz) {
			return provider.getConstructorBypassingReflectionProvider(clazz);
		}

		public ProxyReflectionProvider getProxyReflectionProvider(
				Class<?> clazz, List<Class<?>> interfaces,
				MethodInterceptor... methodInterceptors) {
			return provider.getProxyReflectionProvider(clazz, interfaces,
					methodInterceptors);
		}

		public int hashCode() {
			return provider.hashCode();
		}

		public String toString() {
			return provider.toString();
		}
	}
	
	private class MyReflectionClassProvider<T> implements ClassReflectionProvider<T> {
		
		private PureJavaClassReflectionProvider<T> delegate;
		private Class klass;			

		public MyReflectionClassProvider(Class klass) {
			super();
			this.delegate = new PureJavaClassReflectionProvider<T>(klass);
			this.klass = klass;
		}
		
		public MyReflectionClassProvider(String className) {
			super();
			this.delegate = new PureJavaClassReflectionProvider<T>(className);
		}

		public boolean equals(Object obj) {
			return delegate.equals(obj);
		}

		public int hashCode() {
			return delegate.hashCode();
		}

		public Class<T> reflectClass() {
			return delegate.reflectClass();
		}

		public List<Field> reflectAllFields() {
			return delegate.reflectAllFields();
		}

		public List<Method> reflectAllMethods() {
			return Arrays.asList(klass.getDeclaredMethods());			
		}

		public List<Constructor<T>> reflectAllConstructors() {
			return delegate.reflectAllConstructors();
		}

		public Constructor<T> reflectConstructor(Class<?>[] argumentTypes) {
			return delegate.reflectConstructor(argumentTypes);
		}

		public Field reflectField(String fieldName) {
			return delegate.reflectField(fieldName);
		}

		public Method reflectMethod(String methodName, Class<?>[] argumentTypes) {
			return delegate.reflectMethod(methodName, argumentTypes);
		}

		public String toString() {
			return delegate.toString();
		}
		
	}
		

}
