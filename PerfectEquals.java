/* 
 * Copyright (c) 2023, LitnhJacuzzi. All rights reserved.
 */

package equals;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;

/**
 * @author LitnhJacuzzi
 */
public class PerfectEquals 
{
	private IdentityHashMap<Object, Object> comparedObjects = new IdentityHashMap<>();
	
	public static <T, U> boolean equals(T o1, U o2) {
		return new PerfectEquals().equals0(o1, o2);
	}
	
	private <T, U> boolean equals0(T o1, U o2) {
		if(o1 == null || o2 == null) return (o1 == o2);

		if(o1.getClass() != o2.getClass()) return false;

		if(canDirectlyCloneOrCompare(o1.getClass())) return (o1 == o2);
		
		if(isPackagingClass(o1.getClass())) return o1.equals(o2);
		
		if(o1.getClass() == Object.class) return true;

		if(comparedObjects.containsKey(o1)) return comparedObjects.get(o1) == o2;
		
		if(o1 == o2) return true;
		
		if(o1.getClass().isArray()) {
			if(Array.getLength(o1) != Array.getLength(o2)) 
				return false;
			
			comparedObjects.put(o1, o2);
			comparedObjects.put(o2, o1);
			
			for(int i = 0; i < Array.getLength(o1); i++) {
				if(!equals0(Array.get(o1, i), Array.get(o2, i)))
					return false;
			}
			
			return true;
		}
		
		comparedObjects.put(o1, o2);
		comparedObjects.put(o2, o1);
		
		ArrayList<Field> fields = new ArrayList<Field>();
		Class<?> iterator = o1.getClass();
//		hackPackage(iterator); For JDK 9 ~ 15.
		do {
//			hackPackage(iterator); For JDK 9 ~ 15.
			fields.addAll(Arrays.asList(iterator.getDeclaredFields()));
		}while((iterator = iterator.getSuperclass()) != Object.class);
		
		for(Field field : fields) {
			try {
				if(!Modifier.isStatic(field.getModifiers())) {
					field.setAccessible(true);
					if(!equals0(field.get(o1), field.get(o2))) 
						return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	private static boolean canDirectlyCompare(Class<?> targetClass) {
		if(targetClass.isPrimitive() || /*(targetClass == Module.class) For JDK 9 ~ 15. ||*/ 
				(targetClass == Class.class) || (targetClass.isEnum()))
			return true;
		return false;
	}
	
	private static boolean isPackagingClass(Class<?> targetClass) {
		if((targetClass == String.class) || (targetClass == Character.class) || 
				(targetClass == Boolean.class) || (targetClass == Byte.class) || 
				(targetClass == Short.class) || (targetClass == Integer.class) || 
				(targetClass == Long.class) || (targetClass == Float.class) || 
				(targetClass == Double.class))
			return true;
		return false;
	}
	
	/*
	 * For JDK 9 ~ 15.
	private static void hackPackage(Class<?> targetClass) {
		try {
			Method addOpens = Module.class.getDeclaredMethod("implAddExportsOrOpens", 
					String.class, Module.class, boolean.class, boolean.class);
			addOpens.setAccessible(true);
			addOpens.invoke(targetClass.getModule(), targetClass.getPackageName(), PerfectEquals.class.getModule(), true, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
}
