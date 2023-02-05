/* 
 * Copyright (c) 2023, LitnhJacuzzi. All rights reserved.
 */

package clone;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;

import sun.misc.Unsafe;

/**
 * @author LitnhJacuzzi
 */
public class PerfectClone 
{
	/**
	 * Used by {@code cloneWithInit()} method, determines how to clone the different content.<br>
	 * {@code NEW} - If the different field need to be deep cloned.<br>
	 * {@code DIRECT} - If the different field can be shallow cloned.<br>
	 * {@code EXISTED} - If the different field's correct instance existed in mapped objects.
	 */
	private enum CloneType {
		NEW, DIRECT, EXISTED
	}
	
	private IdentityHashMap<Object, Object> clonedObjects = new IdentityHashMap<>();
	private IdentityHashMap<Object, Object> comparedObjects = new IdentityHashMap<>();
	private CloneType currentCloneType;
	private static Unsafe unsafe = hackUnsafe();
	
	public static <T> T clone(T target) {
		return new PerfectClone().clone0(target);
	}
	
	public static <T> T clone(T target, T init) {
		PerfectClone pc = new PerfectClone();
		if(!pc.cloneWithInit(target, init))
			init = pc.clone0(target);
		return init;
	}
	
	
	@SuppressWarnings("unchecked")
	private <T> T clone0(T target) {
		Object ret = null;
		
		if(target == null)
			return null;
		
		if(canDirectlyClone(target.getClass()))
			return target;
		
		if(target.getClass() == Object.class)
			return (T) new Object();
		
		if(isPackagingClass(target.getClass()))
			return clonePackagingClassObject(target);
		
		Object clonedObject = clonedObjects.get(target);
		if(clonedObject != null) 
			return (T) clonedObject;
		
		if(target.getClass().isArray()) {
			int targetLength = Array.getLength(target);
			ret = (T) Array.newInstance(target.getClass().getComponentType(), targetLength);
			
			clonedObjects.put(target, ret);
			clonedObjects.put(ret, target);
			
			if(targetLength == 0) return (T) ret;
			
			for(int i = 0; i < targetLength; i++) {
				Array.set(ret, i, clone0(Array.get(target, i)));
			}
			
			return (T) ret;
		}
		
		ret = (T) instantiateObject(target.getClass());
		
		clonedObjects.put(target, ret);
		clonedObjects.put(ret, target);
		
		ArrayList<Field> fields = new ArrayList<Field>();
		Class<?> iterator = target.getClass();
//		hackPackage(iterator); For JDK 9 ~ 15.
		do {
//			hackPackage(iterator); For JDK 9 ~ 15.
			fields.addAll(Arrays.asList(iterator.getDeclaredFields()));
		}while((iterator = iterator.getSuperclass()) != Object.class);

		for(Field field : fields) {
			try {
				if(!(Modifier.isStatic(field.getModifiers()))) {
					field.setAccessible(true);
					field.set(ret, clone0(field.get(target)));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return (T) ret;
	}
	
	private <T> boolean cloneWithInit(T target, T init) {
		currentCloneType = CloneType.NEW;
		if(target == null || init == null) return (target == init);
		if(target.getClass() != init.getClass()) return false;

		currentCloneType = CloneType.DIRECT;
		if(canDirectlyClone(target.getClass())) return (target == init);
		
		currentCloneType = CloneType.NEW;
		if(isPackagingClass(target.getClass())) return target.equals(init);
		
		if(target.getClass() == Object.class) return true;
		
		currentCloneType = CloneType.EXISTED;
		if(comparedObjects.containsKey(target)) return comparedObjects.get(target) == init;
		
		if(target == init) return true;
		
		if(target.getClass().isArray()) {
			currentCloneType = CloneType.NEW;
			if(Array.getLength(target) != Array.getLength(init)) 
				return false;
			
			comparedObjects.put(target, init);
			comparedObjects.put(init, target);
			
			for(int i = 0; i < Array.getLength(target); i++) {
				Object targetCurrentValue = Array.get(target, i);
				if(!cloneWithInit(targetCurrentValue, Array.get(init, i))) {
					switch(currentCloneType) {
						case NEW:
							clonedObjects.clear();
							Array.set(init, i, clone0(targetCurrentValue));
							break;
						case DIRECT:
							Array.set(init, i, targetCurrentValue);
							break;
						case EXISTED:
							Array.set(init, i, comparedObjects.get(targetCurrentValue));
							break;
					}
				}
			}
			
			return true;
		}
		
		comparedObjects.put(target, init);
		comparedObjects.put(init, target);
		
		ArrayList<Field> fields = new ArrayList<Field>();
		Class<?> iterator = target.getClass();
//		hackPackage(iterator); For JDK 9 ~ 15.
		do {
//			hackPackage(iterator); For JDK 9 ~ 15.
			fields.addAll(Arrays.asList(iterator.getDeclaredFields()));
		}while((iterator = iterator.getSuperclass()) != Object.class);
		
		for(Field field : fields) {
			try {
				if(!Modifier.isStatic(field.getModifiers())) {
					field.setAccessible(true);
					Object targetFieldValue = field.get(target);
					if(!cloneWithInit(targetFieldValue, field.get(init))) {
						switch(currentCloneType) {
							case NEW:
								clonedObjects.clear();
								field.set(init, clone0(targetFieldValue));
								break;
							case DIRECT:
								field.set(init, targetFieldValue);
								break;
							case EXISTED:
								field.set(init, comparedObjects.get(targetFieldValue));
								break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	/**
	 * Use magical {@code Unsafe} to instantiate an object.
	 */
	@SuppressWarnings("unchecked")
	private static <T> T instantiateObject(Class<T> type) {
		try {
			return (T) unsafe.allocateInstance(type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T clonePackagingClassObject(T target) {
		if(target.getClass() == String.class) {
			return (T) String.valueOf(((String) target).toCharArray());
		}else if(target.getClass() == Character.class) {
			return (T) Character.valueOf(((Character) target).charValue());
		}else if(target.getClass() == Boolean.class) {
			return (T) Boolean.valueOf(((Boolean) target).booleanValue());
		}else if(target.getClass() == Byte.class) {
			return (T) Byte.valueOf(((Byte) target).byteValue());
		}else if(target.getClass() == Short.class) {
			return (T) Short.valueOf(((Short) target).shortValue());
		}else if(target.getClass() == Integer.class) {
			return (T) Integer.valueOf(((Integer) target).intValue());
		}else if(target.getClass() == Long.class) {
			return (T) Long.valueOf(((Long) target).longValue());
		}else if(target.getClass() == Float.class) {
			return (T) Float.valueOf(((Float) target).floatValue());
		}else if(target.getClass() == Double.class) {
			return (T) Double.valueOf(((Double) target).doubleValue());
		}
		
		return null;
	}
	
	private static Unsafe hackUnsafe() {
		try {
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			return (Unsafe) field.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * For JDK 9 ~ 15.
	private static void hackPackage(Class<?> targetClass) {
		try {
			Method addOpens = Module.class.getDeclaredMethod("implAddExportsOrOpens", 
					String.class, Module.class, boolean.class, boolean.class);
			addOpens.setAccessible(true);
			addOpens.invoke(targetClass.getModule(), targetClass.getPackageName(), PerfectClone.class.getModule(), true, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
	
	private static boolean canDirectlyClone(Class<?> targetClass) {
		return (targetClass.isPrimitive() || (targetClass == Class.class) || 
				/*(targetClass == Module.class) For JDK 9 ~ 15. ||*/ (targetClass.isEnum()));
	}
	
	private static boolean isPackagingClass(Class<?> targetClass) {
		return ((targetClass == String.class) || (targetClass == Character.class) ||
				(targetClass == Boolean.class) || (targetClass == Byte.class) ||
				(targetClass == Short.class) || (targetClass == Integer.class) || 
				(targetClass == Long.class) || (targetClass == Float.class) || 
				(targetClass == Double.class));
	}
}
