package equals;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

import clone.PerfectClone;

public class PerfectEquals 
{
	private static IArrayList comparedObjects = new IArrayList();
	
	public static <T, U> boolean equals(T o1, U o2) {
		if(o1 == null || o2 == null) return (o1 == o2);
		
		if(o1.getClass() != o2.getClass()) return false;
		
		if(canDirectlyCompare(o1.getClass())) return (o1 == o2);
		
		if(isPackagingClass(o1.getClass())) return o1.equals(o2);
		
		if(o1.getClass() == Object.class) return true;
		
		if(comparedObjects.indexOf(o1) != -1) return true;
		
		if(o1.getClass().isArray()) {
			if(Array.getLength(o1) != Array.getLength(o2)) return false;
			
			comparedObjects.add(o1);
			
			for(int i = 0; i < Array.getLength(o1); i++) {
				if(!equals(Array.get(o1, i), Array.get(o2, i))) return false;
			}
			
			return true;
		}
		
		comparedObjects.add(o1);
		
		ArrayList<Field> fields = new ArrayList<Field>();
		Class<?> iterator = o1.getClass();
		hackPackage(iterator);
		do {
			hackPackage(iterator);
			fields.addAll(Arrays.asList(iterator.getDeclaredFields()));
		}while((iterator = iterator.getSuperclass()) != Object.class);
		
		for(Field field : fields) {
			try {
				if(!Modifier.isStatic(field.getModifiers())) {
					field.setAccessible(true);
					if(!equals(field.get(o1), field.get(o2))) return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	private static boolean canDirectlyCompare(Class<?> targetClass) {
		if(targetClass.isPrimitive() || (targetClass == String.class) || (targetClass == Class.class) ||
				(targetClass.isEnum()))
			return true;
		return false;
	}
	
	private static boolean isPackagingClass(Class<?> targetClass) {
		if((targetClass == Character.class) ||
				(targetClass == Boolean.class) || (targetClass == Byte.class) ||
				(targetClass == Integer.class) || (targetClass == Long.class) ||
				(targetClass == Float.class) || (targetClass == Double.class))
			return true;
		return false;
	}
	
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
	
	private static class IArrayList extends ArrayList<Object>
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 962042845135067366L;

		@Override
		public int indexOf(Object o) {
			for(int i = 0 ; i < size(); i++) {
				if(o == get(i)) {
					return i;
				}
			}
			return -1;
		}
	}
}