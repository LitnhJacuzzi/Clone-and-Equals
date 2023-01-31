# Clone and Equals
This project implements perfect clone and perfect equals.

# Usage
Clone an object:
```java
<AnyType> clone = PerfectClone.clone(o); // o is the object to be cloned
```
Determine whether two objects are equal:
```java
boolean isEquals = PerfectEquals.equals(o1, o2); // o1, o2 are the objects to be compared.
```

# Principles
## Clone ##
The Clone implementation is based on reflection and designed with absolute "copy standard", which makes all reference type objects associated with the original object EXACTLY the same as the copy but have different memory space, it means, the copy have the same memory data as the original one and all pointers(of ref-type fields) of the copy are pointed to the correct copied instances. This is equivalent to memory replication.
## Equals ##
The Equals implementation is based on reflection and has a ABSOLUTELY strict standard. In short, an objects must equivalent to another's memory replication.  
For example, the following case will make this method return false:  
`o1`: Type=A,Fields=[B b1,B b2]..., b1 point to an instance B1(Type=B...), b2 point to B1.  
`o2`: Type=A,Fields=[B b1,B b2]..., b1 point to an instance B2(equals to B1), b2 point to an instance B3.  
This example indicates that ref-type fields of two objects must have the same reference paths.
