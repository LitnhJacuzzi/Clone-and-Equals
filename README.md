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
## Determine whether two objects are equal:
```java
boolean isEquals = PerfectEquals.equals(o1, o2); // o1, o2 are the objects to be compared.
```

# Principles
## Clone ##
The Clone implementation is based on reflection and designed with absolute "copy standard", which makes all reference type objects associated with the original object EXACTLY the same as the copy but have different memory space, it means, the copy have the same memory data as the original one and all pointers(of ref-type fields) of the copy are pointed to the correct copied instances. This is equivalent to memory replication.
## Equals ##
The Equals implementation is based on reflection and has a ABSOLUTELY strict standard. In short, an objects must be equivalent to another's memory replication.  
For example, the following case will make this method return false:  
`o1`: Type=A,Fields=[B b1,B b2]..., b1 points to an instance B1(Type=B...), b2 points to B1.  
`o2`: Type=A,Fields=[B b1,B b2]..., b1 points to an instance B2(equals to B1), b2 points to an instance B3.  
This example indicates that ref-type fields of two objects must have the same reference paths.
