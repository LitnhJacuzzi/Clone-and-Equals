# Clone and Equals
This project implements perfect clone and perfect equals.

# Usage
## Clone an object:
```java
<AnyType> clone = PerfectClone.clone(o); // o is the object to be cloned
```
If you already have or are able to get an instance of which the type is the same as the object to be cloned, use the method below instead.  
```java
<AnyType> clone = PerfectClone.clone(o, init); // o.getClass() == init.getClass()
```
**NOTE:** You are supposed to avoid cases that the original object's field tree have references to any instance in the initial object, it's against the rule of copy. The clone method will still make them "perfectly equal", only logically. For example:  
`o`: Type=A,Fields=[A a...], a points to `init`(Type=A). The content of `init` is arbitrary.  
And the result `init` will be: Type=A,Fields=[A a...], a points to `o`.  
**Also Note:** Cloned object returned by the first method is **completely** isolated from the original one, while a field in the cloned object returned by the second method may point to the same ref-type instance as that in the original object points to.
## Determine whether two objects are equal:
```java
boolean isEquals = PerfectEquals.equals(o1, o2); // o1, o2 are the objects to be compared.
```  
**IMPORTANT:** If you require one object to be completely isolated from another, make the special comparison `if(o1 == o2)` return false.

# Principles
## Clone
The Clone implementation is based on reflection and designed with absolute "copy standard", which makes all reference type objects associated with the original object EXACTLY the same as the copy but have different memory space, it means, the copy have the same memory data as the original one and all pointers(of ref-type fields) of the copy are pointed to the correct copied instances. This is equivalent to memory replication.
## Equals
The Equals implementation is based on reflection and has an ABSOLUTELY strict standard. In short, an object must be equivalent to another's memory replication.  
For example, the following case will make this method return false:  
`o1`: Type=A,Fields=[B b1,B b2]..., b1 points to an instance B1(Type=B...), b2 points to B1.  
`o2`: Type=A,Fields=[B b1,B b2]..., b1 points to an instance B2(equals to B1), b2 points to an instance B3(equals to B1 but **!=** B2).  
This example indicates that ref-type fields of two objects must have the same reference paths.

# Customize Guide
## Clone
### Replace Serialization
Add `if(!(target.getClass() instanceof Serializable))` after `if(target == null)` and throw `NotSerializableException`, then insert `!Modifier.isTransient(...)` at field judgement and link `!Modifier.isStatic(...)` with `&&`. It will work the same as Serialization and **8x faster** than Serialization.  
### Skip custom immutable classes
Add your own classes to `canDirectlyClone(..)` method.
## Equals
Insert your own comparison rules in the special comparisons at the beginning of the method **properly.(You should consider their positions VERY carefully)**

# Implemented Optimization
Considering that the equals method is much more effective than the clone method, the clone method may be mixed with the equals method to improve its performance. To be concrete, if the `init` object is almost the same as the `o`, only different sections will be cloned.(The equals method will work slightly different while cloning)
