package logisticspipes.asm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModVersionedClass {

	String modId();

	String version(); // Everything below this version will cause the fallback

	String classData(); // Fallback class data Base64 encoded

	String classDataDev(); // Fallbacl class data Baste64 encoded for dev
}
