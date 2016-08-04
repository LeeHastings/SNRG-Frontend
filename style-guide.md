## SNRG Frontend Coding Style

For consistency's sake, and in the event that this code is handed off to new developers, this file describes the style used in the code, in order to remain consistent.

There are a few basic rules:
* There is a maximum line width of 80 characters.
* To decrease width of variable declarations and stay consistent, the method return type is always put above the method name. 
* If a method declaration extends multiple lines, the parameters may be split (but they must be aligned), and any throws declaration will be on the next line.
* The code is indented with tabs and aligned with spaces
* Code is indented using [Stroustrup's style](https://en.wikipedia.org/wiki/Indent_style#Variant:_Stroustrup), unless the given method declaration or conditions span multiple lines, in which case the opening bracket is put on the next line to make it more visible.

Below is a small sample of the style
```java
public void
sendError(Exception e) {
	if(paremeter1){
    	code();
        if(veryLongVariableName.SignificantlyLongerParameterName()
           && anotherLongValiableName.LongParameterGetter() )
        {
        	moreCode();
        }
    }
    else {
    	somethingElse();
    }
}
public ComplexClassType<Parameter1, Parameter2> 
interface_veryLongMethodName(
		LongClassName<Type> inputType, 
        OtherLongClassName inputData)
		throws SomeException
{
	//etc
}
```
